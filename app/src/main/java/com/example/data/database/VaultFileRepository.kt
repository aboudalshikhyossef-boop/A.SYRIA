package com.example.data.database

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.IOException
import java.util.UUID
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream

/**
 * Copies user-selected content into app-private storage and encrypts it as a stream
 * with AES-GCM. Plaintext is never written to a temporary file.
 */
class VaultFileRepository(
    context: Context,
    private val dao: VaultItemDao
) {
    private val appContext = context.applicationContext
    private val resolver = appContext.contentResolver
    private val vaultDirectory = File(appContext.filesDir, "vault_items")
    private val ioMutex = Mutex()

    val items: Flow<List<VaultItem>> = dao.observeAll()

    suspend fun importFromUri(uri: Uri): VaultItem = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val displayName = queryDisplayName(uri)
            val mimeType = resolver.getType(uri) ?: "application/octet-stream"
            val input = resolver.openInputStream(uri)
                ?: throw IOException("The selected file could not be opened.")
            input.use { storeEncrypted(it, displayName, mimeType) }
        }
    }

    suspend fun saveText(displayName: String, text: String): VaultItem = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            require(displayName.isNotBlank()) { "Enter a name for this text." }
            require(text.isNotBlank()) { "The text is empty." }
            text.byteInputStream(Charsets.UTF_8).use {
                storeEncrypted(it, displayName.trim(), "text/plain; charset=utf-8")
            }
        }
    }

    suspend fun exportToUri(id: String, destination: Uri) = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val item = dao.getById(id) ?: throw IOException("The vault item no longer exists.")
            val encryptedFile = resolveVaultFile(item.storedFileName)
            if (!encryptedFile.isFile) throw IOException("The encrypted vault file is missing.")
            // Verify the authentication tag before any plaintext is written to an export destination.
            verifyEncryptedFile(encryptedFile)
            val output = resolver.openOutputStream(destination, "w")
                ?: throw IOException("The destination could not be opened.")
            output.use { decryptInto(encryptedFile, it) }
        }
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val item = dao.getById(id) ?: return@withLock
            val file = resolveVaultFile(item.storedFileName)
            if (file.exists() && !file.delete()) {
                throw IOException("The encrypted file could not be removed.")
            }
            dao.deleteById(id)
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            val all = dao.getAll()
            all.forEach { item ->
                val file = resolveVaultFile(item.storedFileName)
                if (file.exists() && !file.delete()) {
                    throw IOException("Could not remove " + item.getDisplayName() + ".")
                }
                // Remove metadata as each file is deleted, so a partial failure stays consistent.
                dao.deleteById(item.id)
            }
            vaultDirectory.listFiles()?.forEach { file ->
                if ((file.name.endsWith(".asv") || file.name.endsWith(".tmp")) &&
                    file.isFile && !file.delete()
                ) {
                    throw IOException("A leftover encrypted vault file could not be removed.")
                }
            }
        }
    }

    private fun resolveVaultFile(storedFileName: String): File {
        require(storedFileName.matches(Regex("[0-9a-fA-F-]{36}\\.asv"))) {
            "Invalid encrypted vault file reference."
        }
        return File(vaultDirectory, storedFileName)
    }

    /** Remove abandoned temporary files and encrypted files without a metadata row. */
    suspend fun removeOrphanedFiles() = withContext(Dispatchers.IO) {
        ioMutex.withLock {
            if (!vaultDirectory.isDirectory) return@withLock
            val referenced = dao.getAll().map { it.storedFileName }.toSet()
            vaultDirectory.listFiles()?.forEach { candidate ->
                val isTemporary = candidate.name.endsWith(".tmp")
                val isUnreferenced = candidate.name.endsWith(".asv") && candidate.name !in referenced
                if ((isTemporary || isUnreferenced) && candidate.isFile) candidate.delete()
            }
        }
    }

    private suspend fun storeEncrypted(
        input: InputStream,
        displayName: String,
        mimeType: String
    ): VaultItem {
        if (!vaultDirectory.exists() && !vaultDirectory.mkdirs()) {
            throw IOException("The private vault directory could not be created.")
        }

        val id = UUID.randomUUID().toString()
        val finalName = "$id.asv"
        val temporary = File(vaultDirectory, "$id.tmp")
        val destination = File(vaultDirectory, finalName)
        var plainBytes = 0L

        try {
            val cipher = SovereigntyCipher.newVaultFileEncryptCipher()
            FileOutputStream(temporary).use { rawOutput ->
                rawOutput.write(FILE_MAGIC)
                rawOutput.write(cipher.iv)
                CipherOutputStream(rawOutput, cipher).use { encryptedOutput ->
                    val buffer = ByteArray(COPY_BUFFER_SIZE)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        if (count == 0) continue
                        encryptedOutput.write(buffer, 0, count)
                        plainBytes += count
                    }
                }
            }

            // Read the entire ciphertext once before committing metadata. GCM validates its tag at EOF.
            verifyEncryptedFile(temporary)
            if (!temporary.renameTo(destination)) {
                throw IOException("The encrypted file could not be committed.")
            }

            val item = VaultItem(
                id = id,
                encryptedDisplayName = SovereigntyCipher.encrypt(displayName),
                encryptedMimeType = SovereigntyCipher.encrypt(mimeType),
                storedFileName = finalName,
                sizeBytes = plainBytes
            )
            try {
                dao.insert(item)
            } catch (error: Exception) {
                destination.delete()
                throw error
            }
            return item
        } catch (error: Exception) {
            temporary.delete()
            destination.delete()
            throw error
        }
    }

    private fun verifyEncryptedFile(file: File) {
        FileInputStream(file).use { fileInput ->
            val input = DataInputStream(BufferedInputStream(fileInput))
            val magic = ByteArray(FILE_MAGIC.size)
            input.readFully(magic)
            if (!magic.contentEquals(FILE_MAGIC)) throw IOException("Unknown vault file format.")
            val iv = ByteArray(IV_LENGTH_BYTES)
            input.readFully(iv)
            val decryptor = SovereigntyCipher.newVaultFileDecryptCipher(iv)
            CipherInputStream(input, decryptor).use { decrypted ->
                val buffer = ByteArray(COPY_BUFFER_SIZE)
                while (decrypted.read(buffer) >= 0) {
                    // Consume the full stream so the GCM authentication tag is checked.
                }
            }
        }
    }

    private fun decryptInto(file: File, destination: OutputStream) {
        FileInputStream(file).use { fileInput ->
            val input = DataInputStream(BufferedInputStream(fileInput))
            val magic = ByteArray(FILE_MAGIC.size)
            input.readFully(magic)
            if (!magic.contentEquals(FILE_MAGIC)) throw IOException("Unknown vault file format.")
            val iv = ByteArray(IV_LENGTH_BYTES)
            input.readFully(iv)
            val decryptor = SovereigntyCipher.newVaultFileDecryptCipher(iv)
            CipherInputStream(input, decryptor).use { decrypted ->
                decrypted.copyTo(destination, COPY_BUFFER_SIZE)
            }
        }
    }

    private fun queryDisplayName(uri: Uri): String {
        val name = runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) cursor.getString(index) else null
                } else null
            }
        }.getOrNull()
        return name?.takeIf { it.isNotBlank() }?.take(240)
            ?: "vault-item-" + System.currentTimeMillis()
    }

    private companion object {
        const val COPY_BUFFER_SIZE = 64 * 1024
        const val IV_LENGTH_BYTES = 12
        val FILE_MAGIC = byteArrayOf(0x41, 0x53, 0x56, 0x31) // ASV1
    }
}

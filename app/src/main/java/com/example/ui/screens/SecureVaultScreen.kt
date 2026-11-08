package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.InventorIdea
import com.example.data.database.VaultItem
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SecureVaultScreen(viewModel: DashboardViewModel, isArabic: Boolean, onClose: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    DisposableEffect(activity) {
        val window = activity?.window
        val wasSecure = window?.attributes?.flags?.and(android.view.WindowManager.LayoutParams.FLAG_SECURE) != 0
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            if (window != null && wasSecure != true) {
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
    val scope = rememberCoroutineScope()
    val vaultItems by viewModel.vaultItems.collectAsState()
    val legacyIdeas by viewModel.savedIdeas.collectAsState()

    var showAddMenu by remember { mutableStateOf(false) }
    var showTextEditor by remember { mutableStateOf(false) }
    var showImportChoice by remember { mutableStateOf(false) }
    var showWipeConfirmation by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var pendingName by remember { mutableStateOf("") }
    var exportItem by remember { mutableStateOf<VaultItem?>(null) }
    var deleteItem by remember { mutableStateOf<VaultItem?>(null) }
    var selectedIdea by remember { mutableStateOf<InventorIdea?>(null) }
    var noteTitle by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val deleteRequestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        message = if (result.resultCode == Activity.RESULT_OK) {
            if (isArabic) "وافق Android على حذف الأصل من المعرض." else "Android approved deletion of the original media."
        } else {
            if (isArabic) "بقي الأصل في المعرض؛ لم تتم الموافقة على الحذف." else "The original remains in the gallery; deletion was not approved."
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val item = exportItem
        val uri = result.data?.data
        exportItem = null
        if (item != null && uri != null) {
            scope.launch {
                busy = true
                try {
                    viewModel.exportVaultFile(item.id, uri)
                    message = if (isArabic) "تم تصدير نسخة إلى المكان المحدد." else "A decrypted export copy was saved to the selected location."
                } catch (error: Exception) {
                    message = (if (isArabic) "فشل التصدير: " else "Export failed: ") + (error.message ?: "")
                } finally {
                    busy = false
                }
            }
        }
    }

    val picked: (Uri?) -> Unit = { uri ->
        if (uri != null) {
            pendingUri = uri
            pendingName = readDisplayName(context, uri)
            showImportChoice = true
            showAddMenu = false
        }
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), picked)
    val anyFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), picked)

    fun removeOriginal(uri: Uri) {
        try {
            val mediaUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (uri.authority == "media") uri else runCatching { MediaStore.getMediaUri(context, uri) }.getOrNull()
            } else null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && mediaUri != null && mediaUri.authority == "media") {
                val request = MediaStore.createDeleteRequest(context.contentResolver, listOf(mediaUri))
                message = if (isArabic) "بانتظار تأكيد Android لحذف الأصل..." else "Waiting for Android approval to delete the original..."
                deleteRequestLauncher.launch(IntentSenderRequest.Builder(request.intentSender).build())
            } else if (DocumentsContract.isDocumentUri(context, uri)) {
                val deleted = DocumentsContract.deleteDocument(context.contentResolver, uri)
                message = if (deleted) {
                    if (isArabic) "حُفظ الملف مشفّراً وأُزيل الأصل." else "The encrypted copy is saved and the original was removed."
                } else {
                    if (isArabic) "حُفظ الملف، لكن مزوّد الملفات لم يسمح بحذف الأصل." else "The file is safe in the vault, but its document provider did not remove the original."
                }
            } else {
                message = if (isArabic) "حُفظت النسخة المشفّرة، لكن مصدر الملف لا يدعم الحذف؛ بقي الأصل في مكانه." else "The encrypted copy was saved, but this source does not support deletion; the original remains."
            }
        } catch (_: Exception) {
            message = if (isArabic) "تم الحفظ المشفّر، لكن تعذّر حذف الأصل. قد تحتاج لحذفه يدوياً." else "The encrypted copy is saved, but the original could not be deleted. You may need to remove it manually."
        }
    }

    fun importSelected(moveOriginal: Boolean) {
        val uri = pendingUri ?: return
        pendingUri = null
        showImportChoice = false
        scope.launch {
            busy = true
            message = null
            try {
                // importVaultFile returns only after the encrypted GCM stream was verified.
                viewModel.importVaultFile(uri)
                message = if (isArabic) "حُفظ الملف داخل الخزينة بتشفير كامل." else "The file was encrypted and stored in the vault."
                if (moveOriginal) removeOriginal(uri)
            } catch (error: Exception) {
                message = (if (isArabic) "فشل الحفظ: " else "Save failed: ") + (error.message ?: "")
            } finally {
                busy = false
            }
        }
    }

    fun exportSelected(item: VaultItem) {
        exportItem = item
        val mime = item.getMimeType().substringBefore(';').takeIf { '/' in it } ?: "*/*"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
            putExtra(Intent.EXTRA_TITLE, safeExportName(item.getDisplayName()))
        }
        exportLauncher.launch(intent)
    }

    Box(Modifier.fillMaxSize().background(VoidBlack)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (isArabic) "الخزينة الآمنة" else "SECURE VAULT", color = CyberCyan, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text(if (isArabic) "تخزين محلي مشفّر للصور والملفات والنصوص" else "Encrypted local storage for images, files and text", color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp)
                }
                TextButton(onClick = onClose) { Text(if (isArabic) "إغلاق" else "CLOSE", color = CyberCyan) }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showAddMenu = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = VoidBlack)
                ) { Text(if (isArabic) "إضافة إلى الخزينة" else "ADD TO VAULT", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                TextButton(
                    onClick = { showWipeConfirmation = true },
                    enabled = (vaultItems.isNotEmpty() || legacyIdeas.isNotEmpty()) && !busy
                ) { Text(if (isArabic) "مسح الكل" else "WIPE ALL", color = Color.Red, fontSize = 10.sp) }
            }
            message?.let {
                Card(Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF10202A))) {
                    Text(it, Modifier.padding(10.dp), color = Color.White, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(if (isArabic) "العناصر المشفّرة: ${vaultItems.size}" else "ENCRYPTED ITEMS: ${vaultItems.size}", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            HorizontalDivider(color = CyberCyan.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 7.dp))
            Text(
                if (isArabic) "تُخزّن البيانات على هذا الجهاز فقط. حذف التطبيق أو مسح بياناته يزيل الخزينة؛ صدّر نسخة خارجية عند الحاجة."
                else "Vault data stays on this device. Uninstalling the app or clearing its data removes it; export a copy when needed.",
                color = Color.White.copy(alpha = 0.42f),
                fontSize = 9.sp,
                lineHeight = 13.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (vaultItems.isEmpty() && legacyIdeas.isEmpty()) {
                    item {
                        Column(Modifier.fillMaxWidth().padding(vertical = 45.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔐", fontSize = 42.sp)
                            Text(if (isArabic) "الخزينة فارغة" else "THE VAULT IS EMPTY", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(if (isArabic) "أضف صورة أو ملفاً أو نصاً ليُحفظ مشفّراً داخل التطبيق." else "Add an image, any file, or a text note to store it encrypted.", color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp)
                        }
                    }
                }
                if (vaultItems.isNotEmpty()) {
                    item { Text(if (isArabic) "الصور والملفات والنصوص" else "FILES, IMAGES AND TEXT", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    items(vaultItems, key = { "file-${it.id}" }) { item ->
                        VaultItemCard(item, isArabic, { exportSelected(item) }, { deleteItem = item })
                    }
                }
                if (legacyIdeas.isNotEmpty()) {
                    item { Text(if (isArabic) "المخططات المحفوظة سابقاً" else "LEGACY BLUEPRINTS", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    items(legacyIdeas, key = { "idea-${it.id}" }) { idea ->
                        Card(
                            Modifier.fillMaxWidth().clickable { selectedIdea = idea },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B151C))
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(idea.getDecryptedTitle(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(if (isArabic) "مخطط قديم محفوظ" else "Previously saved blueprint", color = CyberCyan.copy(alpha = 0.65f), fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
        if (busy) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CyberCyan)
        }
    }

    if (showAddMenu) AlertDialog(
        onDismissRequest = { showAddMenu = false },
        containerColor = Color(0xFF101820),
        title = { Text(if (isArabic) "ماذا تريد حفظه؟" else "WHAT TO SAVE?", color = CyberCyan) },
        text = {
            Column {
                TextButton(onClick = { imagePicker.launch(arrayOf("image/*")) }) { Text(if (isArabic) "اختيار صورة" else "Choose image", color = Color.White) }
                TextButton(onClick = { anyFilePicker.launch(arrayOf("*/*")) }) { Text(if (isArabic) "اختيار أي ملف" else "Choose any file", color = Color.White) }
                TextButton(onClick = { showAddMenu = false; showTextEditor = true }) { Text(if (isArabic) "كتابة نص أو ملاحظة" else "Write text or note", color = Color.White) }
            }
        },
        confirmButton = { TextButton(onClick = { showAddMenu = false }) { Text(if (isArabic) "إلغاء" else "CANCEL") } }
    )

    if (showTextEditor) AlertDialog(
        onDismissRequest = { showTextEditor = false },
        containerColor = Color(0xFF101820),
        title = { Text(if (isArabic) "حفظ نص مشفّر" else "SAVE ENCRYPTED TEXT", color = CyberCyan) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = noteTitle, onValueChange = { noteTitle = it }, singleLine = true,
                    label = { Text(if (isArabic) "اسم النص" else "Title") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberCyan.copy(alpha = 0.4f))
                )
                OutlinedTextField(
                    value = noteText, onValueChange = { noteText = it }, minLines = 4, maxLines = 8,
                    label = { Text(if (isArabic) "النص" else "Text") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = CyberCyan, unfocusedBorderColor = CyberCyan.copy(alpha = 0.4f))
                )
            }
        },
        confirmButton = {
            TextButton(enabled = noteTitle.isNotBlank() && noteText.isNotBlank() && !busy, onClick = {
                showTextEditor = false
                scope.launch {
                    busy = true
                    try {
                        viewModel.saveVaultText(noteTitle, noteText)
                        noteTitle = ""
                        noteText = ""
                        message = if (isArabic) "حُفظ النص مشفّراً داخل الخزينة." else "The text was encrypted and saved."
                    } catch (error: Exception) {
                        message = (if (isArabic) "فشل حفظ النص: " else "Save failed: ") + (error.message ?: "")
                    } finally { busy = false }
                }
            }) { Text(if (isArabic) "حفظ مشفّر" else "SAVE ENCRYPTED", color = CyberCyan) }
        },
        dismissButton = { TextButton(onClick = { showTextEditor = false }) { Text(if (isArabic) "إلغاء" else "CANCEL", color = Color.White) } }
    )

    if (showImportChoice && pendingUri != null) AlertDialog(
        onDismissRequest = { showImportChoice = false; pendingUri = null },
        containerColor = Color(0xFF101820),
        title = { Text(if (isArabic) "طريقة الحفظ" else "SAVE MODE", color = CyberCyan) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(pendingName, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    if (isArabic) "يمكنك حفظ نسخة مشفّرة مع إبقاء الأصل، أو نقل الأصل بعد نجاح التشفير. قد يطلب Android موافقتك على الحذف."
                    else "Keep the original and store an encrypted copy, or move the original after encryption. Android may ask you to approve deletion.",
                    color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp
                )
            }
        },
        confirmButton = { TextButton(onClick = { importSelected(true) }) { Text(if (isArabic) "نقل الأصل للخزينة" else "MOVE ORIGINAL", color = CyberCyan) } },
        dismissButton = {
            Row {
                TextButton(onClick = { importSelected(false) }) { Text(if (isArabic) "حفظ نسخة" else "KEEP A COPY", color = Color.White) }
                TextButton(onClick = { showImportChoice = false; pendingUri = null }) { Text(if (isArabic) "إلغاء" else "CANCEL", color = Color.White.copy(alpha = 0.65f)) }
            }
        }
    )

    deleteItem?.let { item -> AlertDialog(
        onDismissRequest = { deleteItem = null }, containerColor = Color(0xFF101820),
        title = { Text(if (isArabic) "حذف من الخزينة؟" else "DELETE FROM VAULT?", color = Color.Red) },
        text = { Text(if (isArabic) "سيُحذف الملف المشفّر من التطبيق فقط. لن يتأثر أي أصل بقي خارج الخزينة." else "Only the encrypted vault copy will be deleted; external originals are unaffected.", color = Color.White) },
        confirmButton = { TextButton(onClick = {
            deleteItem = null
            scope.launch {
                busy = true
                try {
                    viewModel.deleteVaultItem(item.id)
                    message = if (isArabic) "حُذف العنصر من الخزينة." else "Item deleted from the vault."
                } catch (error: Exception) { message = error.message ?: "Delete failed" }
                finally { busy = false }
            }
        }) { Text(if (isArabic) "حذف" else "DELETE", color = Color.Red) } },
        dismissButton = { TextButton(onClick = { deleteItem = null }) { Text(if (isArabic) "إلغاء" else "CANCEL", color = Color.White) } }
    ) }

    selectedIdea?.let { idea -> AlertDialog(
        onDismissRequest = { selectedIdea = null }, containerColor = Color(0xFF101820),
        title = { Text(idea.getDecryptedTitle(), color = CyberCyan, fontWeight = FontWeight.Bold) },
        text = { SelectionContainer {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(idea.getDecryptedCategory(), color = CyberCyan, fontSize = 10.sp)
                Text(idea.getDecryptedOriginalIdea(), color = Color.White, fontSize = 12.sp)
                HorizontalDivider(color = CyberCyan.copy(alpha = 0.2f))
                Text(idea.getDecryptedGeminiBlueprint(), color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        } },
        confirmButton = { TextButton(onClick = { viewModel.deleteIdea(idea.id); selectedIdea = null }) { Text(if (isArabic) "حذف" else "DELETE", color = Color.Red) } },
        dismissButton = { TextButton(onClick = { selectedIdea = null }) { Text(if (isArabic) "إغلاق" else "CLOSE", color = CyberCyan) } }
    ) }

    if (showWipeConfirmation) AlertDialog(
        onDismissRequest = { showWipeConfirmation = false }, containerColor = Color(0xFF101820),
        title = { Text(if (isArabic) "مسح الخزينة بالكامل؟" else "WIPE THE VAULT?", color = Color.Red) },
        text = { Text(if (isArabic) "سيُحذف المحتوى المشفّر والمخططات القديمة من هذا الجهاز. لا يمكن التراجع." else "All encrypted files and legacy blueprints on this device will be deleted. This cannot be undone.", color = Color.White) },
        confirmButton = { TextButton(onClick = {
            showWipeConfirmation = false
            scope.launch {
                busy = true
                try {
                    viewModel.wipeVault()
                    message = if (isArabic) "تم مسح محتويات الخزينة." else "The vault was cleared."
                } catch (error: Exception) { message = error.message ?: "Vault wipe failed" }
                finally { busy = false }
            }
        }) { Text(if (isArabic) "تأكيد المسح" else "CONFIRM WIPE", color = Color.Red) } },
        dismissButton = { TextButton(onClick = { showWipeConfirmation = false }) { Text(if (isArabic) "إلغاء" else "CANCEL", color = Color.White) } }
    )
}

@Composable
private fun VaultItemCard(item: VaultItem, isArabic: Boolean, onExport: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF0B151C))) {
        Column(Modifier.padding(12.dp)) {
            Text(item.getDisplayName(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("${formatBytes(item.sizeBytes)} • ${item.getMimeType()}", color = CyberCyan.copy(alpha = 0.72f), fontSize = 9.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onExport) { Text(if (isArabic) "تصدير نسخة" else "EXPORT COPY", color = CyberCyan, fontSize = 9.sp) }
                TextButton(onClick = onDelete) { Text(if (isArabic) "حذف" else "DELETE", color = Color.Red, fontSize = 9.sp) }
            }
        }
    }
}

private fun readDisplayName(context: Context, uri: Uri): String {
    val name = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) else null
        }
    }.getOrNull()
    return name?.takeIf { it.isNotBlank() }?.take(240) ?: "vault-item"
}

private fun safeExportName(name: String): String =
    name.substringAfterLast('/').substringAfterLast('\\')
        .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        .take(120).ifBlank { "vault-export" }

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024L -> "${bytes} B"
    bytes < 1024L * 1024L -> String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0)
    bytes < 1024L * 1024L * 1024L -> String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0))
    else -> String.format(Locale.ROOT, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
}

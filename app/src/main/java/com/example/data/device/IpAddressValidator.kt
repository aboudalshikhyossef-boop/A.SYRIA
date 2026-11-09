package com.example.data.device

import java.net.Inet6Address
import java.net.InetAddress

/**
 * Validates an IP literal without accepting host names or triggering DNS lookups.
 */
internal fun isValidIpAddress(value: String): Boolean {
    if (value.isBlank() || value.length > 45) return false

    if (':' in value) {
        val isHexadecimalIpv6 = value.all { character ->
            character in '0'..'9' ||
                character in 'a'..'f' ||
                character in 'A'..'F' ||
                character == ':'
        }
        if (!isHexadecimalIpv6) return false

        return runCatching {
            InetAddress.getByName(value) is Inet6Address
        }.getOrDefault(false)
    }

    val octets = value.split('.')
    return octets.size == 4 && octets.all { octet ->
        octet.isNotEmpty() &&
            (octet == "0" || octet.first() != '0') &&
            octet.all { it in '0'..'9' } &&
            octet.toIntOrNull()?.let { it in 0..255 } == true
    }
}

package com.example.data.device

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IpAddressValidatorTest {

    @Test
    fun acceptsValidIpv4Literals() {
        assertTrue(isValidIpAddress("127.0.0.1"))
        assertTrue(isValidIpAddress("192.0.2.25"))
        assertTrue(isValidIpAddress("255.255.255.255"))
    }

    @Test
    fun acceptsValidIpv6Literals() {
        assertTrue(isValidIpAddress("::1"))
        assertTrue(isValidIpAddress("2001:db8::1"))
        assertTrue(isValidIpAddress("2001:db8:0:0:0:0:0:1"))
    }

    @Test
    fun rejectsMalformedOrOutOfRangeAddresses() {
        assertFalse(isValidIpAddress(""))
        assertFalse(isValidIpAddress("127.0.0"))
        assertFalse(isValidIpAddress("256.0.0.1"))
        assertFalse(isValidIpAddress("192.0.2.999"))
        assertFalse(isValidIpAddress("::::"))
        assertFalse(isValidIpAddress("1:2:3:4:5:6:7:8:9"))
        assertFalse(isValidIpAddress("example.com"))
        assertFalse(isValidIpAddress("192.0.2.1/path"))
    }

    @Test
    fun rejectsValuesLongerThanAnIpLiteral() {
        assertFalse(isValidIpAddress("a".repeat(46)))
    }
}

package com.example

import com.example.data.location.NetworkLocationRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NetworkLocationRepositoryTest {

    @Test
    fun parsesExactCoordinatesFromIpLookupResponse() {
        val coordinates = NetworkLocationRepository.parseCoordinates(
            """{"latitude":33.5138,"longitude":36.2765}"""
        )

        assertEquals(33.5138, coordinates.latitude, 0.000001)
        assertEquals(36.2765, coordinates.longitude, 0.000001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMissingCoordinates() {
        NetworkLocationRepository.parseCoordinates("""{"city":"Damascus"}""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOutOfRangeLatitude() {
        NetworkLocationRepository.parseCoordinates(
            """{"latitude":200,"longitude":36.2}"""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOutOfRangeLongitude() {
        NetworkLocationRepository.parseCoordinates(
            """{"latitude":33.5,"longitude":-181}"""
        )
    }
}

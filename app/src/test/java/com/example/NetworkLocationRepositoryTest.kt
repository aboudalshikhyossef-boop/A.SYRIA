package com.example

import com.example.data.location.NetworkLocationRepository
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NetworkLocationRepositoryTest {

    @Test
    fun parsesCoordinatesFromIpLookupResponse() {
        val coordinates = NetworkLocationRepository.parseCoordinates(
            """{"latitude":33.5138,"longitude":36.2765}"""
        )
        assertNotNull(coordinates)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMissingCoordinates() {
        NetworkLocationRepository.parseCoordinates("""{"city":"Damascus"}""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOutOfRangeCoordinates() {
        NetworkLocationRepository.parseCoordinates(
            """{"latitude":200,"longitude":36.2}"""
        )
    }
}

package com.example.data.location

import com.batoulapps.adhan.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Resolves an approximate city-level position from the request's public IP.
 * This does not access Android GPS, fused location, Wi-Fi scans, or location permissions.
 */
class NetworkLocationRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(12, TimeUnit.SECONDS)
        .build()
) {
    suspend fun getCoordinates(): Coordinates = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(LOOKUP_URL)
            .header("Accept", "application/json")
            .header("User-Agent", "A.SYRIA-Android")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Network location lookup failed (\${response.code})." }
            val body = response.body?.string().orEmpty()
            parseCoordinates(body)
        }
    }

    companion object {
        private const val LOOKUP_URL = "https://ipapi.co/json/"

        internal fun parseCoordinates(responseBody: String): Coordinates {
            val json = JSONObject(responseBody)
            val latitude = json.optDouble("latitude", Double.NaN)
            val longitude = json.optDouble("longitude", Double.NaN)
            require(latitude.isFinite() && latitude in -90.0..90.0) {
                "Network location response has no valid latitude."
            }
            require(longitude.isFinite() && longitude in -180.0..180.0) {
                "Network location response has no valid longitude."
            }
            return Coordinates(latitude, longitude)
        }
    }
}

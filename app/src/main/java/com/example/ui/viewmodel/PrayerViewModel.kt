package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.example.data.location.NetworkLocationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Prayer calculations use approximate coordinates derived from the public IP
 * address over HTTPS. No Android location provider or GPS permission is used.
 * Damascus remains the deterministic fallback when network lookup is unavailable.
 */
class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val networkLocationRepository = NetworkLocationRepository()
    private val preferences = application.getSharedPreferences("prayer_prefs", Application.MODE_PRIVATE)

    // Prayer times are stable for a local date and a coordinate pair.
    private var cachedPrayerTimes: PrayerTimes? = null
    private var cachedPrayerDateKey: String? = null
    private var cachedLatitude = Double.NaN
    private var cachedLongitude = Double.NaN
    private var cachedTomorrowFajr: Date? = null
    private var cachedTomorrowDateKey: String? = null

    private val _nextPrayerName = MutableStateFlow("...")
    val nextPrayerName: StateFlow<String> = _nextPrayerName.asStateFlow()

    private val _nextPrayerTime = MutableStateFlow("00:00")
    val nextPrayerTime: StateFlow<String> = _nextPrayerTime.asStateFlow()

    private val _allPrayerTimes = MutableStateFlow<Map<String, String>>(emptyMap())
    val allPrayerTimes: StateFlow<Map<String, String>> = _allPrayerTimes.asStateFlow()

    private val _nextPrayerCountdown = MutableStateFlow("00:00:00")
    val nextPrayerCountdown: StateFlow<String> = _nextPrayerCountdown.asStateFlow()

    private val _nextPrayerProgress = MutableStateFlow(0f)
    val nextPrayerProgress: StateFlow<Float> = _nextPrayerProgress.asStateFlow()

    private val _coordinates = MutableStateFlow(
        run {
            preferences.edit().remove("last_lat").remove("last_lon").apply()
            readCachedCoordinates()
        }
    )
    val coordinates: StateFlow<Coordinates?> = _coordinates.asStateFlow()

    private val _qiblaDirection = MutableStateFlow(0.0)
    val qiblaDirection: StateFlow<Double> = _qiblaDirection.asStateFlow()

    init {
        publishCoordinates(_coordinates.value ?: DAMASCUS)
        refreshNetworkCoordinates()
        startTimer()
    }

    private fun refreshNetworkCoordinates() {
        viewModelScope.launch {
            val coordinates = try {
                networkLocationRepository.getCoordinates()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _coordinates.value ?: DAMASCUS
            }
            preferences.edit()
                .putFloat("network_lat", coordinates.latitude.toFloat())
                .putFloat("network_lon", coordinates.longitude.toFloat())
                .apply()
            publishCoordinates(coordinates)
        }
    }

    private fun publishCoordinates(coordinates: Coordinates) {
        _coordinates.value = coordinates
        _qiblaDirection.value = com.batoulapps.adhan.Qibla(coordinates).direction
    }

    private fun readCachedCoordinates(): Coordinates? {
        val latitude = preferences.getFloat("network_lat", Float.NaN).toDouble()
        val longitude = preferences.getFloat("network_lon", Float.NaN).toDouble()
        return if (latitude.isFinite() && longitude.isFinite() &&
            latitude in -90.0..90.0 && longitude in -180.0..180.0
        ) Coordinates(latitude, longitude) else null
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                calculatePrayerTimes()
                delay(1_000)
            }
        }
    }

    private fun calculatePrayerTimes() {
        val coordinates = _coordinates.value ?: DAMASCUS
        val now = Date()
        val prayerTimes = getTodayPrayerTimes(coordinates, now)
        val nextPrayer = prayerTimes.nextPrayer()
        val effectiveNextPrayer = if (nextPrayer == Prayer.NONE) Prayer.FAJR else nextPrayer
        val nextPrayerDate = if (nextPrayer == Prayer.NONE) {
            getTomorrowFajr(coordinates, now)
        } else {
            prayerTimes.timeForPrayer(effectiveNextPrayer)
        }

        val arabicName = when (effectiveNextPrayer) {
            Prayer.FAJR -> "الفجر"
            Prayer.SUNRISE -> "الشروق"
            Prayer.DHUHR -> "الظهر"
            Prayer.ASR -> "العصر"
            Prayer.MAGHRIB -> "المغرب"
            Prayer.ISHA -> "العشاء"
            else -> effectiveNextPrayer.name
        }
        _nextPrayerName.value = arabicName

        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        _nextPrayerTime.value = formatter.format(nextPrayerDate)
        _allPrayerTimes.value = mapOf(
            "الفجر" to formatter.format(prayerTimes.fajr),
            "الشروق" to formatter.format(prayerTimes.sunrise),
            "الظهر" to formatter.format(prayerTimes.dhuhr),
            "العصر" to formatter.format(prayerTimes.asr),
            "المغرب" to formatter.format(prayerTimes.maghrib),
            "العشاء" to formatter.format(prayerTimes.isha)
        )

        val remainingMillis = (nextPrayerDate.time - now.time).coerceAtLeast(0L)
        val hours = remainingMillis / 3_600_000
        val minutes = (remainingMillis % 3_600_000) / 60_000
        val seconds = (remainingMillis % 60_000) / 1_000
        _nextPrayerCountdown.value = String.format(
            Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds
        )

        val currentPrayer = prayerTimes.currentPrayer()
        val currentPrayerDate = prayerTimes.timeForPrayer(currentPrayer) ?: now
        val interval = nextPrayerDate.time - currentPrayerDate.time
        val elapsed = now.time - currentPrayerDate.time
        _nextPrayerProgress.value = if (interval > 0L) {
            (elapsed.toFloat() / interval.toFloat()).coerceIn(0f, 1f)
        } else 1f
    }

    private fun getTodayPrayerTimes(coordinates: Coordinates, now: Date): PrayerTimes {
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(now)
        val cached = cachedPrayerTimes
        if (cached != null && cachedPrayerDateKey == dateKey &&
            cachedLatitude == coordinates.latitude && cachedLongitude == coordinates.longitude
        ) {
            return cached
        }

        val parameters = CalculationMethod.UMM_AL_QURA.parameters.also {
            it.madhab = Madhab.SHAFI
        }
        return PrayerTimes(coordinates, DateComponents.from(now), parameters).also { result ->
            cachedPrayerTimes = result
            cachedPrayerDateKey = dateKey
            cachedLatitude = coordinates.latitude
            cachedLongitude = coordinates.longitude
            cachedTomorrowFajr = null
            cachedTomorrowDateKey = null
        }
    }

    private fun getTomorrowFajr(coordinates: Coordinates, now: Date): Date {
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(now)
        cachedTomorrowFajr?.takeIf {
            cachedTomorrowDateKey == todayKey &&
                cachedLatitude == coordinates.latitude && cachedLongitude == coordinates.longitude
        }?.let { return it }

        val tomorrow = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_YEAR, 1)
        }.time
        val parameters = CalculationMethod.UMM_AL_QURA.parameters.also {
            it.madhab = Madhab.SHAFI
        }
        return PrayerTimes(coordinates, DateComponents.from(tomorrow), parameters).fajr.also { fajr ->
            cachedTomorrowFajr = fajr
            cachedTomorrowDateKey = todayKey
        }
    }
    private companion object {
        val DAMASCUS = Coordinates(33.5138, 36.2765)
    }
}

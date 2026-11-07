package com.example.ui.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

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

    private val _currentLocation = MutableStateFlow<Coordinates?>(null)
    val currentLocation: StateFlow<Coordinates?> = _currentLocation.asStateFlow()

    private val _qiblaDirection = MutableStateFlow(0.0)
    val qiblaDirection: StateFlow<Double> = _qiblaDirection.asStateFlow()

    init {
        updateLocation()
        startTimer()
    }

    fun updateLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    val coords = if (location != null) {
                        // Save location to SharedPreferences for NotificationWorker
                        getApplication<android.app.Application>()
                            .getSharedPreferences("prayer_prefs", android.content.Context.MODE_PRIVATE)
                            .edit()
                            .putFloat("last_lat", location.latitude.toFloat())
                            .putFloat("last_lon", location.longitude.toFloat())
                            .apply()
                        Coordinates(location.latitude, location.longitude)
                    } else {
                        Coordinates(33.5138, 36.2765) // Damascus fallback
                    }
                    _currentLocation.value = coords
                    _qiblaDirection.value = com.batoulapps.adhan.Qibla(coords).direction
                }
                .addOnFailureListener { e ->
                    // Handle failure (e.g. Google Play Services unavailable)
                    val fallback = Coordinates(33.5138, 36.2765)
                    _currentLocation.value = fallback
                    _qiblaDirection.value = com.batoulapps.adhan.Qibla(fallback).direction
                }
        } catch (e: Exception) {
            // Catch SecurityException, IllegalStateException, or any Play Services error
            val fallback = Coordinates(33.5138, 36.2765)
            _currentLocation.value = fallback
            _qiblaDirection.value = com.batoulapps.adhan.Qibla(fallback).direction
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                calculatePrayerTimes()
                delay(1000)
            }
        }
    }

    private fun calculatePrayerTimes() {
        val coords = _currentLocation.value ?: Coordinates(33.5138, 36.2765)
        val date = DateComponents.from(Date())
        // Use Umm Al-Qura for better accuracy in many regions, or keep MWL but allow customization
        // For now, let's ensure we use a robust method. 
        val params = CalculationMethod.UMM_AL_QURA.parameters
        params.madhab = Madhab.SHAFI

        val prayerTimes = PrayerTimes(coords, date, params)
        
        val now = Date()
        val nextPrayer = prayerTimes.nextPrayer()
        
        // Handle case where next prayer is Fajr tomorrow
        val effectiveNextPrayer = if (nextPrayer == Prayer.NONE) Prayer.FAJR else nextPrayer
        val finalNextPrayerTime = if (nextPrayer == Prayer.NONE) {
             val tomorrow = Calendar.getInstance()
             tomorrow.add(Calendar.DAY_OF_YEAR, 1)
             PrayerTimes(coords, DateComponents.from(tomorrow.time), params).fajr
        } else {
            prayerTimes.timeForPrayer(effectiveNextPrayer)
        }

        val prayerNameAr = when(effectiveNextPrayer) {
            Prayer.FAJR -> "الفجر"
            Prayer.SUNRISE -> "الشروق"
            Prayer.DHUHR -> "الظهر"
            Prayer.ASR -> "العصر"
            Prayer.MAGHRIB -> "المغرب"
            Prayer.ISHA -> "العشاء"
            else -> effectiveNextPrayer.name
        }
        _nextPrayerName.value = prayerNameAr
        
        val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        _nextPrayerTime.value = timeFormat.format(finalNextPrayerTime)

        // Calculate all prayer times for display
        val allTimes = mutableMapOf<String, String>()
        allTimes["الفجر"] = timeFormat.format(prayerTimes.fajr)
        allTimes["الشروق"] = timeFormat.format(prayerTimes.sunrise)
        allTimes["الظهر"] = timeFormat.format(prayerTimes.dhuhr)
        allTimes["العصر"] = timeFormat.format(prayerTimes.asr)
        allTimes["المغرب"] = timeFormat.format(prayerTimes.maghrib)
        allTimes["العشاء"] = timeFormat.format(prayerTimes.isha)
        _allPrayerTimes.value = allTimes

        val diffMillis = finalNextPrayerTime.time - now.time
        if (diffMillis > 0) {
            val h = (diffMillis / 3600000).toInt()
            val m = ((diffMillis % 3600000) / 60000).toInt()
            val s = ((diffMillis % 60000) / 1000).toInt()
            _nextPrayerCountdown.value = String.format("%02d:%02d:%02d", h, m, s)
        } else {
            _nextPrayerCountdown.value = "00:00:00"
        }

        // Calculate progress based on prayer intervals
        val currentPrayer = prayerTimes.currentPrayer()
        val currentPrayerTime = prayerTimes.timeForPrayer(currentPrayer) ?: Date()
        
        val totalInterval = finalNextPrayerTime.time - currentPrayerTime.time
        val elapsed = now.time - currentPrayerTime.time
        
        if (totalInterval > 0) {
            _nextPrayerProgress.value = (elapsed.toFloat() / totalInterval.toFloat()).coerceIn(0f, 1f)
        } else {
            _nextPrayerProgress.value = 1f
        }
    }
}

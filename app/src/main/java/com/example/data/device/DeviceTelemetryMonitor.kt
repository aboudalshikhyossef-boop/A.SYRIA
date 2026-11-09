package com.example.data.device

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit

/**
 * Owns device-level telemetry resources and exposes immutable state to the UI layer.
 *
 * The monitor is explicitly started and closed by its owner. Sensor registration can
 * be paused independently while the Activity is not in the foreground.
 */
internal class DeviceTelemetryMonitor(
    application: Application,
    private val scope: CoroutineScope
) : SensorEventListener {

    private val appContext = application.applicationContext
    private val sensorManager =
        appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .callTimeout(12, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val _roll = MutableStateFlow(0f)
    val roll: StateFlow<Float> = _roll.asStateFlow()

    private val _pitch = MutableStateFlow(0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _connectionType = MutableStateFlow("Determining...")
    val connectionType: StateFlow<String> = _connectionType.asStateFlow()

    private val _ipAddress = MutableStateFlow("127.0.0.1")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    private val _batteryPercentage = MutableStateFlow(100)
    val batteryPercentage: StateFlow<Int> = _batteryPercentage.asStateFlow()

    private val _chargingStatus = MutableStateFlow("UNKNOWN")
    val chargingStatus: StateFlow<String> = _chargingStatus.asStateFlow()

    private val _hasInternetConnection = MutableStateFlow(true)
    val hasInternetConnection: StateFlow<Boolean> = _hasInternetConnection.asStateFlow()

    private val smoothingFactor = 0.12f
    private var currentRoll = 0f
    private var currentPitch = 0f
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    private var batteryReceiverRegistered = false
    private var sensorsRegistered = false
    private var monitoringStarted = false
    private var connectivityCallback: ConnectivityManager.NetworkCallback? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let(::updateBatteryState)
        }
    }

    fun start() {
        if (monitoringStarted) return
        monitoringStarted = true

        registerBatteryReceiver()
        monitorConnectivity()

        scope.launch {
            try {
                _ipAddress.value = withContext(Dispatchers.IO) { fetchPublicIp() }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _ipAddress.value = extractLocalIp()
            }
        }
    }

    fun startSensors() {
        if (sensorsRegistered) return

        var registered = false
        accelerometer?.let { sensor ->
            registered = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            ) || registered
        }
        magnetometer?.let { sensor ->
            registered = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            ) || registered
        }
        sensorsRegistered = registered
    }

    fun stopSensors() {
        if (!sensorsRegistered) return
        sensorManager.unregisterListener(this)
        sensorsRegistered = false
        gravity = null
        geomagnetic = null
    }

    fun close() {
        stopSensors()

        connectivityCallback?.let { callback ->
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
        connectivityCallback = null

        if (batteryReceiverRegistered) {
            runCatching { appContext.unregisterReceiver(batteryReceiver) }
            batteryReceiverRegistered = false
        }
        monitoringStarted = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val values = event.values
                val accelerationX = values.getOrNull(0) ?: return
                val accelerationY = values.getOrNull(1) ?: return

                gravity = values.copyOf()
                currentRoll += smoothingFactor * ((-accelerationX * 4f) - currentRoll)
                currentPitch += smoothingFactor * ((accelerationY * 4f) - currentPitch)
                _roll.value = currentRoll
                _pitch.value = currentPitch
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagnetic = event.values.copyOf()
            }
        }

        val gravityValues = gravity ?: return
        val geomagneticValues = geomagnetic ?: return
        val rotationMatrix = FloatArray(9)

        if (SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                gravityValues,
                geomagneticValues
            )
        ) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            _azimuth.value =
                (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun registerBatteryReceiver() {
        if (batteryReceiverRegistered) return

        runCatching {
            ContextCompat.registerReceiver(
                appContext,
                batteryReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }.onSuccess { initialState ->
            batteryReceiverRegistered = true
            initialState?.let(::updateBatteryState)
        }
    }

    private fun updateBatteryState(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level >= 0 && scale > 0) {
            _batteryPercentage.value = ((level.toFloat() / scale.toFloat()) * 100f).toInt()
        }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        _chargingStatus.value = if (
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        ) {
            "CHARGING"
        } else {
            "DISCHARGING"
        }
    }

    private fun monitorConnectivity() {
        if (connectivityCallback != null) return

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: android.net.Network,
                capabilities: NetworkCapabilities
            ) {
                _connectionType.value = when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                    else -> "Connected"
                }
                _hasInternetConnection.value =
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }

            override fun onLost(network: android.net.Network) {
                _connectionType.value = "DISCONNECTED"
                _hasInternetConnection.value = false
            }

            override fun onUnavailable() {
                _connectionType.value = "DISCONNECTED"
                _hasInternetConnection.value = false
            }
        }

        connectivityCallback = callback
        try {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } catch (_: Exception) {
            connectivityCallback = null
            _connectionType.value = "Unknown"
            _hasInternetConnection.value = false
        }
    }

    private fun fetchPublicIp(): String {
        val request = Request.Builder()
            .url("https://api64.ipify.org")
            .get()
            .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.trim()?.takeIf(::isValidIpAddress)
                } else {
                    null
                }
            } ?: extractLocalIp()
        } catch (_: Exception) {
            extractLocalIp()
        }
    }

    private fun extractLocalIp(): String {
        return try {
            NetworkInterface.getNetworkInterfaces()
                .toList()
                .mapNotNull { networkInterface ->
                    networkInterface.inetAddresses.toList()
                        .firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
                        ?.hostAddress
                }
                .firstOrNull() ?: "N/A"
        } catch (_: Exception) {
            "N/A"
        }
    }
}

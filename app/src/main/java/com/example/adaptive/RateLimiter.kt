package com.example.adaptive

import android.content.Context

/**
 * Thread-safe local quota reservation within the application process.
 * The quota is consumed when a request starts, not after it succeeds, so
 * concurrent requests cannot all pass a separate check before being counted.
 */
object RateLimiter {

    private const val PREFS_NAME = "sovereign_rate_limiter"
    private const val KEY_COUNT = "request_count"
    private const val KEY_WINDOW = "window_start_ms"
    private const val MAX_REQUESTS = 5
    private const val WINDOW_MS = 30L * 60 * 1000
    private val lock = Any()

    /** Atomically reserves one request in the fixed window. */
    fun tryAcquire(context: Context): Boolean = synchronized(lock) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        var windowStart = prefs.getLong(KEY_WINDOW, 0L)
        var count = prefs.getInt(KEY_COUNT, 0)

        if (windowStart <= 0L || now < windowStart || now - windowStart >= WINDOW_MS) {
            windowStart = now
            count = 0
        }

        if (count >= MAX_REQUESTS) return@synchronized false

        prefs.edit()
            .putLong(KEY_WINDOW, windowStart)
            .putInt(KEY_COUNT, count + 1)
            .commit()
    }

    /** Compatibility query for UI display; does not reserve quota. */
    fun isAllowed(context: Context): Boolean = synchronized(lock) {
        getRemainingCountLocked(context) > 0
    }

    /** Legacy API retained for compatibility; new request paths should use tryAcquire. */
    fun recordRequest(context: Context) {
        tryAcquire(context)
    }

    fun getRemainingCount(context: Context): Int = synchronized(lock) {
        getRemainingCountLocked(context)
    }

    fun getMaxRequests(): Int = MAX_REQUESTS

    fun getResetSeconds(context: Context): Long = synchronized(lock) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val windowStart = prefs.getLong(KEY_WINDOW, 0L)
        if (windowStart <= 0L) return@synchronized 0L
        val elapsed = System.currentTimeMillis() - windowStart
        if (elapsed < 0L || elapsed >= WINDOW_MS) 0L
        else ((WINDOW_MS - elapsed) / 1000L).coerceAtLeast(0L)
    }

    private fun getRemainingCountLocked(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val windowStart = prefs.getLong(KEY_WINDOW, 0L)
        if (windowStart <= 0L || now < windowStart || now - windowStart >= WINDOW_MS) {
            return MAX_REQUESTS
        }
        return (MAX_REQUESTS - prefs.getInt(KEY_COUNT, 0)).coerceAtLeast(0)
    }
}

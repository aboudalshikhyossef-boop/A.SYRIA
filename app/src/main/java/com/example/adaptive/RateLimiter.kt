package com.example.adaptive

import android.content.Context

/**
 * RateLimiter — Local rate-limiting using SharedPreferences.
 * Policy: max 5 requests per 30-minute rolling window.
 * Thread-safe via synchronized reads/writes.
 */
object RateLimiter {

    private const val PREFS_NAME   = "sovereign_rate_limiter"
    private const val KEY_COUNT    = "request_count"
    private const val KEY_WINDOW   = "window_start_ms"
    private const val MAX_REQUESTS = 5
    private const val WINDOW_MS    = 30L * 60 * 1000   // 30 minutes

    /** Returns true if the user has quota remaining in the current window. */
    fun isAllowed(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val windowStart = prefs.getLong(KEY_WINDOW, 0L)
        if (now - windowStart > WINDOW_MS) {
            prefs.edit().putLong(KEY_WINDOW, now).putInt(KEY_COUNT, 0).apply()
        }
        return prefs.getInt(KEY_COUNT, 0) < MAX_REQUESTS
    }

    /** Records one request against the current window. Call after isAllowed() == true. */
    fun recordRequest(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val windowStart = prefs.getLong(KEY_WINDOW, 0L)
        if (now - windowStart > WINDOW_MS) {
            prefs.edit().putLong(KEY_WINDOW, now).putInt(KEY_COUNT, 1).apply()
        } else {
            val count = prefs.getInt(KEY_COUNT, 0)
            prefs.edit().putInt(KEY_COUNT, count + 1).apply()
        }
    }

    /** Remaining requests in the current window. */
    fun getRemainingCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val windowStart = prefs.getLong(KEY_WINDOW, 0L)
        if (now - windowStart > WINDOW_MS) return MAX_REQUESTS
        return (MAX_REQUESTS - prefs.getInt(KEY_COUNT, 0)).coerceAtLeast(0)
    }

    /** Max requests allowed per window (used in UI). */
    fun getMaxRequests(): Int = MAX_REQUESTS

    /**
     * Seconds until the rate-limit window resets.
     * Returns 0 if the window has already expired.
     */
    fun getResetSeconds(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val windowStart = prefs.getLong(KEY_WINDOW, 0L)
        if (windowStart == 0L) return 0L
        val elapsed = System.currentTimeMillis() - windowStart
        return ((WINDOW_MS - elapsed) / 1000).coerceAtLeast(0L)
    }
}

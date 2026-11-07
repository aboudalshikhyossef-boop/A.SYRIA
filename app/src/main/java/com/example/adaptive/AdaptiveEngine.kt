package com.example.adaptive

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Immutable snapshot of ALL dynamically-configurable UI properties.
 * Every field has a safe default — no null values, crash-proof.
 */
data class UIConfig(
    // ── Colour ──────────────────────────────────────────────────────────────
    val accentColor: String    = "cyan",      // cyan | amber | green | white | red | purple
    val primaryHex: String     = "",          // custom hex override e.g. "#FF5500" (overrides accentColor)
    val backgroundHex: String  = "",          // custom background hex e.g. "#0D1117"

    // ── Typography ───────────────────────────────────────────────────────────
    val fontSize: Float        = 14f,         // 10..24
    val fontWeight: String     = "normal",    // light | normal | bold

    // ── Layout ───────────────────────────────────────────────────────────────
    val cornerRadius: Int      = 12,          // 0..32 (card corner radius in dp)
    val cardStyle: String      = "glass",     // glass | solid | outline | flat
    val compactMode: Boolean   = false,       // tighter spacing everywhere
    val tabStyle: String       = "normal",    // normal | compact

    // ── Visibility ───────────────────────────────────────────────────────────
    val showPrayerTab: Boolean  = true,
    val showAcademyTab: Boolean = true,
    val showStatusBar: Boolean  = true,       // show device/network status widget

    // ── Animation & Motion ───────────────────────────────────────────────────
    val animationsEnabled: Boolean = true,

    // ── Notifications ────────────────────────────────────────────────────────
    val notificationLevel: String = "normal", // silent | normal | active

    // ── Clock Format ─────────────────────────────────────────────────────────
    val is12HourFormat: Boolean = false       // false = 24h (default), true = 12h AM/PM
)

/** Proposed UI change returned by FeedbackService before the user confirms. */
data class UIChange(
    val key: String,
    val value: String,
    val label: String,
    val preview: String
)

/** Composition local — any Composable can read current UIConfig without prop-drilling. */
val LocalAdaptiveConfig = compositionLocalOf { UIConfig() }

/**
 * AdaptiveEngine — Single source of truth for dynamic UI configuration.
 *
 * Persists UIConfig as JSON in SharedPreferences.
 * Exposes [StateFlow<UIConfig>] so Compose re-renders on any change.
 * Validates every change against a strict whitelist — injection-proof.
 */
class AdaptiveEngine(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "sovereign_adaptive_ui"
        private const val KEY_CONFIG  = "ui_config_json_v3"

        private val HEX_REGEX = Regex("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")

        /** Whitelisted keys with validation lambdas. */
        val ALLOWED_KEYS: Map<String, (String) -> Boolean> = mapOf(
            // Colour
            "accentColor"       to { v -> v in setOf("cyan", "amber", "green", "white", "red", "purple") },
            "primaryHex"        to { v -> v.isEmpty() || HEX_REGEX.matches(v) },
            "backgroundHex"     to { v -> v.isEmpty() || HEX_REGEX.matches(v) },
            // Typography
            "fontSize"          to { v -> v.toFloatOrNull()?.let { it in 10f..24f } ?: false },
            "fontWeight"        to { v -> v in setOf("light", "normal", "bold") },
            // Layout
            "cornerRadius"      to { v -> v.toIntOrNull()?.let { it in 0..32 } ?: false },
            "cardStyle"         to { v -> v in setOf("glass", "solid", "outline", "flat") },
            "compactMode"       to { v -> v == "true" || v == "false" },
            "tabStyle"          to { v -> v == "compact" || v == "normal" },
            // Visibility
            "showPrayerTab"     to { v -> v == "true" || v == "false" },
            "showAcademyTab"    to { v -> v == "true" || v == "false" },
            "showStatusBar"     to { v -> v == "true" || v == "false" },
            // Animation
            "animationsEnabled" to { v -> v == "true" || v == "false" },
            // Notifications
            "notificationLevel" to { v -> v in setOf("silent", "normal", "active") },
            // Clock Format
            "is12HourFormat"    to { v -> v == "true" || v == "false" }
        )
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _uiConfig = MutableStateFlow(loadFromPrefs())
    val uiConfig: StateFlow<UIConfig> = _uiConfig.asStateFlow()

    /** Atomically validate and apply confirmed [UIChange] items. Drops invalid keys silently. */
    fun applyChanges(changes: List<UIChange>) {
        var updated = _uiConfig.value
        for (change in changes) {
            val validator = ALLOWED_KEYS[change.key] ?: continue
            if (!validator(change.value)) continue
            updated = when (change.key) {
                "accentColor"       -> updated.copy(accentColor = change.value)
                "primaryHex"        -> updated.copy(primaryHex = change.value)
                "backgroundHex"     -> updated.copy(backgroundHex = change.value)
                "fontSize"          -> updated.copy(fontSize = change.value.toFloat())
                "fontWeight"        -> updated.copy(fontWeight = change.value)
                "cornerRadius"      -> updated.copy(cornerRadius = change.value.toInt())
                "cardStyle"         -> updated.copy(cardStyle = change.value)
                "compactMode"       -> updated.copy(compactMode = change.value == "true")
                "tabStyle"          -> updated.copy(tabStyle = change.value)
                "showPrayerTab"     -> updated.copy(showPrayerTab = change.value == "true")
                "showAcademyTab"    -> updated.copy(showAcademyTab = change.value == "true")
                "showStatusBar"     -> updated.copy(showStatusBar = change.value == "true")
                "animationsEnabled" -> updated.copy(animationsEnabled = change.value == "true")
                "notificationLevel" -> updated.copy(notificationLevel = change.value)
                "is12HourFormat"    -> updated.copy(is12HourFormat = change.value == "true")
                else                -> updated
            }
        }
        saveToPrefs(updated)
        _uiConfig.value = updated
    }

    /** Reset everything to factory defaults. */
    fun resetToDefaults() {
        val default = UIConfig()
        saveToPrefs(default)
        _uiConfig.value = default
    }

    private fun loadFromPrefs(): UIConfig {
        val json = prefs.getString(KEY_CONFIG, null) ?: return UIConfig()
        return try {
            val o = JSONObject(json)
            UIConfig(
                accentColor       = o.optString("accentColor", "cyan"),
                primaryHex        = o.optString("primaryHex", ""),
                backgroundHex     = o.optString("backgroundHex", ""),
                fontSize          = o.optDouble("fontSize", 14.0).toFloat(),
                fontWeight        = o.optString("fontWeight", "normal"),
                cornerRadius      = o.optInt("cornerRadius", 12),
                cardStyle         = o.optString("cardStyle", "glass"),
                compactMode       = o.optBoolean("compactMode", false),
                tabStyle          = o.optString("tabStyle", "normal"),
                showPrayerTab     = o.optBoolean("showPrayerTab", true),
                showAcademyTab    = o.optBoolean("showAcademyTab", true),
                showStatusBar     = o.optBoolean("showStatusBar", true),
                animationsEnabled = o.optBoolean("animationsEnabled", true),
                notificationLevel = o.optString("notificationLevel", "normal"),
                is12HourFormat    = o.optBoolean("is12HourFormat", false)
            )
        } catch (e: Exception) { UIConfig() }
    }

    private fun saveToPrefs(config: UIConfig) {
        val json = JSONObject().apply {
            put("accentColor",       config.accentColor)
            put("primaryHex",        config.primaryHex)
            put("backgroundHex",     config.backgroundHex)
            put("fontSize",          config.fontSize)
            put("fontWeight",        config.fontWeight)
            put("cornerRadius",      config.cornerRadius)
            put("cardStyle",         config.cardStyle)
            put("compactMode",       config.compactMode)
            put("tabStyle",          config.tabStyle)
            put("showPrayerTab",     config.showPrayerTab)
            put("showAcademyTab",    config.showAcademyTab)
            put("showStatusBar",     config.showStatusBar)
            put("animationsEnabled", config.animationsEnabled)
            put("notificationLevel", config.notificationLevel)
            put("is12HourFormat",    config.is12HourFormat)
        }.toString()
        prefs.edit().putString(KEY_CONFIG, json).apply()
    }
}

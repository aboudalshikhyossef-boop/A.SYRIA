package com.example.data

object ContextualVerseEngine {
    
    data class SpiritualInsight(
        val stateType: AmbientStateType,
        val systemAnalysis: String,
        val verseArabic: String,
        val verseTranslation: String,
        val verseReference: String
    )

    enum class AmbientStateType {
        CRITICAL,          // Low power (< 20%) or No connectivity
        HIGH_PERFORMANCE,  // Charging, full power, or secure high-seed WiFi
        NORMAL             // Any other nominal steady-state operation
    }

    fun getInsight(batteryPercentage: Int, isCharging: Boolean, connectionType: String): SpiritualInsight {
        return when {
            // Low battery threshold mapping < 20%
            batteryPercentage < 20 -> {
                SpiritualInsight(
                    stateType = AmbientStateType.CRITICAL,
                    systemAnalysis = "SYSTEM BREATHING SLOWLY DUE TO LOW POWER",
                    verseArabic = "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
                    verseTranslation = "Verily, with hardship comes ease.",
                    verseReference = "Ash-Sharh [94:6]"
                )
            }
            // Severed or missing networking mapping
            connectionType == "DISCONNECTED" || connectionType.contains("Determining") -> {
                SpiritualInsight(
                    stateType = AmbientStateType.CRITICAL,
                    systemAnalysis = "SECURE CHANNEL STATUS: OFFLINE. LOCAL SHIELDS ACTIVE",
                    verseArabic = "وَمَنْ يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ",
                    verseTranslation = "And whoever relies upon Allah - then He is sufficient for him.",
                    verseReference = "At-Talaq [65:3]"
                )
            }
            // Energetic High performance charging state mapping
            isCharging || batteryPercentage >= 95 -> {
                SpiritualInsight(
                    stateType = AmbientStateType.HIGH_PERFORMANCE,
                    systemAnalysis = "POWER RECEPTACLES SATURATED. QUANTUM CORES CHARGED",
                    verseArabic = "اللَّهُ نُورُ السَّمَاوَاتِ وَالْأَرْضِ",
                    verseTranslation = "Allah is the Light of the heavens and the earth.",
                    verseReference = "An-Nur [24:35]"
                )
            }
            // Gigabit WiFi network interface context mapping
            connectionType.contains("WIFI") -> {
                SpiritualInsight(
                    stateType = AmbientStateType.HIGH_PERFORMANCE,
                    systemAnalysis = "DATA SYMPLEX LINK AT PEAK DISPATCH RATES",
                    verseArabic = "يَهْدِي اللَّهُ لِنُورِهِ مَنْ يَشَاءُ",
                    verseTranslation = "Allah guides to His light whom He wills.",
                    verseReference = "An-Nur [24:35]"
                )
            }
            // Standard normal secure states
            else -> {
                SpiritualInsight(
                    stateType = AmbientStateType.NORMAL,
                    systemAnalysis = "SAFE SYSTEM PROTOCOLS ONLINE AND EXTREMELY STABLE",
                    verseArabic = "لَئِنْ شَكَرْتُمْ لَأَزِيدَنَّكُمْ",
                    verseTranslation = "If you are grateful, I will surely increase you.",
                    verseReference = "Ibrahim [14:7]"
                )
            }
        }
    }
}

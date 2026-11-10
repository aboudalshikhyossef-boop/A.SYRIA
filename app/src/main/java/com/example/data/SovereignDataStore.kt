package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.database.SovereigntyCipher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sovereign_settings")

class SovereignDataStore(private val context: Context) {

    companion object {
        val OPERATOR_NAME       = stringPreferencesKey("operator_name")
        val NEURAL_ROLE         = stringPreferencesKey("neural_role")
        val GEMINI_API_KEY      = stringPreferencesKey("gemini_api_key")
        val PROJECT_NAME        = stringPreferencesKey("project_name")
        val PROJECT_ID          = stringPreferencesKey("project_id")
        val PROJECT_NUMBER      = stringPreferencesKey("project_number")
        val IS_ARABIC           = booleanPreferencesKey("is_arabic")
        val STEALTH_MODE        = booleanPreferencesKey("stealth_mode")
        val LOCK_APP_ON_LAUNCH  = booleanPreferencesKey("lock_app_on_launch")
        val CYBER_SCORE         = intPreferencesKey("cyber_score")
        val USER_LEVEL          = stringPreferencesKey("user_level")
        val USER_INTERESTS      = stringSetPreferencesKey("user_interests")
        val CALIBRATION_COMPLETED = booleanPreferencesKey("calibration_completed")
        val USER_GOAL           = stringPreferencesKey("user_goal")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ACADEMY_POINTS      = intPreferencesKey("academy_points")
        val ACADEMY_STREAK      = intPreferencesKey("academy_streak")
        val LAST_QUIZ_DATE      = stringPreferencesKey("last_quiz_date")
        // User profile keys
        val USER_GENDER         = stringPreferencesKey("user_gender")
        val USER_BIRTH_YEAR     = intPreferencesKey("user_birth_year")
        val USER_PROFILE_ANALYSIS = stringPreferencesKey("user_profile_analysis")
        val PROFILE_COMPLETED   = booleanPreferencesKey("profile_completed")
    }

    val operatorName: Flow<String>  = context.dataStore.data.map { it[OPERATOR_NAME]  ?: "Sovereign_Operator" }
    val neuralRole: Flow<String>    = context.dataStore.data.map { it[NEURAL_ROLE]    ?: "Sovereign Node v5" }
    val geminiApiKey: Flow<String> = context.dataStore.data.map {
        SovereigntyCipher.decryptSecret(it[GEMINI_API_KEY])
    }
    val projectName: Flow<String>   = context.dataStore.data.map { it[PROJECT_NAME]   ?: "" }
    val projectId: Flow<String>     = context.dataStore.data.map { it[PROJECT_ID]     ?: "" }
    val projectNumber: Flow<String> = context.dataStore.data.map { it[PROJECT_NUMBER] ?: "" }
    val isArabic: Flow<Boolean>     = context.dataStore.data.map { it[IS_ARABIC]      ?: true }
    val stealthMode: Flow<Boolean>  = context.dataStore.data.map { it[STEALTH_MODE]   ?: false }
    val lockAppOnLaunch: Flow<Boolean> = context.dataStore.data.map { it[LOCK_APP_ON_LAUNCH] ?: true }
    val cyberScore: Flow<Int>       = context.dataStore.data.map { it[CYBER_SCORE]    ?: 0 }
    val userLevel: Flow<String>     = context.dataStore.data.map { it[USER_LEVEL]     ?: "Beginner" }
    val userInterests: Flow<Set<String>> = context.dataStore.data.map { it[USER_INTERESTS] ?: emptySet() }
    val calibrationCompleted: Flow<Boolean> = context.dataStore.data.map { it[CALIBRATION_COMPLETED] ?: false }
    val userGoal: Flow<String>      = context.dataStore.data.map { it[USER_GOAL]      ?: "" }
    val onboardingCompleted: Flow<Boolean>  = context.dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }
    val academyPoints: Flow<Int>    = context.dataStore.data.map { it[ACADEMY_POINTS] ?: 0 }
    val academyStreak: Flow<Int>    = context.dataStore.data.map { it[ACADEMY_STREAK] ?: 0 }
    val lastQuizDate: Flow<String>  = context.dataStore.data.map { it[LAST_QUIZ_DATE] ?: "" }
    // User profile flows
    val userGender: Flow<String>    = context.dataStore.data.map { it[USER_GENDER]   ?: "" }
    val userBirthYear: Flow<Int>    = context.dataStore.data.map { it[USER_BIRTH_YEAR] ?: 0 }
    val userProfileAnalysis: Flow<String> = context.dataStore.data.map { it[USER_PROFILE_ANALYSIS] ?: "" }
    val profileCompleted: Flow<Boolean>   = context.dataStore.data.map { it[PROFILE_COMPLETED] ?: false }

    suspend fun clearLegacyAiSettings() {
        context.dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey("groq_api_key"))
            preferences.remove(booleanPreferencesKey("neural_proxy"))
        }
    }

    suspend fun saveOperatorName(name: String) { context.dataStore.edit { it[OPERATOR_NAME] = name } }
    suspend fun saveNeuralRole(role: String)   { context.dataStore.edit { it[NEURAL_ROLE]   = role } }
     suspend fun saveGeminiApiKey(key: String) {
        context.dataStore.edit { it[GEMINI_API_KEY] = SovereigntyCipher.encryptSecret(key) }
    }

    /** Encrypts a legacy plaintext API key once, without interrupting existing readers. */
    suspend fun migrateLegacyGeminiApiKey() {
        context.dataStore.edit { preferences ->
            val stored = preferences[GEMINI_API_KEY].orEmpty()
            if (stored.isNotBlank() && !SovereigntyCipher.isSecretEncrypted(stored)) {
                preferences[GEMINI_API_KEY] = SovereigntyCipher.encryptSecret(stored)
            }
        }
    }
    suspend fun saveProjectName(name: String)  { context.dataStore.edit { it[PROJECT_NAME]   = name } }
    suspend fun saveProjectId(id: String)      { context.dataStore.edit { it[PROJECT_ID]     = id } }
    suspend fun saveProjectNumber(n: String)   { context.dataStore.edit { it[PROJECT_NUMBER] = n } }
    suspend fun saveIsArabic(v: Boolean)       { context.dataStore.edit { it[IS_ARABIC]      = v } }
    suspend fun saveStealthMode(v: Boolean)    { context.dataStore.edit { it[STEALTH_MODE]   = v } }
    suspend fun saveLockAppOnLaunch(v: Boolean) { context.dataStore.edit { it[LOCK_APP_ON_LAUNCH] = v } }
    suspend fun saveCyberScore(score: Int)     { context.dataStore.edit { it[CYBER_SCORE]    = score } }
    
    /** Atomic score increment; avoids lost updates from concurrent feature actions. */
    suspend fun addCyberScore(points: Int) {
        context.dataStore.edit { preferences ->
            preferences[CYBER_SCORE] = (preferences[CYBER_SCORE] ?: 0) + points
        }
    }
    suspend fun saveUserLevel(level: String)   { context.dataStore.edit { it[USER_LEVEL]     = level } }
    suspend fun saveUserInterests(s: Set<String>) { context.dataStore.edit { it[USER_INTERESTS] = s } }
    suspend fun saveCalibrationCompleted(v: Boolean) { context.dataStore.edit { it[CALIBRATION_COMPLETED] = v } }
    suspend fun saveUserGoal(goal: String)     { context.dataStore.edit { it[USER_GOAL]      = goal } }
    suspend fun saveUserGender(gender: String) { context.dataStore.edit { it[USER_GENDER]    = gender } }
    suspend fun saveUserBirthYear(year: Int)   { context.dataStore.edit { it[USER_BIRTH_YEAR] = year } }
    suspend fun saveUserProfileAnalysis(analysis: String) { context.dataStore.edit { it[USER_PROFILE_ANALYSIS] = analysis } }
    suspend fun setProfileCompleted()          { context.dataStore.edit { it[PROFILE_COMPLETED] = true } }

    suspend fun setOnboardingCompleted() { context.dataStore.edit { it[ONBOARDING_COMPLETED] = true } }

    suspend fun addAcademyPoints(pts: Int) {
        context.dataStore.edit { prefs ->
            prefs[ACADEMY_POINTS] = (prefs[ACADEMY_POINTS] ?: 0) + pts
        }
    }

    suspend fun updateAcademyStreak() {
        val today = LocalDate.now()
        val todayKey = today.toString()
        val yesterdayKey = today.minusDays(1).toString()
        context.dataStore.edit { prefs ->
            val lastDate = prefs[LAST_QUIZ_DATE] ?: ""
            val current = prefs[ACADEMY_STREAK] ?: 0
            prefs[LAST_QUIZ_DATE] = todayKey
            prefs[ACADEMY_STREAK] = when {
                lastDate == todayKey -> current
                lastDate == yesterdayKey -> current + 1
                else -> 1
            }
        }
    }
}

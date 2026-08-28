package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("habittrack_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val langCode = prefs.getString("language", AppLanguage.AUTO.code) ?: AppLanguage.AUTO.code
        val aiEnabled = prefs.getBoolean("ai_enabled", true)
        val dailyAdviceEnabled = prefs.getBoolean("daily_advice_enabled", true)
        val weeklyReviewEnabled = prefs.getBoolean("weekly_review_enabled", true)
        val aiNotificationsEnabled = prefs.getBoolean("ai_notifications_enabled", true)
        val habitNotificationsEnabled = prefs.getBoolean("habit_notifications_enabled", true)
        val waterNotificationsEnabled = prefs.getBoolean("water_notifications_enabled", true)
        val studyNotificationsEnabled = prefs.getBoolean("study_notifications_enabled", true)
        val workoutNotificationsEnabled = prefs.getBoolean("workout_notifications_enabled", true)
        val sleepNotificationsEnabled = prefs.getBoolean("sleep_notifications_enabled", true)
        val morningMotivationEnabled = prefs.getBoolean("morning_motivation_enabled", true)
        val eveningReviewEnabled = prefs.getBoolean("evening_review_enabled", true)
        val streakAlertsEnabled = prefs.getBoolean("streak_alerts_enabled", true)
        val themeMode = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
        val freqName = prefs.getString("advice_frequency", AdviceFrequency.BOTH.name) ?: AdviceFrequency.BOTH.name
        val goals = prefs.getString("user_goals", "Build consistent reading habit, stay hydrated, maintain daily study routine, and limit night screen time.") ?: ""

        val freq = try {
            AdviceFrequency.valueOf(freqName)
        } catch (_: Exception) {
            AdviceFrequency.BOTH
        }

        return UserSettings(
            language = AppLanguage.fromCode(langCode),
            aiEnabled = aiEnabled,
            dailyAdviceEnabled = dailyAdviceEnabled,
            weeklyReviewEnabled = weeklyReviewEnabled,
            aiNotificationsEnabled = aiNotificationsEnabled,
            habitNotificationsEnabled = habitNotificationsEnabled,
            waterNotificationsEnabled = waterNotificationsEnabled,
            studyNotificationsEnabled = studyNotificationsEnabled,
            workoutNotificationsEnabled = workoutNotificationsEnabled,
            sleepNotificationsEnabled = sleepNotificationsEnabled,
            morningMotivationEnabled = morningMotivationEnabled,
            eveningReviewEnabled = eveningReviewEnabled,
            streakAlertsEnabled = streakAlertsEnabled,
            themeMode = themeMode,
            adviceFrequency = freq,
            userGoals = goals
        )
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("language", language.code).apply()
        _settings.value = _settings.value.copy(language = language)
    }

    fun setThemeMode(themeMode: String) {
        prefs.edit().putString("theme_mode", themeMode).apply()
        _settings.value = _settings.value.copy(themeMode = themeMode)
    }

    fun setAiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("ai_enabled", enabled).apply()
        _settings.value = _settings.value.copy(aiEnabled = enabled)
    }

    fun setDailyAdviceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("daily_advice_enabled", enabled).apply()
        _settings.value = _settings.value.copy(dailyAdviceEnabled = enabled)
    }

    fun setWeeklyReviewEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("weekly_review_enabled", enabled).apply()
        _settings.value = _settings.value.copy(weeklyReviewEnabled = enabled)
    }

    fun setAiNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("ai_notifications_enabled", enabled).apply()
        _settings.value = _settings.value.copy(aiNotificationsEnabled = enabled)
    }

    fun setHabitNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("habit_notifications_enabled", enabled).apply()
        _settings.value = _settings.value.copy(habitNotificationsEnabled = enabled)
    }

    fun setWaterNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("water_notifications_enabled", enabled).apply()
        _settings.value = _settings.value.copy(waterNotificationsEnabled = enabled)
    }

    fun setStudyNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("study_notifications_enabled", enabled).apply()
        _settings.value = _settings.value.copy(studyNotificationsEnabled = enabled)
    }

    fun setWorkoutNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("workout_notifications_enabled", enabled).apply()
        _settings.value = _settings.value.copy(workoutNotificationsEnabled = enabled)
    }

    fun setSleepNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sleep_notifications_enabled", enabled).apply()
        _settings.value = _settings.value.copy(sleepNotificationsEnabled = enabled)
    }

    fun setMorningMotivationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("morning_motivation_enabled", enabled).apply()
        _settings.value = _settings.value.copy(morningMotivationEnabled = enabled)
    }

    fun setEveningReviewEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("evening_review_enabled", enabled).apply()
        _settings.value = _settings.value.copy(eveningReviewEnabled = enabled)
    }

    fun setStreakAlertsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("streak_alerts_enabled", enabled).apply()
        _settings.value = _settings.value.copy(streakAlertsEnabled = enabled)
    }

    fun setAdviceFrequency(frequency: AdviceFrequency) {
        prefs.edit().putString("advice_frequency", frequency.name).apply()
        _settings.value = _settings.value.copy(adviceFrequency = frequency)
    }

    fun setUserGoals(goals: String) {
        prefs.edit().putString("user_goals", goals).apply()
        _settings.value = _settings.value.copy(userGoals = goals)
    }
}

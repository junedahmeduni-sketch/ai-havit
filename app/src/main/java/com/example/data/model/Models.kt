package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    AUTO("auto", "Auto (স্বয়ংক্রিয়)", "স্বয়ংক্রিয়"),
    BENGALI("bn", "Bengali (বাংলা)", "বাংলা"),
    ENGLISH("en", "English", "English");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: AUTO
    }
}

enum class HabitCategory(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val iconName: String,
    val iconEmoji: String,
    val colorHex: Long,
    val coachingFocusEn: String,
    val coachingFocusBn: String
) {
    HEALTH(
        id = "Health",
        titleEn = "Health",
        titleBn = "স্বাস্থ্য",
        iconName = "favorite",
        iconEmoji = "🩺",
        colorHex = 0xFF10B981, // Emerald
        coachingFocusEn = "Physical vitality, healthy routines, nutrition, and well-being",
        coachingFocusBn = "শারীরিক সুস্থতা, সঠিক খাদ্যাভ্যাস ও স্বাস্থ্যবিধি"
    ),
    FITNESS(
        id = "Fitness",
        titleEn = "Fitness",
        titleBn = "ব্যায়াম ও ফিটনেস",
        iconName = "directions_run",
        iconEmoji = "🏋️",
        colorHex = 0xFFF97316, // Orange
        coachingFocusEn = "Cardio, workout sessions, step counts, and physical endurance",
        coachingFocusBn = "নিয়মিত ব্যায়াম, দৌড়ানো ও শক্তি বৃদ্ধি"
    ),
    STUDY(
        id = "Study",
        titleEn = "Study",
        titleBn = "পড়াশোনা ও শিক্ষা",
        iconName = "school",
        iconEmoji = "📚",
        colorHex = 0xFF3B82F6, // Blue
        coachingFocusEn = "Focused study sessions, academic discipline, and learning goals",
        coachingFocusBn = "নিয়মিত পড়াশোনা, গভীর মনোযোগ ও নতুন জ্ঞানার্জন"
    ),
    WORK(
        id = "Work",
        titleEn = "Work",
        titleBn = "কাজ ও পেশা",
        iconName = "work",
        iconEmoji = "💼",
        colorHex = 0xFF6366F1, // Indigo
        coachingFocusEn = "Deep work discipline, career projects, and professional execution",
        coachingFocusBn = "কাজে গভীর মনোযোগ, প্রজেক্ট সম্পন্ন করা ও কর্মদক্ষতা"
    ),
    SLEEP(
        id = "Sleep",
        titleEn = "Sleep",
        titleBn = "ঘুম ও বিশ্রাম",
        iconName = "bedtime",
        iconEmoji = "😴",
        colorHex = 0xFF8B5CF6, // Purple
        coachingFocusEn = "Consistent bedtime, sleep hygiene, and restorative recovery",
        coachingFocusBn = "নির্দিষ্ট সময়ে ঘুমানো, পর্যাপ্ত বিশ্রাম ও ঘুমের ভারসাম্য"
    ),
    WATER(
        id = "Water",
        titleEn = "Water",
        titleBn = "পানি পান",
        iconName = "water_drop",
        iconEmoji = "💧",
        colorHex = 0xFF06B6D4, // Cyan
        coachingFocusEn = "Daily hydration targets, fluid intake, and vitality",
        coachingFocusBn = "পর্যাপ্ত পানি পান ও হাইড্রেটেড থাকা"
    ),
    PERSONAL(
        id = "Personal",
        titleEn = "Personal",
        titleBn = "ব্যক্তিগত ও মন",
        iconName = "self_improvement",
        iconEmoji = "🧘",
        colorHex = 0xFFEC4899, // Pink
        coachingFocusEn = "Mindfulness, meditation, journaling, and emotional clarity",
        coachingFocusBn = "মাইন্ডফুলনেস, ধ্যান, আত্মচিন্তা ও মানসিক শান্তি"
    ),
    FINANCE(
        id = "Finance",
        titleEn = "Finance",
        titleBn = "আর্থিক শৃঙ্খলা",
        iconName = "payments",
        iconEmoji = "💰",
        colorHex = 0xFF14B8A6, // Teal
        coachingFocusEn = "Daily budgeting, mindful spending, and saving consistency",
        coachingFocusBn = "সঞ্চয়, অপব্যয় রোধ ও আর্থিক শৃঙ্খলা"
    ),
    OTHER(
        id = "Other",
        titleEn = "Other",
        titleBn = "অন্যান্য",
        iconName = "category",
        iconEmoji = "✨",
        colorHex = 0xFF64748B, // Slate
        coachingFocusEn = "Custom personal routines and positive lifestyle habits",
        coachingFocusBn = "ব্যক্তিগত পছন্দ ও অন্যান্য ভালো অভ্যাস"
    );

    fun getDisplayName(language: AppLanguage): String =
        if (language == AppLanguage.BENGALI) titleBn else titleEn

    fun getCoachingFocus(language: AppLanguage): String =
        if (language == AppLanguage.BENGALI) coachingFocusBn else coachingFocusEn

    companion object {
        fun fromId(id: String): HabitCategory {
            val normalized = id.trim().lowercase()
            return when {
                normalized.contains("water") || normalized.contains("পানি") -> WATER
                normalized.contains("sleep") || normalized.contains("ঘুম") -> SLEEP
                normalized.contains("study") || normalized.contains("read") || normalized.contains("বই") || normalized.contains("পড়া") -> STUDY
                normalized.contains("fit") || normalized.contains("gym") || normalized.contains("workout") || normalized.contains("ব্যায়াম") -> FITNESS
                normalized.contains("health") || normalized.contains("স্বাস্থ্য") -> HEALTH
                normalized.contains("work") || normalized.contains("prod") || normalized.contains("কাজ") -> WORK
                normalized.contains("personal") || normalized.contains("mind") || normalized.contains("ব্যক্তিগত") -> PERSONAL
                normalized.contains("fin") || normalized.contains("টাকা") || normalized.contains("আর্থিক") -> FINANCE
                else -> entries.find { it.id.equals(id, ignoreCase = true) } ?: HEALTH
            }
        }
    }
}

data class CategoryProgress(
    val category: HabitCategory,
    val totalCount: Int,
    val completedCount: Int,
    val completionRate: Float, // 0.0f to 1.0f
    val topStreak: Int,
    val habitsSummary: String
)

enum class AdviceFrequency(val displayNameEn: String, val displayNameBn: String) {
    MORNING("Every Morning (8:00 AM)", "প্রতিদিন সকালে (৮:০০)"),
    EVENING("Every Evening (8:00 PM)", "প্রতিদিন সন্ধ্যায় (৮:০০)"),
    BOTH("Twice Daily (Morning & Evening)", "দিনে দুইবার (সকাল ও সন্ধ্যা)")
}

// 1. User & Account Entity
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "user_default",
    val name: String = "Alex Johnson",
    val email: String = "user@habittrack.ai",
    val passwordHash: String = "password123",
    val avatarEmoji: String = "⚡",
    val xp: Int = 1250,
    val level: Int = 4,
    val streakFreezeCount: Int = 2,
    val isStreakFreezeActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// 2. Habit Entity
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "user_default",
    val titleEn: String,
    val titleBn: String,
    val category: String = "Health",
    val targetDaysPerWeek: Int = 7,
    val streakCount: Int = 0,
    val bestStreak: Int = 0,
    val reminderTime: String = "08:00",
    val iconName: String = "check",
    val colorHex: Long = 0xFF10B981,
    val frequency: String = "Daily", // Daily, Weekly, Weekdays
    val goalTarget: String = "1 time",
    val notes: String = "",
    val isPaused: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// 3. Habit Completion Entity
@Entity(tableName = "habit_completions", primaryKeys = ["habitId", "date"])
data class HabitCompletionEntity(
    val habitId: Long,
    val date: String, // "YYYY-MM-DD"
    val userId: String = "user_default",
    val status: String = "COMPLETED", // COMPLETED, SKIPPED, MISSED
    val isCompleted: Boolean = true,
    val completedAt: Long = System.currentTimeMillis()
)

// 4. Goals Entity
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "user_default",
    val title: String,
    val description: String = "",
    val target: String = "100%",
    val deadline: String = "2026-12-31",
    val progressPct: Int = 0,
    val category: String = "Personal",
    val isLongTerm: Boolean = false,
    val notes: String = "",
    val subtasksJson: String = "", // Comma or JSON formatted breakdown
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// 5. Mood Tracker Entity
@Entity(tableName = "moods")
data class MoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "user_default",
    val date: String, // "YYYY-MM-DD"
    val moodLevel: Int, // 1: Very Bad 😡, 2: Bad 😔, 3: Normal 😐, 4: Good 🙂, 5: Great 😄
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// 6. Daily Journal Entity
@Entity(tableName = "journals")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "user_default",
    val date: String, // "YYYY-MM-DD"
    val title: String,
    val content: String,
    val moodEmoji: String = "✍️",
    val aiSummary: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// 7. Sleep Tracker Entity
@Entity(tableName = "sleep_logs")
data class SleepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "user_default",
    val date: String, // "YYYY-MM-DD"
    val bedtime: String = "23:00",
    val wakeTime: String = "07:00",
    val durationMinutes: Int = 480, // 8 hours
    val goalMinutes: Int = 480,
    val qualityRating: Int = 4, // 1-5
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// 8. Water Tracker Entity
@Entity(tableName = "water_logs")
data class WaterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "user_default",
    val date: String, // "YYYY-MM-DD"
    val glassesDrank: Int = 5,
    val goalGlasses: Int = 8,
    val targetMl: Int = 2000,
    val timestamp: Long = System.currentTimeMillis()
)

// 9. Study Mode Entity
@Entity(tableName = "study_sessions")
data class StudyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "user_default",
    val date: String, // "YYYY-MM-DD"
    val subject: String = "General Study",
    val durationMinutes: Int = 25,
    val sessionType: String = "Pomodoro", // Pomodoro, Deep Work, Review
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// 10. Workout Tracker Entity
@Entity(tableName = "workout_logs")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "user_default",
    val date: String, // "YYYY-MM-DD"
    val workoutType: String = "Cardio & Strength",
    val exercise: String = "Pushups & Running",
    val durationMinutes: Int = 30,
    val sets: Int = 3,
    val reps: Int = 15,
    val caloriesBurned: Int = 180,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// 11. Focus Session Entity
@Entity(tableName = "focus_sessions")
data class FocusEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "user_default",
    val date: String, // "YYYY-MM-DD"
    val durationMinutes: Int = 25,
    val mode: String = "25/5 Pomodoro", // "25/5 Pomodoro", "50/10 Focus", "Custom"
    val taskName: String = "Deep Work Focus",
    val isCompleted: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

// 12. Smart Daily Plan Entity
@Entity(tableName = "daily_plans")
data class DailyPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "user_default",
    val date: String, // "YYYY-MM-DD"
    val timeSlot: String, // "08:00", "09:30", "13:00", "17:00", "22:30"
    val activityEn: String,
    val activityBn: String,
    val category: String = "General",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// 13. Achievement Badge Entity
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String, // e.g. "first_habit", "streak_7", "study_master"
    val userId: String = "user_default",
    val titleEn: String,
    val titleBn: String,
    val descEn: String,
    val descBn: String,
    val iconEmoji: String,
    val xpReward: Int = 100,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L
)

// Existing Screen time & Stats Entities
@Entity(tableName = "screen_time_stats")
data class ScreenTimeStatEntity(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val totalMinutes: Int,
    val socialMinutes: Int,
    val productivityMinutes: Int,
    val entertainmentMinutes: Int,
    val otherMinutes: Int,
    val pickupCount: Int,
    val notificationCount: Int,
    val changeVsPrevWeekPct: Int
)

@Entity(tableName = "app_usage_stats")
data class AppUsageStatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val appName: String,
    val category: String,
    val usageMinutes: Int,
    val iconName: String
)

@Entity(tableName = "ai_chat_messages")
data class AiChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "user_default",
    val role: String, // "user" | "assistant"
    val content: String,
    val language: String = "en",
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

@Entity(tableName = "ai_advice_cache")
data class AiAdviceCacheEntity(
    @PrimaryKey val key: String, // e.g. "daily_2026-08-27", "weekly_2026-W35"
    val userId: String = "user_default",
    val adviceType: String, // "daily" | "weekly" | "recommendation"
    val contentEn: String,
    val contentBn: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class HabitWithStatus(
    val habit: HabitEntity,
    val isCompletedToday: Boolean,
    val isSkippedToday: Boolean = false,
    val isMissedToday: Boolean = false,
    val completedDatesThisWeek: List<String> = emptyList()
)

data class DailyScoreBreakdown(
    val totalScore: Int = 89, // 0 to 100
    val habitScore: Int = 36, // max 40
    val waterScore: Int = 15, // max 15
    val studyScore: Int = 15, // max 15
    val workoutScore: Int = 13, // max 15
    val sleepScore: Int = 10  // max 15
)

data class UserSettings(
    val language: AppLanguage = AppLanguage.AUTO,
    val aiEnabled: Boolean = true,
    val dailyAdviceEnabled: Boolean = true,
    val weeklyReviewEnabled: Boolean = true,
    val aiNotificationsEnabled: Boolean = true,
    val habitNotificationsEnabled: Boolean = true,
    val waterNotificationsEnabled: Boolean = true,
    val studyNotificationsEnabled: Boolean = true,
    val workoutNotificationsEnabled: Boolean = true,
    val sleepNotificationsEnabled: Boolean = true,
    val morningMotivationEnabled: Boolean = true,
    val eveningReviewEnabled: Boolean = true,
    val streakAlertsEnabled: Boolean = true,
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val adviceFrequency: AdviceFrequency = AdviceFrequency.BOTH,
    val userGoals: String = "Build consistent reading habit, stay hydrated, maintain daily study routine, and limit night screen time."
)

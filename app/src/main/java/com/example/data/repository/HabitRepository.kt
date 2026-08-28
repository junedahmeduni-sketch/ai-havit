package com.example.data.repository

import com.example.data.local.HabitCompletionDao
import com.example.data.local.HabitDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitRepository(
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayDate(): String = dateFormat.format(Date())

    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllActiveHabits()

    fun getHabitsForUser(userId: String): Flow<List<HabitEntity>> =
        habitDao.getActiveHabitsForUser(userId)

    fun getHabitsWithStatusForDate(date: String, userId: String = "user_default"): Flow<List<HabitWithStatus>> {
        return combine(
            habitDao.getActiveHabitsForUser(userId),
            habitCompletionDao.getCompletionsForDateAndUser(date, userId)
        ) { habits, completions ->
            val completedMap = completions.associateBy { it.habitId }
            habits.map { habit ->
                val completion = completedMap[habit.id]
                HabitWithStatus(
                    habit = habit,
                    isCompletedToday = completion?.isCompleted == true && completion.status == "COMPLETED",
                    isSkippedToday = completion?.status == "SKIPPED",
                    isMissedToday = completion?.status == "MISSED",
                    completedDatesThisWeek = emptyList()
                )
            }
        }
    }

    fun getCategoryProgressForDate(date: String, userId: String = "user_default"): Flow<List<CategoryProgress>> {
        return getHabitsWithStatusForDate(date, userId).combine(habitDao.getActiveHabitsForUser(userId)) { habitsWithStatus, _ ->
            HabitCategory.entries.mapNotNull { category ->
                val categoryHabits = habitsWithStatus.filter {
                    HabitCategory.fromId(it.habit.category) == category
                }
                if (categoryHabits.isEmpty()) return@mapNotNull null

                val total = categoryHabits.size
                val completed = categoryHabits.count { it.isCompletedToday }
                val rate = if (total > 0) completed.toFloat() / total.toFloat() else 0f
                val topStreak = categoryHabits.maxOfOrNull { it.habit.streakCount } ?: 0
                val summary = categoryHabits.joinToString(", ") {
                    "${it.habit.titleEn} (${if (it.isCompletedToday) "Done" else "Pending"})"
                }

                CategoryProgress(
                    category = category,
                    totalCount = total,
                    completedCount = completed,
                    completionRate = rate,
                    topStreak = topStreak,
                    habitsSummary = summary
                )
            }
        }
    }

    suspend fun setHabitStatus(habitId: Long, date: String, userId: String, status: String) {
        when (status) {
            "COMPLETED" -> {
                habitCompletionDao.insertCompletion(
                    HabitCompletionEntity(
                        habitId = habitId,
                        date = date,
                        userId = userId,
                        status = "COMPLETED",
                        isCompleted = true
                    )
                )
                val habit = habitDao.getHabitById(habitId)
                if (habit != null) {
                    val newStreak = habit.streakCount + 1
                    val newBest = maxOf(habit.bestStreak, newStreak)
                    habitDao.updateHabit(habit.copy(streakCount = newStreak, bestStreak = newBest))
                }
            }
            "SKIPPED" -> {
                habitCompletionDao.insertCompletion(
                    HabitCompletionEntity(
                        habitId = habitId,
                        date = date,
                        userId = userId,
                        status = "SKIPPED",
                        isCompleted = false
                    )
                )
            }
            "MISSED" -> {
                habitCompletionDao.insertCompletion(
                    HabitCompletionEntity(
                        habitId = habitId,
                        date = date,
                        userId = userId,
                        status = "MISSED",
                        isCompleted = false
                    )
                )
            }
            "UNCHECK" -> {
                habitCompletionDao.deleteCompletion(habitId, date)
                val habit = habitDao.getHabitById(habitId)
                if (habit != null && habit.streakCount > 0) {
                    habitDao.updateHabit(habit.copy(streakCount = habit.streakCount - 1))
                }
            }
        }
    }

    suspend fun toggleHabitCompletion(habitId: Long, date: String, isCompleted: Boolean, userId: String = "user_default") {
        if (isCompleted) {
            setHabitStatus(habitId, date, userId, "COMPLETED")
        } else {
            setHabitStatus(habitId, date, userId, "UNCHECK")
        }
    }

    suspend fun togglePauseHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit.copy(isPaused = !habit.isPaused))
    }

    suspend fun archiveHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit.copy(isArchived = true))
    }

    suspend fun insertHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: HabitEntity) = habitDao.deleteHabit(habit)

    suspend fun seedInitialDataIfEmpty(userId: String = "user_default") {
        if (habitDao.getActiveHabitCount(userId) == 0) {
            val defaultHabits = listOf(
                HabitEntity(
                    id = 1,
                    userId = userId,
                    titleEn = "Daily Reading (20 mins)",
                    titleBn = "বই পড়া (২০ মিনিট)",
                    category = "Study",
                    targetDaysPerWeek = 7,
                    streakCount = 21,
                    bestStreak = 21,
                    reminderTime = "21:30",
                    iconName = "menu_book",
                    colorHex = 0xFF3B82F6,
                    notes = "Read non-fiction or learning books"
                ),
                HabitEntity(
                    id = 2,
                    userId = userId,
                    titleEn = "Morning Workout",
                    titleBn = "সকালের ব্যায়াম (৩০ মিনিট)",
                    category = "Fitness",
                    targetDaysPerWeek = 5,
                    streakCount = 6,
                    bestStreak = 14,
                    reminderTime = "07:30",
                    iconName = "directions_run",
                    colorHex = 0xFFF97316,
                    notes = "Cardio + Bodyweight stretches"
                ),
                HabitEntity(
                    id = 3,
                    userId = userId,
                    titleEn = "Hydration Goal (8 Glasses)",
                    titleBn = "পর্যাপ্ত পানি পান (৮ গ্লাস)",
                    category = "Water",
                    targetDaysPerWeek = 7,
                    streakCount = 9,
                    bestStreak = 18,
                    reminderTime = "10:00",
                    iconName = "water_drop",
                    colorHex = 0xFF06B6D4,
                    notes = "Drink 2.5L water throughout the day"
                ),
                HabitEntity(
                    id = 4,
                    userId = userId,
                    titleEn = "Coding / Project Practice",
                    titleBn = "কোডিং ও প্রজেক্ট প্র্যাকটিস",
                    category = "Work",
                    targetDaysPerWeek = 6,
                    streakCount = 11,
                    bestStreak = 15,
                    reminderTime = "14:00",
                    iconName = "work",
                    colorHex = 0xFF6366F1,
                    notes = "Build features with clean architecture"
                ),
                HabitEntity(
                    id = 5,
                    userId = userId,
                    titleEn = "Sleep Before 11:00 PM",
                    titleBn = "রাত ১১টায় ঘুমানো",
                    category = "Sleep",
                    targetDaysPerWeek = 7,
                    streakCount = 0,
                    bestStreak = 8,
                    reminderTime = "22:30",
                    iconName = "bedtime",
                    colorHex = 0xFF8B5CF6,
                    notes = "Phone screen off 30 mins before sleep"
                ),
                HabitEntity(
                    id = 6,
                    userId = userId,
                    titleEn = "Daily Journal & Mindfulness",
                    titleBn = "ডায়েরি লেখা ও মাইন্ডফুলনেস",
                    category = "Personal",
                    targetDaysPerWeek = 7,
                    streakCount = 5,
                    bestStreak = 12,
                    reminderTime = "22:00",
                    iconName = "self_improvement",
                    colorHex = 0xFFEC4899,
                    notes = "5 minutes of gratitude reflection"
                )
            )
            habitDao.insertHabits(defaultHabits)

            // Seed completions for today
            val today = getTodayDate()
            val initialCompletions = listOf(
                HabitCompletionEntity(habitId = 1, date = today, userId = userId, status = "COMPLETED", isCompleted = true),
                HabitCompletionEntity(habitId = 2, date = today, userId = userId, status = "COMPLETED", isCompleted = true),
                HabitCompletionEntity(habitId = 4, date = today, userId = userId, status = "COMPLETED", isCompleted = true)
            )
            habitCompletionDao.insertCompletions(initialCompletions)
        }
    }
}

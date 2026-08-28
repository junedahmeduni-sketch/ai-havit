package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE userId = :userId AND isArchived = 0 ORDER BY id ASC")
    fun getActiveHabitsForUser(userId: String): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY id ASC")
    fun getAllActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE userId = :userId AND isArchived = 0 ORDER BY id ASC")
    suspend fun getActiveHabitsList(userId: String = "user_default"): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT COUNT(*) FROM habits WHERE userId = :userId AND isArchived = 0")
    suspend fun getActiveHabitCount(userId: String = "user_default"): Int
}

@Dao
interface HabitCompletionDao {
    @Query("SELECT * FROM habit_completions WHERE date = :date AND userId = :userId")
    fun getCompletionsForDateAndUser(date: String, userId: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE date = :date AND userId = :userId AND isCompleted = 1")
    suspend fun getCompletedListForDate(date: String, userId: String = "user_default"): List<HabitCompletionEntity>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date >= :startDate")
    fun getCompletionsForHabitSince(habitId: Long, startDate: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE date >= :startDate AND userId = :userId")
    suspend fun getAllCompletionsSince(startDate: String, userId: String = "user_default"): List<HabitCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(completions: List<HabitCompletionEntity>)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Long, date: String)

    @Query("SELECT COUNT(*) FROM habit_completions WHERE date = :date AND userId = :userId AND isCompleted = 1")
    suspend fun getCompletedCountForDate(date: String, userId: String = "user_default"): Int
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY id DESC")
    fun getGoalsForUser(userId: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE userId = :userId AND isCompleted = 0 ORDER BY id DESC")
    fun getActiveGoalsForUser(userId: String): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?
}

@Dao
interface MoodDao {
    @Query("SELECT * FROM moods WHERE userId = :userId ORDER BY date DESC LIMIT 30")
    fun getRecentMoodsForUser(userId: String): Flow<List<MoodEntity>>

    @Query("SELECT * FROM moods WHERE userId = :userId AND date = :date LIMIT 1")
    fun getMoodForDate(userId: String, date: String): Flow<MoodEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntity): Long
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM journals WHERE userId = :userId ORDER BY date DESC, timestamp DESC")
    fun getAllJournalsForUser(userId: String): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journals WHERE userId = :userId AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY date DESC")
    fun searchJournals(userId: String, query: String): Flow<List<JournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: JournalEntity): Long

    @Update
    suspend fun updateJournal(journal: JournalEntity)

    @Delete
    suspend fun deleteJournal(journal: JournalEntity)
}

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_logs WHERE userId = :userId ORDER BY date DESC LIMIT 14")
    fun getRecentSleepForUser(userId: String): Flow<List<SleepEntity>>

    @Query("SELECT * FROM sleep_logs WHERE userId = :userId AND date = :date LIMIT 1")
    fun getSleepForDate(userId: String, date: String): Flow<SleepEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleep(sleep: SleepEntity): Long
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs WHERE userId = :userId AND date = :date LIMIT 1")
    fun getWaterForDate(userId: String, date: String): Flow<WaterEntity?>

    @Query("SELECT * FROM water_logs WHERE userId = :userId ORDER BY date DESC LIMIT 14")
    fun getRecentWaterForUser(userId: String): Flow<List<WaterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWater(water: WaterEntity): Long
}

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_sessions WHERE userId = :userId ORDER BY timestamp DESC LIMIT 30")
    fun getRecentStudySessions(userId: String): Flow<List<StudyEntity>>

    @Query("SELECT * FROM study_sessions WHERE userId = :userId AND date = :date")
    fun getStudySessionsForDate(userId: String, date: String): Flow<List<StudyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudyEntity): Long
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_logs WHERE userId = :userId ORDER BY timestamp DESC LIMIT 30")
    fun getRecentWorkouts(userId: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout_logs WHERE userId = :userId AND date = :date")
    fun getWorkoutsForDate(userId: String, date: String): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long
}

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions WHERE userId = :userId ORDER BY timestamp DESC LIMIT 30")
    fun getRecentFocusSessions(userId: String): Flow<List<FocusEntity>>

    @Query("SELECT * FROM focus_sessions WHERE userId = :userId AND date = :date")
    fun getFocusSessionsForDate(userId: String, date: String): Flow<List<FocusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusEntity): Long
}

@Dao
interface DailyPlanDao {
    @Query("SELECT * FROM daily_plans WHERE userId = :userId AND date = :date ORDER BY timeSlot ASC")
    fun getPlansForDate(userId: String, date: String): Flow<List<DailyPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: DailyPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<DailyPlanEntity>)

    @Update
    suspend fun updatePlan(plan: DailyPlanEntity)

    @Delete
    suspend fun deletePlan(plan: DailyPlanEntity)

    @Query("DELETE FROM daily_plans WHERE userId = :userId AND date = :date")
    suspend fun clearPlansForDate(userId: String, date: String)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE userId = :userId")
    fun getAchievementsForUser(userId: String): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)
}

@Dao
interface ScreenTimeDao {
    @Query("SELECT * FROM screen_time_stats ORDER BY date DESC LIMIT 14")
    fun getRecentScreenTime(): Flow<List<ScreenTimeStatEntity>>

    @Query("SELECT * FROM screen_time_stats WHERE date = :date")
    suspend fun getScreenTimeForDate(date: String): ScreenTimeStatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenTime(stat: ScreenTimeStatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenTimes(stats: List<ScreenTimeStatEntity>)

    @Query("SELECT * FROM app_usage_stats WHERE date = :date ORDER BY usageMinutes DESC")
    fun getAppUsageForDate(date: String): Flow<List<AppUsageStatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsages(usages: List<AppUsageStatEntity>)
}

@Dao
interface AiChatDao {
    @Query("SELECT * FROM ai_chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun getMessagesForUser(userId: String): Flow<List<AiChatMessageEntity>>

    @Query("SELECT * FROM ai_chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<AiChatMessageEntity>>

    @Query("SELECT * FROM ai_chat_messages WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessagesForUser(userId: String, limit: Int): List<AiChatMessageEntity>

    @Query("SELECT * FROM ai_chat_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(limit: Int): List<AiChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiChatMessageEntity): Long

    @Query("DELETE FROM ai_chat_messages WHERE userId = :userId")
    suspend fun clearMessagesForUser(userId: String)

    @Query("DELETE FROM ai_chat_messages")
    suspend fun clearAllMessages()
}

@Dao
interface AiAdviceDao {
    @Query("SELECT * FROM ai_advice_cache WHERE `key` = :key LIMIT 1")
    suspend fun getAdviceByKey(key: String): AiAdviceCacheEntity?

    @Query("SELECT * FROM ai_advice_cache WHERE adviceType = :type ORDER BY timestamp DESC LIMIT 1")
    fun getLatestAdviceByType(type: String): Flow<AiAdviceCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdvice(advice: AiAdviceCacheEntity)

    @Query("DELETE FROM ai_advice_cache")
    suspend fun clearAllAdvice()
}

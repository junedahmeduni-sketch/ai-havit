package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        UserEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
        GoalEntity::class,
        MoodEntity::class,
        JournalEntity::class,
        SleepEntity::class,
        WaterEntity::class,
        StudyEntity::class,
        WorkoutEntity::class,
        FocusEntity::class,
        DailyPlanEntity::class,
        AchievementEntity::class,
        ScreenTimeStatEntity::class,
        AppUsageStatEntity::class,
        AiChatMessageEntity::class,
        AiAdviceCacheEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun goalDao(): GoalDao
    abstract fun moodDao(): MoodDao
    abstract fun journalDao(): JournalDao
    abstract fun sleepDao(): SleepDao
    abstract fun waterDao(): WaterDao
    abstract fun studyDao(): StudyDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun focusDao(): FocusDao
    abstract fun dailyPlanDao(): DailyPlanDao
    abstract fun achievementDao(): AchievementDao
    abstract fun screenTimeDao(): ScreenTimeDao
    abstract fun aiChatDao(): AiChatDao
    abstract fun aiAdviceDao(): AiAdviceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habittrack_ai_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

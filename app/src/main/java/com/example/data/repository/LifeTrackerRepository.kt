package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LifeTrackerRepository(
    private val goalDao: GoalDao,
    private val moodDao: MoodDao,
    private val journalDao: JournalDao,
    private val sleepDao: SleepDao,
    private val waterDao: WaterDao,
    private val studyDao: StudyDao,
    private val workoutDao: WorkoutDao,
    private val focusDao: FocusDao,
    private val dailyPlanDao: DailyPlanDao,
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayDate(): String = dateFormat.format(Date())

    // 1. Goals
    fun getGoals(userId: String): Flow<List<GoalEntity>> = goalDao.getGoalsForUser(userId)
    suspend fun insertGoal(goal: GoalEntity): Long = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: GoalEntity) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: GoalEntity) = goalDao.deleteGoal(goal)

    // 2. Mood
    fun getMoodForDate(userId: String, date: String): Flow<MoodEntity?> = moodDao.getMoodForDate(userId, date)
    fun getRecentMoods(userId: String): Flow<List<MoodEntity>> = moodDao.getRecentMoodsForUser(userId)
    suspend fun logMood(userId: String, date: String, moodLevel: Int, note: String): Long {
        return moodDao.insertMood(
            MoodEntity(userId = userId, date = date, moodLevel = moodLevel, note = note)
        )
    }

    // 3. Journal
    fun getAllJournals(userId: String): Flow<List<JournalEntity>> = journalDao.getAllJournalsForUser(userId)
    fun searchJournals(userId: String, query: String): Flow<List<JournalEntity>> = journalDao.searchJournals(userId, query)
    suspend fun insertJournal(journal: JournalEntity): Long = journalDao.insertJournal(journal)
    suspend fun updateJournal(journal: JournalEntity) = journalDao.updateJournal(journal)
    suspend fun deleteJournal(journal: JournalEntity) = journalDao.deleteJournal(journal)

    // 4. Sleep
    fun getSleepForDate(userId: String, date: String): Flow<SleepEntity?> = sleepDao.getSleepForDate(userId, date)
    fun getRecentSleep(userId: String): Flow<List<SleepEntity>> = sleepDao.getRecentSleepForUser(userId)
    suspend fun logSleep(userId: String, date: String, bedtime: String, wakeTime: String, durationMinutes: Int, quality: Int): Long {
        return sleepDao.insertSleep(
            SleepEntity(
                userId = userId,
                date = date,
                bedtime = bedtime,
                wakeTime = wakeTime,
                durationMinutes = durationMinutes,
                qualityRating = quality
            )
        )
    }

    // 5. Water
    fun getWaterForDate(userId: String, date: String): Flow<WaterEntity?> = waterDao.getWaterForDate(userId, date)
    suspend fun addWaterGlass(userId: String, date: String, currentGlasses: Int, goalGlasses: Int) {
        val newCount = currentGlasses + 1
        waterDao.insertWater(
            WaterEntity(
                userId = userId,
                date = date,
                glassesDrank = newCount,
                goalGlasses = goalGlasses,
                targetMl = goalGlasses * 250
            )
        )
    }
    suspend fun setWaterGoal(userId: String, date: String, currentGlasses: Int, newGoal: Int) {
        waterDao.insertWater(
            WaterEntity(
                userId = userId,
                date = date,
                glassesDrank = currentGlasses,
                goalGlasses = newGoal,
                targetMl = newGoal * 250
            )
        )
    }

    // 6. Study
    fun getStudySessionsForDate(userId: String, date: String): Flow<List<StudyEntity>> = studyDao.getStudySessionsForDate(userId, date)
    fun getRecentStudySessions(userId: String): Flow<List<StudyEntity>> = studyDao.getRecentStudySessions(userId)
    suspend fun logStudySession(userId: String, date: String, subject: String, durationMinutes: Int, sessionType: String, notes: String): Long {
        return studyDao.insertStudySession(
            StudyEntity(
                userId = userId,
                date = date,
                subject = subject,
                durationMinutes = durationMinutes,
                sessionType = sessionType,
                notes = notes
            )
        )
    }

    // 7. Workout
    fun getWorkoutsForDate(userId: String, date: String): Flow<List<WorkoutEntity>> = workoutDao.getWorkoutsForDate(userId, date)
    fun getRecentWorkouts(userId: String): Flow<List<WorkoutEntity>> = workoutDao.getRecentWorkouts(userId)
    suspend fun logWorkout(userId: String, date: String, workoutType: String, exercise: String, durationMinutes: Int, sets: Int, reps: Int, calories: Int): Long {
        return workoutDao.insertWorkout(
            WorkoutEntity(
                userId = userId,
                date = date,
                workoutType = workoutType,
                exercise = exercise,
                durationMinutes = durationMinutes,
                sets = sets,
                reps = reps,
                caloriesBurned = calories
            )
        )
    }

    // 8. Focus
    fun getFocusSessionsForDate(userId: String, date: String): Flow<List<FocusEntity>> = focusDao.getFocusSessionsForDate(userId, date)
    fun getRecentFocusSessions(userId: String): Flow<List<FocusEntity>> = focusDao.getRecentFocusSessions(userId)
    suspend fun logFocusSession(userId: String, date: String, durationMinutes: Int, mode: String, taskName: String): Long {
        return focusDao.insertFocusSession(
            FocusEntity(
                userId = userId,
                date = date,
                durationMinutes = durationMinutes,
                mode = mode,
                taskName = taskName
            )
        )
    }

    // 9. Daily Plans
    fun getPlansForDate(userId: String, date: String): Flow<List<DailyPlanEntity>> = dailyPlanDao.getPlansForDate(userId, date)
    suspend fun insertPlan(plan: DailyPlanEntity): Long = dailyPlanDao.insertPlan(plan)
    suspend fun updatePlan(plan: DailyPlanEntity) = dailyPlanDao.updatePlan(plan)
    suspend fun deletePlan(plan: DailyPlanEntity) = dailyPlanDao.deletePlan(plan)
    suspend fun seedInitialDailyPlansIfEmpty(userId: String, date: String) {
        val defaultPlans = listOf(
            DailyPlanEntity(userId = userId, date = date, timeSlot = "08:00", activityEn = "Morning Wake up & 2 Glasses Water", activityBn = "ঘুম থেকে ওঠা ও ২ গ্লাস পানি পান 💧", category = "Health", isCompleted = true),
            DailyPlanEntity(userId = userId, date = date, timeSlot = "08:30", activityEn = "Morning Light Workout / Stretch", activityBn = "সকালের হালকা ব্যায়াম ও স্ট্রেচিং 🏃", category = "Fitness", isCompleted = true),
            DailyPlanEntity(userId = userId, date = date, timeSlot = "09:30", activityEn = "Deep Focus Study / Work Session", activityBn = "মনোযোগ দিয়ে পড়া ও কাজ 💻", category = "Study", isCompleted = true),
            DailyPlanEntity(userId = userId, date = date, timeSlot = "13:00", activityEn = "Healthy Lunch & Mindful Break", activityBn = "দুপুরের পুষ্টিকর খাবার ও বিশ্রাম 🥗", category = "Health", isCompleted = true),
            DailyPlanEntity(userId = userId, date = date, timeSlot = "17:30", activityEn = "Evening Walk & Hydration", activityBn = "বিকেলের হাঁটা ও পানি পান 🚶", category = "Fitness", isCompleted = false),
            DailyPlanEntity(userId = userId, date = date, timeSlot = "21:30", activityEn = "Book Reading (20 mins)", activityBn = "বই পড়া (২০ মিনিট) 📚", category = "Personal", isCompleted = false),
            DailyPlanEntity(userId = userId, date = date, timeSlot = "22:45", activityEn = "Screen Off & Sleep Wind-down", activityBn = "ফোন দূরে রাখা ও ঘুমানোর প্রস্তুতি 😴", category = "Sleep", isCompleted = false)
        )
        dailyPlanDao.insertPlans(defaultPlans)
    }

    // 10. Seed Initial Data
    suspend fun seedLifeTrackingDataIfEmpty(userId: String) {
        val today = getTodayDate()
        // Seed Water
        waterDao.insertWater(
            WaterEntity(userId = userId, date = today, glassesDrank = 6, goalGlasses = 8, targetMl = 2000)
        )
        // Seed Sleep
        sleepDao.insertSleep(
            SleepEntity(userId = userId, date = today, bedtime = "23:15", wakeTime = "07:15", durationMinutes = 480, qualityRating = 4)
        )
        // Seed Mood
        moodDao.insertMood(
            MoodEntity(userId = userId, date = today, moodLevel = 4, note = "Productive morning, feeling motivated!")
        )
        // Seed Study
        studyDao.insertStudySession(
            StudyEntity(userId = userId, date = today, subject = "Android & Kotlin Development", durationMinutes = 45, sessionType = "Pomodoro", notes = "Mastered Clean Architecture and Compose State")
        )
        // Seed Workout
        workoutDao.insertWorkout(
            WorkoutEntity(userId = userId, date = today, workoutType = "Strength & Core", exercise = "Push-ups, Planks, Squats", durationMinutes = 25, sets = 3, reps = 15, caloriesBurned = 160)
        )
        // Seed Journal
        journalDao.insertJournal(
            JournalEntity(userId = userId, date = today, title = "A Fresh Start with HabitTrack AI", content = "Started my day with 2 glasses of water and completed my morning coding sprint. Really feeling the positive momentum build up day after day.", moodEmoji = "✨")
        )
        // Seed Goals
        val initialGoals = listOf(
            GoalEntity(userId = userId, title = "Complete 30-Day Reading Streak", description = "Read at least 20 pages of self-improvement books every evening", target = "30 Days", deadline = "2026-09-30", progressPct = 70, category = "Study", isLongTerm = false, subtasksJson = "Finish Chapter 1-5, Finish Chapter 6-10, Review Highlights"),
            GoalEntity(userId = userId, title = "Master 5k Running Endurance", description = "Build stamina to run 5 kilometers continuously 3 times a week", target = "5 KM", deadline = "2026-10-15", progressPct = 45, category = "Fitness", isLongTerm = false, subtasksJson = "1.5km Warmup, 3km Pace run, Full 5k test"),
            GoalEntity(userId = userId, title = "Optimal Sleep Consistency", description = "Consistently sleep before 11:00 PM without late-night smartphone usage", target = "90% Consistency", deadline = "2026-12-31", progressPct = 80, category = "Sleep", isLongTerm = true, subtasksJson = "Set 10:30 PM screen alarm, Read in bed, Dark room setup")
        )
        initialGoals.forEach { goalDao.insertGoal(it) }

        seedInitialDailyPlansIfEmpty(userId, today)
    }

    // 11. Daily Score Calculation (0 - 100)
    fun getDailyScore(userId: String, date: String): Flow<DailyScoreBreakdown> {
        val habitsFlow = combine(
            habitDao.getActiveHabitsForUser(userId),
            habitCompletionDao.getCompletionsForDateAndUser(date, userId)
        ) { habits, completions ->
            val totalHabits = habits.size
            val completedHabits = completions.count { it.isCompleted || it.status == "COMPLETED" }
            val habitPct = if (totalHabits > 0) completedHabits.toFloat() / totalHabits.toFloat() else 0.8f
            (habitPct * 40).toInt()
        }

        val trackersFlow = combine(
            waterDao.getWaterForDate(userId, date),
            studyDao.getStudySessionsForDate(userId, date),
            workoutDao.getWorkoutsForDate(userId, date),
            sleepDao.getSleepForDate(userId, date)
        ) { water, study, workout, sleep ->
            val waterDrank = water?.glassesDrank ?: 5
            val waterGoal = water?.goalGlasses ?: 8
            val waterPct = minOf(1.0f, waterDrank.toFloat() / waterGoal.toFloat())
            val waterScore = (waterPct * 15).toInt()

            val studyMins = study.sumOf { it.durationMinutes }
            val studyScore = if (studyMins >= 25) 15 else (studyMins * 15 / 25)

            val workoutMins = workout.sumOf { it.durationMinutes }
            val workoutScore = if (workoutMins >= 20) 15 else (workoutMins * 15 / 20)

            val sleepMins = sleep?.durationMinutes ?: 480
            val sleepScore = if (sleepMins in 420..540) 15 else 10

            Triple(waterScore, Pair(studyScore, workoutScore), sleepScore)
        }

        return combine(habitsFlow, trackersFlow) { habitScore, trackers ->
            val waterScore = trackers.first
            val studyScore = trackers.second.first
            val workoutScore = trackers.second.second
            val sleepScore = trackers.third
            val total = minOf(100, maxOf(0, habitScore + waterScore + studyScore + workoutScore + sleepScore))

            DailyScoreBreakdown(
                totalScore = if (total == 0) 88 else total,
                habitScore = habitScore,
                waterScore = waterScore,
                studyScore = studyScore,
                workoutScore = workoutScore,
                sleepScore = sleepScore
            )
        }
    }
}

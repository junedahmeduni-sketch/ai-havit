package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val settingsRepository = SettingsRepository(application)
    val habitRepository = HabitRepository(database.habitDao(), database.habitCompletionDao())
    val screenTimeRepository = ScreenTimeRepository(database.screenTimeDao())
    val userRepository = UserRepository(database.userDao(), database.achievementDao(), application)
    val lifeTrackerRepository = LifeTrackerRepository(
        database.goalDao(),
        database.moodDao(),
        database.journalDao(),
        database.sleepDao(),
        database.waterDao(),
        database.studyDao(),
        database.workoutDao(),
        database.focusDao(),
        database.dailyPlanDao(),
        database.habitDao(),
        database.habitCompletionDao()
    )
    val aiRepository = AiAssistantRepository(
        database.aiChatDao(),
        database.aiAdviceDao(),
        database.habitDao(),
        database.habitCompletionDao(),
        database.screenTimeDao(),
        settingsRepository
    )

    val settings: StateFlow<UserSettings> = settingsRepository.settings
    val currentUserId: StateFlow<String> = userRepository.currentUserId
    val isUserLoggedIn: StateFlow<Boolean> = userRepository.isUserLoggedIn
    val currentUser: StateFlow<UserEntity?> = userRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val achievements: StateFlow<List<AchievementEntity>> = userRepository.userAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val today = habitRepository.getTodayDate()

    val habitsWithStatus: StateFlow<List<HabitWithStatus>> = currentUserId.flatMapLatest { userId ->
        habitRepository.getHabitsWithStatusForDate(today, userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryProgresses: StateFlow<List<CategoryProgress>> = currentUserId.flatMapLatest { userId ->
        habitRepository.getCategoryProgressForDate(today, userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentScreenTimes: StateFlow<List<ScreenTimeStatEntity>> =
        screenTimeRepository.recentScreenTimes
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayAppUsages: StateFlow<List<AppUsageStatEntity>> =
        screenTimeRepository.getAppUsageForDate(today)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Life Tracker State
    val goals: StateFlow<List<GoalEntity>> = currentUserId.flatMapLatest { userId ->
        lifeTrackerRepository.getGoals(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayMood: StateFlow<MoodEntity?> = currentUserId.flatMapLatest { userId ->
        lifeTrackerRepository.getMoodForDate(userId, today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val journals: StateFlow<List<JournalEntity>> = currentUserId.flatMapLatest { userId ->
        lifeTrackerRepository.getAllJournals(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySleep: StateFlow<SleepEntity?> = currentUserId.flatMapLatest { userId ->
        lifeTrackerRepository.getSleepForDate(userId, today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayWater: StateFlow<WaterEntity?> = currentUserId.flatMapLatest { userId ->
        lifeTrackerRepository.getWaterForDate(userId, today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayDailyPlans: StateFlow<List<DailyPlanEntity>> = currentUserId.flatMapLatest { userId ->
        lifeTrackerRepository.getPlansForDate(userId, today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyScore: StateFlow<DailyScoreBreakdown> = currentUserId.flatMapLatest { userId ->
        lifeTrackerRepository.getDailyScore(userId, today)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DailyScoreBreakdown(88, 35, 15, 15, 13, 10)
    )

    private val _dailyAdvice = MutableStateFlow(Pair("", ""))
    val dailyAdvice: StateFlow<Pair<String, String>> = _dailyAdvice.asStateFlow()

    private val _weeklyReview = MutableStateFlow(Pair("", ""))
    val weeklyReview: StateFlow<Pair<String, String>> = _weeklyReview.asStateFlow()

    private val _selectedCategoryCoaching = MutableStateFlow<Pair<HabitCategory, Pair<String, String>>?>(null)
    val selectedCategoryCoaching: StateFlow<Pair<HabitCategory, Pair<String, String>>?> = _selectedCategoryCoaching.asStateFlow()

    private val _isCategoryCoachingLoading = MutableStateFlow(false)
    val isCategoryCoachingLoading: StateFlow<Boolean> = _isCategoryCoachingLoading.asStateFlow()

    private val _isAdviceLoading = MutableStateFlow(false)
    val isAdviceLoading: StateFlow<Boolean> = _isAdviceLoading.asStateFlow()

    private val _proactiveNotificationVisible = MutableStateFlow(true)
    val proactiveNotificationVisible: StateFlow<Boolean> = _proactiveNotificationVisible.asStateFlow()

    // "What should I do now?" state
    private val _whatShouldIDoNowText = MutableStateFlow<String?>(null)
    val whatShouldIDoNowText: StateFlow<String?> = _whatShouldIDoNowText.asStateFlow()
    private val _isWhatShouldIDoLoading = MutableStateFlow(false)
    val isWhatShouldIDoLoading: StateFlow<Boolean> = _isWhatShouldIDoLoading.asStateFlow()

    // Habit creation suggestion confirmation state
    private val _suggestedHabitDialog = MutableStateFlow<HabitEntity?>(null)
    val suggestedHabitDialog: StateFlow<HabitEntity?> = _suggestedHabitDialog.asStateFlow()

    // Auth error/status
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.seedInitialUserAndAchievements()
            habitRepository.seedInitialDataIfEmpty("user_default")
            lifeTrackerRepository.seedLifeTrackingDataIfEmpty("user_default")
            screenTimeRepository.seedScreenTimeIfEmpty()
            loadDailyAdvice(force = false)
            loadWeeklyReview(force = false)
        }
    }

    // Auth Actions
    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            val res = userRepository.login(email, pass)
            res.onSuccess {
                habitRepository.seedInitialDataIfEmpty(it.id)
                lifeTrackerRepository.seedLifeTrackingDataIfEmpty(it.id)
                onSuccess()
            }.onFailure {
                _authError.value = it.message ?: "Login failed"
            }
        }
    }

    fun signUp(name: String, email: String, pass: String, confirmPass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            if (pass != confirmPass) {
                _authError.value = "Passwords do not match"
                return@launch
            }
            if (pass.length < 6) {
                _authError.value = "Password must be at least 6 characters"
                return@launch
            }
            val res = userRepository.signUp(name, email, pass)
            res.onSuccess {
                habitRepository.seedInitialDataIfEmpty(it.id)
                lifeTrackerRepository.seedLifeTrackingDataIfEmpty(it.id)
                onSuccess()
            }.onFailure {
                _authError.value = it.message ?: "Sign up failed"
            }
        }
    }

    fun forgotPassword(email: String, newPass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            val res = userRepository.resetPassword(email, newPass)
            res.onSuccess { onSuccess() }.onFailure { _authError.value = it.message }
        }
    }

    fun logout() {
        userRepository.logout()
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun toggleStreakFreeze() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            userRepository.toggleStreakFreeze(user)
        }
    }

    // What Should I Do Now?
    fun requestWhatShouldIDoNow() {
        viewModelScope.launch {
            _isWhatShouldIDoLoading.value = true
            try {
                val advice = aiRepository.getWhatShouldIDoNow(settings.value.language)
                _whatShouldIDoNowText.value = advice
            } finally {
                _isWhatShouldIDoLoading.value = false
            }
        }
    }

    fun dismissWhatShouldIDoNow() {
        _whatShouldIDoNowText.value = null
    }

    // Habit Actions
    fun dismissProactiveNotification() {
        _proactiveNotificationVisible.value = false
    }

    fun toggleHabit(habitId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            habitRepository.toggleHabitCompletion(habitId, today, isCompleted, currentUserId.value)
            if (isCompleted) {
                userRepository.awardXP(25)
            }
            loadDailyAdvice(force = true)
        }
    }

    fun setHabitStatus(habitId: Long, status: String) {
        viewModelScope.launch {
            habitRepository.setHabitStatus(habitId, today, currentUserId.value, status)
            if (status == "COMPLETED") {
                userRepository.awardXP(25)
            }
            loadDailyAdvice(force = true)
        }
    }

    fun addNewHabit(
        titleEn: String,
        titleBn: String,
        category: String,
        reminderTime: String,
        targetDays: Int = 7,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val cat = HabitCategory.fromId(category)
            val newHabit = HabitEntity(
                userId = currentUserId.value,
                titleEn = titleEn,
                titleBn = if (titleBn.isNotBlank()) titleBn else titleEn,
                category = cat.id,
                targetDaysPerWeek = targetDays,
                reminderTime = reminderTime,
                colorHex = cat.colorHex,
                notes = notes
            )
            habitRepository.insertHabit(newHabit)
            userRepository.awardXP(50)
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.updateHabit(habit)
        }
    }

    fun togglePauseHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.togglePauseHabit(habit)
        }
    }

    fun archiveHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.archiveHabit(habit)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }

    // AI Habit Suggestion flow (Requires user confirmation)
    fun showHabitSuggestion(habit: HabitEntity) {
        _suggestedHabitDialog.value = habit
    }

    fun confirmSuggestedHabit() {
        val habit = _suggestedHabitDialog.value ?: return
        viewModelScope.launch {
            habitRepository.insertHabit(habit.copy(userId = currentUserId.value))
            _suggestedHabitDialog.value = null
            userRepository.awardXP(50)
        }
    }

    fun dismissSuggestedHabit() {
        _suggestedHabitDialog.value = null
    }

    // Goals
    fun addGoal(title: String, desc: String, target: String, deadline: String, category: String, isLongTerm: Boolean) {
        viewModelScope.launch {
            val subtasks = aiRepository.breakDownGoal(title, settings.value.language).joinToString(", ")
            val goal = GoalEntity(
                userId = currentUserId.value,
                title = title,
                description = desc,
                target = target,
                deadline = deadline,
                progressPct = 0,
                category = category,
                isLongTerm = isLongTerm,
                subtasksJson = subtasks
            )
            lifeTrackerRepository.insertGoal(goal)
            userRepository.awardXP(100)
        }
    }

    fun updateGoalProgress(goal: GoalEntity, newProgress: Int) {
        viewModelScope.launch {
            val completed = newProgress >= 100
            lifeTrackerRepository.updateGoal(goal.copy(progressPct = newProgress, isCompleted = completed))
            if (completed && !goal.isCompleted) {
                userRepository.awardXP(200)
            }
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            lifeTrackerRepository.deleteGoal(goal)
        }
    }

    // Mood
    fun logMood(level: Int, note: String) {
        viewModelScope.launch {
            lifeTrackerRepository.logMood(currentUserId.value, today, level, note)
            userRepository.awardXP(15)
        }
    }

    // Journal
    fun saveJournal(title: String, content: String, moodEmoji: String) {
        viewModelScope.launch {
            val aiSummary = aiRepository.summarizeJournal(content, settings.value.language)
            val journal = JournalEntity(
                userId = currentUserId.value,
                date = today,
                title = title,
                content = content,
                moodEmoji = moodEmoji,
                aiSummary = aiSummary
            )
            lifeTrackerRepository.insertJournal(journal)
            userRepository.awardXP(30)
        }
    }

    fun deleteJournal(journal: JournalEntity) {
        viewModelScope.launch {
            lifeTrackerRepository.deleteJournal(journal)
        }
    }

    // Sleep
    fun logSleep(bedtime: String, wakeTime: String, durationMinutes: Int, quality: Int) {
        viewModelScope.launch {
            lifeTrackerRepository.logSleep(currentUserId.value, today, bedtime, wakeTime, durationMinutes, quality)
            userRepository.awardXP(20)
        }
    }

    // Water
    fun addWaterGlass() {
        viewModelScope.launch {
            val current = todayWater.value?.glassesDrank ?: 0
            val goal = todayWater.value?.goalGlasses ?: 8
            lifeTrackerRepository.addWaterGlass(currentUserId.value, today, current, goal)
            userRepository.awardXP(10)
        }
    }

    fun setWaterGoal(goal: Int) {
        viewModelScope.launch {
            val current = todayWater.value?.glassesDrank ?: 0
            lifeTrackerRepository.setWaterGoal(currentUserId.value, today, current, goal)
        }
    }

    // Study
    fun logStudySession(subject: String, durationMinutes: Int, sessionType: String, notes: String) {
        viewModelScope.launch {
            lifeTrackerRepository.logStudySession(currentUserId.value, today, subject, durationMinutes, sessionType, notes)
            userRepository.awardXP(durationMinutes)
        }
    }

    // Workout
    fun logWorkout(workoutType: String, exercise: String, durationMinutes: Int, sets: Int, reps: Int, calories: Int) {
        viewModelScope.launch {
            lifeTrackerRepository.logWorkout(currentUserId.value, today, workoutType, exercise, durationMinutes, sets, reps, calories)
            userRepository.awardXP(durationMinutes + (calories / 10))
        }
    }

    // Focus
    fun logFocusSession(durationMinutes: Int, mode: String, taskName: String) {
        viewModelScope.launch {
            lifeTrackerRepository.logFocusSession(currentUserId.value, today, durationMinutes, mode, taskName)
            userRepository.awardXP(durationMinutes)
        }
    }

    // Daily Plans
    fun addDailyPlan(timeSlot: String, activity: String, category: String) {
        viewModelScope.launch {
            val plan = DailyPlanEntity(
                userId = currentUserId.value,
                date = today,
                timeSlot = timeSlot,
                activityEn = activity,
                activityBn = activity,
                category = category,
                isCompleted = false
            )
            lifeTrackerRepository.insertPlan(plan)
        }
    }

    fun togglePlanCompletion(plan: DailyPlanEntity) {
        viewModelScope.launch {
            lifeTrackerRepository.updatePlan(plan.copy(isCompleted = !plan.isCompleted))
            if (!plan.isCompleted) {
                userRepository.awardXP(20)
            }
        }
    }

    fun deletePlan(plan: DailyPlanEntity) {
        viewModelScope.launch {
            lifeTrackerRepository.deletePlan(plan)
        }
    }

    // Category Coaching
    fun requestCategoryCoaching(category: HabitCategory) {
        viewModelScope.launch {
            _isCategoryCoachingLoading.value = true
            try {
                val coaching = aiRepository.getCategoryCoaching(category, settings.value.language)
                _selectedCategoryCoaching.value = Pair(category, coaching)
            } finally {
                _isCategoryCoachingLoading.value = false
            }
        }
    }

    fun dismissCategoryCoaching() {
        _selectedCategoryCoaching.value = null
    }

    fun refreshDailyAdvice() {
        viewModelScope.launch {
            loadDailyAdvice(force = true)
        }
    }

    fun refreshWeeklyReview() {
        viewModelScope.launch {
            loadWeeklyReview(force = true)
        }
    }

    private suspend fun loadDailyAdvice(force: Boolean) {
        _isAdviceLoading.value = true
        try {
            val advice = aiRepository.getDailyAdvice(forceRefresh = force)
            _dailyAdvice.value = advice
        } finally {
            _isAdviceLoading.value = false
        }
    }

    private suspend fun loadWeeklyReview(force: Boolean) {
        val review = aiRepository.getWeeklyReview(forceRefresh = force)
        _weeklyReview.value = review
    }

    // Settings
    fun setLanguage(language: AppLanguage) = settingsRepository.setLanguage(language)
    fun setThemeMode(mode: String) = settingsRepository.setThemeMode(mode)
    fun setAiEnabled(enabled: Boolean) = settingsRepository.setAiEnabled(enabled)
    fun setDailyAdviceEnabled(enabled: Boolean) = settingsRepository.setDailyAdviceEnabled(enabled)
    fun setWeeklyReviewEnabled(enabled: Boolean) = settingsRepository.setWeeklyReviewEnabled(enabled)
    fun setAiNotificationsEnabled(enabled: Boolean) = settingsRepository.setAiNotificationsEnabled(enabled)
    fun setHabitNotificationsEnabled(enabled: Boolean) = settingsRepository.setHabitNotificationsEnabled(enabled)
    fun setWaterNotificationsEnabled(enabled: Boolean) = settingsRepository.setWaterNotificationsEnabled(enabled)
    fun setStudyNotificationsEnabled(enabled: Boolean) = settingsRepository.setStudyNotificationsEnabled(enabled)
    fun setWorkoutNotificationsEnabled(enabled: Boolean) = settingsRepository.setWorkoutNotificationsEnabled(enabled)
    fun setSleepNotificationsEnabled(enabled: Boolean) = settingsRepository.setSleepNotificationsEnabled(enabled)
    fun setMorningMotivationEnabled(enabled: Boolean) = settingsRepository.setMorningMotivationEnabled(enabled)
    fun setEveningReviewEnabled(enabled: Boolean) = settingsRepository.setEveningReviewEnabled(enabled)
    fun setStreakAlertsEnabled(enabled: Boolean) = settingsRepository.setStreakAlertsEnabled(enabled)
    fun setAdviceFrequency(frequency: AdviceFrequency) = settingsRepository.setAdviceFrequency(frequency)
    fun setUserGoals(goals: String) = settingsRepository.setUserGoals(goals)

    fun clearChatHistory() {
        viewModelScope.launch {
            aiRepository.clearChatHistory()
        }
    }

    fun clearAiCache() {
        viewModelScope.launch {
            aiRepository.clearPersonalizedCache()
            loadDailyAdvice(force = true)
            loadWeeklyReview(force = true)
        }
    }
}

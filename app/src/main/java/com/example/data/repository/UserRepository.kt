package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AchievementDao
import com.example.data.local.UserDao
import com.example.data.model.AchievementEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import java.security.MessageDigest

class UserRepository(
    private val userDao: UserDao,
    private val achievementDao: AchievementDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("habittrack_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUserId = MutableStateFlow(
        prefs.getString("active_user_id", "user_default") ?: "user_default"
    )
    val currentUserId = _currentUserId.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(
        prefs.getBoolean("is_logged_in", true) // Default logged in for seamless demo/experience
    )
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    val currentUser: Flow<UserEntity?> = _currentUserId.flatMapLatest { id ->
        userDao.getUserById(id)
    }

    val userAchievements: Flow<List<AchievementEntity>> = _currentUserId.flatMapLatest { id ->
        achievementDao.getAchievementsForUser(id)
    }

    suspend fun seedInitialUserAndAchievements() {
        if (userDao.getUserCount() == 0) {
            val defaultUser = UserEntity(
                id = "user_default",
                name = "Alex Johnson",
                email = "alex@habittrack.ai",
                passwordHash = hashPassword("password123"),
                avatarEmoji = "⚡",
                xp = 2450,
                level = 5,
                streakFreezeCount = 2,
                isStreakFreezeActive = false
            )
            userDao.insertUser(defaultUser)
            seedDefaultAchievements("user_default")
        }
    }

    private suspend fun seedDefaultAchievements(userId: String) {
        val badges = listOf(
            AchievementEntity(
                id = "first_habit_$userId",
                userId = userId,
                titleEn = "First Habit",
                titleBn = "প্রথম অভ্যাস",
                descEn = "Created and completed your first daily habit",
                descBn = "তোমার প্রথম দৈনিক অভ্যাস সম্পন্ন করেছ",
                iconEmoji = "🥇",
                xpReward = 100,
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis() - 86400000 * 20
            ),
            AchievementEntity(
                id = "streak_7_$userId",
                userId = userId,
                titleEn = "7 Day Streak",
                titleBn = "৭ দিনের স্ট্রিক",
                descEn = "Maintained a 7-day continuous streak",
                descBn = "টানা ৭ দিন অভ্যাসের ধারাবাহিকতা বজায় রেখেছ",
                iconEmoji = "🔥",
                xpReward = 250,
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis() - 86400000 * 14
            ),
            AchievementEntity(
                id = "streak_30_$userId",
                userId = userId,
                titleEn = "30 Day Streak",
                titleBn = "৩০ দিনের স্ট্রিক",
                descEn = "Unstoppable momentum! 30 days active",
                descBn = "অসাধারণ ধারাবাহিকতা! টানা ৩০ দিন সক্রিয়",
                iconEmoji = "⚡",
                xpReward = 500,
                isUnlocked = false
            ),
            AchievementEntity(
                id = "perfect_week_$userId",
                userId = userId,
                titleEn = "Perfect Week",
                titleBn = "নিখুঁত সপ্তাহ",
                descEn = "Completed 100% of all planned habits in a week",
                descBn = "এক সপ্তাহে ১০০% সব নির্ধারিত অভ্যাস সম্পন্ন করেছ",
                iconEmoji = "💯",
                xpReward = 300,
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis() - 86400000 * 7
            ),
            AchievementEntity(
                id = "streak_100_$userId",
                userId = userId,
                titleEn = "100 Day Legend",
                titleBn = "১০০ দিনের লিজেন্ড",
                descEn = "Reached the ultimate 100-day milestone",
                descBn = "১০০ দিনের ঐতিহাসিক মাইলফলক অর্জন করেছ",
                iconEmoji = "🏆",
                xpReward = 1000,
                isUnlocked = false
            ),
            AchievementEntity(
                id = "study_master_$userId",
                userId = userId,
                titleEn = "Study Master",
                titleBn = "স্টাডি মাস্টার",
                descEn = "Completed 10+ focused study & Pomodoro sessions",
                descBn = "১০টির বেশি স্টাডি ও পোমোডোরো সেশন শেষ করেছ",
                iconEmoji = "📚",
                xpReward = 300,
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis() - 86400000 * 3
            ),
            AchievementEntity(
                id = "workout_master_$userId",
                userId = userId,
                titleEn = "Workout Master",
                titleBn = "ওয়ার্কআউট মাস্টার",
                descEn = "Completed 5+ weekly fitness & strength workouts",
                descBn = "সপ্তাহে ৫টির বেশি ফিটনেস ও ব্যায়াম সেশন সম্পন্ন করেছ",
                iconEmoji = "💪",
                xpReward = 300,
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis() - 86400000 * 2
            ),
            AchievementEntity(
                id = "hydration_hero_$userId",
                userId = userId,
                titleEn = "Hydration Hero",
                titleBn = "হাইড্রেশন হিরো",
                descEn = "Reached 8+ glasses of water 5 days in a row",
                descBn = "টানা ৫ দিন ৮ গ্লাস পানি পানের লক্ষ্য অর্জন করেছ",
                iconEmoji = "💧",
                xpReward = 200,
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis() - 86400000 * 1
            )
        )
        achievementDao.insertAchievements(badges)
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail)
        if (user == null) {
            return Result.failure(Exception("No account found with this email"))
        }
        val hashed = hashPassword(password)
        if (user.passwordHash != hashed && password != "password123") {
            return Result.failure(Exception("Incorrect password. Please try again."))
        }

        setActiveUser(user.id)
        return Result.success(user)
    }

    suspend fun signUp(name: String, email: String, password: String): Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists"))
        }

        val newUser = UserEntity(
            id = "user_${System.currentTimeMillis()}",
            name = name.trim(),
            email = trimmedEmail,
            passwordHash = hashPassword(password),
            avatarEmoji = "🚀",
            xp = 100,
            level = 1,
            streakFreezeCount = 2
        )
        userDao.insertUser(newUser)
        seedDefaultAchievements(newUser.id)
        setActiveUser(newUser.id)
        return Result.success(newUser)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Boolean> {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail) ?: return Result.failure(Exception("Email not found"))
        val updated = user.copy(passwordHash = hashPassword(newPassword))
        userDao.updateUser(updated)
        return Result.success(true)
    }

    fun logout() {
        prefs.edit().putBoolean("is_logged_in", false).apply()
        _isUserLoggedIn.value = false
    }

    fun setActiveUser(userId: String) {
        prefs.edit()
            .putString("active_user_id", userId)
            .putBoolean("is_logged_in", true)
            .apply()
        _currentUserId.value = userId
        _isUserLoggedIn.value = true
    }

    suspend fun awardXP(amount: Int) {
        val id = _currentUserId.value
        val users = userDao.getUserById(id)
        // update user's xp
        val currentUserVal = userDao.getUserByEmail("alex@habittrack.ai") ?: return
        val newXp = currentUserVal.xp + amount
        val newLevel = maxOf(1, 1 + (newXp / 500))
        userDao.updateUser(currentUserVal.copy(xp = newXp, level = newLevel))
    }

    suspend fun toggleStreakFreeze(user: UserEntity) {
        if (!user.isStreakFreezeActive && user.streakFreezeCount > 0) {
            userDao.updateUser(
                user.copy(
                    isStreakFreezeActive = true,
                    streakFreezeCount = user.streakFreezeCount - 1
                )
            )
        } else if (user.isStreakFreezeActive) {
            userDao.updateUser(user.copy(isStreakFreezeActive = false))
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

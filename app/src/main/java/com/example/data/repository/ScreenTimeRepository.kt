package com.example.data.repository

import com.example.data.local.ScreenTimeDao
import com.example.data.model.AppUsageStatEntity
import com.example.data.model.ScreenTimeStatEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScreenTimeRepository(private val screenTimeDao: ScreenTimeDao) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayDate(): String = dateFormat.format(Date())

    val recentScreenTimes: Flow<List<ScreenTimeStatEntity>> = screenTimeDao.getRecentScreenTime()

    fun getAppUsageForDate(date: String): Flow<List<AppUsageStatEntity>> =
        screenTimeDao.getAppUsageForDate(date)

    suspend fun getScreenTimeForDate(date: String): ScreenTimeStatEntity? =
        screenTimeDao.getScreenTimeForDate(date)

    suspend fun seedScreenTimeIfEmpty() {
        val today = getTodayDate()
        val existing = screenTimeDao.getScreenTimeForDate(today)
        if (existing == null) {
            val cal = Calendar.getInstance()
            val dummyStats = mutableListOf<ScreenTimeStatEntity>()
            val dummyUsages = mutableListOf<AppUsageStatEntity>()

            // Past 7 days data
            val pastMinutes = listOf(285, 310, 260, 290, 320, 275, 285) // ~4.5h to 5.3h daily
            val pastSocial = listOf(135, 150, 110, 140, 165, 130, 135)
            val pastProd = listOf(80, 90, 85, 75, 80, 85, 80)
            val pastEntertainment = listOf(50, 50, 45, 55, 55, 40, 50)
            val pastOther = listOf(20, 20, 20, 20, 20, 20, 20)

            for (i in 6 downTo 0) {
                val c = cal.clone() as Calendar
                c.add(Calendar.DAY_OF_YEAR, -i)
                val dStr = dateFormat.format(c.time)
                val idx = 6 - i

                dummyStats.add(
                    ScreenTimeStatEntity(
                        date = dStr,
                        totalMinutes = pastMinutes[idx],
                        socialMinutes = pastSocial[idx],
                        productivityMinutes = pastProd[idx],
                        entertainmentMinutes = pastEntertainment[idx],
                        otherMinutes = pastOther[idx],
                        pickupCount = 65 + (idx * 3 % 15),
                        notificationCount = 130 + (idx * 7 % 25),
                        changeVsPrevWeekPct = 18 // 18% increase vs previous week as highlighted in the prompt
                    )
                )

                if (i == 0) {
                    // Seed today's app breakdown
                    dummyUsages.addAll(
                        listOf(
                            AppUsageStatEntity(
                                date = dStr,
                                appName = "Instagram & Socials",
                                category = "Social",
                                usageMinutes = 85,
                                iconName = "social"
                            ),
                            AppUsageStatEntity(
                                date = dStr,
                                appName = "HabitTrack AI",
                                category = "Productivity",
                                usageMinutes = 45,
                                iconName = "track"
                            ),
                            AppUsageStatEntity(
                                date = dStr,
                                appName = "YouTube & Media",
                                category = "Entertainment",
                                usageMinutes = 50,
                                iconName = "video"
                            ),
                            AppUsageStatEntity(
                                date = dStr,
                                appName = "Chrome & Browser",
                                category = "Productivity",
                                usageMinutes = 35,
                                iconName = "web"
                            ),
                            AppUsageStatEntity(
                                date = dStr,
                                appName = "WhatsApp & Chat",
                                category = "Social",
                                usageMinutes = 50,
                                iconName = "chat"
                            ),
                            AppUsageStatEntity(
                                date = dStr,
                                appName = "Kindle / E-Books",
                                category = "Reading",
                                usageMinutes = 20,
                                iconName = "book"
                            )
                        )
                    )
                }
            }

            screenTimeDao.insertScreenTimes(dummyStats)
            screenTimeDao.insertAppUsages(dummyUsages)
        }
    }
}

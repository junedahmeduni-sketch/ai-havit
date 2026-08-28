package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.components.*
import com.example.ui.theme.ColorStreak
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onOpenAiChat: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habitsWithStatus.collectAsState()
    val categoryProgresses by viewModel.categoryProgresses.collectAsState()
    val selectedCategoryCoaching by viewModel.selectedCategoryCoaching.collectAsState()
    val dailyAdvice by viewModel.dailyAdvice.collectAsState()
    val weeklyReview by viewModel.weeklyReview.collectAsState()
    val isAdviceLoading by viewModel.isAdviceLoading.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isNotificationVisible by viewModel.proactiveNotificationVisible.collectAsState()

    val totalHabits = habits.size
    val completedHabits = habits.count { it.isCompletedToday }
    val progressPct = if (totalHabits > 0) (completedHabits.toFloat() / totalHabits.toFloat()) else 0f

    val dateFormat = remember(settings.language) {
        if (settings.language == AppLanguage.BENGALI)
            SimpleDateFormat("EEEE, d MMMM", Locale("bn"))
        else
            SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH)
    }
    val todayDateString = remember { dateFormat.format(Date()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("home_screen_lazy_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        // 1. Header & Language Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (settings.language == AppLanguage.BENGALI) "স্বাগতম 👋" else "Welcome back 👋",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = todayDateString,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                LanguageToggle(
                    currentLanguage = settings.language,
                    onLanguageSelected = { viewModel.setLanguage(it) }
                )
            }
        }

        // 2. Proactive AI Notification Banner (if enabled and visible)
        if (settings.aiNotificationsEnabled && isNotificationVisible) {
            item {
                ProactiveNotificationBanner(
                    language = settings.language,
                    onDismiss = { viewModel.dismissProactiveNotification() },
                    onClick = onOpenAiChat
                )
            }
        }

        // 3. Daily AI Advice Card (Powered by Gemini)
        if (settings.aiEnabled && settings.dailyAdviceEnabled) {
            item {
                AiAdviceCard(
                    adviceEn = dailyAdvice.first,
                    adviceBn = dailyAdvice.second,
                    language = settings.language,
                    isLoading = isAdviceLoading,
                    onRefresh = { viewModel.refreshDailyAdvice() },
                    onOpenAiChat = onOpenAiChat
                )
            }
        }

        // 4. Today's Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(0.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        com.example.ui.theme.ShapeGeometricCard
                    )
                    .testTag("today_progress_card"),
                shape = com.example.ui.theme.ShapeGeometricCard,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (settings.language == AppLanguage.BENGALI) "আজকের অগ্রগতি" else "Today's Progress",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (settings.language == AppLanguage.BENGALI)
                                    "${totalHabits}টির মধ্যে ${completedHabits}টি habit সম্পন্ন"
                                else
                                    "$completedHabits of $totalHabits habits completed",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Surface(
                            shape = com.example.ui.theme.ShapeGeometricPill,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = "${(progressPct * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { progressPct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(com.example.ui.theme.ShapeSharp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickMetricChip(
                            icon = "🔥",
                            label = if (settings.language == AppLanguage.BENGALI) "সেরা Streak" else "Top Streak",
                            value = if (settings.language == AppLanguage.BENGALI) "২১ দিন" else "21 Days",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        QuickMetricChip(
                            icon = "📱",
                            label = if (settings.language == AppLanguage.BENGALI) "স্ক্রিন টাইম" else "Screen Time",
                            value = if (settings.language == AppLanguage.BENGALI) "৪ঘ ৪৫মি" else "4h 45m",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        QuickMetricChip(
                            icon = "🎯",
                            label = if (settings.language == AppLanguage.BENGALI) "বাকি আছে" else "Remaining",
                            value = "${totalHabits - completedHabits}",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 4.5 Category Balance & Coaching Section
        if (categoryProgresses.isNotEmpty()) {
            item {
                CategoryBalanceSection(
                    categoryProgresses = categoryProgresses,
                    language = settings.language,
                    onCategoryClick = { category ->
                        viewModel.requestCategoryCoaching(category)
                    }
                )
            }
        }

        // 5. Today's Habits Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (settings.language == AppLanguage.BENGALI) "আজকের Habits গ্রিড" else "Today's Habits Grid",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        shape = com.example.ui.theme.ShapeGeometricPill,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = "${habits.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                TextButton(
                    onClick = onNavigateToHabits,
                    modifier = Modifier.testTag("see_all_habits_btn")
                ) {
                    Text(
                        text = if (settings.language == AppLanguage.BENGALI) "সবগুলো দেখুন" else "View All",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }

        // 6. Consistent 2-Column Grid Layout for Habit Cards
        val habitPairs = habits.chunked(2)
        items(habitPairs, key = { pair -> pair.map { it.habit.id }.joinToString("_") }) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    HabitGridCard(
                        habitWithStatus = pair[0],
                        language = settings.language,
                        onToggle = { isCompleted ->
                            viewModel.toggleHabit(pair[0].habit.id, isCompleted)
                        }
                    )
                }
                if (pair.size > 1) {
                    Box(modifier = Modifier.weight(1f)) {
                        HabitGridCard(
                            habitWithStatus = pair[1],
                            language = settings.language,
                            onToggle = { isCompleted ->
                                viewModel.toggleHabit(pair[1].habit.id, isCompleted)
                            }
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // 7. Weekly AI Review Summary
        if (settings.aiEnabled && settings.weeklyReviewEnabled) {
            item {
                WeeklyReviewCard(
                    reviewEn = weeklyReview.first,
                    reviewBn = weeklyReview.second,
                    language = settings.language,
                    onRefresh = { viewModel.refreshWeeklyReview() }
                )
            }
        }
    }

    // Category Coaching Dialog
    selectedCategoryCoaching?.let { (category, coachingPair) ->
        CategoryCoachingDialog(
            category = category,
            coachingPair = coachingPair,
            language = settings.language,
            onDismiss = { viewModel.dismissCategoryCoaching() }
        )
    }
}

@Composable
private fun QuickMetricChip(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Surface(
        shape = com.example.ui.theme.ShapeGeometricSubtle,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.padding(horizontal = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(5.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.5.sp
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                )
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdviceFrequency
import com.example.data.model.AppLanguage
import com.example.ui.components.LanguageToggle
import com.example.ui.theme.ShapeGeometricCard
import com.example.ui.theme.ShapeGeometricPill
import com.example.ui.theme.ShapeGeometricSubtle
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isBangla = settings.language == AppLanguage.BENGALI

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showGoalsDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "সেটিংস ও একাউন্ট" else "Settings & Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    LanguageToggle(
                        currentLanguage = settings.language,
                        onLanguageSelected = { viewModel.setLanguage(it) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = {
            if (snackbarMessage != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(snackbarMessage ?: "")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
        ) {
            // 1. User Profile & Account Card
            item {
                SettingsSectionHeader(
                    title = if (isBangla) "ইউজার প্রোফাইল ও একাউন্ট" else "User Account"
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, ShapeGeometricCard),
                    shape = ShapeGeometricCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = currentUser?.avatarEmoji ?: "⚡", fontSize = 24.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = currentUser?.name ?: "Alex Johnson",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = currentUser?.email ?: "alex@habittrack.ai",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Text(
                                        text = if (isBangla) "লেভেল ${currentUser?.level ?: 5} • ${currentUser?.xp ?: 2450} XP" else "Level ${currentUser?.level ?: 5} • ${currentUser?.xp ?: 2450} XP",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            IconButton(onClick = { showLogoutDialog = true }) {
                                Icon(Icons.Outlined.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // 2. Language Preference Section
            item {
                SettingsSectionHeader(
                    title = if (isBangla) "ভাষা পছন্দ (Language)" else "Language Preference"
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, ShapeGeometricCard),
                    shape = ShapeGeometricCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBangla) "অ্যাপ ও AI ভাষা" else "App & AI Language",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = if (isBangla) "বর্তমানে নির্বাচিত: বাংলা" else "Currently: English",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        LanguageToggle(
                            currentLanguage = settings.language,
                            onLanguageSelected = { viewModel.setLanguage(it) }
                        )
                    }
                }
            }

            // 3. Smart Notifications Center
            item {
                SettingsSectionHeader(
                    title = if (isBangla) "স্মার্ট নোটিফিকেশন সেন্টার" else "Smart Notifications Center"
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, ShapeGeometricCard),
                    shape = ShapeGeometricCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        SettingsSwitchRow(
                            title = if (isBangla) "অভ্যাস রিমাইন্ডার" else "Habit Reminders",
                            subtitle = if (isBangla) "প্রতিটি অভ্যাসের নির্ধারিত সময়ের নোটিফিকেশন" else "Scheduled alerts for daily habits",
                            isChecked = settings.habitNotificationsEnabled,
                            onCheckedChange = { viewModel.setHabitNotificationsEnabled(it) },
                            testTag = "toggle_habit_notif"
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        SettingsSwitchRow(
                            title = if (isBangla) "পানি পানের অ্যালার্ট" else "Water Hydration Alerts",
                            subtitle = if (isBangla) "প্রতি ২ ঘণ্টায় পানি পানের রিমাইন্ডার" else "Periodic hydration reminders throughout the day",
                            isChecked = settings.waterNotificationsEnabled,
                            onCheckedChange = { viewModel.setWaterNotificationsEnabled(it) },
                            testTag = "toggle_water_notif"
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        SettingsSwitchRow(
                            title = if (isBangla) "পড়াশোনা ও ফোকাস নোটিফিকেশন" else "Study & Focus Alerts",
                            subtitle = if (isBangla) "পোমোডোরো সেশন ও পড়ার শিডিউল" else "Focus sessions and sprint reminders",
                            isChecked = settings.studyNotificationsEnabled,
                            onCheckedChange = { viewModel.setStudyNotificationsEnabled(it) },
                            testTag = "toggle_study_notif"
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        SettingsSwitchRow(
                            title = if (isBangla) "ওয়ার্কআউট অ্যালার্ট" else "Workout Reminders",
                            subtitle = if (isBangla) "ব্যায়াম ও ফিটনেস রুটিনের রিমাইন্ডার" else "Daily exercise and fitness schedule reminders",
                            isChecked = settings.workoutNotificationsEnabled,
                            onCheckedChange = { viewModel.setWorkoutNotificationsEnabled(it) },
                            testTag = "toggle_workout_notif"
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        SettingsSwitchRow(
                            title = if (isBangla) "ঘুমের শিডিউল অ্যালার্ট" else "Sleep Schedule Alerts",
                            subtitle = if (isBangla) "ঘুমানোর সময় ও উইন্ড-ডাউন রিমাইন্ডার" else "Bedtime wind-down and wake-up notifications",
                            isChecked = settings.sleepNotificationsEnabled,
                            onCheckedChange = { viewModel.setSleepNotificationsEnabled(it) },
                            testTag = "toggle_sleep_notif"
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        SettingsSwitchRow(
                            title = if (isBangla) "সকালের মোটিভেশন ও প্ল্যানার" else "Morning Motivation & Planner",
                            subtitle = if (isBangla) "সকাল ৮টায় দিনের শুরু করার অনুপ্রেরণা" else "Morning briefing and daily focus overview",
                            isChecked = settings.morningMotivationEnabled,
                            onCheckedChange = { viewModel.setMorningMotivationEnabled(it) },
                            testTag = "toggle_morning_notif"
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        SettingsSwitchRow(
                            title = if (isBangla) "সন্ধ্যার দৈনিক রিভিউ" else "Evening Daily Review",
                            subtitle = if (isBangla) "রাত ৯টায় দৈনিক অগ্রগতি পর্যালোচনা" else "End-of-day progress summary and reflection",
                            isChecked = settings.eveningReviewEnabled,
                            onCheckedChange = { viewModel.setEveningReviewEnabled(it) },
                            testTag = "toggle_evening_notif"
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        SettingsSwitchRow(
                            title = if (isBangla) "Streak ঝুঁকি সতর্কতা" else "Streak Risk Alerts",
                            subtitle = if (isBangla) "Streak ভাঙার পূর্বে সতর্কবার্তা" else "Alerts before your habit streaks break",
                            isChecked = settings.streakAlertsEnabled,
                            onCheckedChange = { viewModel.setStreakAlertsEnabled(it) },
                            testTag = "toggle_streak_notif"
                        )
                    }
                }
            }

            // 4. AI Coach Settings Section
            item {
                SettingsSectionHeader(
                    title = if (isBangla) "AI Personal Coach সেটিংস" else "AI Coach Settings"
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, ShapeGeometricCard),
                    shape = ShapeGeometricCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        SettingsSwitchRow(
                            title = if (isBangla) "AI Coach সক্রিয় রাখুন" else "Enable AI Coach",
                            subtitle = if (isBangla) "স্মার্ট পরামর্শ ও কথোপকথন সহকারী" else "Personalized coaching and action advice",
                            isChecked = settings.aiEnabled,
                            onCheckedChange = { viewModel.setAiEnabled(it) },
                            testTag = "toggle_ai_enabled"
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        SettingsSwitchRow(
                            title = if (isBangla) "দৈনিক AI পরামর্শ" else "Daily AI Advice",
                            subtitle = if (isBangla) "হোম স্ক্রিনে প্রতিদিনের রিকমেন্ডেশন" else "Daily tailored coaching on Home screen",
                            isChecked = settings.dailyAdviceEnabled && settings.aiEnabled,
                            enabled = settings.aiEnabled,
                            onCheckedChange = { viewModel.setDailyAdviceEnabled(it) },
                            testTag = "toggle_daily_advice"
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        SettingsSwitchRow(
                            title = if (isBangla) "সাপ্তাহিক AI রিভিউ" else "Weekly AI Review",
                            subtitle = if (isBangla) "সপ্তাহের পূর্ণাঙ্গ অগ্রগতি ও স্ক্রিন টাইম মূল্যায়ন" else "Comprehensive weekly habit & focus breakdown",
                            isChecked = settings.weeklyReviewEnabled && settings.aiEnabled,
                            enabled = settings.aiEnabled,
                            onCheckedChange = { viewModel.setWeeklyReviewEnabled(it) },
                            testTag = "toggle_weekly_review"
                        )
                    }
                }
            }

            // 5. Personal Goals Section
            item {
                SettingsSectionHeader(
                    title = if (isBangla) "ব্যক্তিগত লক্ষ্য ও ফোকাস" else "Personal Goals & Focus"
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, ShapeGeometricCard)
                        .clickable { showGoalsDialog = true }
                        .testTag("edit_goals_card"),
                    shape = ShapeGeometricCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBangla) "আমার প্রধান ফোকাস" else "My Core Focus",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = settings.userGoals,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                maxLines = 2
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Goals",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 6. Privacy & Data Controls Section
            item {
                SettingsSectionHeader(
                    title = if (isBangla) "গোপনীয়তা ও ডাটা কন্ট্রোল" else "Privacy & Data Controls"
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, ShapeGeometricCard),
                    shape = ShapeGeometricCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showClearHistoryDialog = true },
                            shape = ShapeGeometricSubtle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_clear_chat_history"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBangla) "AI চ্যাট হিস্ট্রি মুছুন" else "Clear AI Chat History"
                            )
                        }

                        OutlinedButton(
                            onClick = { showClearCacheDialog = true },
                            shape = ShapeGeometricSubtle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_clear_ai_cache")
                        ) {
                            Icon(Icons.Outlined.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBangla) "AI ক্যাশ ও পার্সোনালাইজড ডাটা মুছুন" else "Clear Personalized AI Cache"
                            )
                        }

                        Surface(
                            shape = ShapeGeometricSubtle,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = "Privacy",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isBangla)
                                        "আপনার ব্যক্তিগত মেসেজের কন্টেন্ট কখনোই বাইরে পাঠানো হয় না। সকল ডাটা আপনার ডিভাইসে সুরক্ষিত থাকে।"
                                    else
                                        "Your data remains strictly private on your device. Only habit status and screen time aggregates are processed for coaching.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = ShapeGeometricCard,
            title = { Text(if (isBangla) "লগআউট করবেন?" else "Log out?") },
            text = { Text(if (isBangla) "আপনি কি নিশ্চিত যে আপনার একাউন্ট থেকে লগআউট করতে চান?" else "Are you sure you want to log out of your account?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBangla) "লগআউট" else "Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Dialogs
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            shape = ShapeGeometricCard,
            title = {
                Text(if (isBangla) "AI চ্যাট হিস্ট্রি মুছবেন?" else "Clear AI Chat History?")
            },
            text = {
                Text(if (isBangla) "আপনার সকল পূর্ববর্তী চ্যাট মুছে ফেলা হবে।" else "All previous chat messages with the AI coach will be deleted.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearChatHistory()
                        showClearHistoryDialog = false
                        snackbarMessage = if (isBangla) "চ্যাট হিস্ট্রি মুছে ফেলা হয়েছে" else "Chat history cleared successfully"
                    }
                ) {
                    Text(if (isBangla) "মুছুন" else "Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            shape = ShapeGeometricCard,
            title = {
                Text(if (isBangla) "AI ক্যাশ মুছবেন?" else "Clear Personalized AI Cache?")
            },
            text = {
                Text(if (isBangla) "ক্যাশ করা দৈনিক ও সাপ্তাহিক পরামর্শ মুছে নতুন করে তৈরি করা হবে।" else "Cached daily advice and weekly reviews will be cleared and regenerated.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAiCache()
                        showClearCacheDialog = false
                        snackbarMessage = if (isBangla) "AI ক্যাশ মুছে ফেলা হয়েছে" else "AI cache cleared successfully"
                    }
                ) {
                    Text(if (isBangla) "মুছুন" else "Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    if (showGoalsDialog) {
        var tempGoals by remember { mutableStateOf(settings.userGoals) }
        AlertDialog(
            onDismissRequest = { showGoalsDialog = false },
            shape = ShapeGeometricCard,
            title = {
                Text(if (isBangla) "আপনার লক্ষ্য নির্ধারণ করুন" else "Edit Personal Goals")
            },
            text = {
                OutlinedTextField(
                    value = tempGoals,
                    onValueChange = { tempGoals = it },
                    shape = ShapeGeometricSubtle,
                    label = { Text(if (isBangla) "লক্ষ্য (Goals)" else "Goals") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goals_input_field"),
                    maxLines = 4
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setUserGoals(tempGoals)
                        showGoalsDialog = false
                    }
                ) {
                    Text(if (isBangla) "সংরক্ষণ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalsDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.testTag(testTag)
        )
    }
}

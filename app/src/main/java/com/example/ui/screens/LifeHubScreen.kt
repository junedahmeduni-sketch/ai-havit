package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.ShapeGeometricCard
import com.example.ui.theme.ShapeGeometricPill
import com.example.ui.theme.ShapeGeometricSubtle
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeHubScreen(
    viewModel: MainViewModel,
    onOpenAiChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val isBangla = settings.language == AppLanguage.BENGALI

    val currentUser by viewModel.currentUser.collectAsState()
    val dailyScore by viewModel.dailyScore.collectAsState()
    val todayWater by viewModel.todayWater.collectAsState()
    val todaySleep by viewModel.todaySleep.collectAsState()
    val todayMood by viewModel.todayMood.collectAsState()
    val todayPlans by viewModel.todayDailyPlans.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val journals by viewModel.journals.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val whatShouldIDoNowText by viewModel.whatShouldIDoNowText.collectAsState()
    val isWhatShouldIDoLoading by viewModel.isWhatShouldIDoLoading.collectAsState()

    // Dialog states
    var showGoalDialog by remember { mutableStateOf(false) }
    var showMoodDialog by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }
    var showStudyDialog by remember { mutableStateOf(false) }
    var showWorkoutDialog by remember { mutableStateOf(false) }
    var showJournalDialog by remember { mutableStateOf(false) }
    var showPlanDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("life_hub_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        // 1. Header & XP / Level / Streak Freeze Banner
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currentUser?.avatarEmoji ?: "⚡",
                                        fontSize = 22.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentUser?.name ?: "Alex Johnson",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isBangla) "লেভেল ${currentUser?.level ?: 5} • ${currentUser?.xp ?: 2450} XP" else "Level ${currentUser?.level ?: 5} • ${currentUser?.xp ?: 2450} XP",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }

                        // Streak Freeze Button
                        Button(
                            onClick = { viewModel.toggleStreakFreeze() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentUser?.isStreakFreezeActive == true) Color(0xFF0284C7) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (currentUser?.isStreakFreezeActive == true) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = ShapeGeometricPill,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (currentUser?.isStreakFreezeActive == true) {
                                    if (isBangla) "❄️ সক্রিয়" else "❄️ Protected"
                                } else {
                                    if (isBangla) "❄️ সুরক্ষা (${currentUser?.streakFreezeCount ?: 2})" else "❄️ Freeze (${currentUser?.streakFreezeCount ?: 2})"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
        }

        // 2. "What should I do now?" AI Prominent Button
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), ShapeGeometricCard)
                    .clickable { viewModel.requestWhatShouldIDoNow() }
                    .testTag("what_should_i_do_now_btn"),
                shape = ShapeGeometricCard,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isBangla) "এখন আমার কী করা উচিত? 💡" else "What should I do now? 💡",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            )
                            Text(
                                text = if (isBangla) "AI কোচ তোমার সময় ও অভ্যাস অনুযায়ী পদক্ষেপ জানাবে" else "AI Coach recommends your optimal immediate next step",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), fontSize = 11.sp)
                            )
                        }
                    }

                    if (isWhatShouldIDoLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 3. Daily Score (0 - 100) Card
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBangla) "দৈনিক স্কোর" else "Daily Score",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isBangla) "অভ্যাস, ঘুম, পানি ও পড়াশোনার সামগ্রিক বিশ্লেষণ" else "Holistic Habit, Sleep, Water & Focus breakdown",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Surface(
                            shape = ShapeGeometricPill,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = "${dailyScore.totalScore} / 100",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { dailyScore.totalScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ScoreSubPill("🎯 " + if (isBangla) "অভ্যাস" else "Habits", "${dailyScore.habitScore}/40")
                        ScoreSubPill("💧 " + if (isBangla) "পানি" else "Water", "${dailyScore.waterScore}/15")
                        ScoreSubPill("📚 " + if (isBangla) "পড়াশোনা" else "Study", "${dailyScore.studyScore}/15")
                        ScoreSubPill("💪 " + if (isBangla) "ওয়ার্কআউট" else "Workout", "${dailyScore.workoutScore}/15")
                        ScoreSubPill("😴 " + if (isBangla) "ঘুম" else "Sleep", "${dailyScore.sleepScore}/15")
                    }
                }
            }
        }

        // 4. Quick Life Trackers Grid (Water, Sleep, Mood, Study, Workout, Journal)
        item {
            Text(
                text = if (isBangla) "লাইফ ট্র্যাকার্স" else "Life Trackers",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Water Card
                TrackerCard(
                    modifier = Modifier.weight(1f),
                    icon = "💧",
                    title = if (isBangla) "পানি পান" else "Water",
                    subtitle = "${todayWater?.glassesDrank ?: 6} / ${todayWater?.goalGlasses ?: 8} " + if (isBangla) "গ্লাস" else "glasses",
                    actionLabel = "+১ গ্লাস",
                    onClick = { viewModel.addWaterGlass() }
                )

                // Sleep Card
                TrackerCard(
                    modifier = Modifier.weight(1f),
                    icon = "😴",
                    title = if (isBangla) "ঘুম" else "Sleep",
                    subtitle = if (todaySleep != null) "${todaySleep!!.durationMinutes / 60}h ${todaySleep!!.durationMinutes % 60}m" else if (isBangla) "৮ ঘণ্টা (ভালো)" else "8h 00m",
                    actionLabel = if (isBangla) "লগ করুন" else "Log Sleep",
                    onClick = { showSleepDialog = true }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mood Card
                TrackerCard(
                    modifier = Modifier.weight(1f),
                    icon = when (todayMood?.moodLevel ?: 4) {
                        5 -> "🤩"
                        4 -> "😊"
                        3 -> "😐"
                        2 -> "😔"
                        else -> "😫"
                    },
                    title = if (isBangla) "আজকের মুড" else "Mood",
                    subtitle = todayMood?.note?.take(18) ?: if (isBangla) "ভালো ও অনুপ্রাণিত" else "Good & Motivated",
                    actionLabel = if (isBangla) "মুড দিন" else "Log Mood",
                    onClick = { showMoodDialog = true }
                )

                // Study / Pomodoro Card
                TrackerCard(
                    modifier = Modifier.weight(1f),
                    icon = "📚",
                    title = if (isBangla) "পড়াশোনা ও ফোকাস" else "Study & Focus",
                    subtitle = if (isBangla) "৪৫ মিনিট পোমোডোরো" else "45m Pomodoro",
                    actionLabel = if (isBangla) "স্টাডি সেশন" else "Log Study",
                    onClick = { showStudyDialog = true }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Workout Card
                TrackerCard(
                    modifier = Modifier.weight(1f),
                    icon = "💪",
                    title = if (isBangla) "ওয়ার্কআউট" else "Workout",
                    subtitle = if (isBangla) "২৫ মিনিট • ১৬০ ক্যালরি" else "25 mins • 160 kcal",
                    actionLabel = if (isBangla) "ওয়ার্কআউট যোগ" else "Log Workout",
                    onClick = { showWorkoutDialog = true }
                )

                // Journal Card
                TrackerCard(
                    modifier = Modifier.weight(1f),
                    icon = "📝",
                    title = if (isBangla) "দৈনিক ডায়েরি" else "Journal",
                    subtitle = if (journals.isNotEmpty()) "${journals.size} " + if (isBangla) "এন্ট্রি" else "entries" else if (isBangla) "কৃতজ্ঞতা ও চিন্তা" else "Gratitude note",
                    actionLabel = if (isBangla) "নতুন এন্ট্রি" else "New Entry",
                    onClick = { showJournalDialog = true }
                )
            }
        }

        // 5. Smart Daily Planner Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBangla) "স্মার্ট ডেইলি প্ল্যানার 📅" else "Smart Daily Planner 📅",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = { showPlanDialog = true }) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "Add Plan", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (todayPlans.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeGeometricCard,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (isBangla) "আজকের কোনো শিডিউল নেই। নতুন যোগ করুন!" else "No plan scheduled yet. Add one!",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            items(todayPlans, key = { it.id }) { plan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, ShapeGeometricSubtle),
                    shape = ShapeGeometricSubtle,
                    colors = CardDefaults.cardColors(
                        containerColor = if (plan.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Checkbox(
                                checked = plan.isCompleted,
                                onCheckedChange = { viewModel.togglePlanCompletion(plan) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = ShapeGeometricPill,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = plan.timeSlot,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBangla) plan.activityBn else plan.activityEn,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (plan.isCompleted) FontWeight.Normal else FontWeight.SemiBold
                                )
                            )
                        }

                        IconButton(onClick = { viewModel.deletePlan(plan) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // 6. Goals & AI Subtask Breakdown Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBangla) "লক্ষ্য ও AI মাইলস্টোন 🎯" else "Goals & AI Milestones 🎯",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = { showGoalDialog = true }) {
                    Text(if (isBangla) "+ নতুন লক্ষ্য" else "+ Add Goal")
                }
            }
        }

        items(goals, key = { it.id }) { goal ->
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            if (goal.description.isNotBlank()) {
                                Text(
                                    text = goal.description,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        Surface(
                            shape = ShapeGeometricPill,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${goal.progressPct}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = goal.progressPct.toFloat(),
                        onValueChange = { viewModel.updateGoalProgress(goal, it.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (goal.subtasksJson.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isBangla) "🤖 AI মাইলস্টোন ধাপসমূহ:" else "🤖 AI Subtask Breakdown:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        goal.subtasksJson.split(",").forEach { subtask ->
                            if (subtask.isNotBlank()) {
                                Row(
                                    modifier = Modifier.padding(top = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.CheckCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = subtask.trim(),
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Achievements & Badges Showcase
        item {
            Text(
                text = if (isBangla) "অর্জন ও ব্যাজ 🏆" else "Achievements & Badges 🏆",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(achievements, key = { it.id }) { ach ->
                    Surface(
                        shape = ShapeGeometricCard,
                        color = if (ach.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.width(140.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = ach.iconEmoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isBangla) ach.titleBn else ach.titleEn,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+${ach.xpReward} XP",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            )
                            if (ach.isUnlocked) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isBangla) "✓ আনলকড" else "✓ Unlocked",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF16A34A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: "What Should I Do Now?"
    whatShouldIDoNowText?.let { text ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissWhatShouldIDoNow() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isBangla) "AI কোচের তাৎক্ষণিক পরামর্শ" else "AI Coach Action Step")
                }
            },
            text = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissWhatShouldIDoNow() }) {
                    Text(if (isBangla) "ধন্যবাদ, শুরু করছি! 🚀" else "Got it, let's do it! 🚀")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.dismissWhatShouldIDoNow()
                    onOpenAiChat()
                }) {
                    Text(if (isBangla) "কোচের সাথে চ্যাট করুন" else "Chat with Coach")
                }
            }
        )
    }

    // Dialog: Add Goal
    if (showGoalDialog) {
        var goalTitle by remember { mutableStateOf("") }
        var goalDesc by remember { mutableStateOf("") }
        var goalTarget by remember { mutableStateOf("") }
        var goalCategory by remember { mutableStateOf("Study") }

        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text(if (isBangla) "নতুন লক্ষ্য নির্ধারণ করুন" else "Add New Goal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text(if (isBangla) "লক্ষ্যের নাম" else "Goal Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = goalDesc,
                        onValueChange = { goalDesc = it },
                        label = { Text(if (isBangla) "বিবরণ" else "Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = goalTarget,
                        onValueChange = { goalTarget = it },
                        label = { Text(if (isBangla) "টার্গেট (যেমন: ৩০ দিন / ৫ কিমি)" else "Target (e.g. 30 Days / 5 KM)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (goalTitle.isNotBlank()) {
                        viewModel.addGoal(goalTitle, goalDesc, goalTarget, "2026-12-31", goalCategory, false)
                        showGoalDialog = false
                    }
                }) {
                    Text(if (isBangla) "সংরক্ষণ করুন" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Dialog: Log Mood
    if (showMoodDialog) {
        var selectedMoodLevel by remember { mutableStateOf(5) }
        var moodNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showMoodDialog = false },
            title = { Text(if (isBangla) "আজকের অনুভূতি কেমন?" else "How are you feeling today?") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf(Pair(5, "🤩"), Pair(4, "😊"), Pair(3, "😐"), Pair(2, "😔"), Pair(1, "😫")).forEach { (lvl, emoji) ->
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { selectedMoodLevel = lvl },
                                shape = CircleShape,
                                color = if (selectedMoodLevel == lvl) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (selectedMoodLevel == lvl) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = moodNote,
                        onValueChange = { moodNote = it },
                        label = { Text(if (isBangla) "অনুভূতির কারণ / ছোট নোট" else "Optional note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.logMood(selectedMoodLevel, moodNote)
                    showMoodDialog = false
                }) {
                    Text(if (isBangla) "লগ করুন" else "Log Mood")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMoodDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Dialog: Log Sleep
    if (showSleepDialog) {
        var durationHrs by remember { mutableStateOf("8") }
        var bedtime by remember { mutableStateOf("23:00") }
        var wakeTime by remember { mutableStateOf("07:00") }

        AlertDialog(
            onDismissRequest = { showSleepDialog = false },
            title = { Text(if (isBangla) "ঘুমের হিসাব লগ করুন" else "Log Sleep Record") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = durationHrs,
                        onValueChange = { durationHrs = it },
                        label = { Text(if (isBangla) "ঘুমের সময়কাল (ঘণ্টা)" else "Duration (hours)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bedtime,
                        onValueChange = { bedtime = it },
                        label = { Text(if (isBangla) "ঘুমানোর সময় (যেমন ২৩:০০)" else "Bedtime (e.g. 23:00)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wakeTime,
                        onValueChange = { wakeTime = it },
                        label = { Text(if (isBangla) "ওঠার সময় (যেমন ০৭:০০)" else "Wake up time (e.g. 07:00)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val mins = (durationHrs.toDoubleOrNull() ?: 8.0) * 60
                    viewModel.logSleep(bedtime, wakeTime, mins.toInt(), 4)
                    showSleepDialog = false
                }) {
                    Text(if (isBangla) "সংরক্ষণ করুন" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSleepDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Dialog: Log Study
    if (showStudyDialog) {
        var subject by remember { mutableStateOf("") }
        var durationMins by remember { mutableStateOf("30") }

        AlertDialog(
            onDismissRequest = { showStudyDialog = false },
            title = { Text(if (isBangla) "স্টাডি ও পোমোডোরো সেশন" else "Log Study Session") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text(if (isBangla) "বিষয় / টপিক" else "Subject / Topic") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = durationMins,
                        onValueChange = { durationMins = it },
                        label = { Text(if (isBangla) "সময়কাল (মিনিট)" else "Duration (minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (subject.isNotBlank()) {
                        val mins = durationMins.toIntOrNull() ?: 30
                        viewModel.logStudySession(subject, mins, "Pomodoro", "Focused sprint")
                        showStudyDialog = false
                    }
                }) {
                    Text(if (isBangla) "যোগ করুন" else "Save Session")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStudyDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Dialog: Log Workout
    if (showWorkoutDialog) {
        var exerciseName by remember { mutableStateOf("") }
        var workoutMins by remember { mutableStateOf("20") }
        var calories by remember { mutableStateOf("120") }

        AlertDialog(
            onDismissRequest = { showWorkoutDialog = false },
            title = { Text(if (isBangla) "ওয়ার্কআউট লগ করুন" else "Log Workout") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = exerciseName,
                        onValueChange = { exerciseName = it },
                        label = { Text(if (isBangla) "ব্যায়ামের নাম (যেমন: পুশ-আপ / রানিং)" else "Exercise Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = workoutMins,
                        onValueChange = { workoutMins = it },
                        label = { Text(if (isBangla) "সময়কাল (মিনিট)" else "Duration (minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text(if (isBangla) "আনুমানিক ক্যালরি" else "Est. Calories Burned") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (exerciseName.isNotBlank()) {
                        val mins = workoutMins.toIntOrNull() ?: 20
                        val cal = calories.toIntOrNull() ?: 120
                        viewModel.logWorkout("Fitness", exerciseName, mins, 3, 12, cal)
                        showWorkoutDialog = false
                    }
                }) {
                    Text(if (isBangla) "সংরক্ষণ করুন" else "Save Workout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWorkoutDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Dialog: Daily Journal
    if (showJournalDialog) {
        var journalTitle by remember { mutableStateOf("") }
        var journalContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showJournalDialog = false },
            title = { Text(if (isBangla) "আজকের ডায়েরি ও চিন্তা" else "Daily Journal Entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = journalTitle,
                        onValueChange = { journalTitle = it },
                        label = { Text(if (isBangla) "শিরোনাম" else "Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = journalContent,
                        onValueChange = { journalContent = it },
                        label = { Text(if (isBangla) "আজকের অনুভূতি ও অভিজ্ঞতা..." else "Write thoughts, gratitude...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (journalTitle.isNotBlank() && journalContent.isNotBlank()) {
                        viewModel.saveJournal(journalTitle, journalContent, "✨")
                        showJournalDialog = false
                    }
                }) {
                    Text(if (isBangla) "সংরক্ষণ ও AI সামারি" else "Save with AI Summary")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJournalDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Dialog: Add Daily Plan
    if (showPlanDialog) {
        var planTime by remember { mutableStateOf("10:00") }
        var planActivity by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPlanDialog = false },
            title = { Text(if (isBangla) "নতুন রুটিন বা কাজ যোগ করুন" else "Schedule Daily Plan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = planTime,
                        onValueChange = { planTime = it },
                        label = { Text(if (isBangla) "সময় (যেমন: ১০:০০)" else "Time (e.g. 10:00)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = planActivity,
                        onValueChange = { planActivity = it },
                        label = { Text(if (isBangla) "কাজ বা শিডিউল" else "Activity / Task") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (planActivity.isNotBlank()) {
                        viewModel.addDailyPlan(planTime, planActivity, "General")
                        showPlanDialog = false
                    }
                }) {
                    Text(if (isBangla) "যোগ করুন" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPlanDialog = false }) {
                    Text(if (isBangla) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun ScoreSubPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun TrackerCard(
    icon: String,
    title: String,
    subtitle: String,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, ShapeGeometricCard),
        shape = ShapeGeometricCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = icon, fontSize = 20.sp)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.5.sp),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                shape = ShapeGeometricSubtle,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                )
            }
        }
    }
}

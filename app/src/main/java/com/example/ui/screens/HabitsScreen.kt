package com.example.ui.screens

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.HabitCategory
import com.example.data.model.HabitEntity
import com.example.data.model.HabitWithStatus
import com.example.ui.components.LanguageToggle
import com.example.ui.theme.ShapeGeometricCard
import com.example.ui.theme.ShapeGeometricPill
import com.example.ui.theme.ShapeGeometricSubtle
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: MainViewModel,
    onOpenAiChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habitsWithStatus.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val suggestedHabitDialog by viewModel.suggestedHabitDialog.collectAsState()
    val isBangla = settings.language == AppLanguage.BENGALI

    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<HabitEntity?>(null) }

    val categories = listOf("All") + HabitCategory.entries.map { it.id }

    val filteredHabits = remember(habits, selectedCategory) {
        if (selectedCategory == "All") habits
        else habits.filter {
            val cat = HabitCategory.fromId(it.habit.category)
            cat.id.equals(selectedCategory, ignoreCase = true) || it.habit.category.equals(selectedCategory, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("habits_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "সকল Habits" else "All Habits",
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_habit_fab"),
                shape = ShapeGeometricCard,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBangla) "নতুন Habit" else "New Habit",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Filter Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { catId ->
                    val isSelected = selectedCategory == catId
                    val catLabel = if (catId == "All") {
                        if (isBangla) "সবগুলো" else "All"
                    } else {
                        val habitCat = HabitCategory.fromId(catId)
                        if (isBangla) habitCat.titleBn else habitCat.titleEn
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = catId },
                        label = { Text(catLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = ShapeGeometricPill,
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("filter_$catId")
                    )
                }
            }

            // Habits List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
            ) {
                // Streak highlight card if 21-day streak exists
                val topStreakHabit = habits.maxByOrNull { it.habit.streakCount }
                if (topStreakHabit != null && topStreakHabit.habit.streakCount >= 20) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, ShapeGeometricCard)
                                .testTag("streak_celebration_card"),
                            shape = ShapeGeometricCard,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = ShapeGeometricSubtle,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Text(text = "🔥", fontSize = 20.sp, modifier = Modifier.padding(6.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBangla) "২১ দিনের Streak চলছে! 🔥" else "21-Day Streak Active! 🔥",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (isBangla)
                                            "তোমার ${topStreakHabit.habit.titleBn}-এ ২১ দিনের streak চলছে। আজকের habitগুলো সম্পন্ন করে streak বজায় রাখো।"
                                        else
                                            "You have a 21-day streak on ${topStreakHabit.habit.titleEn}. Complete today's habits to maintain the streak.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }

                items(filteredHabits, key = { it.habit.id }) { item ->
                    DetailedHabitCard(
                        habitWithStatus = item,
                        isBangla = isBangla,
                        onSetStatus = { status -> viewModel.setHabitStatus(item.habit.id, status) },
                        onPauseToggle = { viewModel.togglePauseHabit(item.habit) },
                        onArchive = { viewModel.archiveHabit(item.habit) },
                        onDelete = { viewModel.deleteHabit(item.habit) },
                        onEdit = { habitToEdit = item.habit }
                    )
                }
            }
        }
    }

    // Add Habit Dialog
    if (showAddDialog) {
        AddOrEditHabitDialog(
            habitToEdit = null,
            isBangla = isBangla,
            onDismiss = { showAddDialog = false },
            onSave = { titleEn, titleBn, catId, reminder, days, notes ->
                viewModel.addNewHabit(titleEn, titleBn, catId, reminder, days, notes)
                showAddDialog = false
            }
        )
    }

    // Edit Habit Dialog
    habitToEdit?.let { habit ->
        AddOrEditHabitDialog(
            habitToEdit = habit,
            isBangla = isBangla,
            onDismiss = { habitToEdit = null },
            onSave = { titleEn, titleBn, catId, reminder, days, notes ->
                viewModel.updateHabit(
                    habit.copy(
                        titleEn = titleEn,
                        titleBn = titleBn,
                        category = catId,
                        reminderTime = reminder,
                        targetDaysPerWeek = days,
                        notes = notes
                    )
                )
                habitToEdit = null
            }
        )
    }

    // AI Habit Suggestion Confirmation Dialog (MANDATORY USER CONFIRMATION)
    suggestedHabitDialog?.let { habit ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuggestedHabit() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isBangla) "নতুন Habit সাজেশন" else "AI Habit Recommendation")
                }
            },
            text = {
                Column {
                    Text(
                        text = if (isBangla)
                            "AI কোচ তোমার লাইফস্টাইল বিশ্লেষণ করে এই নতুন habit-টি প্রস্তাব করছে:\n\n📌 ${habit.titleBn} (${habit.reminderTime})"
                        else
                            "AI Coach recommends adding this new habit based on your routines:\n\n📌 ${habit.titleEn} (${habit.reminderTime})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBangla) "তুমি কি এই habit-টি তৈরি করতে সম্মত?" else "Would you like to confirm and create this habit?",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.confirmSuggestedHabit() }) {
                    Text(if (isBangla) "হ্যাঁ, তৈরি করুন ✓" else "Yes, Create ✓")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSuggestedHabit() }) {
                    Text(if (isBangla) "না, ধন্যবাদ" else "Decline")
                }
            }
        )
    }
}

@Composable
private fun DetailedHabitCard(
    habitWithStatus: HabitWithStatus,
    isBangla: Boolean,
    onSetStatus: (String) -> Unit,
    onPauseToggle: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val habit = habitWithStatus.habit
    val category = HabitCategory.fromId(habit.category)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (habitWithStatus.isCompletedToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
                ShapeGeometricCard
            ),
        shape = ShapeGeometricCard,
        colors = CardDefaults.cardColors(
            containerColor = if (habitWithStatus.isCompletedToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = ShapeGeometricSubtle,
                        color = Color(category.colorHex).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(category.colorHex).copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = category.iconEmoji, fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBangla) habit.titleBn else habit.titleEn,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "⏰ ${habit.reminderTime}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = "🔥 ${habit.streakCount} " + if (isBangla) "দিন" else "days",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color(0xFFF97316), fontWeight = FontWeight.SemiBold)
                            )
                            if (habit.isPaused) {
                                Text(
                                    text = "⏸ " + if (isBangla) "স্থগিত" else "Paused",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                )
                            }
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isBangla) "এডিট করুন" else "Edit Habit") },
                            onClick = { showMenu = false; onEdit() },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (habit.isPaused) (if (isBangla) "চালু করুন" else "Resume") else (if (isBangla) "স্থগিত করুন" else "Pause")) },
                            onClick = { showMenu = false; onPauseToggle() },
                            leadingIcon = { Icon(if (habit.isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isBangla) "আর্কাইভ করুন" else "Archive") },
                            onClick = { showMenu = false; onArchive() },
                            leadingIcon = { Icon(Icons.Outlined.Archive, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isBangla) "মুছে ফেলুন" else "Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Complete, Skip, Miss, Uncheck
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Complete Button
                Button(
                    onClick = {
                        if (habitWithStatus.isCompletedToday) onSetStatus("UNCHECK") else onSetStatus("COMPLETED")
                    },
                    modifier = Modifier.weight(1.2f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (habitWithStatus.isCompletedToday) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = ShapeGeometricSubtle,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(if (habitWithStatus.isCompletedToday) Icons.Filled.CheckCircle else Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (habitWithStatus.isCompletedToday) (if (isBangla) "সম্পন্ন ✓" else "Completed ✓") else (if (isBangla) "সম্পন্ন করুন" else "Complete"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                }

                // Skip Button
                OutlinedButton(
                    onClick = {
                        if (habitWithStatus.isSkippedToday) onSetStatus("UNCHECK") else onSetStatus("SKIPPED")
                    },
                    modifier = Modifier.weight(0.9f).height(36.dp),
                    shape = ShapeGeometricSubtle,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (habitWithStatus.isSkippedToday) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (habitWithStatus.isSkippedToday) (if (isBangla) "বাদ দেওয়া ⏭" else "Skipped ⏭") else (if (isBangla) "বাদ দিন" else "Skip"),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                    )
                }

                // Miss Button
                OutlinedButton(
                    onClick = {
                        if (habitWithStatus.isMissedToday) onSetStatus("UNCHECK") else onSetStatus("MISSED")
                    },
                    modifier = Modifier.weight(0.9f).height(36.dp),
                    shape = ShapeGeometricSubtle,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (habitWithStatus.isMissedToday) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (habitWithStatus.isMissedToday) (if (isBangla) "মিস ❌" else "Missed ❌") else (if (isBangla) "মিস" else "Miss"),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = if (habitWithStatus.isMissedToday) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddOrEditHabitDialog(
    habitToEdit: HabitEntity?,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (titleEn: String, titleBn: String, category: String, reminder: String, days: Int, notes: String) -> Unit
) {
    var titleEn by remember { mutableStateOf(habitToEdit?.titleEn ?: "") }
    var titleBn by remember { mutableStateOf(habitToEdit?.titleBn ?: "") }
    var selectedCategory by remember { mutableStateOf(habitToEdit?.let { HabitCategory.fromId(it.category) } ?: HabitCategory.HEALTH) }
    var reminderTime by remember { mutableStateOf(habitToEdit?.reminderTime ?: "08:00") }
    var targetDays by remember { mutableStateOf(habitToEdit?.targetDaysPerWeek?.toString() ?: "7") }
    var notes by remember { mutableStateOf(habitToEdit?.notes ?: "") }

    val categories = HabitCategory.entries

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = ShapeGeometricCard,
        title = {
            Text(
                text = if (habitToEdit == null) {
                    if (isBangla) "নতুন Habit যোগ করুন" else "Create New Habit"
                } else {
                    if (isBangla) "Habit এডিট করুন" else "Edit Habit"
                },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = titleEn,
                    onValueChange = { titleEn = it },
                    label = { Text("Title (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeGeometricSubtle,
                    singleLine = true
                )

                OutlinedTextField(
                    value = titleBn,
                    onValueChange = { titleBn = it },
                    label = { Text("Title in Bengali (বাংলা)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeGeometricSubtle,
                    singleLine = true
                )

                Text(
                    text = if (isBangla) "ক্যাটাগরি নির্বাচন করুন" else "Select Category",
                    style = MaterialTheme.typography.labelMedium
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        val catTitle = if (isBangla) cat.titleBn else cat.titleEn
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text("${cat.iconEmoji} $catTitle") },
                            shape = ShapeGeometricPill,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = reminderTime,
                        onValueChange = { reminderTime = it },
                        label = { Text(if (isBangla) "রিমাইন্ডার (08:30)" else "Reminder (08:30)") },
                        modifier = Modifier.weight(1f),
                        shape = ShapeGeometricSubtle,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = targetDays,
                        onValueChange = { targetDays = it },
                        label = { Text(if (isBangla) "সপ্তাহে দিন (৭)" else "Days/wk (7)") },
                        modifier = Modifier.weight(1f),
                        shape = ShapeGeometricSubtle,
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isBangla) "ছোট নোট / কেন এই অভ্যাস" else "Notes / Motivation") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeGeometricSubtle
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleEn.isNotBlank()) {
                        val days = targetDays.toIntOrNull() ?: 7
                        onSave(titleEn, titleBn, selectedCategory.id, reminderTime, days, notes)
                    }
                },
                enabled = titleEn.isNotBlank(),
                shape = ShapeGeometricCard
            ) {
                Text(if (isBangla) "সংরক্ষণ করুন" else "Save Habit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = ShapeGeometricCard) {
                Text(if (isBangla) "বাতিল" else "Cancel")
            }
        }
    )
}

package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.AppUsageStatEntity
import com.example.ui.components.LanguageToggle
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTimeScreen(
    viewModel: MainViewModel,
    onOpenAiChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenTimes by viewModel.recentScreenTimes.collectAsState()
    val appUsages by viewModel.todayAppUsages.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val todayStat = screenTimes.firstOrNull()
    val totalMinutes = todayStat?.totalMinutes ?: 285
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val changePct = todayStat?.changeVsPrevWeekPct ?: 18

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_time_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (settings.language == AppLanguage.BENGALI) "স্ক্রিন টাইম ও পরিসংখ্যান" else "Screen Time & Analytics",
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
            // 1. Overview Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(0.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, com.example.ui.theme.ShapeGeometricCard)
                        .testTag("screen_time_hero_card"),
                    shape = com.example.ui.theme.ShapeGeometricCard,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (settings.language == AppLanguage.BENGALI) "আজকের মোট স্ক্রিন টাইম" else "Today's Screen Time",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = "${hours}h ${minutes}m",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            // Trend badge (+18%)
                            Surface(
                                shape = com.example.ui.theme.ShapeGeometricPill,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Trend up",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+${changePct}%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Bar Visualization
                        val socialMins = todayStat?.socialMinutes ?: 135
                        val prodMins = todayStat?.productivityMinutes ?: 80
                        val entMins = todayStat?.entertainmentMinutes ?: 50
                        val otherMins = todayStat?.otherMinutes ?: 20

                        CategoryStackedBar(
                            social = socialMins,
                            productivity = prodMins,
                            entertainment = entMins,
                            other = otherMins
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CategoryLegendItem(
                                color = MaterialTheme.colorScheme.primary,
                                label = if (settings.language == AppLanguage.BENGALI) "সোশ্যাল" else "Social",
                                time = "${socialMins}m"
                            )
                            CategoryLegendItem(
                                color = MaterialTheme.colorScheme.secondary,
                                label = if (settings.language == AppLanguage.BENGALI) "প্রোডাক্টিভ" else "Prod",
                                time = "${prodMins}m"
                            )
                            CategoryLegendItem(
                                color = MaterialTheme.colorScheme.tertiary,
                                label = if (settings.language == AppLanguage.BENGALI) "মিডিয়া" else "Media",
                                time = "${entMins}m"
                            )
                            CategoryLegendItem(
                                color = MaterialTheme.colorScheme.outline,
                                label = if (settings.language == AppLanguage.BENGALI) "অন্যান্য" else "Other",
                                time = "${otherMins}m"
                            )
                        }
                    }
                }
            }

            // 2. Smart AI Recommendation for Screen Time
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(0.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, com.example.ui.theme.ShapeGeometricCard)
                        .testTag("ai_screen_time_recommendation_card"),
                    shape = com.example.ui.theme.ShapeGeometricCard,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(com.example.ui.theme.ShapeGeometricSubtle)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(1.dp, MaterialTheme.colorScheme.outline, com.example.ui.theme.ShapeGeometricSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Tip",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (settings.language == AppLanguage.BENGALI)
                                    "AI স্মার্ট পরামর্শ"
                                else
                                    "AI Smart Recommendation",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (settings.language == AppLanguage.BENGALI)
                                    "গত ৭ দিনে তোমার screen time বেড়েছে। রাতে নির্দিষ্ট সময়ের পর social media limit করার চেষ্টা করতে পারো।"
                                else
                                    "Over the past 7 days, your screen time increased by 18%. Try limiting social media after 9:30 PM to improve sleep.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }

            // 3. Activity & Notification Telemetry (Privacy Safe)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TelemetryMiniCard(
                        title = if (settings.language == AppLanguage.BENGALI) "ডিভাইস পিক-আপ" else "Pickups Today",
                        value = "${todayStat?.pickupCount ?: 68}",
                        subtext = if (settings.language == AppLanguage.BENGALI) "প্রতি ঘণ্টায় ৩.৪ বার" else "~3.4 per hour",
                        icon = Icons.Outlined.TouchApp,
                        modifier = Modifier.weight(1f)
                    )

                    TelemetryMiniCard(
                        title = if (settings.language == AppLanguage.BENGALI) "নোটিফিকেশন" else "Notifications",
                        value = "${todayStat?.notificationCount ?: 142}",
                        subtext = if (settings.language == AppLanguage.BENGALI) "গোপনীয়তা সুরক্ষিত" else "Privacy Safe",
                        icon = Icons.Outlined.Notifications,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. App Usage Breakdown List
            item {
                Text(
                    text = if (settings.language == AppLanguage.BENGALI) "অ্যাপ ব্যবহারের বিবরণ" else "App Usage Breakdown",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(appUsages, key = { it.id }) { app ->
                AppUsageRowItem(appUsage = app)
            }
        }
    }
}

@Composable
private fun CategoryStackedBar(
    social: Int,
    productivity: Int,
    entertainment: Int,
    other: Int
) {
    val total = (social + productivity + entertainment + other).toFloat().coerceAtLeast(1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(com.example.ui.theme.ShapeSharp)
            .border(1.dp, MaterialTheme.colorScheme.outline, com.example.ui.theme.ShapeSharp)
    ) {
        Box(
            modifier = Modifier
                .weight(social / total)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary)
        )
        Box(
            modifier = Modifier
                .weight(productivity / total)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.secondary)
        )
        Box(
            modifier = Modifier
                .weight(entertainment / total)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.tertiary)
        )
        Box(
            modifier = Modifier
                .weight(other / total)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outline)
        )
    }
}

@Composable
private fun CategoryLegendItem(color: Color, label: String, time: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(com.example.ui.theme.ShapeSharp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
            )
            Text(
                text = time,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun TelemetryMiniCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(0.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, com.example.ui.theme.ShapeGeometricCard),
        shape = com.example.ui.theme.ShapeGeometricCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun AppUsageRowItem(appUsage: AppUsageStatEntity) {
    Surface(
        shape = com.example.ui.theme.ShapeGeometricSubtle,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(com.example.ui.theme.ShapeGeometricSubtle)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, com.example.ui.theme.ShapeGeometricSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (appUsage.category) {
                        "Social" -> Icons.Default.Share
                        "Productivity" -> Icons.Default.CheckCircle
                        "Entertainment" -> Icons.Default.PlayCircle
                        else -> Icons.Default.Book
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appUsage.appName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = appUsage.category,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Text(
                text = "${appUsage.usageMinutes} mins",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

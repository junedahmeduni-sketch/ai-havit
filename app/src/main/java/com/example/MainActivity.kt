package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.components.FloatingAiButton
import com.example.ui.screens.*
import com.example.ui.theme.HabitTrackTheme
import com.example.ui.viewmodel.AiChatViewModel
import com.example.ui.viewmodel.MainViewModel

enum class AppTab(
    val titleEn: String,
    val titleBn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", "হোম", Icons.Filled.Home, Icons.Outlined.Home),
    HABITS("Habits", "অভ্যাস", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircleOutline),
    LIFE_HUB("Life Hub", "লাইফ হাব", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    STATS("Stats", "স্ক্রিন টাইম", Icons.Filled.Analytics, Icons.Outlined.Analytics),
    AI_ASSISTANT("AI Coach", "AI সহকারী", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    SETTINGS("Settings", "সেটিংস", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val aiChatViewModel: AiChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitTrackTheme {
                val isUserLoggedIn by mainViewModel.isUserLoggedIn.collectAsState()

                if (!isUserLoggedIn) {
                    AuthScreen(
                        viewModel = mainViewModel,
                        onAuthSuccess = { }
                    )
                } else {
                    MainAppScreen(
                        mainViewModel = mainViewModel,
                        aiChatViewModel = aiChatViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppScreen(
    mainViewModel: MainViewModel,
    aiChatViewModel: AiChatViewModel
) {
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    val settings by mainViewModel.settings.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_scaffold"),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    ),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                AppTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val label = if (settings.language == AppLanguage.BENGALI) tab.titleBn else tab.titleEn
                    val isAiTab = tab == AppTab.AI_ASSISTANT

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            if (isAiTab) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = label,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(com.example.ui.theme.ShapeSharp)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = label
                                )
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 9.5.sp
                                ),
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            // Floating AI button present on Home and Life Hub screens
            if ((selectedTab == AppTab.HOME || selectedTab == AppTab.LIFE_HUB) && settings.aiEnabled) {
                FloatingAiButton(
                    onClick = { selectedTab = AppTab.AI_ASSISTANT }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.HOME -> HomeScreen(
                    viewModel = mainViewModel,
                    onOpenAiChat = { selectedTab = AppTab.AI_ASSISTANT },
                    onNavigateToHabits = { selectedTab = AppTab.HABITS },
                    onNavigateToStats = { selectedTab = AppTab.STATS }
                )

                AppTab.HABITS -> HabitsScreen(
                    viewModel = mainViewModel,
                    onOpenAiChat = { selectedTab = AppTab.AI_ASSISTANT }
                )

                AppTab.LIFE_HUB -> LifeHubScreen(
                    viewModel = mainViewModel,
                    onOpenAiChat = { selectedTab = AppTab.AI_ASSISTANT }
                )

                AppTab.STATS -> ScreenTimeScreen(
                    viewModel = mainViewModel,
                    onOpenAiChat = { selectedTab = AppTab.AI_ASSISTANT }
                )

                AppTab.AI_ASSISTANT -> AiChatScreen(
                    viewModel = aiChatViewModel,
                    onLanguageToggle = { mainViewModel.setLanguage(it) }
                )

                AppTab.SETTINGS -> SettingsScreen(
                    viewModel = mainViewModel
                )
            }
        }
    }
}

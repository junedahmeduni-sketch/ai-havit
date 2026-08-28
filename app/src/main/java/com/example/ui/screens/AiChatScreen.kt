package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.data.model.AiChatMessageEntity
import com.example.data.model.AppLanguage
import com.example.ui.components.LanguageToggle
import com.example.ui.viewmodel.AiChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel,
    onLanguageToggle: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val displayedTypingText by viewModel.displayedTypingText.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val suggestedQuestions = remember(settings.language) {
        viewModel.getSuggestedQuestions(settings.language)
    }

    LaunchedEffect(chatMessages.size, displayedTypingText) {
        if (chatMessages.isNotEmpty() || displayedTypingText != null) {
            coroutineScope.launch {
                listState.animateScrollToItem(
                    index = maxOf(0, chatMessages.size + (if (displayedTypingText != null || isLoading) 1 else 0) - 1)
                )
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_chat_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini AI",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (settings.language == AppLanguage.BENGALI) "HabitTrack AI কোচ" else "HabitTrack AI Coach",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (settings.language == AppLanguage.BENGALI) "Gemini 2.5 Flash • বাংলা ও English" else "Gemini 2.5 Flash • Bangla & English",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    LanguageToggle(
                        currentLanguage = settings.language,
                        onLanguageSelected = onLanguageToggle
                    )

                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.testTag("clear_chat_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Clear Chat History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Privacy Header Banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Privacy",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (settings.language == AppLanguage.BENGALI)
                            "🔒 গোপনীয়তা সুরক্ষিত: শুধু habit ও স্ক্রিন টাইম পরিসংখ্যান ব্যবহার করা হয়।"
                        else
                            "🔒 Privacy Protected: Only aggregate habit & screen time stats are used.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Message History List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Empty state greeting if no messages yet
                if (chatMessages.isEmpty() && displayedTypingText == null && !isLoading) {
                    item {
                        WelcomeAiCard(
                            language = settings.language,
                            onQuickQuestion = { viewModel.sendMessage(it) }
                        )
                    }
                }

                items(chatMessages, key = { it.id }) { message ->
                    ChatMessageBubble(
                        message = message,
                        currentLanguage = settings.language
                    )
                }

                // Streaming / typing animation message bubble
                if (displayedTypingText != null) {
                    item {
                        AssistantTypingBubble(
                            text = displayedTypingText ?: "",
                            isComplete = false
                        )
                    }
                } else if (isLoading) {
                    item {
                        AiThinkingBubble(language = settings.language)
                    }
                }
            }

            // Suggested Question Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("suggested_questions_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestedQuestions) { question ->
                    Surface(
                        modifier = Modifier
                            .clip(com.example.ui.theme.ShapeGeometricPill)
                            .border(1.dp, MaterialTheme.colorScheme.outline, com.example.ui.theme.ShapeGeometricPill)
                            .clickable { viewModel.sendMessage(question) }
                            .testTag("chip_${question.take(10)}"),
                        shape = com.example.ui.theme.ShapeGeometricPill,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Error banner if any
            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { viewModel.onInputTextChanged(it) },
                        modifier = Modifier
                            .weight(1f)
                            .clip(com.example.ui.theme.ShapeGeometricSubtle)
                            .border(1.dp, MaterialTheme.colorScheme.outline, com.example.ui.theme.ShapeGeometricSubtle)
                            .testTag("ai_chat_input_field"),
                        placeholder = {
                            Text(
                                text = if (settings.language == AppLanguage.BENGALI)
                                    "আপনার Habit কোচকে জিজ্ঞাসা করুন..."
                                else
                                    "Ask your Habit Coach...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                            )
                        },
                        shape = com.example.ui.theme.ShapeGeometricSubtle,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = { viewModel.sendMessage() },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(com.example.ui.theme.ShapeGeometricSubtle)
                            .background(
                                if (inputText.isNotBlank() && !isLoading)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                com.example.ui.theme.ShapeGeometricSubtle
                            )
                            .testTag("ai_send_message_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isLoading)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            shape = com.example.ui.theme.ShapeGeometricCard,
            title = {
                Text(
                    text = if (settings.language == AppLanguage.BENGALI) "চ্যাট হিস্ট্রি মুছবেন?" else "Clear Chat History?"
                )
            },
            text = {
                Text(
                    text = if (settings.language == AppLanguage.BENGALI)
                        "আপনার পূর্ববর্তী সকল AI আলাপচারিতা মুছে ফেলা হবে।"
                    else
                        "All previous AI chat messages will be permanently deleted."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearChat()
                        showClearConfirmDialog = false
                    },
                    modifier = Modifier.testTag("confirm_clear_chat_btn")
                ) {
                    Text(
                        text = if (settings.language == AppLanguage.BENGALI) "মুছুন" else "Clear",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(text = if (settings.language == AppLanguage.BENGALI) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun WelcomeAiCard(
    language: AppLanguage,
    onQuickQuestion: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .shadow(0.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, com.example.ui.theme.ShapeGeometricCard),
        shape = com.example.ui.theme.ShapeGeometricCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(com.example.ui.theme.ShapeGeometricSubtle)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.outline, com.example.ui.theme.ShapeGeometricSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Assistant",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (language == AppLanguage.BENGALI)
                    "HabitTrack AI পার্সোনাল কোচে স্বাগতম!"
                else
                    "Welcome to HabitTrack AI Coach!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (language == AppLanguage.BENGALI)
                    "আমি তোমার হ্যাবিট কমপ্লিশন, স্ট্রিক, মিসড হ্যাবিট এবং স্ক্রিন টাইম বিশ্লেষণ করে কার্যকর পরামর্শ দিতে প্রস্তুত।"
                else
                    "I analyze your habit completions, streaks, missed habits, and screen time trends to provide actionable productivity coaching.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: AiChatMessageEntity,
    currentLanguage: AppLanguage
) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(com.example.ui.theme.ShapeGeometricSubtle)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(1.dp, MaterialTheme.colorScheme.outline, com.example.ui.theme.ShapeGeometricSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = com.example.ui.theme.ShapeGeometricCard,
            color = if (isUser)
                MaterialTheme.colorScheme.primary
            else if (message.isError)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surface,
            border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 0.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isUser)
                        MaterialTheme.colorScheme.onPrimary
                    else if (message.isError)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                ),
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun AssistantTypingBubble(
    text: String,
    isComplete: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(com.example.ui.theme.ShapeGeometricSubtle)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(1.dp, MaterialTheme.colorScheme.outline, com.example.ui.theme.ShapeGeometricSubtle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = com.example.ui.theme.ShapeGeometricCard,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 0.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                ),
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun AiThinkingBubble(language: AppLanguage) {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")
    val dotAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "dot1"
    )
    val dotAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200), RepeatMode.Reverse), label = "dot2"
    )
    val dotAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400), RepeatMode.Reverse), label = "dot3"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(com.example.ui.theme.ShapeGeometricSubtle)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(1.dp, MaterialTheme.colorScheme.outline, com.example.ui.theme.ShapeGeometricSubtle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = com.example.ui.theme.ShapeGeometricCard,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(6.dp).clip(com.example.ui.theme.ShapeSharp).background(MaterialTheme.colorScheme.primary.copy(alpha = dotAlpha1)))
                Box(modifier = Modifier.size(6.dp).clip(com.example.ui.theme.ShapeSharp).background(MaterialTheme.colorScheme.primary.copy(alpha = dotAlpha2)))
                Box(modifier = Modifier.size(6.dp).clip(com.example.ui.theme.ShapeSharp).background(MaterialTheme.colorScheme.primary.copy(alpha = dotAlpha3)))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == AppLanguage.BENGALI) "AI ভাবছে..." else "AI is analyzing...",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
    }
}

package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AiChatMessageEntity
import com.example.data.model.AppLanguage
import com.example.data.repository.AiAssistantRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AiChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val settingsRepo = SettingsRepository(application)
    private val aiRepository = AiAssistantRepository(
        database.aiChatDao(),
        database.aiAdviceDao(),
        database.habitDao(),
        database.habitCompletionDao(),
        database.screenTimeDao(),
        settingsRepo
    )

    val chatMessages: StateFlow<List<AiChatMessageEntity>> =
        aiRepository.chatMessages
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings = settingsRepo.settings

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _displayedTypingText = MutableStateFlow<String?>(null)
    val displayedTypingText: StateFlow<String?> = _displayedTypingText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun sendMessage(customText: String? = null) {
        val query = (customText ?: _inputText.value).trim()
        if (query.isBlank() || _isLoading.value) return

        _inputText.value = ""
        _errorMessage.value = null
        val currentLang = settings.value.language

        viewModelScope.launch {
            val isBangla = aiRepository.isBengaliQuery(query, currentLang)
            val effectiveLang = if (isBangla) AppLanguage.BENGALI else currentLang

            // Insert user message in DB
            aiRepository.saveUserMessage(query, effectiveLang)

            _isLoading.value = true
            try {
                // Call Gemini / smart coach
                val response = aiRepository.askAssistant(query, effectiveLang)

                // Typing animation effect for realistic conversational feel
                animateTyping(response, effectiveLang)
            } catch (e: Exception) {
                val err = if (effectiveLang == AppLanguage.BENGALI)
                    "দুঃখিত, সংযোগে সমস্যা হয়েছে। অনুগ্রহ করে আবার চেষ্টা করো।"
                else
                    "Sorry, an error occurred while connecting to AI. Please try again."
                aiRepository.saveAssistantMessage(err, effectiveLang, isError = true)
                _errorMessage.value = err
            } finally {
                _isLoading.value = false
                _displayedTypingText.value = null
            }
        }
    }

    private suspend fun animateTyping(fullText: String, language: AppLanguage) {
        // Quick typing effect for UX delight
        val words = fullText.split(" ")
        val sb = StringBuilder()
        for (w in words) {
            sb.append(w).append(" ")
            _displayedTypingText.value = sb.toString()
            delay(25)
        }
        _displayedTypingText.value = null
        aiRepository.saveAssistantMessage(fullText, language)
    }

    fun clearChat() {
        viewModelScope.launch {
            aiRepository.clearChatHistory()
            _errorMessage.value = null
        }
    }

    fun getSuggestedQuestions(language: AppLanguage): List<String> {
        return if (language == AppLanguage.BENGALI) {
            listOf(
                "আজ আমার progress কেমন? 📊",
                "ক্যাটাগরি ভিত্তিক বিশ্লেষণ 📑",
                "স্বাস্থ্য ও ঘুমের পরামর্শ 🩺",
                "কাজের প্রোডাক্টিভিটি কোচিং 💼",
                "ব্যক্তিগত ও মাইন্ডফুলনেস 🧘",
                "কোন habit-এ সমস্যা হচ্ছে? ⚠️"
            )
        } else {
            listOf(
                "How is my progress today? 📊",
                "Category balance breakdown 📑",
                "Health & Sleep coaching 🩺",
                "Work & Deep focus coaching 💼",
                "Personal mindfulness advice 🧘",
                "Which habit needs improvement? ⚠️"
            )
        }
    }
}

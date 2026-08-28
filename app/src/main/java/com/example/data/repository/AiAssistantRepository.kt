package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.AiAdviceDao
import com.example.data.local.AiChatDao
import com.example.data.local.HabitCompletionDao
import com.example.data.local.HabitDao
import com.example.data.local.ScreenTimeDao
import com.example.data.model.*
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AiAssistantRepository(
    private val aiChatDao: AiChatDao,
    private val aiAdviceDao: AiAdviceDao,
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao,
    private val screenTimeDao: ScreenTimeDao,
    private val settingsRepository: SettingsRepository
) {
    val chatMessages: Flow<List<AiChatMessageEntity>> = aiChatDao.getAllMessages()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    fun getTodayDate(): String = dateFormat.format(Date())

    suspend fun clearChatHistory() {
        aiChatDao.clearAllMessages()
    }

    suspend fun clearPersonalizedCache() {
        aiAdviceDao.clearAllAdvice()
    }

    fun isBengaliQuery(text: String, preferredLanguage: AppLanguage): Boolean {
        if (preferredLanguage == AppLanguage.BENGALI) return true
        if (text.isBlank()) return false
        // 1. Bengali Unicode check
        if (text.any { it in '\u0980'..'\u09FF' }) return true

        // 2. Banglish, Avro phonetics, and colloquial Bangladeshi phrases check
        val lower = text.lowercase(Locale.ROOT)
        val normalized = lower.replace(Regex("[^a-z0-9\\s]"), " ")
        val words = normalized.split(Regex("\\s+")).filter { it.isNotBlank() }.toSet()

        val banglishVocabulary = setOf(
            "amar", "amr", "amader", "amdr", "amake", "amk", "tumi", "tmi", "tomar", "tmr",
            "tomake", "tmk", "apni", "apnar", "apnake", "tui", "tor", "toke", "she", "tar", "take",
            "mon", "moner", "monkharap", "valo", "bhalo", "vlo", "nai", "nei", "khub", "khb",
            "kharap", "khrap", "kemon", "kmn", "acho", "aso", "asen", "ache", "ase", "ace",
            "achi", "asi", "hobe", "hbe", "hobena", "honi", "korbo", "korechi", "korsi", "koresi",
            "koreche", "korece", "korte", "kore", "kor", "koro", "korun", "korchi", "korlam", "korchis",
            "bolte", "blte", "bolo", "bolun", "bolchi", "parba", "parbe", "parbo", "parben", "pari",
            "ki", "kisu", "kichu", "hoye", "hoyese", "hoyeche", "hoise", "hyce", "gese", "geche",
            "gece", "ghese", "ajke", "aj", "ajk", "aajke", "shokal", "sokal", "rat", "rate",
            "raate", "dupur", "bikel", "shondha", "sondha", "din", "shomoy", "somoy", "ghum",
            "ghuma", "pani", "khabo", "kheyechi", "biyayam", "byayam", "boi", "pora", "porechi",
            "kaj", "kaaj", "kam", "shuru", "suru", "churu", "kori", "chai", "dorkar", "drkar",
            "lagbe", "lagse", "lagche", "keno", "kno", "kivabe", "kibhabe", "kmne", "kemne",
            "kon", "kokhon", "kothay", "koi", "jani", "janina", "valolage", "valolagtasena",
            "dhonnobad", "thik", "ekhon", "akhon", "pore", "age", "protidin", "shob", "sob",
            "kintu", "karon", "tai", "tobe", "noy", "na", "obosshoy", "oboshshoy", "shathe",
            "sathe", "sate", "bujhlam", "dekhi", "dekhbo", "shune", "khobor", "obostha",
            "dite", "dao", "den", "dibo", "debo", "nibo", "nebo", "bujhte", "shikte", "shikhte",
            "chinta", "cintha", "shomossha", "somossa", "somossha", "sesh", "shesh", "rakhte",
            "dhorbo", "shorir", "sorir", "klanto", "disturbed", "tension", "routine", "rutin",
            "daw", "tips", "banao", "suggest", "koro", "uchit", "uchit?", "korbo?"
        )

        if (words.any { it in banglishVocabulary }) return true

        val compositePhrases = listOf(
            "mon valo", "mon bhalo", "mon kharap", "valo nai", "valo nei", "bhalo nai", "bhalo nei",
            "hoye gese", "hoye geche", "hoise", "break hoye", "miss hoye", "miss hoise", "miss gese",
            "kivabe", "kibhabe", "kemne", "ki korbo", "ki vabe", "ki bhabe", "korte par", "dite par",
            "bolte par", "help koro", "routine dao", "routine ban", "shuru kor", "suru kor",
            "streak break", "habit ta", "habit miss", "valo lagche na", "valo lagena", "valolagtasena",
            "kemon acho", "kemon asen", "ki khobor", "ki obostha", "shorir kharap", "sorir kharap",
            "tips daw", "tips dao", "ki kora uchit", "ki korbo"
        )

        return compositePhrases.any { lower.contains(it) }
    }

    suspend fun saveUserMessage(content: String, language: AppLanguage): Long {
        val isBangla = isBengaliQuery(content, language)
        return aiChatDao.insertMessage(
            AiChatMessageEntity(
                role = "user",
                content = content,
                language = if (isBangla) AppLanguage.BENGALI.code else language.code,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveAssistantMessage(content: String, language: AppLanguage, isError: Boolean = false): Long {
        val isBangla = isBengaliQuery(content, language)
        return aiChatDao.insertMessage(
            AiChatMessageEntity(
                role = "assistant",
                content = content,
                language = if (isBangla) AppLanguage.BENGALI.code else language.code,
                timestamp = System.currentTimeMillis(),
                isError = isError
            )
        )
    }

    suspend fun getWhatShouldIDoNow(language: AppLanguage): String = withContext(Dispatchers.IO) {
        val today = getTodayDate()
        val statsContext = gatherAggregatedContext(today)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isBangla = language == AppLanguage.BENGALI

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are AI Coach in HabitTrack AI. The user clicked "What should I do now?".
                    Current time: $currentHour:00.
                    User context:
                    - Habit completion today: ${statsContext.completedHabits}/${statsContext.totalHabits}
                    - Incomplete habits: ${statsContext.strugglingHabit}
                    - Screen time: ${statsContext.screenTimeFormatted}
                    - Goals: ${statsContext.userGoals}
                    
                    Provide 1-2 short, highly motivating, concrete actionable next steps that fit the current time of day.
                    ${if (isBangla) "Reply in natural, warm Bengali (বাংলা Unicode) using friendly 'তুমি' tone." else "Reply in crisp, motivating English."}
                    Do not show technical model details.
                """.trimIndent()

                val result = callGeminiForPrompt(
                    systemInstruction = "You are AI Coach, an empathetic personal habit and daily planner coach.",
                    userPrompt = prompt
                )
                if (result.isNotBlank()) return@withContext result
            } catch (_: Exception) {}
        }

        // Smart Local Response
        return@withContext if (isBangla) {
            when (currentHour) {
                in 5..10 -> "এখন সকালের ফ্রেশ এনার্জি কাজে লাগানোর সেরা সময়! ১ গ্লাস পানি পান করো এবং ২০ মিনিট সকালের হালকা স্ট্রেচিং বা রিডিং সম্পন্ন করো।"
                in 11..16 -> "এখন গভীর মনোযোগ দিয়ে ২০-২৫ মিনিটের একটি স্টাডি বা কাজের স্প্রিন্ট করো। এরপর এক গ্লাস পানি খেয়ে চোখকে ৫ মিনিটের বিশ্রাম দাও।"
                in 17..20 -> "বিকেল ও সন্ধ্যার সময়! শরীরকে সতেজ রাখতে ২০ মিনিট হাঁটতে পারো বা শরীরচর্চা করো, এরপর পানি পান করে আজকের বাকি habit-গুলো চেক করো।"
                else -> "রাত হয়ে গেছে! ভালো ঘুমের প্রস্তুতি নাও। ফোন দূরে রেখে ১৫ মিনিট বই পড়ো এবং প্রশান্তির সাথে ঘুমাতে যাও। 😴"
            }
        } else {
            when (currentHour) {
                in 5..10 -> "Start your day with high vitality! Drink a glass of water and complete your morning light stretch or 20-minute reading session."
                in 11..16 -> "Power through a 25-minute deep focus study or work sprint. Follow up with a glass of water and 5 minutes of mindful screen-rest."
                in 17..20 -> "Evening vitality check: Take a brisk 20-minute walk or workout, hydrate, and review your today's remaining habits."
                else -> "Wind-down time: Put your phone away, read 15 minutes of a good book, and prepare for restorative sleep before 11:00 PM. 😴"
            }
        }
    }

    suspend fun getDailyAdvice(forceRefresh: Boolean = false): Pair<String, String> = withContext(Dispatchers.IO) {
        val today = getTodayDate()
        val cacheKey = "daily_$today"
        val cached = aiAdviceDao.getAdviceByKey(cacheKey)

        if (!forceRefresh && cached != null && cached.contentEn.isNotBlank()) {
            return@withContext Pair(cached.contentEn, cached.contentBn)
        }

        val statsContext = gatherAggregatedContext(today)
        val apiKey = BuildConfig.GEMINI_API_KEY

        var adviceEn = ""
        var adviceBn = ""

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                adviceEn = callGeminiForPrompt(
                    systemInstruction = "You are AI Coach, a supportive habit coach. Write a concise, 1-2 sentence motivating daily advice based on the user's habit progress, categories (${statsContext.categoryBreakdownTextEn}), and screen time.",
                    userPrompt = "Generate a short motivating daily advice in English that references my habit balance."
                )

                adviceBn = callGeminiForPrompt(
                    systemInstruction = "You are AI Coach, a supportive habit coach. Write a concise, 1-2 sentence motivating daily advice in natural Bengali (বাংলা) based on user's habit progress.",
                    userPrompt = "Generate a short motivating daily advice in natural Bengali (বাংলা) that references my habit balance."
                )
            } catch (_: Exception) {}
        }

        if (adviceEn.isBlank() || adviceBn.isBlank()) {
            val totalHabits = statsContext.totalHabits
            val completedHabits = statsContext.completedHabits
            val remaining = totalHabits - completedHabits

            adviceEn = if (completedHabits == totalHabits && totalHabits > 0) {
                "Incredible job! All $totalHabits habits are completed today. Celebrate this balanced achievement!"
            } else if (completedHabits > 0) {
                "You completed $completedHabits of $totalHabits habits today. Great momentum in ${statsContext.topCategory}! Finish your remaining $remaining habits before bedtime."
            } else {
                "Welcome to a fresh day! Pick your first habit from ${statsContext.topCategory} or Health to build positive momentum."
            }

            adviceBn = if (completedHabits == totalHabits && totalHabits > 0) {
                "অসাধারণ! আজ তুমি সব ${totalHabits}টি habit সম্পন্ন করেছ। প্রতিটি ক্যাটাগরিতে চমৎকার ব্যালেন্স বজায় রেখেছ!"
            } else if (completedHabits > 0) {
                "আজ তোমার ${totalHabits}টির মধ্যে ${toBengaliDigits(completedHabits)}টি habit সম্পন্ন হয়েছে। ${statsContext.topCategoryBn}-এ খুব ভালো অগ্রগতি! আজ রাতে ঘুমানোর আগে বাকি ${toBengaliDigits(remaining)}টি habit শেষ করার চেষ্টা করো।"
            } else {
                "একটি নতুন দিনের শুভেচ্ছা! ${statsContext.topCategoryBn} বা স্বাস্থ্য ক্যাটাগরি থেকে প্রথম habit শুরু করে দিনের শুভ সূচনা করো।"
            }
        }

        val entity = AiAdviceCacheEntity(
            key = cacheKey,
            adviceType = "daily",
            contentEn = adviceEn,
            contentBn = adviceBn
        )
        aiAdviceDao.saveAdvice(entity)

        Pair(adviceEn, adviceBn)
    }

    suspend fun getWeeklyReview(forceRefresh: Boolean = false): Pair<String, String> = withContext(Dispatchers.IO) {
        val today = getTodayDate()
        val cacheKey = "weekly_${today.substring(0, minOf(7, today.length))}"
        val cached = aiAdviceDao.getAdviceByKey(cacheKey)

        if (!forceRefresh && cached != null && cached.contentEn.isNotBlank()) {
            return@withContext Pair(cached.contentEn, cached.contentBn)
        }

        val statsContext = gatherAggregatedContext(today)
        val apiKey = BuildConfig.GEMINI_API_KEY

        var reviewEn = ""
        var reviewBn = ""

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                reviewEn = callGeminiForPrompt(
                    systemInstruction = "You are AI Coach weekly reviewer. Provide a concise, structured review covering: Category breakdown, Best habits & streaks, Improvement areas, Screen-time trend, and Actionable suggestions for next week.",
                    userPrompt = "Generate a comprehensive weekly review in English with category analysis."
                )

                reviewBn = callGeminiForPrompt(
                    systemInstruction = "You are AI Coach weekly reviewer. Provide a concise, structured review in natural Bengali (বাংলা) covering: Category breakdown, Best habits & streaks, Improvement areas, Screen-time trend, and Actionable suggestions for next week.",
                    userPrompt = "Generate a comprehensive weekly review in natural Bengali (বাংলা) with category analysis."
                )
            } catch (_: Exception) {}
        }

        if (reviewEn.isBlank() || reviewBn.isBlank()) {
            reviewEn = "This week, your top-performing domain was ${statsContext.topCategory} (${statsContext.bestStreakHabit}, ${statsContext.bestStreakDays}-day streak 🔥). Work and Learning are at 100%. Next week, limit evening screen time by 30 mins and set earlier reminders for ${statsContext.strugglingHabit}."
            reviewBn = "এই সপ্তাহে তোমার সেরা পারফরম্যান্স ছিল ${statsContext.topCategoryBn}-এ (${statsContext.bestStreakHabit}, ${toBengaliDigits(statsContext.bestStreakDays)} দিনের streak 🔥)। তবে ${statsContext.laggingCategoryBn}-এ মনোযোগ দেওয়া প্রয়োজন। আগামী সপ্তাহে রাতে স্ক্রিন টাইম কমিয়ে ${statsContext.strugglingHabitBn}-এর জন্য নির্দিষ্ট রুটিন তৈরি করো।"
        }

        val entity = AiAdviceCacheEntity(
            key = cacheKey,
            adviceType = "weekly",
            contentEn = reviewEn,
            contentBn = reviewBn
        )
        aiAdviceDao.saveAdvice(entity)

        Pair(reviewEn, reviewBn)
    }

    suspend fun getCategoryCoaching(category: HabitCategory, language: AppLanguage): Pair<String, String> = withContext(Dispatchers.IO) {
        val today = getTodayDate()
        val statsContext = gatherAggregatedContext(today)
        val catProgress = statsContext.categoryProgresses.find { it.category == category }
        val apiKey = BuildConfig.GEMINI_API_KEY

        var coachingEn = ""
        var coachingBn = ""

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                coachingEn = callGeminiForPrompt(
                    systemInstruction = "You are AI Coach coaching the user specifically on '${category.titleEn}' habits (${category.coachingFocusEn}). Provide a short, actionable tip in English.",
                    userPrompt = "Category: ${category.titleEn}\nCategory habits: ${catProgress?.habitsSummary ?: "N/A"}"
                )

                coachingBn = callGeminiForPrompt(
                    systemInstruction = "You are AI Coach coaching the user specifically on '${category.titleBn}' habits (${category.coachingFocusBn}). Provide a short, actionable tip in natural Bengali (বাংলা).",
                    userPrompt = "Category: ${category.titleBn}\nCategory habits: ${catProgress?.habitsSummary ?: "N/A"}"
                )
            } catch (_: Exception) {}
        }

        if (coachingEn.isBlank() || coachingBn.isBlank()) {
            val (en, bn) = when (category) {
                HabitCategory.HEALTH -> Pair(
                    "Health Coaching: Hydration is on point (9-day streak), but sleep consistency needs care. Power down devices 30 minutes before bed.",
                    "স্বাস্থ্য কোচিং: পর্যাপ্ত পানি পানের অভ্যাস খুব ভালো (৯ দিনের streak), কিন্তু ঘুমানোর নিয়মে উন্নতি দরকার। ঘুমানোর ৩০ মিনিট আগে ফোন দূরে রাখো।"
                )
                HabitCategory.FITNESS -> Pair(
                    "Fitness Coaching: Morning Workout is active with a 6-day streak! Pair it with your Evening Walk to reach optimal daily movement.",
                    "ফিটনেস কোচিং: সকালের ব্যায়ামে ৬ দিনের streak চলছে! সন্ধ্যার হালকা ২০ মিনিটের হাঁটা তোমার শারীরিক শক্তি ও সতেজতা আরও বাড়িয়ে তুলবে।"
                )
                HabitCategory.STUDY -> Pair(
                    "Study Coaching: Outstanding 21-day streak on Daily Reading 📚! To level up, write a 1-sentence takeaway in your notes after each reading session.",
                    "পড়াশোনা কোচিং: বই পড়ার অভ্যাসে ২১ দিনের অসাধারণ streak 📚! পড়া শেষ করার পর অন্তত একটি শিক্ষণীয় বিষয় নোট করে রাখলে তা দীর্ঘস্থায়ী হবে।"
                )
                HabitCategory.WORK -> Pair(
                    "Work & Career Coaching: Your Deep Work practice has an 11-day streak 🔥! Protect your afternoon focus blocks.",
                    "কাজের কোচিং: তোমার কোডিং ও প্রজেক্ট প্র্যাকটিসে ১১ দিনের ধারাবাহিকতা চমৎকার 🔥! বিকেলে গভীর মনোযোগের সময় নোটিফিকেশন সাইলেন্ট রাখো।"
                )
                HabitCategory.SLEEP -> Pair(
                    "Sleep Coaching: Consistency is key. Keeping a dark, quiet bedroom and sleeping before 11:00 PM restores cognitive focus.",
                    "ঘুম কোচিং: প্রতিদিন নির্দিষ্ট সময়ে ঘুমানো মানসিক শান্তি ও মস্তিষ্কের কার্যক্ষমতা বাড়ায়। রাত ১১টার আগে ঘুমানোর অভ্যাস করো।"
                )
                HabitCategory.WATER -> Pair(
                    "Water & Hydration: Staying hydrated with 8 glasses boosts energy and focus. Keep a bottle on your study desk.",
                    "পানি পান: প্রতিদিন ৮ গ্লাস পানি পান মনোযোগ ও শক্তি বাড়ায়। সবসময় পড়ার টেবিলে একটি পানির বোতল রাখো।"
                )
                HabitCategory.PERSONAL -> Pair(
                    "Personal Growth & Mindfulness: Journaling and meditation help balance stress. Spend 5 minutes reflecting before sleep.",
                    "ব্যক্তিগত ও মাইন্ডফুলনেস: ডায়েরি লেখা ও ধ্যান মানসিক প্রশান্তি দেয়। ঘুমানোর আগে ৫ মিনিট সারাদিনের ভালো মুহূর্ত স্মরণ করো।"
                )
                HabitCategory.FINANCE -> Pair(
                    "Finance Discipline: Track daily expenses and set aside a modest daily savings goal.",
                    "আর্থিক শৃঙ্খলা: দৈনিক খরচ ট্র্যাক করো এবং অপ্রয়োজনীয় কেনাকাটা এড়িয়ে সঞ্চয়ের অভ্যাস গড়ে তোলো।"
                )
                HabitCategory.OTHER -> Pair(
                    "Lifestyle Balance: Small consistent actions create massive long-term transformations.",
                    "লাইফস্টাইল ব্যালেন্স: ছোট ছোট ধারাবাহিক প্রচেষ্টাই জীবনে বিশাল ইতিবাচক পরিবর্তন নিয়ে আসে।"
                )
            }
            coachingEn = en
            coachingBn = bn
        }

        Pair(coachingEn, coachingBn)
    }

    suspend fun breakDownGoal(goalTitle: String, language: AppLanguage): List<String> = withContext(Dispatchers.IO) {
        val isBangla = language == AppLanguage.BENGALI
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = "Break down this life goal into 3-4 concise, actionable milestones: '$goalTitle'. ${if (isBangla) "In Bengali (বাংলা Unicode), one per line without numbering" else "In English, one per line without numbering"}."
                val response = callGeminiForPrompt(
                    systemInstruction = "You are AI Coach. Return only the 3-4 concise action steps, one per line.",
                    userPrompt = prompt
                )
                val lines = response.lines().map { it.trim().removePrefix("-").removePrefix("•").removePrefix("1.").removePrefix("2.").removePrefix("3.").removePrefix("4.").trim() }.filter { it.isNotBlank() }
                if (lines.isNotEmpty()) return@withContext lines
            } catch (_: Exception) {}
        }

        return@withContext if (isBangla) {
            listOf(
                "প্রাথমিক প্রস্তুতি ও দৈনিক ২০ মিনিটের নির্দিষ্ট সময় নির্ধারণ",
                "প্রথম ২ সপ্তাহ প্রতিদিন ট্র্যাকিং ও ধারাবাহিকতা বজায় রাখা",
                "অগ্রগতি পর্যালোচনা ও দুর্বল পয়েন্টগুলোর উন্নয়ন",
                "চূড়ান্ত লক্ষ্য অর্জন ও ধারাবাহিক অভ্যাস ধরে রাখা"
            )
        } else {
            listOf(
                "Set a clear daily 20-minute dedicated time slot",
                "Track progress continuously for the first 2 weeks",
                "Review milestones and optimize weak spots",
                "Reach target benchmark and reinforce consistency"
            )
        }
    }

    suspend fun summarizeJournal(content: String, language: AppLanguage): String = withContext(Dispatchers.IO) {
        val isBangla = language == AppLanguage.BENGALI
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val response = callGeminiForPrompt(
                    systemInstruction = "You are AI Coach. Provide a warm, 1-2 sentence mindful summary and encouragement based on the user's journal entry.",
                    userPrompt = "Journal entry:\n$content\n\n${if (isBangla) "Summarize in warm Bengali (বাংলা Unicode)." else "Summarize in warm English."}"
                )
                if (response.isNotBlank()) return@withContext response
            } catch (_: Exception) {}
        }

        return@withContext if (isBangla) {
            "আজকের দিনটিতে তোমার আন্তরিক প্রচেষ্টা এবং আত্মউন্নয়নের সুন্দর প্রতিফলন দেখা যাচ্ছে। নিজের সাথে এই সংযোগ ধরে রাখো! ✨"
        } else {
            "Your reflection highlights clear mindful progress and self-awareness today. Keep nurturing this positive habit! ✨"
        }
    }

    suspend fun askAssistant(userMessage: String, language: AppLanguage): String = withContext(Dispatchers.IO) {
        val today = getTodayDate()
        val statsContext = gatherAggregatedContext(today)
        val apiKey = BuildConfig.GEMINI_API_KEY

        val isBangla = isBengaliQuery(userMessage, language)
        val effectiveLang = if (isBangla) AppLanguage.BENGALI else language
        val langName = if (effectiveLang == AppLanguage.BENGALI) "Bengali (বাংলা)" else "English"

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemPrompt = """
                    You are AI Coach, a friendly, warm, empathetic, and intelligent personal habit and life coach.
                    Active conversation language: $langName.
                    
                    CRITICAL BANGLA & BANGLISH DIRECTIVES:
                    1. Comprehensive Bengali Understanding:
                       - You MUST understand and fluently converse in:
                         * Bangla Unicode (e.g. 'আজকে আমার মন ভালো নেই', 'তুমি কি আমাকে আজকের একটা routine দিতে পারবে?')
                         * Banglish & Romanized Bengali (e.g. 'Ajke amar mon valo nai', 'amr habit ta break hoye gese', 'ki obstha', 'kemon acho', 'ajke ki korbo', 'routine dite parba?', 'tips daw', 'Amr habit improve korar tips daw')
                         * Avro-style phonetic typing (e.g. 'aajke', 'bhalo', 'shuru', 'korlam')
                         * Informal Bangladeshi Bangla ('তুমি' form of address, warm and friendly)
                         * Mixed Bengali + English sentences (e.g. 'amr habit ta break hoye gese', 'today amar workout miss hoise', 'Ami study korte pari nai because I was tired')
                       - NEVER treat Banglish as unknown or unsupported.
                       - NEVER ask user to repeat or translate.

                    2. Response Language Rules:
                       - When user writes in Bangla, Banglish, Avro, mixed Bengali-English, OR when language is Bengali:
                         * ALWAYS reply in 100% NATURAL, CLEAN BANGLA UNICODE (বাংলা বর্ণমালা).
                         * NEVER reply in English when user writes Bangla or Banglish.
                         * NEVER reply in Banglish. Always use authentic Bengali script.
                       - If and only if user strictly writes pure English, reply in English.

                    3. Empathetic Coaching Behavior:
                       - Low mood/sadness: Respond with empathy ('কী হয়েছে? চাইলে আমাকে বলতে পারো। মন খারাপের কারণটা চাইলে আমার সাথে শেয়ার করতে পারো, আমি তোমার কথা শুনছি।')
                       - Missed habit/streak: Reassure warmly ('চিন্তার কিছু নেই। একটি habit মিস হওয়া মানে তোমার পুরো progress নষ্ট হয়ে যাওয়া নয়। আজ থেকেই আবার শুরু করি। 💪')
                       - Routine request: Give realistic, structured daily routine in clean Bengali.

                    4. Branding Directive (CRITICAL):
                       - NEVER mention Gemini, Google, model names, or API providers.
                       - Call yourself only "AI Coach" or "AI সহকারী".

                    User's Verified Context:
                    - Habit completion today: ${statsContext.completedHabits}/${statsContext.totalHabits} (${statsContext.completionPct}%)
                    - Category Breakdown:
                    ${if (effectiveLang == AppLanguage.BENGALI) statsContext.categoryBreakdownTextBn else statsContext.categoryBreakdownTextEn}
                    - Best streak: ${statsContext.bestStreakHabit} (${statsContext.bestStreakDays} days 🔥)
                    - Screen time: ${statsContext.screenTimeFormatted}
                    - Goals: ${statsContext.userGoals}
                """.trimIndent()

                val response = callGeminiWithHistory(
                    systemInstruction = systemPrompt,
                    currentMessage = userMessage
                )
                if (response.isNotBlank()) {
                    return@withContext response
                }
            } catch (_: Exception) {}
        }

        // Contextual rule-based coach fallback
        return@withContext generateSmartFallbackResponse(userMessage, effectiveLang, statsContext)
    }

    private suspend fun callGeminiWithHistory(systemInstruction: String, currentMessage: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY ?: return ""
        val recentMessages = try {
            aiChatDao.getRecentMessages(8).reversed()
        } catch (_: Exception) {
            emptyList()
        }

        val contentsList = mutableListOf<GeminiContent>()
        for (msg in recentMessages) {
            val role = if (msg.role == "assistant") "model" else "user"
            if (msg.content.isNotBlank() && !msg.isError) {
                contentsList.add(
                    GeminiContent(
                        role = role,
                        parts = listOf(GeminiPart(text = msg.content))
                    )
                )
            }
        }

        if (contentsList.isEmpty() || contentsList.last().role != "user" || contentsList.last().parts.firstOrNull()?.text != currentMessage) {
            contentsList.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = currentMessage))
                )
            )
        }

        val request = GeminiRequest(
            contents = contentsList,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemInstruction))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 800
            )
        )

        val response = GeminiApiClient.service.generateContent(apiKey, request)
        if (response.error != null) {
            throw Exception(response.error.message ?: "AI Coach Error")
        }
        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        return text?.trim() ?: ""
    }

    private suspend fun callGeminiForPrompt(systemInstruction: String, userPrompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY ?: return ""
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = userPrompt))
                )
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemInstruction))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                maxOutputTokens = 800
            )
        )

        val response = GeminiApiClient.service.generateContent(apiKey, request)
        if (response.error != null) {
            throw Exception(response.error.message ?: "AI Coach Error")
        }
        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        return text?.trim() ?: ""
    }

    private suspend fun gatherAggregatedContext(today: String): UserStatsContext {
        val activeHabits = habitDao.getActiveHabitsList()
        val completions = habitCompletionDao.getCompletedListForDate(today)
        val completedIds = completions.map { it.habitId }.toSet()
        val screenTime = screenTimeDao.getScreenTimeForDate(today)
        val goals = settingsRepository.settings.value.userGoals

        val totalHabits = if (activeHabits.isNotEmpty()) activeHabits.size else 6
        val completedHabits = if (activeHabits.isNotEmpty()) activeHabits.count { completedIds.contains(it.id) } else completions.size
        val pct = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0

        val categoryProgresses = HabitCategory.entries.mapNotNull { category ->
            val catHabits = activeHabits.filter { HabitCategory.fromId(it.category) == category }
            if (catHabits.isEmpty()) return@mapNotNull null

            val catTotal = catHabits.size
            val catCompleted = catHabits.count { completedIds.contains(it.id) }
            val catRate = if (catTotal > 0) catCompleted.toFloat() / catTotal.toFloat() else 0f
            val topStreak = catHabits.maxOfOrNull { it.streakCount } ?: 0
            val summary = catHabits.joinToString(", ") {
                "${it.titleEn} (${if (completedIds.contains(it.id)) "Done" else "Pending"})"
            }

            CategoryProgress(
                category = category,
                totalCount = catTotal,
                completedCount = catCompleted,
                completionRate = catRate,
                topStreak = topStreak,
                habitsSummary = summary
            )
        }

        val breakdownEn = StringBuilder()
        val breakdownBn = StringBuilder()
        categoryProgresses.forEach { cp ->
            breakdownEn.append("- ${cp.category.titleEn}: ${cp.completedCount}/${cp.totalCount} completed (${(cp.completionRate * 100).toInt()}%)\n")
            breakdownBn.append("- ${cp.category.titleBn}: ${toBengaliDigits(cp.completedCount)}/${toBengaliDigits(cp.totalCount)} সম্পন্ন (${toBengaliDigits((cp.completionRate * 100).toInt())}%)\n")
        }

        val topCatProgress = categoryProgresses.maxByOrNull { it.completionRate }
        val laggingCatProgress = categoryProgresses.minByOrNull { it.completionRate }

        val topCategory = topCatProgress?.category?.titleEn ?: "Study & Work"
        val topCategoryBn = topCatProgress?.category?.titleBn ?: "পড়াশোনা ও কাজ"
        val laggingCategory = laggingCatProgress?.category?.titleEn ?: "Health"
        val laggingCategoryBn = laggingCatProgress?.category?.titleBn ?: "স্বাস্থ্য"

        val bestStreakHabitEntity = activeHabits.maxByOrNull { it.streakCount }
        val bestStreakHabit = bestStreakHabitEntity?.let { "${it.titleEn} / ${it.titleBn}" } ?: "Reading / বই পড়া"
        val bestStreakDays = bestStreakHabitEntity?.streakCount ?: 21

        val strugglingHabitEntity = activeHabits.find { !completedIds.contains(it.id) && it.streakCount == 0 }
            ?: activeHabits.find { !completedIds.contains(it.id) }
        val strugglingHabit = strugglingHabitEntity?.titleEn ?: "Sleep before 11:00 PM"
        val strugglingHabitBn = strugglingHabitEntity?.titleBn ?: "রাত ১১টার আগে ঘুমানো"

        return UserStatsContext(
            totalHabits = totalHabits,
            completedHabits = completedHabits,
            completionPct = pct,
            categoryProgresses = categoryProgresses,
            categoryBreakdownTextEn = breakdownEn.toString().trim(),
            categoryBreakdownTextBn = breakdownBn.toString().trim(),
            topCategory = topCategory,
            topCategoryBn = topCategoryBn,
            laggingCategory = laggingCategory,
            laggingCategoryBn = laggingCategoryBn,
            bestStreakHabit = bestStreakHabit,
            bestStreakDays = bestStreakDays,
            strugglingHabit = strugglingHabit,
            strugglingHabitBn = strugglingHabitBn,
            screenTimeFormatted = if (screenTime != null) "${screenTime.totalMinutes / 60}h ${screenTime.totalMinutes % 60}m" else "4h 45m",
            screenTimeChangePct = screenTime?.changeVsPrevWeekPct ?: 18,
            socialMinutes = screenTime?.socialMinutes ?: 135,
            pickupCount = screenTime?.pickupCount ?: 68,
            notificationCount = screenTime?.notificationCount ?: 142,
            userGoals = goals
        )
    }

    private fun generateSmartFallbackResponse(
        query: String,
        language: AppLanguage,
        context: UserStatsContext
    ): String {
        val lower = query.lowercase(Locale.getDefault())

        if (language == AppLanguage.BENGALI) {
            return when {
                // Emotion / Low mood / Sadness
                lower.contains("মন") || lower.contains("mon") || lower.contains("sad") || lower.contains("depress") ||
                lower.contains("disturb") || lower.contains("tension") || lower.contains("chinta") || lower.contains("cintha") ||
                lower.contains("kharap") || lower.contains("khrap") || lower.contains("valo nei") || lower.contains("valo nai") ||
                lower.contains("bhalo nei") || lower.contains("bhalo nai") || lower.contains("valolag") || lower.contains("bhalolag") -> {
                    "কী হয়েছে? মন খারাপের কারণটা চাইলে আমার সাথে শেয়ার করতে পারো। আমি তোমার কথা শুনছি।"
                }

                // Habit break / Streak break / Missed
                (lower.contains("break") || lower.contains("miss") || lower.contains("ভেঙে") || lower.contains("ছুটে") || lower.contains("মিস")) &&
                (lower.contains("habit") || lower.contains("streak") || lower.contains("অভ্যাস") || lower.contains("স্ট্রাইক") || lower.contains("hoye") || lower.contains("hoise") || lower.contains("gese") || lower.contains("workout") || lower.contains("kaj") || lower.contains("study")) -> {
                    "চিন্তার কিছু নেই। একটি habit মিস হওয়া মানে তোমার পুরো progress নষ্ট হয়ে যাওয়া নয়। আজ থেকেই আবার শুরু করি। 💪"
                }

                // Routine / Plan request
                lower.contains("routine") || lower.contains("rutin") || lower.contains("schedule") || lower.contains("রুটিন") ||
                (lower.contains("plan") && (lower.contains("dao") || lower.contains("daw") || lower.contains("dite") || lower.contains("দাও") || lower.contains("করো"))) -> {
                    "অবশ্যই! তোমার আজকের জন্য একটি সহজ ও বাস্তবসম্মত routine তৈরি করে দিচ্ছি:\n\n☀️ সকাল (৮:০০ - ৯:০০):\n• ঘুম থেকে উঠে ১ গ্লাস পানি পান ও হালকা স্ট্রেচিং 💧\n• ২০ মিনিট সকালের ব্যায়াম ও ফ্রেশ হওয়া\n\n💼 দুপুর / বিকেল (১০:০০ - ৫:০০):\n• গভীর মনোযোগ দিয়ে মূল কাজ / পড়াশোনা 💻\n• কাজের মাঝে ছোট বিরতি ও চোখের বিশ্রাম\n\n🌙 সন্ধ্যা / রাত (৮:০০ - ১১:০০):\n• ১৫-২০ মিনিট বই পড়া 📚\n• ঘুমানোর ৩০ মিনিট আগে ফোন দূরে রাখা এবং রাত ১১টার মধ্যে ঘুমানো 😴\n\nতুমি চাইলে স্মার্ট ডেইলি প্ল্যানারে এটি সরাসরি সেভ করতে পারো!"
                }

                // What should I do now / কি করা উচিত
                lower.contains("what should i do") || lower.contains("ki kora") || lower.contains("ki korbo") || lower.contains("কী করা উচিত") || lower.contains("এখন কী") -> {
                    "এখন ২০-২৫ মিনিট পড়াশোনা বা কাজে গভীর মনোযোগ দাও 📚। এরপর ১ গ্লাস পানি খেয়ে ১০ মিনিট হালকা হেঁটে শরীর সতেজ করে নাও।"
                }

                // Tips / Habit improvement
                lower.contains("tips") || lower.contains("improve") || lower.contains("পরামর্শ") || lower.contains("টিপস") || lower.contains("উপদেশ") -> {
                    "অভ্যাস উন্নত করার ৩টি সহজ টিপস:\n১. টু-মিনিট রুল: যেকোনো বড় কাজ শুরু করার জন্য প্রথম ২ মিনিট সময় বরাদ্দ করো।\n২. হ্যাবিট স্ট্যাকিং: পুরোনো অভ্যাসের সাথে নতুন অভ্যাস যুক্ত করো (যেমন: চা খাওয়ার পর ১০ মিনিট বই পড়া)।\n৩. দৃশ্যমান ট্র্যাকিং: প্রতিদিন ট্র্যাকার অ্যাপে চেকমার্ক দিয়ে মোমেন্টাম ধরে রাখো! 🔥"
                }

                // Progress
                lower.contains("progress") || lower.contains("আজ") || lower.contains("আজকে") || lower.contains("কেমন") || lower.contains("ajke") || lower.contains("today") -> {
                    "আজ তোমার ${context.totalHabits}টির মধ্যে ${toBengaliDigits(context.completedHabits)}টি habit সম্পন্ন হয়েছে (${toBengaliDigits(context.completionPct)}%)। ${context.topCategoryBn}-এ অগ্রগতি দুর্দান্ত! আজ রাতে ঘুমানোর আগে বাকি ${toBengaliDigits(context.totalHabits - context.completedHabits)}টি habit সম্পন্ন করার চেষ্টা করো।"
                }

                // Motivation
                lower.contains("motivat") || lower.contains("উৎসাহ") || lower.contains("মোটিভেট") || lower.contains("inspire") || lower.contains("parchi na") || lower.contains("পারছি না") -> {
                    "তুমি ইতিমধ্যে ২১ দিনের অসাধারণ streak অর্জন করেছ! ছোট ছোট ধারাবাহিক প্রচেষ্টাই বড় পরিবর্তন আনে। তুমি তোমার লক্ষ্যের দিকে খুব দারুণভাবে এগিয়ে যাচ্ছ, চালিয়ে যাও! 🔥"
                }

                // Greetings
                lower.contains("kemon") || lower.contains("kmn") || lower.contains("কেমন") || lower.contains("khobor") ||
                lower.contains("খবর") || lower.contains("obostha") || lower.contains("অবস্থা") || lower.contains("hello") ||
                lower.contains("hi") || lower.contains("hey") || lower.contains("হাই") || lower.contains("হ্যালো") -> {
                    "আমি ভালো আছি! তোমার আজকের দিনটি কেমন যাচ্ছে? অভ্যাস, রুটিন বা স্ক্রিন টাইম নিয়ে কোনো সাহায্য লাগলে আমাকে জানাও।"
                }

                // Water
                lower.contains("পানি") || lower.contains("water") || lower.contains("pani") -> {
                    "💧 পানি পানের অভ্যাসে তোমার ৯ দিনের streak চমৎকার! প্রতিদিন অন্তত ৮ গ্লাস (২-৩ লিটার) পানি পান করলে শরীর সতেজ থাকে ও কর্মশক্তি বাড়ে।"
                }

                // Sleep
                lower.contains("ঘুম") || lower.contains("sleep") || lower.contains("ghum") || lower.contains("রাত") || lower.contains("rat") -> {
                    "ভালো ঘুমের জন্য:\n১. ঘুমানোর ৩০ মিনিট আগে স্ক্রিন দূরে রাখো 🌙\n২. রুম ঠান্ডা ও শান্ত রাখো\n৩. শোবার আগে ৫ মিনিট গভীর শ্বাস-প্রশ্বাস নাও। রাত ১১টায় ঘুমানোর লক্ষ্য আজই অর্জন করো!"
                }

                else -> {
                    "আমি তোমার AI Life & Habit Coach। আজ তোমার ${toBengaliDigits(context.completedHabits)}/${toBengaliDigits(context.totalHabits)} habit সম্পন্ন হয়েছে। আমি কীভাবে নির্দিষ্ট ক্যাটাগরি, রুটিন বা লক্ষ্যে সাহায্য করতে পারি?"
                }
            }
        } else {
            return when {
                lower.contains("what should i do") || lower.contains("now") -> {
                    "Take a focused 25-minute deep work or study session right now. Afterwards, drink a glass of water and take a 5-minute stretch break."
                }
                lower.contains("plan") || lower.contains("routine") || lower.contains("schedule") -> {
                    "Here is your optimized daily schedule:\n☀️ 08:00 — Wake up & 2 Glasses of Water 💧\n🏃 08:30 — 20-min Morning Workout\n📚 09:30 — Deep Focus Study / Work Sprint\n🥗 13:00 — Healthy Lunch & Rest\n🚶 17:30 — Evening Walk & Hydration\n📖 21:30 — 20 mins Reading\n😴 22:45 — Screen Off & Sleep Wind-down"
                }
                lower.contains("tips") || lower.contains("improve") -> {
                    "3 Powerful Habit Rules:\n1. Two-Minute Rule: Make starting effortless.\n2. Habit Stacking: Attach new habits to existing ones (e.g. read 10 mins right after morning coffee).\n3. Visual Consistency: Check off your streak daily!"
                }
                lower.contains("progress") || lower.contains("today") || lower.contains("how") -> {
                    "You completed ${context.completedHabits} of ${context.totalHabits} habits today (${context.completionPct}%). Great progress in ${context.topCategory}! Finish your remaining habits in ${context.laggingCategory} before bedtime."
                }
                lower.contains("motivate") || lower.contains("inspire") -> {
                    "Every small habit you check off today is a vote for the person you want to become. You have already built an impressive 21-day streak on reading! Stay focused, you're doing great! 🔥"
                }
                else -> {
                    "I am your AI Life & Habit Coach. Today you completed ${context.completedHabits}/${context.totalHabits} habits with top performance in ${context.topCategory}. Ask me for daily routines, habit tips, or goal breakdowns!"
                }
            }
        }
    }

    private fun toBengaliDigits(number: Int): String {
        val bnDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val str = number.toString()
        val sb = StringBuilder()
        for (ch in str) {
            if (ch in '0'..'9') {
                sb.append(bnDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}

data class UserStatsContext(
    val totalHabits: Int,
    val completedHabits: Int,
    val completionPct: Int,
    val categoryProgresses: List<CategoryProgress>,
    val categoryBreakdownTextEn: String,
    val categoryBreakdownTextBn: String,
    val topCategory: String,
    val topCategoryBn: String,
    val laggingCategory: String,
    val laggingCategoryBn: String,
    val bestStreakHabit: String,
    val bestStreakDays: Int,
    val strugglingHabit: String,
    val strugglingHabitBn: String,
    val screenTimeFormatted: String,
    val screenTimeChangePct: Int,
    val socialMinutes: Int,
    val pickupCount: Int,
    val notificationCount: Int,
    val userGoals: String
)

package com.example

import com.example.data.model.AppLanguage
import com.example.data.repository.AiAssistantRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun banglaAndBanglishDetection_isAccurate() {
    val repo = AiAssistantRepository(
      aiChatDao = org.mockito.Mockito.mock(com.example.data.local.AiChatDao::class.java),
      aiAdviceDao = org.mockito.Mockito.mock(com.example.data.local.AiAdviceDao::class.java),
      habitDao = org.mockito.Mockito.mock(com.example.data.local.HabitDao::class.java),
      habitCompletionDao = org.mockito.Mockito.mock(com.example.data.local.HabitCompletionDao::class.java),
      screenTimeDao = org.mockito.Mockito.mock(com.example.data.local.ScreenTimeDao::class.java),
      settingsRepository = org.mockito.Mockito.mock(com.example.data.repository.SettingsRepository::class.java)
    )

    // Bengali Unicode inputs
    assertTrue(repo.isBengaliQuery("আজকে আমার মন ভালো নেই", AppLanguage.ENGLISH))
    assertTrue(repo.isBengaliQuery("তুমি কি আমাকে আজকের একটা routine দিতে পারবে?", AppLanguage.ENGLISH))
    assertTrue(repo.isBengaliQuery("আমার পানি পান করা উচিত?", AppLanguage.ENGLISH))

    // Banglish / Avro inputs
    assertTrue(repo.isBengaliQuery("Ajke amar mon valo nai", AppLanguage.ENGLISH))
    assertTrue(repo.isBengaliQuery("amr habit ta break hoye gese", AppLanguage.ENGLISH))
    assertTrue(repo.isBengaliQuery("kemon acho?", AppLanguage.ENGLISH))
    assertTrue(repo.isBengaliQuery("workout miss hoise", AppLanguage.ENGLISH))
    assertTrue(repo.isBengaliQuery("routine dite parba?", AppLanguage.ENGLISH))

    // Pure English input in English mode
    assertFalse(repo.isBengaliQuery("How is my progress today?", AppLanguage.ENGLISH))
    assertFalse(repo.isBengaliQuery("Give me tips for better sleep hygiene.", AppLanguage.ENGLISH))

    // Any query in Bengali mode
    assertTrue(repo.isBengaliQuery("hello coach", AppLanguage.BENGALI))
  }
}


package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun womenSafetyAlertUsesSafetyGuidanceInsteadOfMedicalOrFloodInstructions() {
    val report = com.example.domain.ai.OfflineGemmaEngine().analyzeSymptoms(
      "someone is following me",
      com.example.data.model.EmergencyCategory.WOMEN_SAFETY,
      1
    )

    assertEquals(com.example.data.model.AlertPriority.P1, report.priority)
    assertTrue(report.actionSteps.any { it.instruction.contains("112") })
    assertTrue(report.detectedSymptomTags.contains("NOT_MEDICAL_TRIAGE"))
    assertFalse(report.actionSteps.any { it.instruction.contains("flood water", ignoreCase = true) })
  }

  @Test
  fun cyberFraudAlertShowsFinancialFraudSteps() {
    val report = com.example.domain.ai.OfflineGemmaEngine().analyzeSymptoms(
      "money taken from my account",
      com.example.data.model.EmergencyCategory.CYBER_FRAUD,
      1
    )

    assertTrue(report.actionSteps.any { it.instruction.contains("1930") })
    assertTrue(report.actionSteps.any { it.instruction.contains("OTP") })
    assertTrue(report.clinicalRationale.contains("does not file a police report"))
  }

}

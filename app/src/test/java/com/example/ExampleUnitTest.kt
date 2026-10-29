package com.example

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

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

  @Test
  fun whistleAnalyzerDetectsLoudTargetTone() {
    val sampleRate = 16_000
    val tone = ShortArray(1024) { index ->
      (6000.0 * sin(2.0 * PI * 3200 * index / sampleRate)).toInt().toShort()
    }

    val analysis = com.example.domain.sensor.WhistleSignalAnalyzer.analyze(tone, tone.size, sampleRate)

    assertTrue(analysis.isWhistle)
    assertEquals(3200, analysis.dominantFrequencyHz)
  }

  @Test
  fun whistleAnalyzerRejectsQuietAudioAndBroadbandNoise() {
    val quiet = ShortArray(1024) { index ->
      (100.0 * sin(2.0 * PI * 3200 * index / 16_000)).toInt().toShort()
    }
    val noise = ShortArray(1024) { Random(indexSeed(it)).nextInt(-7000, 7001).toShort() }

    assertFalse(com.example.domain.sensor.WhistleSignalAnalyzer.analyze(quiet, quiet.size, 16_000).isWhistle)
    assertFalse(com.example.domain.sensor.WhistleSignalAnalyzer.analyze(noise, noise.size, 16_000).isWhistle)
  }

  private fun indexSeed(index: Int) = index * 31 + 7
}

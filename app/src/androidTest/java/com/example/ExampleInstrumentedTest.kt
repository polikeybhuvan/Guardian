package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
  @Test
  fun useAppContext() {
    // Context of the app under test.
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    assertEquals(BuildConfig.APPLICATION_ID, appContext.packageName)
  }

  @Test
  fun bundledGemmaAnswersAnEmergencyQuestionOffline() = runBlocking {
    val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as SosMeshApp
    withTimeout(120_000) {
      while (!app.triageService.isOfflineGemmaInstalled) delay(250)
    }

    val response = app.triageService.processChatInputAsync(
      "Flood water is entering my home. What should I do?"
    )

    assertFalse(response.text.isBlank())
    assertFalse(response.text == "Sorry, I can only help regarding emergency situations.")
  }
}

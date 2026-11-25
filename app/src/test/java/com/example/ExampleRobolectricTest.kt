package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun readStringFromContextIsApexMath() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ApexMath", appName)
    }

    @Test
    fun primaryNavigationAndBasicCalculationWork() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("nav_calculator").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_learn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_graph").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_notebook").assertIsDisplayed()

        // Tap 7 + 5 = on custom keypad
        composeTestRule.onNodeWithText("7").performClick()
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("5").performClick()
        composeTestRule.onNodeWithText("=").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("= 12").assertIsDisplayed()

        // Navigate to Learn tab and verify Algebra modules render
        composeTestRule.onNodeWithTag("nav_learn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Offline Curriculum").assertIsDisplayed()
    }
}

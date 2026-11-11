package com.example

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.screens.AcademyScreen
import com.example.ui.screens.CyberGlossaryScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ResourcesScreen
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.PrayerViewModel
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Phase5LearningScreensInteractionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun academyLibraryGlossaryAndResourcesControlsRemainInteractive() {
        assumeTrue("Phase 5 interaction checks run on API 37.", Build.VERSION.SDK_INT >= 37)
        val vm = DashboardViewModel(composeRule.activity.application)
        val prayerVm = PrayerViewModel(composeRule.activity.application)
        var activeScreen by mutableStateOf("academy")
        composeRule.mainClock.autoAdvance = false

        composeRule.setContent {
            Box(Modifier.fillMaxSize().background(Color(0xFF050B16))) {
                when (activeScreen) {
                    "academy" -> AcademyScreen(
                        viewModel = vm,
                        onNavigateToGlossary = { activeScreen = "glossary" },
                        onNavigateToChecklist = { activeScreen = "checklist" }
                    )
                    "library" -> LibraryScreen(vm, prayerVm) { activeScreen = "academy" }
                    "glossary" -> CyberGlossaryScreen(vm) { activeScreen = "academy" }
                    "resources" -> ResourcesScreen(vm) { activeScreen = "academy" }
                    else -> Text("Checklist destination", modifier = Modifier.testTag("phase5-destination"))
                }
            }
        }
        composeRule.mainClock.advanceTimeBy(1800)
        composeRule.waitForIdle()

        // Academy: language switch, level chips and both navigation callbacks.
        composeRule.onNodeWithTag("academy-language-toggle").performClick()
        composeRule.mainClock.advanceTimeBy(400)
        composeRule.onNodeWithTag("academy-language-toggle").performClick()
        composeRule.onNodeWithTag("academy-filter-advanced").performClick()
        composeRule.onNodeWithTag("academy-course-privacy_rights").assertExists()
        composeRule.onNodeWithTag("academy-filter-all").performClick()
        composeRule.onNodeWithTag("academy-open-glossary").performClick()
        composeRule.mainClock.advanceTimeBy(400)
        composeRule.onNodeWithTag("glossary-back").assertExists()
        activeScreen = "academy"
        composeRule.mainClock.advanceTimeBy(400)
        composeRule.onNodeWithTag("academy-open-checklist").performClick()
        composeRule.onNodeWithTag("phase5-destination").assertExists()
        activeScreen = "academy"
        composeRule.mainClock.advanceTimeBy(400)
        composeRule.onNodeWithTag("academy-resume").assertExists()

        // Library: category tabs, grid/list, search/clear, favorites and detail sheet.
        activeScreen = "library"
        composeRule.mainClock.advanceTimeBy(650)
        composeRule.onNodeWithTag("library-tab-hadiths").performClick()
        composeRule.onNodeWithTag("library-view-toggle").performClick()
        composeRule.onNodeWithTag("library-view-toggle").performClick()
        composeRule.onNodeWithTag("library-search-input").performTextInput("no-such-library-entry")
        composeRule.onNodeWithTag("library-search-clear").performClick()
        composeRule.onNodeWithTag("library-tab-duas").performClick()
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.onAllNodes(hasTestTag("library-item"))[0].performClick()
        composeRule.mainClock.advanceTimeBy(400)
        composeRule.onNodeWithTag("library-detail-sheet").assertExists()

        // Glossary: category filters, alphabetized list, definition sheet and search.
        activeScreen = "glossary"
        composeRule.mainClock.advanceTimeBy(650)
        composeRule.onNodeWithTag("glossary-category-شبكات").performClick()
        composeRule.onAllNodes(hasTestTag("glossary-term"))[0].performClick()
        composeRule.mainClock.advanceTimeBy(400)
        composeRule.onNodeWithTag("glossary-definition-sheet").assertExists()
        activeScreen = "glossary"
        composeRule.mainClock.advanceTimeBy(350)
        composeRule.onNodeWithTag("glossary-search").performTextInput("zz-no-match")
        composeRule.onNodeWithTag("glossary-category-all").performClick()
        composeRule.onNodeWithTag("glossary-search").performTextClearance()

        // Resources: category filtering, grid/list, favorite heart and back button.
        activeScreen = "resources"
        composeRule.mainClock.advanceTimeBy(650)
        composeRule.onNodeWithTag("resources-category-leaks").performClick()
        composeRule.onNodeWithTag("resources-view-toggle").performClick()
        composeRule.onNodeWithTag("resources-view-toggle").performClick()
        composeRule.onAllNodes(hasTestTag("resource-card"))[0].assertExists()
        composeRule.onAllNodes(hasTestTag("resource-favorite"))[0].performClick()
        composeRule.onNodeWithTag("resource-open-leaks").assertExists()
        composeRule.onNodeWithTag("resources-category-all").performClick()
        composeRule.onNodeWithTag("resources-back").performClick()
        composeRule.onNodeWithTag("academy-language-toggle").assertExists()
    }
}

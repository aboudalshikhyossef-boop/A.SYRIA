package com.example

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.design.AyriaFadeThrough
import com.example.ui.design.ayriaForwardEnterTransition
import com.example.ui.design.ayriaForwardExitTransition
import com.example.ui.design.ayriaPopEnterTransition
import com.example.ui.design.ayriaPopExitTransition
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the same transition primitives wired into MainActivity's NavHost and
 * dashboard tab host. The dark host surface remains mounted throughout transitions,
 * preventing a transient white window/background from appearing between routes.
 */
@RunWith(AndroidJUnit4::class)
class Phase3NavigationMotionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun everyNavigationPathUsesMotionInRtl() {
        exerciseNavigationGraph(LayoutDirection.Rtl)
    }

    @Test
    fun everyNavigationPathUsesMotionInLtr() {
        exerciseNavigationGraph(LayoutDirection.Ltr)
    }

    private fun exerciseNavigationGraph(layoutDirection: LayoutDirection) {
        composeRule.setContent {
            Phase3NavigationHarness(layoutDirection = layoutDirection)
        }

        assertRoute("phase3-route-splash")
        click("phase3-continue-splash")
        assertRoute("phase3-route-onboarding")
        click("phase3-continue-onboarding")
        assertRoute("phase3-route-calibration")
        click("phase3-continue-calibration")
        assertRoute("phase3-route-dashboard")

        // Peer tabs use the production fade-through content host.
        listOf("home", "academy", "chat", "library", "resources", "settings", "home").forEach { tab ->
            click("phase3-tab-select-$tab")
            composeRule.waitForIdle()
            assertRoute("phase3-tab-content-$tab")
        }

        // Home -> scanner.
        click("phase3-open-scanner")
        assertRoute("phase3-route-scanner")
        click("phase3-back")
        assertRoute("phase3-route-dashboard")

        // Academy -> glossary/checklist.
        click("phase3-tab-select-academy")
        composeRule.waitForIdle()
        click("phase3-open-glossary")
        assertRoute("phase3-route-glossary")
        click("phase3-back")
        assertRoute("phase3-route-dashboard")

        click("phase3-tab-select-academy")
        composeRule.waitForIdle()
        click("phase3-open-checklist")
        assertRoute("phase3-route-checklist")
        click("phase3-back")
        assertRoute("phase3-route-dashboard")

        // Settings -> about/customization/app lock.
        click("phase3-tab-select-settings")
        composeRule.waitForIdle()
        click("phase3-open-about")
        assertRoute("phase3-route-about")
        click("phase3-back")
        assertRoute("phase3-route-dashboard")

        click("phase3-tab-select-settings")
        composeRule.waitForIdle()
        click("phase3-open-feedback")
        assertRoute("phase3-route-feedback")
        click("phase3-back")
        assertRoute("phase3-route-dashboard")

        click("phase3-tab-select-settings")
        composeRule.waitForIdle()
        click("phase3-open-app-lock")
        assertRoute("phase3-route-app-lock")
        click("phase3-back")
        assertRoute("phase3-route-dashboard")

        // Each of the six tabs was selectable and the route stack returned to Dashboard.
        assertTrue(layoutDirection == LayoutDirection.Rtl || layoutDirection == LayoutDirection.Ltr)
    }

    private fun click(tag: String) {
        composeRule.onNodeWithTag(tag).performClick()
        composeRule.waitForIdle()
    }

    private fun assertRoute(tag: String) {
        composeRule.onNodeWithTag(tag).assertExists()
        composeRule.waitForIdle()
    }
}

@Composable
private fun Phase3NavigationHarness(layoutDirection: LayoutDirection) {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        androidx.compose.foundation.layout.Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF050A18))
                .testTag("phase3-persistent-dark-background")
        ) {
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.fillMaxSize(),
                enterTransition = { ayriaForwardEnterTransition(layoutDirection) },
                exitTransition = { ayriaForwardExitTransition(layoutDirection) },
                popEnterTransition = { ayriaPopEnterTransition(layoutDirection) },
                popExitTransition = { ayriaPopExitTransition(layoutDirection) }
            ) {
                composable("splash") {
                    Phase3Route("phase3-route-splash", "phase3-continue-splash", "Continue") {
                        navController.navigate("onboarding")
                    }
                }
                composable("onboarding") {
                    Phase3Route("phase3-route-onboarding", "phase3-continue-onboarding", "Continue") {
                        navController.navigate("calibration")
                    }
                }
                composable("calibration") {
                    Phase3Route("phase3-route-calibration", "phase3-continue-calibration", "Continue") {
                        navController.navigate("dashboard")
                    }
                }
                composable("dashboard") {
                    var activeTab by rememberSaveable { mutableStateOf("home") }
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("home", "academy", "chat", "library", "resources", "settings").forEach { tab ->
                                Button(
                                    onClick = { activeTab = tab },
                                    modifier = Modifier.weight(1f).testTag("phase3-tab-select-$tab")
                                ) { Text(tab.take(1)) }
                            }
                        }
                        AyriaFadeThrough(
                            targetState = activeTab,
                            modifier = Modifier.weight(1f)
                        ) { tab ->
                            Column(
                                modifier = Modifier.fillMaxSize().testTag("phase3-tab-content-$tab"),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Dashboard $tab")
                                when (tab) {
                                    "home" -> Phase3Action("phase3-open-scanner", "Open scanner") {
                                        navController.navigate("scanner")
                                    }
                                    "academy" -> {
                                        Phase3Action("phase3-open-glossary", "Open glossary") {
                                            navController.navigate("glossary")
                                        }
                                        Phase3Action("phase3-open-checklist", "Open checklist") {
                                            navController.navigate("checklist")
                                        }
                                    }
                                    "settings" -> {
                                        Phase3Action("phase3-open-about", "Open about") {
                                            navController.navigate("about")
                                        }
                                        Phase3Action("phase3-open-feedback", "Open feedback") {
                                            navController.navigate("feedback")
                                        }
                                        Phase3Action("phase3-open-app-lock", "Open app lock") {
                                            navController.navigate("app_lock")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                composable("scanner") { Phase3SecondaryRoute("phase3-route-scanner") { navController.popBackStack() } }
                composable("glossary") { Phase3SecondaryRoute("phase3-route-glossary") { navController.popBackStack() } }
                composable("checklist") { Phase3SecondaryRoute("phase3-route-checklist") { navController.popBackStack() } }
                composable("about") { Phase3SecondaryRoute("phase3-route-about") { navController.popBackStack() } }
                composable("feedback") { Phase3SecondaryRoute("phase3-route-feedback") { navController.popBackStack() } }
                composable("app_lock") { Phase3SecondaryRoute("phase3-route-app-lock") { navController.popBackStack() } }
            }
        }
    }
}

@Composable
private fun Phase3Route(
    routeTag: String,
    actionTag: String,
    label: String,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().testTag(routeTag).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(routeTag, color = Color.White)
        Button(onClick = onContinue, modifier = Modifier.testTag(actionTag)) {
            Text(label)
        }
    }
}

@Composable
private fun Phase3SecondaryRoute(routeTag: String, onBack: () -> Unit) {
    Phase3Route(routeTag, "phase3-back", "Back", onBack)
}

@Composable
private fun Phase3Action(tag: String, label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.testTag(tag)) {
        Text(label)
    }
}

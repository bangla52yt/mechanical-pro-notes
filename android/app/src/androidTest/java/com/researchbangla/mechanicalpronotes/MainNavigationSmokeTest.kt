package com.researchbangla.mechanicalpronotes

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.researchbangla.mechanicalpronotes.presentation.MainActivity
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Workstream G (AUD-005): Smoke instrumented test verifying MainActivity launches cleanly,
 * renders a visible root window, and survives activity recreation without crashing.
 */
@RunWith(AndroidJUnit4::class)
class MainNavigationSmokeTest {

    @Test
    fun mainActivityLaunchesAndSurvivesRecreate() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onView(isRoot()).check(matches(isDisplayed()))
            scenario.recreate()
            onView(isRoot()).check(matches(isDisplayed()))
        }
    }
}

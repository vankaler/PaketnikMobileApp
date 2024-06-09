package com.example.paketnikapp

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @Test
    fun testLoginActivityLaunches() {
        // Launch the LoginActivity
        val scenario = ActivityScenario.launch(LoginActivity::class.java)

        // Check if the activity is launched successfully
        scenario.onActivity { activity ->
            val currentActivity = InstrumentationRegistry.getInstrumentation().targetContext
            assertEquals(activity.localClassName, LoginActivity::class.java.simpleName)
        }
    }
}

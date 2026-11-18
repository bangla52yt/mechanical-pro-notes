package com.researchbangla.mechanicalpronotes.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.researchbangla.mechanicalpronotes.MechanicalProApplication
import com.researchbangla.mechanicalpronotes.domain.model.UserPreferences
import com.researchbangla.mechanicalpronotes.presentation.navigation.AppNavigation
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.toThemeMode

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as MechanicalProApplication).container

        setContent {
            val userPrefs by appContainer.userPreferencesRepository.userPreferences
                .collectAsState(initial = UserPreferences())

            MechanicalProTheme(
                themeMode = userPrefs.themeMode.toThemeMode(),
                highLegibility = userPrefs.isLargePlantFloorFont
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(appContainer = appContainer)
                }
            }
        }
    }
}

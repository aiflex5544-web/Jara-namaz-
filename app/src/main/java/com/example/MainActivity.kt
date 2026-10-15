package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.FullScreenMosqueMode
import com.example.ui.screens.MainMosqueScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MosqueBlack
import com.example.ui.theme.NoorTimeTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Notification permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            NoorTimeTheme(ledTheme = uiState.theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MosqueBlack
                ) {
                    when {
                        uiState.isFullscreenMode -> {
                            FullScreenMosqueMode(
                                currentTime = uiState.currentTime,
                                currentDate = uiState.currentDate,
                                schedule = uiState.schedule,
                                countdown = uiState.countdown,
                                cityName = uiState.selectedCityName,
                                theme = uiState.theme,
                                language = uiState.language,
                                onExit = { viewModel.setFullscreen(false) }
                            )
                        }
                        uiState.isSettingsOpen -> {
                            SettingsScreen(
                                prefs = viewModel.prefs,
                                theme = uiState.theme,
                                language = uiState.language,
                                onSettingsChanged = { viewModel.onSettingsChanged() },
                                onClose = { viewModel.setSettingsOpen(false) }
                            )
                        }
                        else -> {
                            MainMosqueScreen(
                                state = uiState,
                                isAlertEnabled = { viewModel.isAlertEnabled(it) },
                                onToggleAlert = { viewModel.toggleAlert(it) },
                                onSelectTheme = { viewModel.selectTheme(it) },
                                onToggleLanguage = { viewModel.toggleLanguage() },
                                onCityClick = { viewModel.setSettingsOpen(true) },
                                onFullscreenClick = { viewModel.setFullscreen(true) },
                                onSettingsClick = { viewModel.setSettingsOpen(true) },
                                onRefresh = { viewModel.refresh() }
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cyber3d.ball.SimpleBall3DScreen
import com.example.cyber3d.battleground.Battleground3DScreen
import com.example.cyber3d.game.GameViewModel
import com.example.cyber3d.ui.ArchitectureTourScreen
import com.example.cyber3d.ui.GameCanvas
import com.example.cyber3d.ui.GameOverlay
import com.example.cyber3d.ui.HangarScreen
import com.example.cyber3d.ui.StartScreen
import com.example.ui.theme.CyberRunnerTheme

enum class ScreenState {
    BATTLEGROUND,
    BALL_3D,
    START,
    GAME,
    HANGAR,
    TOUR
}

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CyberRunnerTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.soundSynth.stopSynthwaveMusic()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.uiState.value.musicEnabled && !viewModel.uiState.value.isPaused && !viewModel.uiState.value.isGameOver) {
            viewModel.soundSynth.startSynthwaveMusic()
        }
    }
}

@Composable
fun MainApp(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf(ScreenState.BATTLEGROUND) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { _ ->
        when (currentScreen) {
            ScreenState.BATTLEGROUND -> {
                Battleground3DScreen(
                    onSwitchToSciFi = { currentScreen = ScreenState.BALL_3D }
                )
            }

            ScreenState.BALL_3D -> {
                BackHandler {
                    currentScreen = ScreenState.BATTLEGROUND
                }
                SimpleBall3DScreen(
                    onSwitchToSciFiRunner = { currentScreen = ScreenState.START }
                )
            }

            ScreenState.START -> {
                BackHandler {
                    currentScreen = ScreenState.BATTLEGROUND
                }
                StartScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onStartGame = {
                        viewModel.resetGame()
                        currentScreen = ScreenState.GAME
                    },
                    onOpenHangar = { currentScreen = ScreenState.HANGAR },
                    onOpenTour = { currentScreen = ScreenState.TOUR }
                )
            }

            ScreenState.GAME -> {
                BackHandler {
                    if (uiState.isPaused || uiState.isGameOver) {
                        currentScreen = ScreenState.BATTLEGROUND
                    } else {
                        viewModel.pauseGame()
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    GameCanvas(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    GameOverlay(
                        viewModel = viewModel,
                        uiState = uiState,
                        onOpenHangar = { currentScreen = ScreenState.HANGAR },
                        onOpenTour = { currentScreen = ScreenState.TOUR }
                    )
                }
            }

            ScreenState.HANGAR -> {
                BackHandler {
                    currentScreen = ScreenState.GAME
                }
                HangarScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onBack = { currentScreen = ScreenState.GAME }
                )
            }

            ScreenState.TOUR -> {
                BackHandler {
                    currentScreen = ScreenState.BATTLEGROUND
                }
                ArchitectureTourScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onBack = { currentScreen = ScreenState.BATTLEGROUND }
                )
            }
        }
    }
}

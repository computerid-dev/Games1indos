package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GameScreen
import com.example.ui.GameUiState
import com.example.ui.GameViewModel
import com.example.ui.components.GameWebView
import com.example.ui.components.mapAndroidKeyCodeToJs
import com.example.ui.screens.ControlsGuideModal
import com.example.ui.screens.GameOverOverlay
import com.example.ui.screens.HudOverlay
import com.example.ui.screens.MainMenuLandscape
import com.example.ui.screens.PauseOverlay
import com.example.ui.theme.BajajNusantaraTheme
import com.example.ui.theme.BajajOrange
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.WarmCream

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableEdgeToEdge()
        configureImmersiveLandscape()

        setContent {
            BajajNusantaraTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                BajajNusantaraApp(state = state, viewModel = viewModel)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            configureImmersiveLandscape()
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.uiState.value.screen == GameScreen.Playing) {
            viewModel.pauseGame()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val currentScreen = viewModel.uiState.value.screen
        if (currentScreen == GameScreen.Playing) {
            val jsCode = mapAndroidKeyCodeToJs(event.keyCode)
            if (jsCode != null) {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    viewModel.forwardKey(jsCode, true)
                } else if (event.action == KeyEvent.ACTION_UP) {
                    viewModel.forwardKey(jsCode, false)
                }
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun configureImmersiveLandscape() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
private fun BajajNusantaraApp(
    state: GameUiState,
    viewModel: GameViewModel
) {
    BackHandler(enabled = state.showControlsModal) {
        viewModel.setControlsModalVisible(false)
    }
    BackHandler(enabled = !state.showControlsModal && state.screen == GameScreen.Playing) {
        viewModel.pauseGame()
    }
    BackHandler(enabled = !state.showControlsModal && state.screen == GameScreen.Paused) {
        viewModel.resumeGame()
    }
    BackHandler(enabled = !state.showControlsModal && state.screen == GameScreen.GameOver) {
        viewModel.returnToMenu()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Persistent Three.js 3D WebGL Canvas Layer
        GameWebView(
            viewModel = viewModel,
            isPlaying = state.screen == GameScreen.Playing,
            modifier = Modifier.fillMaxSize()
        )

        when (state.screen) {
            GameScreen.Menu -> {
                MainMenuLandscape(
                    state = state,
                    onSelectStage = viewModel::selectStage,
                    onStartGame = viewModel::startGame,
                    onToggleLanguage = viewModel::toggleLanguage,
                    onToggleSound = viewModel::toggleSound,
                    onShowControls = { viewModel.setControlsModalVisible(true) }
                )
            }

            GameScreen.Playing -> {
                HudOverlay(
                    state = state,
                    onPause = viewModel::pauseGame,
                    onToggleSound = viewModel::toggleSound,
                    onCycleCamera = viewModel::cycleCamera,
                    onSteerLeft = { pressed -> viewModel.setTouchInput("left", pressed) },
                    onSteerRight = { pressed -> viewModel.setTouchInput("right", pressed) },
                    onJump = { viewModel.setTouchInput("jump", true) },
                    onTurbo = { viewModel.setTouchInput("turbo", true) }
                )
            }

            GameScreen.Paused -> {
                PauseOverlay(
                    state = state,
                    onResume = viewModel::resumeGame,
                    onRestart = viewModel::startGame,
                    onReturnToMenu = viewModel::returnToMenu,
                    onToggleMouseSteer = viewModel::toggleMouseSteer,
                    onToggleTouchControls = viewModel::toggleTouchControls
                )
            }

            GameScreen.GameOver -> {
                GameOverOverlay(
                    state = state,
                    onRestart = viewModel::startGame,
                    onReturnToMenu = viewModel::returnToMenu
                )
            }
        }

        if (state.showControlsModal) {
            ControlsGuideModal(
                state = state,
                onDismiss = { viewModel.setControlsModalVisible(false) },
                onToggleMouseSteer = viewModel::toggleMouseSteer,
                onToggleTouchControls = viewModel::toggleTouchControls
            )
        }

        state.engineError?.let { errorMsg ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBg.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (state.settings.language == "id") "Gagal Memuat 3D Engine" else "3D Engine Load Error",
                            style = MaterialTheme.typography.titleLarge,
                            color = WarmCream
                        )
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = viewModel::retryLoadEngine,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BajajOrange,
                                contentColor = Color(0xFF1A0A00)
                            )
                        ) {
                            Text(text = if (state.settings.language == "id") "Coba Lagi" else "Retry")
                        }
                    }
                }
            }
        }
    }
}

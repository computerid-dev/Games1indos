package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PlayerSettings
import com.example.data.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class GameScreen {
    Menu,
    Playing,
    Paused,
    GameOver
}

data class StageInfo(
    val index: Int,
    val titleId: String,
    val titleEn: String,
    val subtitleId: String,
    val subtitleEn: String,
    val accentHex: Long
)

val INDONESIA_STAGES = listOf(
    StageInfo(
        index = 0,
        titleId = "Jakarta - Monas",
        titleEn = "Jakarta - Monas",
        subtitleId = "Senja Ibu Kota & Warung Tenda",
        subtitleEn = "Capital Dusk & Street Stalls",
        accentHex = 0xFFFF6D00
    ),
    StageInfo(
        index = 1,
        titleId = "Yogyakarta - Prambanan",
        titleEn = "Yogyakarta - Prambanan",
        subtitleId = "Senja Candi & Obor Wayang",
        subtitleEn = "Ancient Temples & Torchlight",
        accentHex = 0xFFFFB300
    ),
    StageInfo(
        index = 2,
        titleId = "Bali - Pesisir Dewata",
        titleEn = "Bali - Coastal Shrine",
        subtitleId = "Pura Candi Bentar & Laut Tropis",
        subtitleEn = "Split Gates & Tropical Coast",
        accentHex = 0xFF00BFA5
    )
)

data class GameUiState(
    val screen: GameScreen = GameScreen.Menu,
    val settings: PlayerSettings = PlayerSettings(),
    val engineReady: Boolean = false,
    val engineError: String? = null,
    val showControlsModal: Boolean = false,
    val score: Int = 0,
    val coins: Int = 0,
    val distanceMeters: Int = 0,
    val combo: Int = 0,
    val turboCharge: Int = 40,
    val turboActive: Boolean = false,
    val hasShield: Boolean = false,
    val magnetSeconds: Int = 0,
    val speedKmh: Int = 74,
    val activeInput: String = "touch",
    val cameraMode: Int = 0,
    val lastRunIsNewRecord: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _jsCommands = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val jsCommands: SharedFlow<String> = _jsCommands.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.settingsFlow.collect { loaded ->
                _uiState.update { current ->
                    current.copy(settings = loaded)
                }
                if (_uiState.value.engineReady) {
                    syncSettingsToJs(loaded)
                }
            }
        }
    }

    private fun sendJs(script: String) {
        _jsCommands.tryEmit(script)
    }

    private fun syncSettingsToJs(s: PlayerSettings) {
        sendJs("window.GameAPI && window.GameAPI.setSoundEnabled(${s.soundEnabled});")
        sendJs("window.GameAPI && window.GameAPI.setMouseSteerEnabled(${s.mouseSteerEnabled});")
        if (_uiState.value.screen == GameScreen.Menu) {
            sendJs("window.GameAPI && window.GameAPI.setStagePreview(${s.selectedStage});")
        }
    }

    fun onEngineReady() {
        _uiState.update { it.copy(engineReady = true, engineError = null) }
        syncSettingsToJs(_uiState.value.settings)
    }

    fun onEngineError(message: String) {
        _uiState.update { it.copy(engineError = message) }
    }

    fun retryLoadEngine() {
        _uiState.update { it.copy(engineError = null, engineReady = false) }
        sendJs("__RELOAD__")
    }

    fun selectStage(stageIndex: Int) {
        viewModelScope.launch {
            repository.setSelectedStage(stageIndex)
        }
        sendJs("window.GameAPI && window.GameAPI.setStagePreview($stageIndex);")
    }

    fun startGame() {
        val stage = _uiState.value.settings.selectedStage
        _uiState.update {
            it.copy(
                screen = GameScreen.Playing,
                score = 0,
                coins = 0,
                distanceMeters = 0,
                combo = 0,
                turboCharge = 40,
                turboActive = false,
                hasShield = false,
                magnetSeconds = 0,
                lastRunIsNewRecord = false,
                showControlsModal = false
            )
        }
        sendJs("window.GameAPI && window.GameAPI.startGame($stage);")
    }

    fun pauseGame() {
        if (_uiState.value.screen == GameScreen.Playing) {
            _uiState.update { it.copy(screen = GameScreen.Paused) }
            sendJs("window.GameAPI && window.GameAPI.pauseGame();")
        }
    }

    fun resumeGame() {
        if (_uiState.value.screen == GameScreen.Paused) {
            _uiState.update { it.copy(screen = GameScreen.Playing) }
            sendJs("window.GameAPI && window.GameAPI.resumeGame();")
        }
    }

    fun returnToMenu() {
        _uiState.update { it.copy(screen = GameScreen.Menu, showControlsModal = false) }
        sendJs("window.GameAPI && window.GameAPI.returnToMenu();")
    }

    fun setControlsModalVisible(visible: Boolean) {
        _uiState.update { it.copy(showControlsModal = visible) }
    }

    fun toggleLanguage() {
        val next = if (_uiState.value.settings.language == "id") "en" else "id"
        viewModelScope.launch { repository.setLanguage(next) }
    }

    fun toggleSound() {
        val next = !_uiState.value.settings.soundEnabled
        viewModelScope.launch { repository.setSoundEnabled(next) }
        sendJs("window.GameAPI && window.GameAPI.setSoundEnabled($next);")
    }

    fun toggleMouseSteer() {
        val next = !_uiState.value.settings.mouseSteerEnabled
        viewModelScope.launch { repository.setMouseSteerEnabled(next) }
        sendJs("window.GameAPI && window.GameAPI.setMouseSteerEnabled($next);")
    }

    fun toggleTouchControls() {
        val next = !_uiState.value.settings.showTouchControls
        viewModelScope.launch { repository.setShowTouchControls(next) }
    }

    fun cycleCamera() {
        sendJs("window.GameAPI && window.GameAPI.cycleCamera();")
    }

    fun setTouchInput(action: String, pressed: Boolean) {
        sendJs("window.GameAPI && window.GameAPI.setTouchInput('$action', $pressed);")
    }

    fun forwardKey(code: String, isDown: Boolean) {
        if (_uiState.value.screen == GameScreen.Playing) {
            sendJs("window.GameAPI && window.GameAPI.handleNativeKey('$code', $isDown);")
        }
    }

    fun onHudUpdate(jsonStr: String) {
        try {
            val obj = JSONObject(jsonStr)
            _uiState.update {
                it.copy(
                    score = obj.optInt("score", it.score),
                    coins = obj.optInt("coins", it.coins),
                    distanceMeters = obj.optInt("distance", it.distanceMeters),
                    combo = obj.optInt("combo", it.combo),
                    turboCharge = obj.optInt("turboCharge", it.turboCharge),
                    turboActive = obj.optBoolean("turboActive", it.turboActive),
                    hasShield = obj.optBoolean("hasShield", it.hasShield),
                    magnetSeconds = obj.optInt("magnetSeconds", it.magnetSeconds),
                    speedKmh = obj.optInt("speedKmh", it.speedKmh),
                    activeInput = obj.optString("activeInput", it.activeInput),
                    cameraMode = obj.optInt("cameraMode", it.cameraMode)
                )
            }
        } catch (_: Exception) {
        }
    }

    fun onPauseRequestedFromJs() {
        _uiState.update { it.copy(screen = GameScreen.Paused) }
    }

    fun onGameOverFromJs(jsonStr: String) {
        try {
            val obj = JSONObject(jsonStr)
            val finalScore = obj.optInt("score", _uiState.value.score)
            val runCoins = obj.optInt("coins", _uiState.value.coins)
            val runDistance = obj.optInt("distance", _uiState.value.distanceMeters)
            val isNewRecord = finalScore > _uiState.value.settings.bestScore && finalScore > 0

            _uiState.update {
                it.copy(
                    screen = GameScreen.GameOver,
                    score = finalScore,
                    coins = runCoins,
                    distanceMeters = runDistance,
                    lastRunIsNewRecord = isNewRecord
                )
            }
            viewModelScope.launch {
                repository.recordRunResult(finalScore, runCoins)
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(screen = GameScreen.GameOver) }
        }
    }
}

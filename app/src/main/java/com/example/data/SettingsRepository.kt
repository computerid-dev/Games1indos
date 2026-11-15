package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bajaj_nusantara_prefs")

data class PlayerSettings(
    val bestScore: Int = 0,
    val totalCoins: Int = 0,
    val language: String = "id",
    val soundEnabled: Boolean = true,
    val mouseSteerEnabled: Boolean = true,
    val showTouchControls: Boolean = true,
    val selectedStage: Int = 0
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val BEST_SCORE = intPreferencesKey("best_score")
        val TOTAL_COINS = intPreferencesKey("total_coins")
        val LANGUAGE = stringPreferencesKey("language")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MOUSE_STEER_ENABLED = booleanPreferencesKey("mouse_steer_enabled")
        val SHOW_TOUCH_CONTROLS = booleanPreferencesKey("show_touch_controls")
        val SELECTED_STAGE = intPreferencesKey("selected_stage")
    }

    val settingsFlow: Flow<PlayerSettings> = context.dataStore.data.map { prefs ->
        PlayerSettings(
            bestScore = prefs[Keys.BEST_SCORE] ?: 0,
            totalCoins = prefs[Keys.TOTAL_COINS] ?: 0,
            language = prefs[Keys.LANGUAGE] ?: "id",
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            mouseSteerEnabled = prefs[Keys.MOUSE_STEER_ENABLED] ?: true,
            showTouchControls = prefs[Keys.SHOW_TOUCH_CONTROLS] ?: true,
            selectedStage = prefs[Keys.SELECTED_STAGE] ?: 0
        )
    }

    suspend fun recordRunResult(runScore: Int, runCoins: Int) {
        context.dataStore.edit { prefs ->
            val currentBest = prefs[Keys.BEST_SCORE] ?: 0
            if (runScore > currentBest) {
                prefs[Keys.BEST_SCORE] = runScore
            }
            val currentCoins = prefs[Keys.TOTAL_COINS] ?: 0
            prefs[Keys.TOTAL_COINS] = currentCoins + runCoins
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs -> prefs[Keys.LANGUAGE] = lang }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setMouseSteerEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.MOUSE_STEER_ENABLED] = enabled }
    }

    suspend fun setShowTouchControls(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SHOW_TOUCH_CONTROLS] = enabled }
    }

    suspend fun setSelectedStage(stage: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.SELECTED_STAGE] = stage }
    }
}

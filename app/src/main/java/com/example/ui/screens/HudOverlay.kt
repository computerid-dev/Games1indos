package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.GameUiState
import com.example.ui.INDONESIA_STAGES
import com.example.ui.theme.BajajOrange
import com.example.ui.theme.GarudaGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.TropicalCyan
import com.example.ui.theme.TropicalEmerald
import com.example.ui.theme.WarmCream

@Composable
fun HudOverlay(
    state: GameUiState,
    onPause: () -> Unit,
    onToggleSound: () -> Unit,
    onCycleCamera: () -> Unit,
    onSteerLeft: (Boolean) -> Unit,
    onSteerRight: (Boolean) -> Unit,
    onJump: () -> Unit,
    onTurbo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isId = state.settings.language == "id"
    val stage = INDONESIA_STAGES.getOrElse(state.settings.selectedStage) { INDONESIA_STAGES[0] }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Top-Left: Score, Distance, Speed, Combo
            Surface(
                color = ObsidianBg.copy(alpha = 0.72f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GarudaGold.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isId) "SKOR" else "SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${state.score}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = GarudaGold
                        )
                    }

                    Column {
                        Text(
                            text = "${state.distanceMeters} m",
                            style = MaterialTheme.typography.titleMedium,
                            color = WarmCream
                        )
                        Text(
                            text = "${state.speedKmh} km/j",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.turboActive) TropicalCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (state.combo > 1) {
                        Surface(
                            color = BajajOrange,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "x${state.combo}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF1A0A00),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Top-Center: Stage & Active Powerups
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = ObsidianBg.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = if (isId) stage.titleId else stage.titleEn,
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmCream,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.hasShield) {
                        Surface(
                            color = GarudaGold.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF1F1400),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isId) "Perisai Keris" else "Keris Shield",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF1F1400)
                                )
                            }
                        }
                    }

                    if (state.magnetSeconds > 0) {
                        Surface(
                            color = TropicalEmerald.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = WarmCream,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${state.magnetSeconds}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WarmCream
                                )
                            }
                        }
                    }
                }
            }

            // Top-Right: Coins + Quick Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = ObsidianBg.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GarudaGold.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MonetizationOn,
                            contentDescription = null,
                            tint = GarudaGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${state.coins}",
                            style = MaterialTheme.typography.titleMedium,
                            color = WarmCream
                        )
                    }
                }

                FilledTonalIconButton(
                    onClick = onCycleCamera,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("camera_btn"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = ObsidianCard.copy(alpha = 0.85f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Videocam,
                        contentDescription = if (isId) "Sudut Kamera" else "Camera Angle",
                        tint = WarmCream
                    )
                }

                FilledTonalIconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("hud_sound_btn"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = ObsidianCard.copy(alpha = 0.85f)
                    )
                ) {
                    Icon(
                        imageVector = if (state.settings.soundEnabled) {
                            Icons.AutoMirrored.Filled.VolumeUp
                        } else {
                            Icons.AutoMirrored.Filled.VolumeOff
                        },
                        contentDescription = if (isId) "Suara" else "Sound",
                        tint = WarmCream
                    )
                }

                FilledTonalIconButton(
                    onClick = onPause,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("pause_btn"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = BajajOrange
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Pause,
                        contentDescription = if (isId) "Jeda" else "Pause",
                        tint = Color(0xFF1A0A00)
                    )
                }
            }
        }

        // Bottom Arcade Touch Controls (Leave center open for Swipe / Mouse Steer!)
        if (state.settings.showTouchControls) {
            // Bottom-Left: Left / Right Hold-to-Steer Pad
            Row(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HoldSteerButton(
                    onHoldChanged = onSteerLeft,
                    modifier = Modifier.testTag("steer_left_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = if (isId) "Belok Kiri" else "Steer Left",
                        tint = WarmCream,
                        modifier = Modifier.size(34.dp)
                    )
                }

                HoldSteerButton(
                    onHoldChanged = onSteerRight,
                    modifier = Modifier.testTag("steer_right_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = if (isId) "Belok Kanan" else "Steer Right",
                        tint = WarmCream,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            // Bottom-Right: Klakson Turbo & Jump Buttons
            Row(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val turboReady = state.turboCharge >= 100 && !state.turboActive
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = onTurbo,
                        enabled = turboReady,
                        modifier = Modifier
                            .size(58.dp)
                            .testTag("turbo_btn"),
                        shape = CircleShape,
                        color = when {
                            state.turboActive -> TropicalCyan.copy(alpha = 0.88f)
                            turboReady -> GarudaGold
                            else -> ObsidianCard.copy(alpha = 0.7f)
                        },
                        border = BorderStroke(
                            width = 2.dp,
                            color = if (turboReady) WarmCream else GarudaGold.copy(alpha = 0.35f)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = if (isId) "Klakson Turbo" else "Turbo Boost",
                                tint = if (turboReady || state.turboActive) Color(0xFF1A0A00) else GarudaGold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (state.turboCharge / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.width(56.dp),
                        color = if (state.turboActive) TropicalCyan else GarudaGold,
                        trackColor = ObsidianBg.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    onClick = onJump,
                    modifier = Modifier
                        .size(66.dp)
                        .testTag("jump_btn"),
                    shape = CircleShape,
                    color = BajajOrange.copy(alpha = 0.88f),
                    border = BorderStroke(2.dp, WarmCream.copy(alpha = 0.75f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = if (isId) "Lompat" else "Jump",
                            tint = Color(0xFF1A0A00),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HoldSteerButton(
    onHoldChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .size(62.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onHoldChanged(true)
                    waitForUpOrCancellation()
                    onHoldChanged(false)
                }
            },
        shape = CircleShape,
        color = ObsidianBg.copy(alpha = 0.65f),
        border = BorderStroke(1.5.dp, WarmCream.copy(alpha = 0.45f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

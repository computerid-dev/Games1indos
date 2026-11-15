package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.GameUiState
import com.example.ui.theme.BajajOrange
import com.example.ui.theme.GarudaGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.WarmCream

@Composable
fun PauseOverlay(
    state: GameUiState,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onReturnToMenu: () -> Unit,
    onToggleMouseSteer: () -> Unit,
    onToggleTouchControls: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isId = state.settings.language == "id"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
            border = BorderStroke(1.5.dp, GarudaGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isId) "PERMAINAN DIJEDA" else "GAME PAUSED",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GarudaGold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isId) "Setir Otomatis Mouse (OTG/PC)" else "Mouse Hover Steering (OTG/PC)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmCream
                    )
                    Switch(
                        checked = state.settings.mouseSteerEnabled,
                        onCheckedChange = { onToggleMouseSteer() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isId) "Tombol Layar Sentuh" else "On-Screen Touch Buttons",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmCream
                    )
                    Switch(
                        checked = state.settings.showTouchControls,
                        onCheckedChange = { onToggleTouchControls() }
                    )
                }

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("resume_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BajajOrange,
                        contentColor = Color(0xFF1A0A00)
                    )
                ) {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isId) "Lanjutkan" else "Resume")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRestart,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("restart_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isId) "Ulangi" else "Restart")
                    }

                    OutlinedButton(
                        onClick = onReturnToMenu,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("menu_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isId) "Menu" else "Menu")
                    }
                }
            }
        }
    }
}

@Composable
fun GameOverOverlay(
    state: GameUiState,
    onRestart: () -> Unit,
    onReturnToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isId = state.settings.language == "id"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.78f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
            border = BorderStroke(2.dp, BajajOrange)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.lastRunIsNewRecord) {
                    Surface(
                        color = GarudaGold,
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFF1F1400),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isId) "REKOR BARU!" else "NEW RECORD!",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF1F1400)
                            )
                        }
                    }
                }

                Text(
                    text = if (isId) "BAJAJ MOGOK!" else "RIDE OVER!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = WarmCream
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatColumn(
                        label = if (isId) "Skor Akhir" else "Final Score",
                        value = "${state.score}",
                        highlight = true
                    )
                    StatColumn(
                        label = if (isId) "Koin" else "Coins",
                        value = "+${state.coins}",
                        highlight = false
                    )
                    StatColumn(
                        label = if (isId) "Jarak" else "Distance",
                        value = "${state.distanceMeters} m",
                        highlight = false
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReturnToMenu,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("gameover_menu_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Home, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isId) "Menu Utama" else "Main Menu")
                    }

                    Button(
                        onClick = onRestart,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp)
                            .testTag("gameover_retry_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BajajOrange,
                            contentColor = Color(0xFF1A0A00)
                        )
                    ) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isId) "GAS LAGI!" else "PLAY AGAIN!",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ControlsGuideModal(
    state: GameUiState,
    onDismiss: () -> Unit,
    onToggleMouseSteer: () -> Unit,
    onToggleTouchControls: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isId = state.settings.language == "id"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.82f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
            border = BorderStroke(1.5.dp, GarudaGold.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isId) "Panduan Kontrol (HP & PC)" else "Controls Guide (Mobile & PC)",
                        style = MaterialTheme.typography.titleLarge,
                        color = GarudaGold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = if (isId) "Tutup" else "Close",
                            tint = WarmCream
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ControlColumn(
                        icon = Icons.Filled.TouchApp,
                        title = if (isId) "Layar Sentuh" else "Touch Screen",
                        details = if (isId) {
                            "• Geser kiri/kanan di jalan atau tahan tombol panah\n• Ketuk jalan / tombol Atas untuk Lompat\n• Tekan Petir saat penuh untuk Klakson Turbo"
                        } else {
                            "• Drag road left/right or hold arrow buttons\n• Tap road or Up button to Jump\n• Tap Bolt when charged for Turbo Boost"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ControlColumn(
                        icon = Icons.Filled.Keyboard,
                        title = "Keyboard",
                        details = if (isId) {
                            "• A / D atau Panah Kiri/Kanan: Belok\n• W / Spasi / Panah Atas: Lompat\n• Shift / E: Klakson Turbo\n• C: Kamera • Esc / P: Jeda"
                        } else {
                            "• A / D or Left/Right Arrows: Steer\n• W / Space / Up Arrow: Jump\n• Shift / E: Turbo Boost\n• C: Camera • Esc / P: Pause"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ControlColumn(
                        icon = Icons.Filled.Mouse,
                        title = "Mouse (OTG/PC)",
                        details = if (isId) {
                            "• Geser Mouse Kiri/Kanan: Setir Bajaj\n• Klik Kiri: Lompat\n• Klik Kanan: Klakson Turbo\n• Roda Mouse: Ganti Kamera"
                        } else {
                            "• Move Mouse Left/Right: Steer\n• Left Click: Jump\n• Right Click: Turbo Boost\n• Scroll Wheel: Camera Angle"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isId) "Setir Mouse" else "Mouse Steer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmCream
                        )
                        Switch(
                            checked = state.settings.mouseSteerEnabled,
                            onCheckedChange = { onToggleMouseSteer() }
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isId) "Tombol Sentuh" else "Touch Buttons",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmCream
                        )
                        Switch(
                            checked = state.settings.showTouchControls,
                            onCheckedChange = { onToggleTouchControls() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, highlight: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = if (highlight) GarudaGold else WarmCream
        )
    }
}

@Composable
private fun ControlColumn(
    icon: ImageVector,
    title: String,
    details: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ObsidianCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = GarudaGold, modifier = Modifier.size(18.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = WarmCream)
            }
            Text(
                text = details,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

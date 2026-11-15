package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.GameUiState
import com.example.ui.INDONESIA_STAGES
import com.example.ui.theme.BajajOrange
import com.example.ui.theme.BatikCrimson
import com.example.ui.theme.GarudaGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.WarmCream

@Composable
fun MainMenuLandscape(
    state: GameUiState,
    onSelectStage: (Int) -> Unit,
    onStartGame: () -> Unit,
    onToggleLanguage: () -> Unit,
    onToggleSound: () -> Unit,
    onShowControls: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isId = state.settings.language == "id"

    Box(modifier = modifier.fillMaxSize()) {
        // Subtle Nusantara artwork backdrop blended over the live 3D Three.js scene
        Image(
            painter = painterResource(id = R.drawable.img_nusantara_banner),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.24f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            ObsidianBg.copy(alpha = 0.88f),
                            ObsidianBg.copy(alpha = 0.45f),
                            ObsidianBg.copy(alpha = 0.86f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Brand Identity, Stats, Input Compatibility, Quick Settings
            Column(
                modifier = Modifier
                    .weight(1.05f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = BatikCrimson.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, GarudaGold.copy(alpha = 0.65f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DirectionsCar,
                                contentDescription = null,
                                tint = GarudaGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isId) "GAME 3D NUSANTARA • THREE.JS" else "INDONESIAN 3D ARCADE • THREE.JS",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarmCream
                            )
                        }
                    }

                    Text(
                        text = "BAJAJ NUSANTARA",
                        style = MaterialTheme.typography.displaySmall,
                        color = WarmCream
                    )

                    Text(
                        text = if (isId) {
                            "Ngebut melintasi Jakarta, Yogyakarta, & Bali! Hindari angkot dan kumpulkan Koin Nusantara."
                        } else {
                            "Race across Jakarta, Yogyakarta & Bali! Dodge traffic and collect Nusantara Coins."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Input Support Chips (Touch / Keyboard / Mouse)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InputBadge(icon = Icons.Filled.TouchApp, label = if (isId) "Sentuh" else "Touch")
                        InputBadge(icon = Icons.Filled.Keyboard, label = "Keyboard")
                        InputBadge(icon = Icons.Filled.Mouse, label = "Mouse")
                    }
                }

                // Best Score & Total Coins Card + Action Row
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard.copy(alpha = 0.92f)),
                        border = BorderStroke(1.dp, GarudaGold.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    tint = GarudaGold,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isId) "Skor Terbaik" else "Best Score",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${state.settings.bestScore}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = GarudaGold
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isId) "Koin Nusantara" else "Nusantara Coins",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${state.settings.totalCoins}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = WarmCream
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onShowControls,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("controls_guide_btn"),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Gamepad,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isId) "Kontrol" else "Controls",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        FilledTonalIconButton(
                            onClick = onToggleLanguage,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("language_toggle_btn"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = ObsidianCard
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Language,
                                    contentDescription = if (isId) "Ganti Bahasa" else "Switch Language",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isId) "ID" else "EN",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }

                        FilledTonalIconButton(
                            onClick = onToggleSound,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("sound_toggle_btn"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = ObsidianCard
                            )
                        ) {
                            Icon(
                                imageVector = if (state.settings.soundEnabled) {
                                    Icons.AutoMirrored.Filled.VolumeUp
                                } else {
                                    Icons.AutoMirrored.Filled.VolumeOff
                                },
                                contentDescription = if (isId) "Suara" else "Sound"
                            )
                        }
                    }
                }
            }

            // Right Column: Stage Selection & Play Button
            Column(
                modifier = Modifier
                    .weight(1.05f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isId) "PILIH WILAYAH NUSANTARA" else "SELECT INDONESIAN STAGE",
                        style = MaterialTheme.typography.labelLarge,
                        color = GarudaGold
                    )

                    INDONESIA_STAGES.forEach { stage ->
                        val selected = state.settings.selectedStage == stage.index
                        val accent = Color(stage.accentHex)
                        Card(
                            onClick = { onSelectStage(stage.index) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("stage_card_${stage.index}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) {
                                    ObsidianCard.copy(alpha = 0.96f)
                                } else {
                                    ObsidianCard.copy(alpha = 0.68f)
                                }
                            ),
                            border = BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(accent)
                                    )
                                    Column {
                                        Text(
                                            text = if (isId) stage.titleId else stage.titleEn,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = WarmCream
                                        )
                                        Text(
                                            text = if (isId) stage.subtitleId else stage.subtitleEn,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (selected) {
                                    Surface(
                                        color = accent.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (isId) "DIPILIH" else "ACTIVE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = accent,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onStartGame,
                    enabled = state.engineReady,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("play_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BajajOrange,
                        contentColor = Color(0xFF1A0A00)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            !state.engineReady -> if (isId) "MEMUAT DUNIA 3D..." else "LOADING 3D ENGINE..."
                            isId -> "MAIN SEKARANG!"
                            else -> "PLAY NOW!"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }
    }
}

@Composable
private fun InputBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        color = ObsidianCard.copy(alpha = 0.85f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GarudaGold,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = WarmCream
            )
        }
    }
}

package com.example.cyber3d.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyber3d.game.GameUiState
import com.example.cyber3d.game.GameViewModel

@Composable
fun GameOverlay(
    viewModel: GameViewModel,
    uiState: GameUiState,
    onOpenHangar: () -> Unit,
    onOpenTour: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // TOP HUD: Score, Multiplier, Shields, Telemetry
        TopHud(
            uiState = uiState,
            onPause = { viewModel.pauseGame() },
            onToggleMusic = { viewModel.toggleMusic() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        )

        // BOTTOM CONTROLS: Left, Right, Jump, Fire Laser
        if (!uiState.isGameOver && !uiState.isPaused) {
            BottomControls(
                viewModel = viewModel,
                uiState = uiState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 20.dp, end = 20.dp)
            )
        }

        // OVERDRIVE ACTIVE BANNER
        if (uiState.isOverdrive) {
            OverdriveBanner(
                timeLeft = uiState.overdriveTimeLeft,
                isBengali = uiState.isBengali,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 110.dp)
            )
        }

        // GAME OVER MODAL
        AnimatedVisibility(
            visible = uiState.isGameOver,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier.align(Alignment.Center)
        ) {
            GameOverCard(
                uiState = uiState,
                onRestart = { viewModel.resetGame() },
                onOpenHangar = onOpenHangar,
                onOpenTour = onOpenTour
            )
        }

        // PAUSE MODAL
        AnimatedVisibility(
            visible = uiState.isPaused && !uiState.isGameOver,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            PauseCard(
                viewModel = viewModel,
                uiState = uiState,
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.resetGame() },
                onOpenHangar = onOpenHangar,
                onOpenTour = onOpenTour
            )
        }
    }
}

@Composable
private fun TopHud(
    uiState: GameUiState,
    onPause: () -> Unit,
    onToggleMusic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Score & Multiplier
            Column {
                Text(
                    text = "${uiState.score}",
                    color = Color(0xFF00F0FF),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF007A), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format("%.1fx MULT", uiState.multiplier),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isBengali) "দূরত্ব: ${uiState.distance.toInt()}মি" else "${uiState.distance.toInt()}m",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Right: Shields, Music Toggle, Pause Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Shield Batteries
                Row(
                    modifier = Modifier
                        .background(Color(0xFF0F172A).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shields",
                        tint = if (uiState.shields > 0) Color(0xFF00FF88) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    repeat(uiState.maxShields) { idx ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .size(width = 8.dp, height = 12.dp)
                                .background(
                                    if (idx < uiState.shields) Color(0xFF00FF88) else Color(0xFF334155),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onToggleMusic,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1E293B).copy(alpha = 0.8f), CircleShape)
                        .testTag("toggle_music_button")
                ) {
                    Icon(
                        imageVector = if (uiState.musicEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Music",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onPause,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1E293B).copy(alpha = 0.8f), CircleShape)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Mini Android Hardware Telemetry Pill
        Row(
            modifier = Modifier
                .background(Color(0x990A0F1D), RoundedCornerShape(12.dp))
                .border(0.8.dp, Color(0xFF00F0FF).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color(0xFF00FF88), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${uiState.fps} FPS | ${String.format("%.1f", uiState.frameTimeMs)}ms | HWUI Canvas 3D",
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun BottomControls(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Left Side: Steer Left & Right Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Steer Left Button
            Surface(
                onClick = { viewModel.steer(-2.5f) },
                shape = CircleShape,
                color = Color(0x990F172A),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00F0FF)),
                modifier = Modifier
                    .size(64.dp)
                    .testTag("steer_left_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Steer Left",
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(-90f)
                    )
                }
            }

            // Steer Right Button
            Surface(
                onClick = { viewModel.steer(2.5f) },
                shape = CircleShape,
                color = Color(0x990F172A),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00F0FF)),
                modifier = Modifier
                    .size(64.dp)
                    .testTag("steer_right_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Steer Right",
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(90f)
                    )
                }
            }
        }

        // Right Side: Jump/Elevation & Laser Blaster Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            // Jump Button
            Surface(
                onClick = { viewModel.triggerJump() },
                shape = CircleShape,
                color = Color(0x990F172A),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD600)),
                modifier = Modifier
                    .size(64.dp)
                    .testTag("jump_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Jump",
                        tint = Color(0xFFFFD600),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Laser Fire Button
            Surface(
                onClick = { viewModel.fireLaser() },
                shape = CircleShape,
                color = Color(0xCCFF007A),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                modifier = Modifier
                    .size(72.dp)
                    .testTag("fire_laser_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (uiState.isBengali) "লেজার" else "FIRE",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverdriveBanner(
    timeLeft: Float,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.horizontalGradient(listOf(Color(0xFFFF007A), Color(0xFFFFE600))),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 18.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (isBengali) "🔥 হাইপার ওভারড্রাইভ! (${String.format("%.1f", timeLeft)}s)" else "🔥 HYPER OVERDRIVE! (${String.format("%.1f", timeLeft)}s)",
            color = Color.Black,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun GameOverCard(
    uiState: GameUiState,
    onRestart: () -> Unit,
    onOpenHangar: () -> Unit,
    onOpenTour: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xEE090D1A),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF007A)),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp)
            .testTag("game_over_card")
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (uiState.isBengali) "খেলা সমাপ্ত" else "MISSION FAILED",
                color = Color(0xFFFF007A),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (uiState.isBengali) "অর্জিত স্কোর" else "FINAL SCORE",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${uiState.score}",
                color = Color(0xFF00F0FF),
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131B2E), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (uiState.isBengali) "সর্বোচ্চ" else "BEST", color = Color(0xFF64748B), fontSize = 11.sp)
                    Text("${uiState.highScore}", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (uiState.isBengali) "কোর সংগ্রহ" else "CORES", color = Color(0xFF64748B), fontSize = 11.sp)
                    Text("${uiState.coresCollected}", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (uiState.isBengali) "ড্রোন ধ্বংস" else "BLASTED", color = Color(0xFF64748B), fontSize = 11.sp)
                    Text("${uiState.dronesBlasted}", color = Color(0xFFFF007A), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Action: Play Again
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("play_again_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isBengali) "আবার খেলুন" else "PLAY AGAIN",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ship Hangar & Architecture Tour Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onOpenHangar,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hangar_button")
                ) {
                    Text(
                        text = if (uiState.isBengali) "🚀 হ্যাঙ্গার" else "🚀 HANGAR",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onOpenTour,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("architecture_tour_button")
                ) {
                    Text(
                        text = if (uiState.isBengali) "⚡ অ্যান্ডয়েড আর্কিটেকচার" else "⚡ TECH TOUR",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PauseCard(
    viewModel: GameViewModel,
    uiState: GameUiState,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onOpenHangar: () -> Unit,
    onOpenTour: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xEE090D1A),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00F0FF)),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp)
            .testTag("pause_card")
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (uiState.isBengali) "বিরাম (PAUSED)" else "PAUSED",
                color = Color(0xFF00F0FF),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Audio & Haptic quick switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { viewModel.toggleSound() },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (uiState.soundEnabled) "SFX: ON" else "SFX: OFF",
                        fontSize = 11.sp,
                        color = if (uiState.soundEnabled) Color(0xFF00FF88) else Color.Gray
                    )
                }
                OutlinedButton(
                    onClick = { viewModel.toggleMusic() },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (uiState.musicEnabled) "BGM: ON" else "BGM: OFF",
                        fontSize = 11.sp,
                        color = if (uiState.musicEnabled) Color(0xFF00FF88) else Color.Gray
                    )
                }
                OutlinedButton(
                    onClick = { viewModel.toggleHaptics() },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (uiState.hapticsEnabled) "HAPTIC: ON" else "OFF",
                        fontSize = 11.sp,
                        color = if (uiState.hapticsEnabled) Color(0xFF00FF88) else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("resume_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (uiState.isBengali) "চালিয়ে যান" else "RESUME",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onRestart,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = if (uiState.isBengali) "পুনরায় শুরু" else "RESTART", color = Color.White, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenTour,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = if (uiState.isBengali) "টেক আর্কিটেকচার" else "TECH TOUR", color = Color(0xFF38BDF8), fontSize = 11.sp)
                }
            }
        }
    }
}

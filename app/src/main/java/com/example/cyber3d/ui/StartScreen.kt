package com.example.cyber3d.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyber3d.game.GameMode
import com.example.cyber3d.game.GameUiState
import com.example.cyber3d.game.GameViewModel

@Composable
fun StartScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    onStartGame: () -> Unit,
    onOpenHangar: () -> Unit,
    onOpenTour: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBn = uiState.isBengali

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B16))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 32.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Language & Audio Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High Score Badge
                Surface(
                    color = Color(0xFF131B2E),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "সর্বোচ্চ স্কোর: " else "BEST: ",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${uiState.highScore}",
                            color = Color(0xFF00F0FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                            .testTag("start_language_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            tint = Color(0xFFFF007A),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleMusic() },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                            .testTag("start_music_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.musicEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Music",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Neon Glowing Title
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (isBn) "সাইবার রানার ৩ডি" else "CYBERRUNNER 3D",
                    color = Color(0xFF00F0FF).copy(alpha = glowAlpha),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isBn) "নেটিভ অ্যান্ড্রয়েড ৩ডি ইঞ্জিন ও সাই-ফাই গেম" else "Native Android 3D Engine & Sci-Fi Odyssey",
                color = Color(0xFFFF007A),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hardware Engine Badge
            Surface(
                color = Color(0x990F172A),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Text(
                    text = "Kotlin • Jetpack Compose 3D Canvas • AudioTrack PCM • 60 FPS",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Game Mode Selector
            Text(
                text = if (isBn) "গেম মোড নির্বাচন করুন:" else "SELECT GAME MODE:",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ModeCard(
                title = if (isBn) "ইনফিনিট সাইবারওয়ে (Infinite)" else "Infinite Cyberway",
                subtitle = if (isBn) "গতির রোমাঞ্চ, লেজার ব্লাস্টার ও হাই-স্কোর মিশন" else "Dodge hazards, blast drones, collect cores",
                icon = Icons.Default.AllInclusive,
                isSelected = uiState.gameMode == GameMode.INFINITE_RUNNER,
                color = Color(0xFF00F0FF),
                onClick = { viewModel.setGameMode(GameMode.INFINITE_RUNNER) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ModeCard(
                title = if (isBn) "বস হান্ট (Boss Battle)" else "Boss Hunt: Apex Core",
                subtitle = if (isBn) "অ্যানোমালি ম্যাট্রিক্স বসের সাথে ৩ডি আকাশযুদ্ধ" else "Battle the floating 3D Apex fortress",
                icon = Icons.Default.Bolt,
                isSelected = uiState.gameMode == GameMode.BOSS_HUNT,
                color = Color(0xFFFF007A),
                onClick = { viewModel.setGameMode(GameMode.BOSS_HUNT) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ModeCard(
                title = if (isBn) "জেন ক্রুজ (Zen Flight)" else "Zen Cruise",
                subtitle = if (isBn) "বাধা ছাড়া শান্ত মহাকাশীয় উড্ডয়ন ও নিওন দৃশ্য" else "Relaxing hazard-free neon flight",
                icon = Icons.Default.Spa,
                isSelected = uiState.gameMode == GameMode.ZEN_CRUISE,
                color = Color(0xFF00FF88),
                onClick = { viewModel.setGameMode(GameMode.ZEN_CRUISE) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Primary Launch Button
            Button(
                onClick = onStartGame,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_game_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "খেলা শুরু করুন (LAUNCH GAME)" else "LAUNCH MISSION",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Hangar & Android Lab Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenHangar,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF131B2E)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("start_hangar_button")
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "শিপ হ্যাঙ্গার" else "Ship Hangar",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onOpenTour,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF131B2E)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("start_tour_button")
                ) {
                    Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "অ্যান্ড্রয়েড ল্যাব" else "Android Lab",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF162035) else Color(0xFF0E1526)
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) color else Color(0xFF1E293B)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = if (isSelected) Color.White else Color(0xFFCBD5E1), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = subtitle, color = Color(0xFF64748B), fontSize = 11.sp)
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
            }
        }
    }
}

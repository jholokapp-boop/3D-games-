package com.example.cyber3d.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyber3d.game.GameUiState
import com.example.cyber3d.game.GameViewModel

@Composable
fun ArchitectureTourScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val isBn = uiState.isBengali

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D18))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 40.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                            .testTag("tour_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBn) "অ্যান্ড্রয়েড ল্যাব: ওয়েব বনাম নেটিভ" else "ANDROID LAB: WEB VS NATIVE",
                            color = Color(0xFF00F0FF),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (isBn) "কীভাবে এই ৩ডি অ্যাপ ও গেম কাজ করছে" else "Under the hood architecture comparison",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    // Language switcher button
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .testTag("toggle_language_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            tint = Color(0xFFFF007A)
                        )
                    }
                }
            }

            // Interactive 3D Engine Control Center
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = Color(0xFF00F0FF))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "লাইভ ৩ডি ইঞ্জিন কন্ট্রোল" else "LIVE 3D ENGINE CONTROLS",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Toggle Wireframe
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBn) "ওয়্যারফ্রেম মোড (Wireframe Mode)" else "Wireframe Mode",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBn) "ভেক্টরের জ্যামিতি রেখা দেখুন" else "Renders bare polygon edges",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = uiState.wireframeMode,
                                onCheckedChange = { viewModel.toggleWireframe() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF))
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Toggle Fog
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBn) "দূরবর্তী কুয়াশা (Distance Fog)" else "Distance Fog",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBn) "গভীরতা অনুসারে ফেড এফেক্ট" else "Atmospheric depth shading",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = uiState.fogEnabled,
                                onCheckedChange = { viewModel.toggleFog() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF))
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // FOV Slider
                        Text(
                            text = if (isBn) "ক্যামেরা FOV (দৃষ্টির কোণ): ${uiState.fov.toInt()}°" else "Camera FOV: ${uiState.fov.toInt()}°",
                            color = Color(0xFF00F0FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = uiState.fov,
                            onValueChange = { viewModel.updateFov(it) },
                            valueRange = 40f..90f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00F0FF),
                                activeTrackColor = Color(0xFF00F0FF)
                            )
                        )
                    }
                }
            }

            // Interactive AudioTrack Synthesizer Tester
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF007A).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFFFF007A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "নেটিভ অডিও ট্র্যাক সিন্থেসাইজার পরীক্ষা" else "LIVE NATIVE AUDIO SYNTH",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn)
                                "কোনো MP3 বা সাউন্ড ফাইল ব্যবহার করা হয়নি! বিশুদ্ধ গাণিতিক সাইন-তরঙ্গ দিয়ে অ্যান্ড্রয়েড হার্ডওয়্যারে রিয়েল-টাইমে শব্দ তৈরি হচ্ছে।"
                            else
                                "Zero sound files! Synthesized in real-time by pumping raw 16-bit PCM waveform math directly into android.media.AudioTrack.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.soundSynth.playLaser() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("⚡ Laser", fontSize = 11.sp, color = Color(0xFF00F0FF))
                            }
                            OutlinedButton(
                                onClick = { viewModel.soundSynth.playPickup() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("💎 Chime", fontSize = 11.sp, color = Color(0xFF00FF88))
                            }
                            OutlinedButton(
                                onClick = { viewModel.soundSynth.playBoost() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🔥 Boost", fontSize = 11.sp, color = Color(0xFFFFE600))
                            }
                            OutlinedButton(
                                onClick = { viewModel.soundSynth.playExplosion() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("💥 Crash", fontSize = 11.sp, color = Color(0xFFFF007A))
                            }
                        }
                    }
                }
            }

            // Comparison 1: Jetpack Compose vs HTML DOM
            item {
                ComparisonCard(
                    icon = Icons.Default.Layers,
                    title = if (isBn) "১. ইউজার ইন্টারফেস: Compose বনাম Web DOM" else "1. UI: Jetpack Compose vs Web DOM",
                    webTitle = "Web (React / DOM)",
                    webDesc = if (isBn)
                        "ভার্চুয়াল ডম (Virtual DOM) ডিফিঙের মাধ্যমে ব্রাউজারের ভারী HTML/CSS এলিমেন্ট ট্রি আপডেট করে। ব্রাউজার লেআউট ও পেইন্ট করতে একাধিক ফ্রেম ড্রপ হতে পারে।"
                    else
                        "Virtual DOM reconciliation updates heavyweight C++ browser DOM trees, causing layout recalculations and repaints.",
                    androidTitle = "Android (Jetpack Compose)",
                    androidDesc = if (isBn)
                        "কম্পোজ সরাসরি মেমোরিতে স্লট টেবিল (Slot Table) ব্যবহার করে। কোনো DOM নেই! অ্যান্ড্রয়েডের গ্রাফিক্স ইঞ্জিন Skia/RenderNode সরাসরি GPU কমান্ড পাঠায়, ফলে সর্বোচ্চ মসৃণ 60-120 FPS পাওয়া যায়।"
                    else
                        "Declarative Kotlin compiler plugin with slot-table memoization. No DOM! Directly outputs GPU render commands to native Skia/HWUI pipeline."
                )
            }

            // Comparison 2: 3D Graphics Pipeline
            item {
                ComparisonCard(
                    icon = Icons.Default.Speed,
                    title = if (isBn) "২. ৩ডি রেন্ডারিং: অ্যান্ড্রয়েড ক্যানভাস বনাম WebGL" else "2. 3D Graphics: Canvas 3D vs WebGL",
                    webTitle = "Web (HTML5 Canvas / WebGL)",
                    webDesc = if (isBn)
                        "ব্রাউজারের স্যান্ডবক্সের ভেতর দিয়ে রান করে। জাভাস্ক্রিপ্টের গার্বেজ কালেকশন (GC pause) হলে গেম বা অ্যানিমেশনে ল্যাগ/স্টাটার দেখা দেয়।"
                    else
                        "Runs through browser sandbox. JavaScript garbage collection pauses cause micro-stutters during 3D physics.",
                    androidTitle = "Android (Native 3D Projection)",
                    androidDesc = if (isBn)
                        "অ্যান্ড্রয়েডে আমরা গণিত (Matrix4 & Vec3) দিয়ে 3D পয়েন্টকে 2D স্ক্রিনে প্রজেক্ট করে পেন্টার্স অ্যালগরিদম (Painter's Algorithm) দিয়ে সরাসরি ড্র করছি। ART রানটাইমের জেনারেশনাল GC এবং 0-কপি মেমরি থাকায় কোনো ল্যাগ নেই।"
                    else
                        "Direct vector mathematics (Vec3, Camera projection, Painter's back-to-front sorting). ART runtime's generational GC ensures jitter-free rendering."
                )
            }

            // Comparison 3: Concurrency & Threads
            item {
                ComparisonCard(
                    icon = Icons.Default.Memory,
                    title = if (isBn) "৩. মাল্টিথ্রেডিং: কোরোoutine বনাম JS ইভেন্ট লুপ" else "3. Concurrency: Coroutines vs JS Event Loop",
                    webTitle = "Web (Single-Threaded JS Event Loop)",
                    webDesc = if (isBn)
                        "জাভাস্ক্রিপ্ট মূলত একটি একক থ্রেডে (Single Thread) চলে। কোনো ভারী হিসাব করলে পুরো পেজ বা অ্যানিমেশন ফ্রিজ হয়ে যায় (ওয়েব ওয়ার্কার ছাড়া)।"
                    else
                        "Single main thread with async event loop. Heavy computational loops block the UI thread unless offloaded to Web Workers.",
                    androidTitle = "Android (Kotlin Coroutines)",
                    androidDesc = if (isBn)
                        "Kotlin Coroutines অত্যন্ত হালকা ভার্চুয়াল থ্রেডের মতো কাজ করে। UI ড্র করার জন্য Dispatchers.Main এবং ব্যাকগ্রাউন্ড সাউন্ড ও গণনার জন্য Dispatchers.Default স্বাধীনভাবে বহু CPU কোরে সমান্তরালে চলে।"
                    else
                        "True multi-core structured concurrency. Dispatchers.Default leverages CPU cores for sound/physics, while Dispatchers.Main drives Choreographer frames."
                )
            }

            // Comparison 4: Native Hardware Access
            item {
                ComparisonCard(
                    icon = Icons.Default.Vibration,
                    title = if (isBn) "৪. হার্ডওয়্যার অ্যাক্সেস: হ্যাপটিক্স ও সাউন্ড" else "4. Hardware: Haptics & Direct Audio",
                    webTitle = "Web (Browser Limitations)",
                    webDesc = if (isBn)
                        "ব্রাউজারে ভাইব্রেশন বা অডিও প্রায়ই ইউজার ইন্টারেকশন ছাড়া ব্লক থাকে, এবং লো-লেভেল সাউন্ড বাফারে সরাসরি হার্ডওয়্যার অ্যাক্সেস সীমিত।"
                    else
                        "Constrained by browser security sandbox, missing fine-grained vibration waveforms or low-level PCM audio buffers.",
                    androidTitle = "Android (Direct Hardware SDK)",
                    androidDesc = if (isBn)
                        "Android OS সরাসরি ভাইব্রেটর মোটর (VibrationEffect ওয়ান-শট/ওয়েভফর্ম) এবং AudioTrack-এ সরাসরি 16-বিট বাফার স্ট্রিম করতে দেয়, যা কোনো বিলম্ব ছাড়াই আসল কনসোল গেমের মতো প্রতিক্রিয়া দেয়।"
                    else
                        "Direct OS access to linear resonant actuators (LRA haptics) and sub-5ms low-latency AudioTrack PCM buffers."
                )
            }
        }
    }
}

@Composable
private fun ComparisonCard(
    icon: ImageVector,
    title: String,
    webTitle: String,
    webDesc: String,
    androidTitle: String,
    androidDesc: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Web Side
            Surface(
                color = Color(0xFF1C132B),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF007A).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "🌐 $webTitle", color = Color(0xFFFF007A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = webDesc, color = Color(0xFFCBD5E1), fontSize = 11.sp, lineHeight = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Android Side
            Surface(
                color = Color(0xFF0F2538),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FF88).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "🤖 $androidTitle", color = Color(0xFF00FF88), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = androidDesc, color = Color(0xFFCBD5E1), fontSize = 11.sp, lineHeight = 15.sp)
                }
            }
        }
    }
}

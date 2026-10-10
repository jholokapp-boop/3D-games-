package com.example.cyber3d.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyber3d.engine.Camera3D
import com.example.cyber3d.engine.Mesh3D
import com.example.cyber3d.engine.Point2D
import com.example.cyber3d.engine.Vec3
import com.example.cyber3d.game.GameUiState
import com.example.cyber3d.game.GameViewModel
import com.example.cyber3d.game.SHIPS
import com.example.cyber3d.game.ShipSkin

@Composable
fun HangarScreen(
    viewModel: GameViewModel,
    uiState: GameUiState,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var selectedShip by remember { androidx.compose.runtime.mutableStateOf(uiState.currentShip) }
    var shipYaw by remember { mutableFloatStateOf(0.4f) }
    var shipPitch by remember { mutableFloatStateOf(0.2f) }

    // Automatic idle turntable rotation
    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastTime != 0L) {
                    val dt = (now - lastTime) / 1_000_000_000f
                    shipYaw += dt * 0.7f
                }
                lastTime = now
            }
        }
    }

    val camera = remember {
        Camera3D(
            position = Vec3(0f, 1.2f, -5.2f),
            pitch = 0.15f,
            yaw = 0f,
            fov = 55f
        )
    }

    val shipMesh = remember(selectedShip) {
        Mesh3D.createStarfighter(selectedShip.primaryColor, selectedShip.accentColor)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D18))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF1E293B), CircleShape)
                        .testTag("hangar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = if (uiState.isBengali) "শিপ হ্যাঙ্গার ৩ডি" else "3D SHIP HANGAR",
                        color = Color(0xFF00F0FF),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (uiState.isBengali) "স্পর্শ করে ৩ডি মডেলটি ঘুরিয়ে দেখুন" else "Drag to inspect 3D ship geometry",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3D Turntable Inspection Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF1A233D), Color(0xFF090E1A)),
                            radius = 450f
                        )
                    )
                    .border(1.5.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            shipYaw -= dragAmount.x * 0.015f
                            shipPitch = (shipPitch - dragAmount.y * 0.015f).coerceIn(-0.6f, 0.6f)
                        }
                    }
            ) {
                // Turntable Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw circular holographic pedestal
                    drawCircle(
                        color = selectedShip.primaryColor.copy(alpha = 0.15f),
                        radius = w * 0.38f,
                        center = Offset(w * 0.5f, h * 0.78f)
                    )
                    drawCircle(
                        color = selectedShip.accentColor.copy(alpha = 0.4f),
                        radius = w * 0.38f,
                        center = Offset(w * 0.5f, h * 0.78f),
                        style = Stroke(width = 1.8f)
                    )

                    // Transform & Project ship faces
                    val transformedVerts = shipMesh.transform(
                        position = Vec3(0f, 0f, 0f),
                        rotation = Vec3(shipPitch, shipYaw, 0f),
                        scale = Vec3(1.1f, 1.1f, 1.1f)
                    )

                    val projectedVerts = arrayOfNulls<Point2D>(transformedVerts.size)
                    for (i in transformedVerts.indices) {
                        projectedVerts[i] = camera.project(transformedVerts[i], w, h)
                    }

                    val polyList = mutableListOf<Pair<List<Point2D>, Color>>()
                    for (face in shipMesh.faces) {
                        var valid = true
                        var sumZ = 0f
                        val pts = mutableListOf<Point2D>()
                        for (idx in face.indices) {
                            val pt = projectedVerts.getOrNull(idx)
                            if (pt == null) {
                                valid = false
                                break
                            }
                            pts.add(pt)
                            sumZ += pt.z
                        }
                        if (valid && pts.size >= 3) {
                            polyList.add(pts to face.baseColor)
                        }
                    }

                    // Sort back to front
                    polyList.sortByDescending { (pts, _) -> pts.map { it.z }.average() }

                    val path = Path()
                    for ((pts, col) in polyList) {
                        path.reset()
                        path.moveTo(pts[0].x, pts[0].y)
                        for (i in 1 until pts.size) {
                            path.lineTo(pts[i].x, pts[i].y)
                        }
                        path.close()

                        drawPath(path, col)
                        drawPath(path, col.copy(alpha = 0.9f), style = Stroke(width = 1.2f))
                    }
                }

                // Interactive drag badge
                Text(
                    text = if (uiState.isBengali) "🔄 স্পর্শে ৩ডি রোটেট" else "🔄 Touch & Drag 3D",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ship Selector Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SHIPS.forEach { ship ->
                    val isSelected = ship.id == selectedShip.id
                    Card(
                        onClick = {
                            selectedShip = ship
                            viewModel.selectShip(ship)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) ship.primaryColor else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ship_card_${ship.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(ship.primaryColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (uiState.isBengali) ship.nameBn else ship.nameEn,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ship Specs Panel
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF131B2E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (uiState.isBengali) selectedShip.descriptionBn else selectedShip.descriptionEn,
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Spec: Speed
                    StatBar(
                        icon = Icons.Default.Speed,
                        label = if (uiState.isBengali) "গতিবেগ" else "Top Speed",
                        progress = selectedShip.speedMultiplier / 1.4f,
                        color = Color(0xFF00FF88)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Spec: Agility
                    StatBar(
                        icon = Icons.Default.FlashOn,
                        label = if (uiState.isBengali) "ঘূর্ণন ক্ষমতা" else "Agility / Turn",
                        progress = selectedShip.agilityMultiplier / 1.5f,
                        color = Color(0xFF00F0FF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Spec: Shield
                    StatBar(
                        icon = Icons.Default.Shield,
                        label = if (uiState.isBengali) "কোয়ান্টাম শিল্ড" else "Shield Capacity",
                        progress = selectedShip.shieldCapacity / 4.0f,
                        color = Color(0xFFFF007A)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Launch / Equip Button
            Button(
                onClick = {
                    viewModel.selectShip(selectedShip)
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = selectedShip.primaryColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("launch_ship_button")
            ) {
                Text(
                    text = if (uiState.isBengali) "🚀 এই শিপ নিয়ে উড়ান শুরু করুন" else "🚀 LAUNCH INTO HYPERWAY",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun StatBar(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    progress: Float,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 11.sp, modifier = Modifier.width(110.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            color = color,
            trackColor = Color(0xFF1E293B),
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}

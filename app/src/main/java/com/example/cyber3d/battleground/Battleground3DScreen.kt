package com.example.cyber3d.battleground

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyber3d.engine.Camera3D
import com.example.cyber3d.engine.Point2D
import com.example.cyber3d.engine.Vec3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Battleground3DScreen(
    onSwitchToSciFi: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Spectator Camera position in world space
    var camX by remember { mutableFloatStateOf(0f) }
    var camY by remember { mutableFloatStateOf(14f) } // Height (14 = tactical drone, 3 = ground TPS)
    var camZ by remember { mutableFloatStateOf(-22f) }

    // Camera rotation
    var camYaw by remember { mutableFloatStateOf(0f) }
    var camPitch by remember { mutableFloatStateOf(0.48f) } // Looking down at battlefield

    // Velocity for smooth drone camera movement
    var vx by remember { mutableFloatStateOf(0f) }
    var vz by remember { mutableFloatStateOf(0f) }

    // Target position for smooth teleport to landmarks
    var targetPos by remember { mutableStateOf<Vec3?>(null) }

    // Pulsing smoke effect for Airdrop red flare
    var flareTick by remember { mutableFloatStateOf(0f) }

    // 60 FPS Camera Physics & Animation
    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastTime != 0L) {
                    val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.033f)

                    flareTick += dt * 4f

                    // Move camera with velocity
                    if (targetPos != null) {
                        val t = targetPos!!
                        camX += (t.x - camX) * (dt * 5f)
                        camZ += (t.z - 16f - camZ) * (dt * 5f)
                        camY += (12f - camY) * (dt * 5f)
                        if (kotlin.math.abs(camX - t.x) < 0.2f && kotlin.math.abs(camZ - (t.z - 16f)) < 0.2f) {
                            targetPos = null
                        }
                    } else {
                        // Forward/Back relative to camera Yaw
                        val forwardX = sin(camYaw) * vz
                        val forwardZ = cos(camYaw) * vz
                        val strafeX = cos(camYaw) * vx
                        val strafeZ = -sin(camYaw) * vx

                        camX = (camX + (forwardX + strafeX) * dt).coerceIn(-28f, 28f)
                        camZ = (camZ + (forwardZ + strafeZ) * dt).coerceIn(-28f, 28f)

                        // Damping
                        vx *= (1f - dt * 5f)
                        vz *= (1f - dt * 5f)
                    }
                }
                lastTime = now
            }
        }
    }

    val camera = remember(camX, camY, camZ, camYaw, camPitch) {
        Camera3D(
            position = Vec3(camX, camY, camZ),
            pitch = camPitch,
            yaw = camYaw,
            fov = 58f,
            farClip = 220f
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // 3D Battleground Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Touch drag anywhere on screen to look around 360°!
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        camYaw += dragAmount.x * 0.012f
                        camPitch = (camPitch + dragAmount.y * 0.012f).coerceIn(0.05f, 1.45f)
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Sky Gradient (Bermuda Sunset / Clear Sky)
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E3A8A), // Deep evening blue
                        Color(0xFF2563EB),
                        Color(0xFFEA580C), // Orange sunset horizon
                        Color(0xFFF97316)
                    ),
                    startY = 0f,
                    endY = h * 0.52f
                )
            )

            // 2. Grassy Terrain Base
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF14532D), // Bermuda dark grass
                        Color(0xFF166534),
                        Color(0xFF15803D)
                    ),
                    startY = h * 0.52f,
                    endY = h
                ),
                topLeft = Offset(0f, h * 0.52f),
                size = androidx.compose.ui.geometry.Size(w, h * 0.48f)
            )

            // 3. Render 3D Ground Grid & Roads
            drawBattlegroundRoads(camera, w, h)

            // 4. Render 3D Structures & Props (Bunkers, Containers, Watchtower, Barrels)
            draw3DStructures(camera, w, h, flareTick)

            // 5. Render Spawn Markers (where player & enemy characters will appear)
            drawSpawnMarkers(camera, w, h)
        }

        // Top HUD: Status Banner, Altitude Toggle, Mini-Map Radar
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: Map Title & Status
                Column {
                    Surface(
                        color = Color(0xDD0F172A),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF97316))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚔️ ব্যাটেলগ্রাউন্ড ম্যাপ (Battle Map)",
                                color = Color(0xFFF97316),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = Color(0xBB1E293B),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "👀 ড্রোন স্পেক্টেটর মোড (আঙুল দিয়ে টেনে চারপাশ দেখুন)",
                            color = Color(0xFFCBD5E1),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Right: Mini-map Radar
                MiniMapRadar(
                    camX = camX,
                    camZ = camZ,
                    camYaw = camYaw,
                    modifier = Modifier.size(90.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Landmark Quick-Travel Pill Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(BattlegroundData.landmarks) { lm ->
                    Surface(
                        onClick = { targetPos = lm.position },
                        color = Color(0xDD1E293B),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lm.icon, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = lm.nameBn,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Camera Altitude Quick Buttons (Top-Right under Radar)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Drone View (High altitude)
            Surface(
                onClick = { camY = 22f; camPitch = 0.85f },
                shape = CircleShape,
                color = Color(0xCC0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Flight, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        Text("টপ ভিউ", color = Color.White, fontSize = 8.sp)
                    }
                }
            }

            // Eye Level Ground View
            Surface(
                onClick = { camY = 3.2f; camPitch = 0.12f },
                shape = CircleShape,
                color = Color(0xCC0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E)),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                        Text("গ্রাউন্ড", color = Color.White, fontSize = 8.sp)
                    }
                }
            }
        }

        // Bottom Controls: Drone Navigation Flight-Pad
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // D-Pad to fly camera forward, back, left, right across battlefield
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xDD0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { vz = 18f },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Forward", tint = Color.White)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        IconButton(
                            onClick = { vx = -18f },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Move Left", tint = Color.White)
                        }

                        IconButton(
                            onClick = { vz = -18f },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Back", tint = Color.White)
                        }

                        IconButton(
                            onClick = { vx = 18f },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Move Right", tint = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Awaiting Character Notice
            Surface(
                color = Color(0xCC1E293B),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEAB308))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏳ প্লেগ্রাউন্ড প্রস্তুত! আপনি বন্ধুদের ছবি বা ক্যারেক্টার বললে এখানে বসানো হবে।",
                        color = Color(0xFFFDE047),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Tactical Radar / Mini-map in top right corner
 */
@Composable
private fun MiniMapRadar(
    camX: Float,
    camZ: Float,
    camYaw: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = Color(0xDD0B1329),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF22C55E)),
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.width / 2f
            val center = Offset(r, r)

            // Safe zone circle
            drawCircle(color = Color(0x3322C55E), radius = r * 0.85f, center = center)
            drawCircle(color = Color(0xFF22C55E), radius = r * 0.85f, center = center, style = Stroke(width = 1.2f))

            // Crosshairs
            drawLine(Color(0x55FFFFFF), Offset(r, 4f), Offset(r, size.height - 4f), strokeWidth = 1f)
            drawLine(Color(0x55FFFFFF), Offset(4f, r), Offset(size.width - 4f, r), strokeWidth = 1f)

            // Draw Landmark dots on radar
            val mapSpan = 56f // -28 to +28
            for (lm in BattlegroundData.landmarks) {
                val radarX = center.x + (lm.position.x / mapSpan) * (r * 1.5f)
                val radarY = center.y + (lm.position.z / mapSpan) * (r * 1.5f)
                drawCircle(Color(0xFFFFD600), radius = 3f, center = Offset(radarX, radarY))
            }

            // Draw Camera Drone Position & Vision Cone
            val playerRadarX = center.x + (camX / mapSpan) * (r * 1.5f)
            val playerRadarY = center.y + (camZ / mapSpan) * (r * 1.5f)
            drawCircle(Color(0xFF38BDF8), radius = 4f, center = Offset(playerRadarX, playerRadarY))

            // Vision cone heading
            val coneLen = 14f
            val endX = playerRadarX + sin(camYaw) * coneLen
            val endY = playerRadarY + cos(camYaw) * coneLen
            drawLine(Color(0xFF38BDF8), Offset(playerRadarX, playerRadarY), Offset(endX, endY), strokeWidth = 2f)
        }
    }
}

/**
 * Renders intersecting 3D asphalt roadways and terrain lines.
 */
private fun DrawScope.drawBattlegroundRoads(cam: Camera3D, w: Float, h: Float) {
    val roadWidth = 4.5f
    val roadLength = 26f

    // Main East-West Road
    val p1 = cam.project(Vec3(-roadLength, 0.02f, -roadWidth / 2f), w, h)
    val p2 = cam.project(Vec3(roadLength, 0.02f, -roadWidth / 2f), w, h)
    val p3 = cam.project(Vec3(roadLength, 0.02f, roadWidth / 2f), w, h)
    val p4 = cam.project(Vec3(-roadLength, 0.02f, roadWidth / 2f), w, h)

    if (p1 != null && p2 != null && p3 != null && p4 != null) {
        val roadPath = Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            lineTo(p4.x, p4.y)
            close()
        }
        drawPath(roadPath, Color(0xFF334155)) // Dark asphalt
    }

    // North-South Road
    val n1 = cam.project(Vec3(-roadWidth / 2f, 0.03f, -roadLength), w, h)
    val n2 = cam.project(Vec3(roadWidth / 2f, 0.03f, -roadLength), w, h)
    val n3 = cam.project(Vec3(roadWidth / 2f, 0.03f, roadLength), w, h)
    val n4 = cam.project(Vec3(-roadWidth / 2f, 0.03f, roadLength), w, h)

    if (n1 != null && n2 != null && n3 != null && n4 != null) {
        val roadPath = Path().apply {
            moveTo(n1.x, n1.y)
            lineTo(n2.x, n2.y)
            lineTo(n3.x, n3.y)
            lineTo(n4.x, n4.y)
            close()
        }
        drawPath(roadPath, Color(0xFF334155))
    }
}

/**
 * Renders 3D Bunkers, Shipping Containers, Watchtower, Barrels and Airdrop crate.
 */
private fun DrawScope.draw3DStructures(cam: Camera3D, w: Float, h: Float, flareTick: Float) {
    // Sort structures back-to-front (depth sort)
    val sorted = BattlegroundData.structures.sortedByDescending { struct ->
        val rel = struct.position - cam.position
        rel.x * rel.x + rel.y * rel.y + rel.z * rel.z
    }

    val boxPath = Path()

    for (s in sorted) {
        val hw = s.size.x / 2f
        val hgt = s.size.y
        val hd = s.size.z / 2f
        val pos = s.position

        // 8 vertices of a 3D box
        val v0 = cam.project(Vec3(pos.x - hw, pos.y, pos.z - hd), w, h)
        val v1 = cam.project(Vec3(pos.x + hw, pos.y, pos.z - hd), w, h)
        val v2 = cam.project(Vec3(pos.x + hw, pos.y, pos.z + hd), w, h)
        val v3 = cam.project(Vec3(pos.x - hw, pos.y, pos.z + hd), w, h)

        val v4 = cam.project(Vec3(pos.x - hw, pos.y + hgt, pos.z - hd), w, h)
        val v5 = cam.project(Vec3(pos.x + hw, pos.y + hgt, pos.z - hd), w, h)
        val v6 = cam.project(Vec3(pos.x + hw, pos.y + hgt, pos.z + hd), w, h)
        val v7 = cam.project(Vec3(pos.x - hw, pos.y + hgt, pos.z + hd), w, h)

        if (v0 != null && v1 != null && v2 != null && v3 != null &&
            v4 != null && v5 != null && v6 != null && v7 != null) {

            // Draw Roof (v4, v5, v6, v7)
            boxPath.reset()
            boxPath.moveTo(v4.x, v4.y)
            boxPath.lineTo(v5.x, v5.y)
            boxPath.lineTo(v6.x, v6.y)
            boxPath.lineTo(v7.x, v7.y)
            boxPath.close()
            drawPath(boxPath, s.primaryColor)
            drawPath(boxPath, Color.Black.copy(alpha = 0.3f), style = Stroke(width = 1.2f))

            // Draw Front Wall (v3, v2, v6, v7)
            boxPath.reset()
            boxPath.moveTo(v3.x, v3.y)
            boxPath.lineTo(v2.x, v2.y)
            boxPath.lineTo(v6.x, v6.y)
            boxPath.lineTo(v7.x, v7.y)
            boxPath.close()
            drawPath(boxPath, s.primaryColor.copy(alpha = 0.9f))
            drawPath(boxPath, Color.Black.copy(alpha = 0.3f), style = Stroke(width = 1.2f))

            // Draw Left Wall (v0, v3, v7, v4)
            boxPath.reset()
            boxPath.moveTo(v0.x, v0.y)
            boxPath.lineTo(v3.x, v3.y)
            boxPath.lineTo(v7.x, v7.y)
            boxPath.lineTo(v4.x, v4.y)
            boxPath.close()
            drawPath(boxPath, s.primaryColor.copy(alpha = 0.8f))

            // Draw Right Wall (v1, v2, v6, v5)
            boxPath.reset()
            boxPath.moveTo(v1.x, v1.y)
            boxPath.lineTo(v2.x, v2.y)
            boxPath.lineTo(v6.x, v6.y)
            boxPath.lineTo(v5.x, v5.y)
            boxPath.close()
            drawPath(boxPath, s.primaryColor.copy(alpha = 0.8f))

            // If Watchtower, draw stilt legs
            if (s.type == StructureType.WATCHTOWER) {
                drawLine(Color(0xFF451A03), Offset(v0.x, v0.y), Offset(v4.x, v4.y), strokeWidth = 3f)
                drawLine(Color(0xFF451A03), Offset(v1.x, v1.y), Offset(v5.x, v5.y), strokeWidth = 3f)
                drawLine(Color(0xFF451A03), Offset(v2.x, v2.y), Offset(v6.x, v6.y), strokeWidth = 3f)
                drawLine(Color(0xFF451A03), Offset(v3.x, v3.y), Offset(v7.x, v7.y), strokeWidth = 3f)
            }

            // If Airdrop Crate, draw red smoke flare particle rising from top
            if (s.type == StructureType.AIRDROP_CRATE) {
                val smokeTop = cam.project(Vec3(pos.x, pos.y + hgt + 4f, pos.z), w, h)
                val crateTop = cam.project(Vec3(pos.x, pos.y + hgt, pos.z), w, h)
                if (smokeTop != null && crateTop != null) {
                    drawLine(
                        brush = Brush.verticalGradient(listOf(Color(0x00EF4444), Color(0xCCEF4444))),
                        start = Offset(smokeTop.x, smokeTop.y),
                        end = Offset(crateTop.x, crateTop.y),
                        strokeWidth = 14f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

/**
 * Draws future character spawn markers on the battlefield.
 */
private fun DrawScope.drawSpawnMarkers(cam: Camera3D, w: Float, h: Float) {
    val spawns = listOf(
        Triple("P1 (আপনার প্লেয়ার)", Vec3(0f, 0.1f, -4f), Color(0xFF22C55E)),
        Triple("E1 (শত্রু ১)", Vec3(10f, 0.1f, -6f), Color(0xFFEF4444)),
        Triple("E2 (শত্রু ২)", Vec3(-10f, 0.1f, 10f), Color(0xFFEF4444))
    )

    for ((label, pos, col) in spawns) {
        val proj = cam.project(pos, w, h)
        if (proj != null) {
            val r = (32f / (proj.z * 0.08f)).coerceIn(8f, 28f)
            // Glowing landing beacon ring
            drawCircle(col.copy(alpha = 0.25f), radius = r * 1.8f, center = Offset(proj.x, proj.y))
            drawCircle(col, radius = r, center = Offset(proj.x, proj.y), style = Stroke(width = 2f))
            drawCircle(Color.White, radius = r * 0.35f, center = Offset(proj.x, proj.y))
        }
    }
}

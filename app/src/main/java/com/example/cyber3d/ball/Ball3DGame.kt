package com.example.cyber3d.ball

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyber3d.audio.SoundSynth
import com.example.cyber3d.engine.Camera3D
import com.example.cyber3d.engine.Point2D
import com.example.cyber3d.engine.Vec3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class StarTarget(
    val id: Int,
    val x: Float,
    val z: Float,
    var isCollected: Boolean = false
)

@Composable
fun SimpleBall3DScreen(
    onSwitchToSciFiRunner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundSynth = remember { SoundSynth(context) }

    // Ball 3D Physics State
    var ballX by remember { mutableFloatStateOf(0f) }
    var ballY by remember { mutableFloatStateOf(0.7f) } // Height (0.7 is resting on platform)
    var ballZ by remember { mutableFloatStateOf(0f) }
    var ballVx by remember { mutableFloatStateOf(0f) }
    var ballVy by remember { mutableFloatStateOf(0f) }
    var ballVz by remember { mutableFloatStateOf(0f) }
    var ballRollAngleX by remember { mutableFloatStateOf(0f) }
    var ballRollAngleZ by remember { mutableFloatStateOf(0f) }

    // 360 Camera Orbit angles
    var cameraYaw by remember { mutableFloatStateOf(0.35f) } // Horizontal orbit
    var cameraPitch by remember { mutableFloatStateOf(0.55f) } // Vertical elevation angle
    var cameraDistance by remember { mutableFloatStateOf(16f) }

    // Accelerometer / Sensor readings
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableStateOf(false) }

    // Star targets
    val stars = remember {
        mutableStateListOf(
            StarTarget(1, -4.5f, -4.5f),
            StarTarget(2, 4.5f, -4.5f),
            StarTarget(3, -4.5f, 4.5f),
            StarTarget(4, 4.5f, 4.5f),
            StarTarget(5, 0f, 0f)
        )
    }
    var score by remember { mutableIntStateOf(0) }
    var showWinMessage by remember { mutableStateOf(false) }

    // Register Accelerometer sensor for physical phone tilting
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.values.size >= 2) {
                    hasSensor = true
                    // In portrait: event.values[0] is X tilt, event.values[1] is Y tilt
                    tiltX = (-event.values[0] * 0.12f).coerceIn(-1.5f, 1.5f)
                    tiltY = (event.values[1] * 0.12f).coerceIn(-1.5f, 1.5f)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // 60 FPS Physics Loop
    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastTime != 0L) {
                    val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.033f)

                    // Apply tilt acceleration from phone or controls
                    ballVx += tiltX * 22f * dt
                    ballVz += tiltY * 22f * dt

                    // Friction / damping
                    ballVx *= (1f - dt * 2.2f)
                    ballVz *= (1f - dt * 2.2f)

                    // Gravity & vertical bounce
                    ballVy -= 28f * dt
                    ballY += ballVy * dt
                    if (ballY <= 0.7f) {
                        ballY = 0.7f
                        if (kotlin.math.abs(ballVy) > 1.5f) {
                            soundSynth.playPickup()
                            ballVy = -ballVy * 0.45f // Elastic bounce
                        } else {
                            ballVy = 0f
                        }
                    }

                    // Update positions
                    ballX += ballVx * dt
                    ballZ += ballVz * dt

                    // Platform boundaries (-6f to +6f) with elastic bounce
                    val bound = 6.0f
                    if (ballX < -bound) {
                        ballX = -bound
                        ballVx = -ballVx * 0.6f
                        soundSynth.playPickup()
                    } else if (ballX > bound) {
                        ballX = bound
                        ballVx = -ballVx * 0.6f
                        soundSynth.playPickup()
                    }

                    if (ballZ < -bound) {
                        ballZ = -bound
                        ballVz = -ballVz * 0.6f
                        soundSynth.playPickup()
                    } else if (ballZ > bound) {
                        ballZ = bound
                        ballVz = -ballVz * 0.6f
                        soundSynth.playPickup()
                    }

                    // Sphere rotational spin angles for visible 3D rolling!
                    val radius = 0.7f
                    ballRollAngleZ -= (ballVx * dt) / radius
                    ballRollAngleX += (ballVz * dt) / radius

                    // Star collection check
                    for (star in stars) {
                        if (!star.isCollected) {
                            val dx = ballX - star.x
                            val dz = ballZ - star.z
                            if (sqrt(dx * dx + dz * dz) < 1.2f) {
                                star.isCollected = true
                                score++
                                soundSynth.playBoost()
                                if (score >= stars.size) {
                                    showWinMessage = true
                                }
                            }
                        }
                    }
                }
                lastTime = now
            }
        }
    }

    // Camera calculation based on spherical coordinates (Yaw, Pitch, Distance)
    val camera = remember(cameraYaw, cameraPitch, cameraDistance, ballX, ballZ) {
        val camY = ballY + cameraDistance * sin(cameraPitch).toFloat()
        val horizontalDist = cameraDistance * cos(cameraPitch).toFloat()
        val camX = ballX + horizontalDist * sin(cameraYaw).toFloat()
        val camZ = ballZ - horizontalDist * cos(cameraYaw).toFloat()

        Camera3D(
            position = Vec3(camX, camY, camZ),
            pitch = cameraPitch - 0.05f,
            yaw = cameraYaw,
            fov = 55f
        )
    }

    fun resetGame() {
        ballX = 0f
        ballY = 0.7f
        ballZ = 0f
        ballVx = 0f
        ballVy = 0f
        ballVz = 0f
        tiltX = 0f
        tiltY = 0f
        score = 0
        showWinMessage = false
        stars.forEach { it.isCollected = false }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // 3D Canvas Rendering
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Drag anywhere on screen to freely orbit the 3D camera 360°!
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        cameraYaw += dragAmount.x * 0.012f
                        cameraPitch = (cameraPitch + dragAmount.y * 0.012f).coerceIn(0.15f, 1.45f)
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Draw Space / Gradient Floor
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF090D18), Color(0xFF1E293B)),
                    startY = 0f,
                    endY = h
                )
            )

            // 2. Render 3D Platform Ground (12 x 12 units) with checker tiles & borders
            draw3DPlatform(camera, w, h)

            // 3. Render 3D Stars (Collectibles)
            for (star in stars) {
                if (!star.isCollected) {
                    draw3DStar(camera, w, h, star.x, 0.4f, star.z)
                }
            }

            // 4. Render 3D Ball Shadow on platform
            draw3DBallShadow(camera, w, h, ballX, ballZ, ballY)

            // 5. Render 3D Ball Sphere with latitude bands, shading & 3D rotation!
            draw3DSphere(
                cam = camera,
                w = w,
                h = h,
                cx = ballX,
                cy = ballY,
                cz = ballZ,
                radius = 0.7f,
                rollAngleX = ballRollAngleX,
                rollAngleZ = ballRollAngleZ
            )
        }

        // Top Header
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 42.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title & Instructions
                Column {
                    Text(
                        text = "৩ডি বল গেম (3D Ball Arena)",
                        color = Color(0xFF00F0FF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "স্ক্রিনে টেনে ৩৬০° ক্যামেরা ঘুরিয়ে ৩ডি দেখুন",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                // Star Score Pill
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD600))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$score / ${stars.size}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Control Pills (Reset, Jump, Camera Preset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Reset Button
                    Surface(
                        onClick = { resetGame() },
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("reset_ball_button")
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("রিসেট", color = Color.White, fontSize = 11.sp)
                        }
                    }

                    // Jump/Bounce Button
                    Surface(
                        onClick = {
                            if (ballY <= 0.75f) {
                                ballVy = 8.5f
                                soundSynth.playBoost()
                            }
                        },
                        color = Color(0xFF00E5FF),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("bounce_ball_button")
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("লাফ (Jump)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                // Switch to Sci-Fi Runner
                Surface(
                    onClick = onSwitchToSciFiRunner,
                    color = Color(0xFFFF007A),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("switch_to_runner_button")
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("স্পেসশিপ গেম", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Bottom Directional Controls (D-Pad to move ball here & there)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Up Button (Forward Z)
            DirectionButton(
                icon = Icons.Default.KeyboardArrowUp,
                label = "সামনে",
                onClick = { tiltY = 1.0f; ballVz += 4.5f }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                // Left Button
                DirectionButton(
                    icon = Icons.Default.KeyboardArrowLeft,
                    label = "বামে",
                    onClick = { tiltX = -1.0f; ballVx -= 4.5f }
                )

                // Down Button
                DirectionButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    label = "পেছনে",
                    onClick = { tiltY = -1.0f; ballVz -= 4.5f }
                )

                // Right Button
                DirectionButton(
                    icon = Icons.Default.KeyboardArrowRight,
                    label = "ডানে",
                    onClick = { tiltX = 1.0f; ballVx += 4.5f }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "💡 টিপ: মোবাইল টিল্ট করুন বা বোতামে ট্যাপ করে বল সরান",
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )
        }

        // Win Banner
        AnimatedVisibility(
            visible = showWinMessage,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xEE090D1A)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD600)),
                modifier = Modifier.padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD600), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("দারুণ! সব তারা সংগ্রহ শেষ!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("আপনি সফলভাবে ৩ডি বলটি পুরো কোর্সে নিয়ন্ত্রণ করেছেন।", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { resetGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600))
                    ) {
                        Text("পুনরায় খেলুন", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xCC1E293B),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00F0FF)),
        modifier = Modifier.size(54.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF00F0FF),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Renders the 3D Tiled Platform with borders.
 */
private fun DrawScope.draw3DPlatform(cam: Camera3D, w: Float, h: Float) {
    val size = 6.0f
    val step = 1.5f
    val tilePath = Path()

    // Draw tiles
    var x = -size
    while (x < size) {
        var z = -size
        while (z < size) {
            val p1 = cam.project(Vec3(x, 0f, z), w, h)
            val p2 = cam.project(Vec3(x + step, 0f, z), w, h)
            val p3 = cam.project(Vec3(x + step, 0f, z + step), w, h)
            val p4 = cam.project(Vec3(x, 0f, z + step), w, h)

            if (p1 != null && p2 != null && p3 != null && p4 != null) {
                tilePath.reset()
                tilePath.moveTo(p1.x, p1.y)
                tilePath.lineTo(p2.x, p2.y)
                tilePath.lineTo(p3.x, p3.y)
                tilePath.lineTo(p4.x, p4.y)
                tilePath.close()

                val isEven = (((x / step).toInt() + (z / step).toInt()) % 2 == 0)
                val color = if (isEven) Color(0xFF1E293B) else Color(0xFF0F172A)
                drawPath(tilePath, color)
                drawPath(tilePath, Color(0xFF334155), style = Stroke(width = 1f))
            }
            z += step
        }
        x += step
    }

    // Platform outer glowing border
    val corners = listOf(
        Vec3(-size, 0f, -size),
        Vec3(size, 0f, -size),
        Vec3(size, 0f, size),
        Vec3(-size, 0f, size)
    )
    val projCorners = corners.mapNotNull { cam.project(it, w, h) }
    if (projCorners.size == 4) {
        val borderPath = Path().apply {
            moveTo(projCorners[0].x, projCorners[0].y)
            lineTo(projCorners[1].x, projCorners[1].y)
            lineTo(projCorners[2].x, projCorners[2].y)
            lineTo(projCorners[3].x, projCorners[3].y)
            close()
        }
        drawPath(borderPath, Color(0xFF00F0FF), style = Stroke(width = 3f))
    }
}

/**
 * Draws the contact shadow under the 3D ball.
 */
private fun DrawScope.draw3DBallShadow(
    cam: Camera3D,
    w: Float,
    h: Float,
    bx: Float,
    bz: Float,
    by: Float
) {
    val proj = cam.project(Vec3(bx, 0.02f, bz), w, h)
    if (proj != null) {
        // Shadow scales with height (shrinks and fades as ball jumps)
        val heightFactor = (1f - (by - 0.7f) * 0.15f).coerceIn(0.2f, 1f)
        val shadowRadius = (32f / (proj.z * 0.08f)) * heightFactor
        drawCircle(
            color = Color.Black.copy(alpha = 0.55f * heightFactor),
            radius = shadowRadius.coerceIn(4f, 40f),
            center = Offset(proj.x, proj.y)
        )
    }
}

/**
 * Draws a glowing 3D star collectible on the platform.
 */
private fun DrawScope.draw3DStar(
    cam: Camera3D,
    w: Float,
    h: Float,
    sx: Float,
    sy: Float,
    sz: Float
) {
    val proj = cam.project(Vec3(sx, sy, sz), w, h)
    if (proj != null) {
        val r = (24f / (proj.z * 0.08f)).coerceIn(6f, 22f)
        drawCircle(
            color = Color(0xFFFFD600),
            radius = r,
            center = Offset(proj.x, proj.y)
        )
        drawCircle(
            color = Color.White,
            radius = r * 0.45f,
            center = Offset(proj.x, proj.y)
        )
    }
}

/**
 * Renders a true 3D Sphere with latitude lines and light reflection.
 */
private fun DrawScope.draw3DSphere(
    cam: Camera3D,
    w: Float,
    h: Float,
    cx: Float,
    cy: Float,
    cz: Float,
    radius: Float,
    rollAngleX: Float,
    rollAngleZ: Float
) {
    val centerProj = cam.project(Vec3(cx, cy, cz), w, h) ?: return

    // Apparent 2D pixel radius from perspective
    val fovRad = Math.toRadians(cam.fov.toDouble()).toFloat()
    val focalLength = (w * 0.5f) / kotlin.math.tan(fovRad * 0.5f)
    val screenRadius = (radius / centerProj.z) * focalLength

    // Base Sphere gradient (3D shaded look)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF00FFFF),
                Color(0xFF0077B6),
                Color(0xFF023E8A),
                Color(0xFF03045E)
            ),
            center = Offset(centerProj.x - screenRadius * 0.3f, centerProj.y - screenRadius * 0.35f),
            radius = screenRadius * 1.1f
        ),
        radius = screenRadius,
        center = Offset(centerProj.x, centerProj.y)
    )

    // Render 3D Latitude and Longitude rings rotated by ballRollAngleX and ballRollAngleZ
    // This gives immediate visual confirmation of physical 3D rolling!
    val ringSegments = 16
    for (ringIdx in -1..1) {
        val ringY = ringIdx * (radius * 0.45f)
        val ringRadius = sqrt(radius * radius - ringY * ringY)
        val ringPath = Path()
        var first = true

        for (i in 0..ringSegments) {
            val theta = (i * 2 * PI / ringSegments).toFloat()
            // Local point on sphere ring
            var p = Vec3(ringRadius * cos(theta), ringY, ringRadius * sin(theta))
            // Apply sphere roll rotations
            p = p.rotateX(rollAngleX)
            p = p.rotateZ(rollAngleZ)
            // World point
            val worldP = Vec3(cx + p.x, cy + p.y, cz + p.z)
            val ringProj = cam.project(worldP, w, h)
            if (ringProj != null) {
                if (first) {
                    ringPath.moveTo(ringProj.x, ringProj.y)
                    first = false
                } else {
                    ringPath.lineTo(ringProj.x, ringProj.y)
                }
            }
        }

        drawPath(
            path = ringPath,
            color = Color(0xFFFF007A).copy(alpha = 0.75f),
            style = Stroke(width = 1.8f)
        )
    }

    // Specular highlight (glowing white reflection on top-left)
    drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = screenRadius * 0.22f,
        center = Offset(centerProj.x - screenRadius * 0.35f, centerProj.y - screenRadius * 0.35f)
    )
}

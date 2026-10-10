package com.example.cyber3d.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.cyber3d.engine.Mesh3D
import com.example.cyber3d.engine.Point2D
import com.example.cyber3d.engine.Vec3
import com.example.cyber3d.game.CollectibleType
import com.example.cyber3d.game.GameUiState
import com.example.cyber3d.game.GameViewModel
import com.example.cyber3d.game.ObstacleType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Renderable 2D polygon with depth Z for Painter's Algorithm sorting.
 */
private class ProjectedPolygon(
    val points: List<Point2D>,
    val depth: Float,
    val color: Color,
    val isWireframeOnly: Boolean,
    val isEmissive: Boolean
)

@Composable
fun GameCanvas(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    // 60+ FPS Frame ticker connected to Choreographer / Compose Frame Clock
    LaunchedEffect(uiState.isGameOver, uiState.isPaused) {
        var lastTime = 0L
        while (!uiState.isGameOver && !uiState.isPaused) {
            withFrameNanos { now ->
                if (lastTime != 0L) {
                    val deltaSec = (now - lastTime) / 1_000_000_000f
                    viewModel.updateFrame(deltaSec)
                }
                lastTime = now
            }
        }
    }

    // Static starfield positions in 3D
    val stars = remember {
        val rand = Random(1337)
        List(180) {
            Vec3(
                (rand.nextFloat() - 0.5f) * 140f,
                rand.nextFloat() * 60f + 5f,
                rand.nextFloat() * 200f
            )
        }
    }

    // Pre-created meshes
    val shipMesh = remember(uiState.currentShip) {
        Mesh3D.createStarfighter(uiState.currentShip.primaryColor, uiState.currentShip.accentColor)
    }
    val crystalMesh = remember {
        Mesh3D.createCrystal(Color(0xFF00F0FF))
    }
    val droneMesh = remember {
        Mesh3D.createDrone(Color(0xFFFF0055))
    }
    val barrierMesh = remember {
        Mesh3D.createBarrier(1.8f, 2.2f, 0.8f, Color(0xFFFF5500))
    }
    val speedRingMesh = remember {
        Mesh3D.createSpeedRing(1.6f, 0.25f, Color(0xFF00FFCC))
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        viewModel.triggerJump()
                    },
                    onTap = {
                        viewModel.fireLaser()
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        viewModel.steer(dragAmount.x)
                    }
                )
            }
    ) {
        val w = size.width
        val h = size.height
        val cam = viewModel.camera

        // 1. Draw Sci-Fi Gradient Sky & Horizon
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF060913),
                    Color(0xFF0E1428),
                    Color(0xFF280B3B),
                    Color(0xFF450A4A)
                ),
                startY = 0f,
                endY = h * 0.65f
            )
        )

        // Lower terrain / digital void
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF140824),
                    Color(0xFF080B17),
                    Color(0xFF03050B)
                ),
                startY = h * 0.65f,
                endY = h
            ),
            topLeft = Offset(0f, h * 0.65f),
            size = androidx.compose.ui.geometry.Size(w, h * 0.35f)
        )

        // 2. Render 3D Stars & Warp Streaks
        drawStars(stars, viewModel.playerPos.z, cam, w, h, uiState.isOverdrive)

        // 3. Render 3D Neon Highway Grid
        drawHighwayGrid(viewModel.playerPos.z, cam, w, h, uiState.isOverdrive)

        // 4. Gather & Project all 3D scene objects for Painter's Algorithm
        val polyList = mutableListOf<ProjectedPolygon>()

        // (A) Player Starfighter
        val playerWorldPos = Vec3(
            viewModel.playerPos.x,
            viewModel.playerPos.y + viewModel.playerElevation,
            viewModel.playerPos.z
        )
        val playerRotation = Vec3(
            viewModel.playerPitch,
            0f,
            viewModel.playerRoll
        )
        val transformedPlayerVerts = shipMesh.transform(playerWorldPos, playerRotation)
        projectMeshFaces(shipMesh, transformedPlayerVerts, cam, w, h, polyList, uiState.wireframeMode, uiState.fogEnabled)

        // (B) Obstacles
        for (obs in viewModel.obstacles) {
            if (obs.isDestroyed) continue
            val (mesh, scale) = when (obs.type) {
                ObstacleType.DRONE -> droneMesh to Vec3(1f, 1f, 1f)
                ObstacleType.PYRAMID_SPIKE -> barrierMesh to Vec3(1f, 1f, 1f)
                ObstacleType.SPEED_RING -> speedRingMesh to Vec3(1f, 1f, 1f)
                ObstacleType.LASER_WALL -> barrierMesh to Vec3(2f, 1.2f, 0.8f)
            }
            val verts = mesh.transform(obs.position, obs.rotation, scale)
            projectMeshFaces(mesh, verts, cam, w, h, polyList, uiState.wireframeMode, uiState.fogEnabled)
        }

        // (C) Collectibles (Spinning Crystals / Powerups)
        for (col in viewModel.collectibles) {
            if (col.isCollected) continue
            val rot = Vec3(0f, col.rotationY, 0.2f)
            val verts = crystalMesh.transform(col.position, rot)
            projectMeshFaces(crystalMesh, verts, cam, w, h, polyList, uiState.wireframeMode, uiState.fogEnabled)
        }

        // Sort all polygons back-to-front (furthest depth Z first)
        polyList.sortByDescending { it.depth }

        // Draw sorted polygons
        val polyPath = Path()
        for (poly in polyList) {
            if (poly.points.size < 3) continue
            polyPath.reset()
            polyPath.moveTo(poly.points[0].x, poly.points[0].y)
            for (i in 1 until poly.points.size) {
                polyPath.lineTo(poly.points[i].x, poly.points[i].y)
            }
            polyPath.close()

            if (poly.isWireframeOnly || uiState.wireframeMode) {
                drawPath(
                    path = polyPath,
                    color = poly.color,
                    style = Stroke(width = 2.0f)
                )
            } else {
                drawPath(
                    path = polyPath,
                    color = poly.color
                )
                // Crisp glowing wireframe edge
                drawPath(
                    path = polyPath,
                    color = poly.color.copy(alpha = 0.9f),
                    style = Stroke(width = 1.2f)
                )
            }
        }

        // 5. Render 3D Laser Projectiles
        for (laser in viewModel.lasers) {
            val pStart = cam.project(laser.position, w, h)
            val pEnd = cam.project(laser.position + Vec3(0f, 0f, 2.5f), w, h)
            if (pStart != null && pEnd != null) {
                drawLine(
                    color = Color(0xFF00FFFF),
                    start = Offset(pStart.x, pStart.y),
                    end = Offset(pEnd.x, pEnd.y),
                    strokeWidth = 6.0f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(pStart.x, pStart.y),
                    end = Offset(pEnd.x, pEnd.y),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
        }

        // 6. Render 3D Particles
        for (p in viewModel.particles) {
            val proj = cam.project(p.position, w, h)
            if (proj != null) {
                val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                val pixelSize = (p.size * 25f / proj.z).coerceIn(1.5f, 18f)
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = pixelSize,
                    center = Offset(proj.x, proj.y)
                )
            }
        }

        // 7. Overdrive speed tunnel effect
        if (uiState.isOverdrive) {
            drawOverdriveSpeedTunnel(w, h)
        }
    }
}

/**
 * Renders the endless curved 3D Neon Highway in space.
 */
private fun DrawScope.drawHighwayGrid(
    playerZ: Float,
    cam: com.example.cyber3d.engine.Camera3D,
    w: Float,
    h: Float,
    isOverdrive: Boolean
) {
    val roadWidth = 9.0f
    val segmentLength = 4.0f
    val startZ = (playerZ / segmentLength).toInt() * segmentLength
    val totalSegments = 28

    val gridPath = Path()
    val railLeftColor = if (isOverdrive) Color(0xFFFFD700) else Color(0xFF00F0FF)
    val railRightColor = if (isOverdrive) Color(0xFFFF007A) else Color(0xFFFF00A0)

    for (i in 0 until totalSegments) {
        val z1 = startZ + i * segmentLength
        val z2 = z1 + segmentLength

        val p1Left = cam.project(Vec3(-roadWidth / 2f, 0f, z1), w, h)
        val p1Right = cam.project(Vec3(roadWidth / 2f, 0f, z1), w, h)
        val p2Left = cam.project(Vec3(-roadWidth / 2f, 0f, z2), w, h)
        val p2Right = cam.project(Vec3(roadWidth / 2f, 0f, z2), w, h)

        if (p1Left != null && p1Right != null && p2Left != null && p2Right != null) {
            // Road quad
            gridPath.reset()
            gridPath.moveTo(p1Left.x, p1Left.y)
            gridPath.lineTo(p1Right.x, p1Right.y)
            gridPath.lineTo(p2Right.x, p2Right.y)
            gridPath.lineTo(p2Left.x, p2Left.y)
            gridPath.close()

            // Alternating checker grid with distance fade
            val dist = z1 - playerZ
            val fade = (1f - (dist / 110f)).coerceIn(0f, 1f)
            val isEven = (i + (startZ / segmentLength).toInt()) % 2 == 0
            val fillColor = if (isEven) {
                Color(0xFF101935).copy(alpha = 0.55f * fade)
            } else {
                Color(0xFF172345).copy(alpha = 0.35f * fade)
            }

            drawPath(gridPath, fillColor)

            // Cross line
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.35f * fade),
                start = Offset(p1Left.x, p1Left.y),
                end = Offset(p1Right.x, p1Right.y),
                strokeWidth = 1.5f
            )

            // Side glowing rails
            drawLine(
                color = railLeftColor.copy(alpha = 0.85f * fade),
                start = Offset(p1Left.x, p1Left.y),
                end = Offset(p2Left.x, p2Left.y),
                strokeWidth = 3.5f
            )
            drawLine(
                color = railRightColor.copy(alpha = 0.85f * fade),
                start = Offset(p1Right.x, p1Right.y),
                end = Offset(p2Right.x, p2Right.y),
                strokeWidth = 3.5f
            )

            // Center lane dotted line
            val p1Center = cam.project(Vec3(0f, 0f, z1), w, h)
            val p2Center = cam.project(Vec3(0f, 0f, z2), w, h)
            if (p1Center != null && p2Center != null && isEven) {
                drawLine(
                    color = Color(0xFFFFE600).copy(alpha = 0.65f * fade),
                    start = Offset(p1Center.x, p1Center.y),
                    end = Offset(p2Center.x, p2Center.y),
                    strokeWidth = 2.0f
                )
            }
        }
    }
}

/**
 * Renders 3D drifting starfield with perspective warping.
 */
private fun DrawScope.drawStars(
    stars: List<Vec3>,
    playerZ: Float,
    cam: com.example.cyber3d.engine.Camera3D,
    w: Float,
    h: Float,
    isOverdrive: Boolean
) {
    for (s in stars) {
        // Infinite wrap-around in Z
        val relZ = (s.z - (playerZ % 200f) + 200f) % 200f
        val starPos = Vec3(s.x, s.y, playerZ + relZ)
        val proj = cam.project(starPos, w, h)
        if (proj != null && proj.y < h * 0.65f) {
            val brightness = (1f - proj.z / 180f).coerceIn(0.2f, 1f)
            val starSize = if (isOverdrive) 2.5f else 1.8f

            if (isOverdrive) {
                // Streak motion blur
                val streakEnd = cam.project(starPos + Vec3(0f, 0f, 6f), w, h)
                if (streakEnd != null) {
                    drawLine(
                        color = Color(0xFF00FFFF).copy(alpha = brightness * 0.7f),
                        start = Offset(proj.x, proj.y),
                        end = Offset(streakEnd.x, streakEnd.y),
                        strokeWidth = 2.0f
                    )
                }
            } else {
                drawCircle(
                    color = Color.White.copy(alpha = brightness),
                    radius = starSize,
                    center = Offset(proj.x, proj.y)
                )
            }
        }
    }
}

/**
 * Projects a 3D mesh's faces into 2D polygon list with depth and lighting.
 */
private fun projectMeshFaces(
    mesh: Mesh3D,
    transformedVerts: List<Vec3>,
    cam: com.example.cyber3d.engine.Camera3D,
    w: Float,
    h: Float,
    outputList: MutableList<ProjectedPolygon>,
    wireframeMode: Boolean,
    fogEnabled: Boolean
) {
    // Project all vertices first
    val projectedVerts = arrayOfNulls<Point2D>(transformedVerts.size)
    for (i in transformedVerts.indices) {
        projectedVerts[i] = cam.project(transformedVerts[i], w, h)
    }

    // Directional light vector from upper-left-front
    val lightDir = Vec3(0.5f, 1.0f, -0.6f).normalize()

    for (face in mesh.faces) {
        var allValid = true
        var sumZ = 0f
        val facePoints = mutableListOf<Point2D>()

        for (idx in face.indices) {
            val pt = projectedVerts.getOrNull(idx)
            if (pt == null) {
                allValid = false
                break
            }
            facePoints.add(pt)
            sumZ += pt.z
        }

        if (allValid && facePoints.size >= 3) {
            val avgDepth = sumZ / facePoints.size

            // Compute lighting normal if face has 3+ vertices
            val v0 = transformedVerts[face.indices[0]]
            val v1 = transformedVerts[face.indices[1]]
            val v2 = transformedVerts[face.indices[2]]
            val edge1 = v1 - v0
            val edge2 = v2 - v0
            val normal = edge1.cross(edge2).normalize()

            val diffuse = if (face.emissive) 1.0f else (normal.dot(lightDir) * 0.45f + 0.55f).coerceIn(0.25f, 1.0f)

            var shadedColor = Color(
                red = (face.baseColor.red * diffuse).coerceIn(0f, 1f),
                green = (face.baseColor.green * diffuse).coerceIn(0f, 1f),
                blue = (face.baseColor.blue * diffuse).coerceIn(0f, 1f),
                alpha = face.baseColor.alpha
            )

            // Distance fog blending
            if (fogEnabled) {
                val fogFactor = (avgDepth / 120f).coerceIn(0f, 0.85f)
                val fogColor = Color(0xFF0F1528)
                shadedColor = Color(
                    red = shadedColor.red * (1f - fogFactor) + fogColor.red * fogFactor,
                    green = shadedColor.green * (1f - fogFactor) + fogColor.green * fogFactor,
                    blue = shadedColor.blue * (1f - fogFactor) + fogColor.blue * fogFactor,
                    alpha = shadedColor.alpha
                )
            }

            outputList.add(
                ProjectedPolygon(
                    points = facePoints,
                    depth = avgDepth,
                    color = shadedColor,
                    isWireframeOnly = wireframeMode || face.wireframeOnly,
                    isEmissive = face.emissive
                )
            )
        }
    }
}

/**
 * High-speed warp vignette for Hyper Overdrive mode.
 */
private fun DrawScope.drawOverdriveSpeedTunnel(w: Float, h: Float) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x33FF007A),
                Color(0x7700FFFF)
            ),
            center = Offset(w * 0.5f, h * 0.55f),
            radius = w * 0.75f
        )
    )
}

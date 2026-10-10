package com.example

import androidx.compose.ui.graphics.Color
import com.example.cyber3d.engine.Camera3D
import com.example.cyber3d.engine.Mesh3D
import com.example.cyber3d.engine.Vec3
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun vec3_mathOperations_areAccurate() {
        val v1 = Vec3(1f, 2f, 3f)
        val v2 = Vec3(4f, 5f, 6f)

        val sum = v1 + v2
        assertEquals(5f, sum.x, 0.001f)
        assertEquals(7f, sum.y, 0.001f)
        assertEquals(9f, sum.z, 0.001f)

        val dot = v1.dot(v2) // 4 + 10 + 18 = 32
        assertEquals(32f, dot, 0.001f)

        val cross = Vec3(1f, 0f, 0f).cross(Vec3(0f, 1f, 0f))
        assertEquals(Vec3(0f, 0f, 1f), cross)
    }

    @Test
    fun camera3D_projectsVisiblePoints() {
        val camera = Camera3D(
            position = Vec3(0f, 2f, -5f),
            pitch = 0f,
            yaw = 0f,
            fov = 60f
        )

        // Point directly in front of camera at Z = 10
        val visiblePoint = Vec3(0f, 2f, 10f)
        val proj = camera.project(visiblePoint, 1080f, 1920f)
        assertNotNull(proj)
        // Should be roughly centered on screen
        assertEquals(540f, proj!!.x, 1f)
        assertEquals(960f, proj.y, 1f)

        // Point behind camera at Z = -10
        val behindPoint = Vec3(0f, 2f, -10f)
        val behindProj = camera.project(behindPoint, 1080f, 1920f)
        assertNull(behindProj)
    }

    @Test
    fun mesh3D_createsValidStarfighter() {
        val mesh = Mesh3D.createStarfighter(Color.Cyan, Color.Magenta)
        assertTrue(mesh.vertices.isNotEmpty())
        assertTrue(mesh.faces.isNotEmpty())

        val transformed = mesh.transform(position = Vec3(0f, 0f, 5f))
        assertEquals(mesh.vertices.size, transformed.size)
    }
}

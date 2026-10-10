package com.example.cyber3d.engine

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * A 3D Face consisting of vertex indices and shading info.
 */
data class Face3D(
    val indices: IntArray,
    val baseColor: Color,
    val wireframeOnly: Boolean = false,
    val emissive: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Face3D
        return indices.contentEquals(other.indices) && baseColor == other.baseColor
    }

    override fun hashCode(): Int {
        var result = indices.contentHashCode()
        result = 31 * result + baseColor.hashCode()
        return result
    }
}

/**
 * 3D Mesh object with vertices, faces, and transformation helpers.
 */
class Mesh3D(
    val vertices: List<Vec3>,
    val faces: List<Face3D>
) {
    /**
     * Transforms this mesh by scale, rotation (pitch, yaw, roll), and translation.
     */
    fun transform(
        position: Vec3,
        rotation: Vec3 = Vec3(), // pitch(X), yaw(Y), roll(Z)
        scale: Vec3 = Vec3(1f, 1f, 1f)
    ): List<Vec3> {
        return vertices.map { v ->
            var p = Vec3(v.x * scale.x, v.y * scale.y, v.z * scale.z)
            if (rotation.x != 0f) p = p.rotateX(rotation.x)
            if (rotation.y != 0f) p = p.rotateY(rotation.y)
            if (rotation.z != 0f) p = p.rotateZ(rotation.z)
            p + position
        }
    }

    companion object {
        /**
         * Creates a 3D Cyber Starfighter mesh.
         */
        fun createStarfighter(primaryColor: Color, accentColor: Color): Mesh3D {
            val v = listOf(
                // 0: Nose tip
                Vec3(0f, 0f, 2.2f),
                // 1: Cockpit top
                Vec3(0f, 0.45f, 0.4f),
                // 2: Cockpit back
                Vec3(0f, 0.35f, -1.0f),
                // 3: Fuselage bottom
                Vec3(0f, -0.3f, 0.0f),
                // 4: Left wing root
                Vec3(-0.6f, 0.0f, -0.4f),
                // 5: Right wing root
                Vec3(0.6f, 0.0f, -0.4f),
                // 6: Left wing tip
                Vec3(-2.2f, 0.15f, -1.4f),
                // 7: Right wing tip
                Vec3(2.2f, 0.15f, -1.4f),
                // 8: Left wing fin up
                Vec3(-2.2f, 0.7f, -1.5f),
                // 9: Right wing fin up
                Vec3(2.2f, 0.7f, -1.5f),
                // 10: Tail fin top
                Vec3(0f, 1.1f, -1.6f),
                // 11: Tail base
                Vec3(0f, 0.0f, -1.8f),
                // 12: Left thruster
                Vec3(-0.4f, 0.05f, -1.9f),
                // 13: Right thruster
                Vec3(0.4f, 0.05f, -1.9f)
            )

            val f = listOf(
                // Cockpit front / nose facets
                Face3D(intArrayOf(0, 1, 4), primaryColor),
                Face3D(intArrayOf(0, 5, 1), primaryColor),
                Face3D(intArrayOf(0, 4, 3), primaryColor.copy(alpha = 0.85f)),
                Face3D(intArrayOf(0, 3, 5), primaryColor.copy(alpha = 0.85f)),

                // Cockpit canopy (Glass accent)
                Face3D(intArrayOf(1, 2, 4), accentColor),
                Face3D(intArrayOf(1, 5, 2), accentColor),

                // Wings
                Face3D(intArrayOf(4, 2, 6), primaryColor),
                Face3D(intArrayOf(2, 5, 7), primaryColor),
                Face3D(intArrayOf(4, 6, 12), primaryColor.copy(alpha = 0.9f)),
                Face3D(intArrayOf(5, 13, 7), primaryColor.copy(alpha = 0.9f)),

                // Wingtips & vertical winglets
                Face3D(intArrayOf(6, 8, 4), accentColor, emissive = true),
                Face3D(intArrayOf(7, 5, 9), accentColor, emissive = true),

                // Tail fin
                Face3D(intArrayOf(2, 10, 11), accentColor, emissive = true),

                // Engine exhaust thrusters (Glowing neon)
                Face3D(intArrayOf(12, 2, 11), Color(0xFF00FFFF), emissive = true),
                Face3D(intArrayOf(13, 11, 2), Color(0xFF00FFFF), emissive = true)
            )

            return Mesh3D(v, f)
        }

        /**
         * Creates an Octahedron / Energy Crystal mesh.
         */
        fun createCrystal(color: Color): Mesh3D {
            val s = 0.8f
            val v = listOf(
                Vec3(0f, s, 0f),      // 0: Top
                Vec3(0f, -s, 0f),     // 1: Bottom
                Vec3(-s * 0.7f, 0f, 0f), // 2: Left
                Vec3(s * 0.7f, 0f, 0f),  // 3: Right
                Vec3(0f, 0f, s * 0.7f),  // 4: Front
                Vec3(0f, 0f, -s * 0.7f)  // 5: Back
            )

            val f = listOf(
                Face3D(intArrayOf(0, 4, 2), color, emissive = true),
                Face3D(intArrayOf(0, 3, 4), color.copy(alpha = 0.85f), emissive = true),
                Face3D(intArrayOf(0, 5, 3), color, emissive = true),
                Face3D(intArrayOf(0, 2, 5), color.copy(alpha = 0.85f), emissive = true),
                Face3D(intArrayOf(1, 2, 4), color.copy(alpha = 0.75f), emissive = true),
                Face3D(intArrayOf(1, 4, 3), color.copy(alpha = 0.9f), emissive = true),
                Face3D(intArrayOf(1, 3, 5), color.copy(alpha = 0.75f), emissive = true),
                Face3D(intArrayOf(1, 5, 2), color.copy(alpha = 0.9f), emissive = true)
            )
            return Mesh3D(v, f)
        }

        /**
         * Creates a 3D Hazard Spike Drone mesh.
         */
        fun createDrone(color: Color): Mesh3D {
            val r = 0.9f
            val c = 0.4f
            val v = listOf(
                Vec3(0f, r, 0f),   // 0: +Y
                Vec3(0f, -r, 0f),  // 1: -Y
                Vec3(r, 0f, 0f),   // 2: +X
                Vec3(-r, 0f, 0f),  // 3: -X
                Vec3(0f, 0f, r),   // 4: +Z
                Vec3(0f, 0f, -r),  // 5: -Z
                // Central core box
                Vec3(c, c, c), Vec3(-c, c, c), Vec3(-c, -c, c), Vec3(c, -c, c),
                Vec3(c, c, -c), Vec3(-c, c, -c), Vec3(-c, -c, -c), Vec3(c, -c, -c)
            )

            val f = listOf(
                Face3D(intArrayOf(0, 6, 7), color),
                Face3D(intArrayOf(0, 10, 11), color),
                Face3D(intArrayOf(2, 6, 10), color),
                Face3D(intArrayOf(3, 7, 11), color),
                Face3D(intArrayOf(4, 6, 7), Color(0xFFFF0055), emissive = true),
                Face3D(intArrayOf(1, 8, 9), color.copy(alpha = 0.8f)),
                Face3D(intArrayOf(5, 10, 11), Color(0xFFFF0055), emissive = true)
            )
            return Mesh3D(v, f)
        }

        /**
         * Creates a 3D Hazard Obelisk / Laser Barrier mesh.
         */
        fun createBarrier(width: Float, height: Float, depth: Float, color: Color): Mesh3D {
            val hw = width / 2f
            val hd = depth / 2f
            val v = listOf(
                Vec3(-hw, 0f, -hd),
                Vec3(hw, 0f, -hd),
                Vec3(hw, 0f, hd),
                Vec3(-hw, 0f, hd),
                Vec3(-hw * 0.7f, height, -hd * 0.7f),
                Vec3(hw * 0.7f, height, -hd * 0.7f),
                Vec3(hw * 0.7f, height, hd * 0.7f),
                Vec3(-hw * 0.7f, height, hd * 0.7f)
            )

            val f = listOf(
                Face3D(intArrayOf(3, 2, 6, 7), color), // Front face
                Face3D(intArrayOf(0, 4, 5, 1), color.copy(alpha = 0.7f)), // Back face
                Face3D(intArrayOf(0, 3, 7, 4), color.copy(alpha = 0.85f)), // Left face
                Face3D(intArrayOf(1, 5, 6, 2), color.copy(alpha = 0.85f)), // Right face
                Face3D(intArrayOf(4, 7, 6, 5), Color(0xFFFF0077), emissive = true) // Top glowing cap
            )
            return Mesh3D(v, f)
        }

        /**
         * Creates a 3D Cyber Gate / Speed Ring mesh.
         */
        fun createSpeedRing(radius: Float, thickness: Float, color: Color): Mesh3D {
            val segments = 8
            val v = mutableListOf<Vec3>()
            val f = mutableListOf<Face3D>()

            val innerR = radius - thickness
            val outerR = radius

            for (i in 0 until segments) {
                val angle = (i * 2 * Math.PI / segments).toFloat()
                val nextAngle = ((i + 1) * 2 * Math.PI / segments).toFloat()

                val cos1 = cos(angle)
                val sin1 = sin(angle)
                val cos2 = cos(nextAngle)
                val sin2 = sin(nextAngle)

                val idx = v.size
                v.add(Vec3(innerR * cos1, innerR * sin1 + radius, 0f))
                v.add(Vec3(outerR * cos1, outerR * sin1 + radius, 0f))
                v.add(Vec3(outerR * cos2, outerR * sin2 + radius, 0f))
                v.add(Vec3(innerR * cos2, innerR * sin2 + radius, 0f))

                f.add(Face3D(intArrayOf(idx, idx + 1, idx + 2, idx + 3), color, emissive = true))
            }
            return Mesh3D(v, f)
        }

        /**
         * Creates a 3D Boss Core Matrix mesh.
         */
        fun createBossMesh(): Mesh3D {
            val r = 2.4f
            val v = listOf(
                Vec3(0f, r, 0f), Vec3(0f, -r, 0f),
                Vec3(r, 0f, 0f), Vec3(-r, 0f, 0f),
                Vec3(0f, 0f, r), Vec3(0f, 0f, -r),
                Vec3(r * 0.7f, r * 0.7f, 0f), Vec3(-r * 0.7f, r * 0.7f, 0f),
                Vec3(r * 0.7f, -r * 0.7f, 0f), Vec3(-r * 0.7f, -r * 0.7f, 0f)
            )
            val f = listOf(
                Face3D(intArrayOf(0, 4, 6), Color(0xFFFF0055), emissive = true),
                Face3D(intArrayOf(0, 7, 4), Color(0xFFFF0055), emissive = true),
                Face3D(intArrayOf(1, 8, 4), Color(0xFFFF3300), emissive = true),
                Face3D(intArrayOf(1, 4, 9), Color(0xFFFF3300), emissive = true),
                Face3D(intArrayOf(6, 2, 8), Color(0xFFFF7700), emissive = true),
                Face3D(intArrayOf(7, 9, 3), Color(0xFFFF7700), emissive = true)
            )
            return Mesh3D(v, f)
        }
    }
}

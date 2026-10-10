package com.example.cyber3d.engine

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-performance 3D Vector for real-time graphics transformations.
 */
data class Vec3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    operator fun plus(other: Vec3): Vec3 = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3): Vec3 = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float): Vec3 = Vec3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float): Vec3 = if (scalar != 0f) Vec3(x / scalar, y / scalar, z / scalar) else this

    fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z

    fun cross(other: Vec3): Vec3 = Vec3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalize(): Vec3 {
        val len = length()
        return if (len > 0.0001f) this / len else this
    }

    fun rotateX(radians: Float): Vec3 {
        val c = cos(radians)
        val s = sin(radians)
        return Vec3(x, y * c - z * s, y * s + z * c)
    }

    fun rotateY(radians: Float): Vec3 {
        val c = cos(radians)
        val s = sin(radians)
        return Vec3(x * c + z * s, y, -x * s + z * c)
    }

    fun rotateZ(radians: Float): Vec3 {
        val c = cos(radians)
        val s = sin(radians)
        return Vec3(x * c - y * s, x * s + y * c, z)
    }
}

/**
 * 2D projected screen coordinate with original depth Z for depth sorting.
 */
data class Point2D(
    val x: Float,
    val y: Float,
    val z: Float // camera depth
)

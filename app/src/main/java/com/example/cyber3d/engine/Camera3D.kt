package com.example.cyber3d.engine

class Camera3D(
    var position: Vec3 = Vec3(0f, 2.5f, -6f),
    var pitch: Float = 0.12f, // slight tilt looking down
    var yaw: Float = 0f,
    var roll: Float = 0f,
    var fov: Float = 60f, // degrees
    var nearClip: Float = 0.8f,
    var farClip: Float = 160f
) {
    /**
     * Projects a 3D world-space coordinate into 2D screen pixels.
     * Returns null if point is behind the camera or beyond far clipping.
     */
    fun project(
        point: Vec3,
        screenWidth: Float,
        screenHeight: Float
    ): Point2D? {
        // Translate relative to camera
        val rel = point - position

        // Apply camera rotation (pitch & roll & yaw)
        // Order: Yaw around Y, Pitch around X, Roll around Z
        var p = rel
        if (yaw != 0f) p = p.rotateY(-yaw)
        if (pitch != 0f) p = p.rotateX(-pitch)
        if (roll != 0f) p = p.rotateZ(-roll)

        if (p.z < nearClip || p.z > farClip) {
            return null
        }

        // Focal length calculation based on FOV
        val fovRad = Math.toRadians(fov.toDouble()).toFloat()
        val focalLength = (screenWidth * 0.5f) / kotlin.math.tan(fovRad * 0.5f)

        val screenX = (screenWidth * 0.5f) + (p.x / p.z) * focalLength
        val screenY = (screenHeight * 0.5f) - (p.y / p.z) * focalLength

        return Point2D(screenX, screenY, p.z)
    }
}

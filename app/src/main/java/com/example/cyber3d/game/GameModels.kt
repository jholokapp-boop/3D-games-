package com.example.cyber3d.game

import androidx.compose.ui.graphics.Color
import com.example.cyber3d.engine.Vec3

enum class GameMode {
    INFINITE_RUNNER,
    BOSS_HUNT,
    ZEN_CRUISE
}

enum class ObstacleType {
    PYRAMID_SPIKE,
    DRONE,
    LASER_WALL,
    SPEED_RING
}

enum class CollectibleType {
    ENERGY_CORE,
    SHIELD_BOOST,
    HYPER_OVERDRIVE
}

data class ShipSkin(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val primaryColor: Color,
    val accentColor: Color,
    val speedMultiplier: Float,
    val agilityMultiplier: Float,
    val shieldCapacity: Int
)

data class Obstacle(
    val id: Long,
    val type: ObstacleType,
    var position: Vec3,
    var rotation: Vec3 = Vec3(),
    val rotSpeed: Vec3 = Vec3(),
    val radius: Float = 1.0f,
    var isDestroyed: Boolean = false,
    var hitPoints: Int = 1
)

data class Collectible(
    val id: Long,
    val type: CollectibleType,
    var position: Vec3,
    var rotationY: Float = 0f,
    var isCollected: Boolean = false,
    val radius: Float = 0.9f
)

data class LaserBolt(
    val id: Long,
    var position: Vec3,
    val velocity: Vec3,
    var lifetime: Float = 1.6f
)

data class Particle3D(
    var position: Vec3,
    var velocity: Vec3,
    val color: Color,
    var life: Float,
    val maxLife: Float,
    val size: Float
)

data class BossEntity(
    var position: Vec3 = Vec3(0f, 3.5f, 45f),
    var health: Float = 100f,
    val maxHealth: Float = 100f,
    var attackTimer: Float = 0f,
    var isActive: Boolean = false,
    var rotationY: Float = 0f
)

data class TrackSegment(
    val z: Float,
    val leftBound: Float,
    val rightBound: Float,
    val elevation: Float,
    val color: Color
)

package com.example.cyber3d.battleground

import androidx.compose.ui.graphics.Color
import com.example.cyber3d.engine.Vec3

enum class StructureType {
    BUNKER_BUILDING,
    CARGO_CONTAINER,
    WATCHTOWER,
    AIRDROP_CRATE,
    OIL_BARREL,
    COVER_WALL,
    ROAD_SEGMENT
}

data class MapStructure(
    val id: Int,
    val type: StructureType,
    val position: Vec3,
    val size: Vec3, // width, height, depth
    val rotationY: Float = 0f,
    val primaryColor: Color,
    val accentColor: Color = Color.DarkGray,
    val label: String? = null
)

data class Landmark(
    val nameBn: String,
    val nameEn: String,
    val position: Vec3,
    val icon: String
)

object BattlegroundData {
    // 5 Distinct Landmark Zones
    val landmarks = listOf(
        Landmark("ক্লক টাওয়ার বেস", "Clock Tower Base", Vec3(-14f, 0f, -12f), "🏰"),
        Landmark("সেন্ট্রাল ফ্যাক্টরি", "Central Factory", Vec3(0f, 0f, 0f), "🏭"),
        Landmark("কার্গো হ্যাঙ্গার", "Cargo Hangar", Vec3(14f, 0f, -10f), "📦"),
        Landmark("বাঙ্কার আলফা", "Bunker Alpha", Vec3(-12f, 0f, 14f), "🛡️"),
        Landmark("এয়ারড্রপ ড্রপজোন", "Airdrop Zone", Vec3(12f, 0f, 12f), "🎁")
    )

    // Pre-placed battlefield structures & tactical cover props
    val structures = listOf(
        // === Central Factory Area (0, 0) ===
        MapStructure(
            id = 1,
            type = StructureType.BUNKER_BUILDING,
            position = Vec3(0f, 0f, 0f),
            size = Vec3(8f, 4f, 10f),
            primaryColor = Color(0xFF475569),
            accentColor = Color(0xFF64748B),
            label = "মেইন ফ্যাক্টরি"
        ),
        // Central cover walls
        MapStructure(
            id = 2,
            type = StructureType.COVER_WALL,
            position = Vec3(0f, 0f, 7f),
            size = Vec3(6f, 1.8f, 0.6f),
            primaryColor = Color(0xFF94A3B8)
        ),
        MapStructure(
            id = 3,
            type = StructureType.COVER_WALL,
            position = Vec3(0f, 0f, -7f),
            size = Vec3(6f, 1.8f, 0.6f),
            primaryColor = Color(0xFF94A3B8)
        ),

        // === Cargo Docks (14, -10) ===
        MapStructure(
            id = 10,
            type = StructureType.CARGO_CONTAINER,
            position = Vec3(12f, 0f, -8f),
            size = Vec3(3.2f, 2.6f, 7f),
            primaryColor = Color(0xFFDC2626), // Red military shipping container
            accentColor = Color(0xFF991B1B)
        ),
        MapStructure(
            id = 11,
            type = StructureType.CARGO_CONTAINER,
            position = Vec3(16f, 0f, -9f),
            size = Vec3(3.2f, 2.6f, 7f),
            primaryColor = Color(0xFF2563EB), // Blue container
            accentColor = Color(0xFF1D4ED8)
        ),
        MapStructure(
            id = 12,
            type = StructureType.CARGO_CONTAINER,
            position = Vec3(14f, 2.6f, -8.5f), // Stacked container on top!
            size = Vec3(3.2f, 2.6f, 7f),
            primaryColor = Color(0xFFD97706), // Yellow/amber container
            accentColor = Color(0xFFB45309)
        ),

        // Oil Barrels near cargo
        MapStructure(
            id = 13,
            type = StructureType.OIL_BARREL,
            position = Vec3(9.5f, 0f, -6f),
            size = Vec3(1.1f, 1.8f, 1.1f),
            primaryColor = Color(0xFFEF4444)
        ),
        MapStructure(
            id = 14,
            type = StructureType.OIL_BARREL,
            position = Vec3(10.8f, 0f, -5.5f),
            size = Vec3(1.1f, 1.8f, 1.1f),
            primaryColor = Color(0xFFEF4444)
        ),

        // === Bunker Alpha Area (-12, 14) ===
        MapStructure(
            id = 20,
            type = StructureType.BUNKER_BUILDING,
            position = Vec3(-12f, 0f, 14f),
            size = Vec3(7f, 3.2f, 6f),
            primaryColor = Color(0xFF334155),
            accentColor = Color(0xFF1E293B),
            label = "বাঙ্কার ১"
        ),
        MapStructure(
            id = 21,
            type = StructureType.COVER_WALL,
            position = Vec3(-12f, 0f, 9.5f),
            size = Vec3(5f, 1.6f, 0.6f),
            primaryColor = Color(0xFF64748B)
        ),

        // === Watchtower Hill (-14, -12) ===
        MapStructure(
            id = 30,
            type = StructureType.WATCHTOWER,
            position = Vec3(-14f, 0f, -12f),
            size = Vec3(3.5f, 8.5f, 3.5f),
            primaryColor = Color(0xFF78350F), // Wood/military steel watchtower
            accentColor = Color(0xFF92400E),
            label = "স্নাইপার টাওয়ার"
        ),

        // === Airdrop Zone (12, 12) ===
        MapStructure(
            id = 40,
            type = StructureType.AIRDROP_CRATE,
            position = Vec3(12f, 0f, 12f),
            size = Vec3(2.5f, 2.2f, 2.5f),
            primaryColor = Color(0xFFDC2626), // Red & blue airdrop crate with flare
            accentColor = Color(0xFFFBBF24),
            label = "সাপ্লাই এয়ারড্রপ"
        )
    )
}

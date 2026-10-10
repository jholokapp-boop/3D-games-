package com.example.cyber3d.game

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cyber3d.audio.SoundSynth
import com.example.cyber3d.engine.Camera3D
import com.example.cyber3d.engine.Vec3
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

data class GameUiState(
    val score: Int = 0,
    val highScore: Int = 0,
    val multiplier: Float = 1.0f,
    val distance: Float = 0f,
    val speed: Float = 22f,
    val shields: Int = 3,
    val maxShields: Int = 3,
    val isOverdrive: Boolean = false,
    val overdriveTimeLeft: Float = 0f,
    val coresCollected: Int = 0,
    val dronesBlasted: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val gameMode: GameMode = GameMode.INFINITE_RUNNER,
    val currentShip: ShipSkin = SHIPS[0],
    val boss: BossEntity = BossEntity(),
    // Performance telemetry
    val fps: Int = 60,
    val frameTimeMs: Float = 16.6f,
    val renderedPolys: Int = 0,
    val activeParticles: Int = 0,
    // Settings & Dev inspection
    val isBengali: Boolean = true,
    val wireframeMode: Boolean = false,
    val fogEnabled: Boolean = true,
    val fov: Float = 62f,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

val SHIPS = listOf(
    ShipSkin(
        id = "specter",
        nameEn = "Aero-Specter",
        nameBn = "অ্যারো-স্পেক্টার",
        descriptionEn = "Balanced agility and dual particle cannons. Great for precision maneuvering.",
        descriptionBn = "চমৎকার ব্যালেন্সড গতি ও সুনির্দিষ্ট নিয়ন্ত্রণ। নতুন ও অভিজ্ঞ উভয়ের জন্য সেরা।",
        primaryColor = Color(0xFF00E5FF),
        accentColor = Color(0xFFFF007F),
        speedMultiplier = 1.0f,
        agilityMultiplier = 1.1f,
        shieldCapacity = 3
    ),
    ShipSkin(
        id = "vortex",
        nameEn = "Vortex-Interceptor",
        nameBn = "ভর্টেক্স-ইন্টারসেপ্টর",
        descriptionEn = "Hyper-thrust propulsion with maximum top speed and aggressive banking roll.",
        descriptionBn = "হাইপার-থ্রাস্ট গতি এবং তীব্র টার্নিং রুল। সর্বোচ্চ স্কোর গড়ার জন্য আদর্শ।",
        primaryColor = Color(0xFF00FF88),
        accentColor = Color(0xFFFFD600),
        speedMultiplier = 1.25f,
        agilityMultiplier = 1.3f,
        shieldCapacity = 2
    ),
    ShipSkin(
        id = "titan",
        nameEn = "Quantum-Titan",
        nameBn = "কোয়ান্টাম-টাইটান",
        descriptionEn = "Reinforced cyber shielding and magnetic core attractor with heavy armor.",
        descriptionBn = "উন্নত কোয়ান্টাম শিল্ড এবং শক্তিশালী বর্ম। বাধা মোকাবিলায় সবচেয়ে সহনশীল।",
        primaryColor = Color(0xFF9D4EDD),
        accentColor = Color(0xFFFF6B00),
        speedMultiplier = 0.9f,
        agilityMultiplier = 0.95f,
        shieldCapacity = 4
    )
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val soundSynth = SoundSynth(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    // 3D Camera & Scene state
    val camera = Camera3D()

    // Player 3D Physics
    var playerPos = Vec3(0f, 0.4f, 0f)
        private set
    var playerVelocityX = 0f
        private set
    var playerRoll = 0f
        private set
    var playerPitch = 0f
        private set
    var playerElevation = 0f
        private set
    var targetLaneX = 0f
        private set

    // Active entities in world
    val obstacles = mutableListOf<Obstacle>()
    val collectibles = mutableListOf<Collectible>()
    val lasers = mutableListOf<LaserBolt>()
    val particles = mutableListOf<Particle3D>()

    private var nextEntityId = 1L
    private var lastSpawnZ = 20f
    private var lastFrameNanos = 0L
    private var frameCount = 0
    private var fpsTimer = 0f

    init {
        resetGame()
    }

    fun selectShip(ship: ShipSkin) {
        _uiState.update { it.copy(currentShip = ship, shields = ship.shieldCapacity, maxShields = ship.shieldCapacity) }
    }

    fun toggleLanguage() {
        _uiState.update { it.copy(isBengali = !it.isBengali) }
    }

    fun toggleSound() {
        val next = !_uiState.value.soundEnabled
        soundSynth.isSoundEnabled = next
        _uiState.update { it.copy(soundEnabled = next) }
    }

    fun toggleMusic() {
        val next = !_uiState.value.musicEnabled
        soundSynth.isMusicEnabled = next
        if (next) soundSynth.startSynthwaveMusic() else soundSynth.stopSynthwaveMusic()
        _uiState.update { it.copy(musicEnabled = next) }
    }

    fun toggleHaptics() {
        val next = !_uiState.value.hapticsEnabled
        soundSynth.isHapticsEnabled = next
        _uiState.update { it.copy(hapticsEnabled = next) }
    }

    fun toggleWireframe() {
        _uiState.update { it.copy(wireframeMode = !it.wireframeMode) }
    }

    fun toggleFog() {
        _uiState.update { it.copy(fogEnabled = !it.fogEnabled) }
    }

    fun updateFov(newFov: Float) {
        camera.fov = newFov
        _uiState.update { it.copy(fov = newFov) }
    }

    fun setGameMode(mode: GameMode) {
        _uiState.update { it.copy(gameMode = mode) }
        resetGame()
    }

    fun pauseGame() {
        _uiState.update { it.copy(isPaused = true) }
        soundSynth.stopSynthwaveMusic()
    }

    fun resumeGame() {
        _uiState.update { it.copy(isPaused = false) }
        if (_uiState.value.musicEnabled) soundSynth.startSynthwaveMusic()
    }

    fun resetGame() {
        playerPos = Vec3(0f, 0.4f, 0f)
        playerVelocityX = 0f
        playerRoll = 0f
        playerPitch = 0f
        playerElevation = 0f
        targetLaneX = 0f

        obstacles.clear()
        collectibles.clear()
        lasers.clear()
        particles.clear()

        lastSpawnZ = 20f

        val ship = _uiState.value.currentShip
        _uiState.update {
            it.copy(
                score = 0,
                multiplier = 1.0f,
                distance = 0f,
                speed = 24f * ship.speedMultiplier,
                shields = ship.shieldCapacity,
                maxShields = ship.shieldCapacity,
                isOverdrive = false,
                overdriveTimeLeft = 0f,
                coresCollected = 0,
                dronesBlasted = 0,
                isGameOver = false,
                isPaused = false,
                boss = BossEntity(isActive = (it.gameMode == GameMode.BOSS_HUNT))
            )
        }

        // Spawn initial runway
        spawnAhead(120f)
        if (_uiState.value.musicEnabled) {
            soundSynth.startSynthwaveMusic()
        }
    }

    // Steering input from touch / virtual joystick / drag
    fun steer(horizontalOffset: Float) {
        // Clamped horizontal position (-4.5f to +4.5f)
        val sensitivity = _uiState.value.currentShip.agilityMultiplier
        targetLaneX = (targetLaneX + horizontalOffset * 0.08f * sensitivity).coerceIn(-4.2f, 4.2f)
    }

    fun setTargetLaneDirect(x: Float) {
        targetLaneX = x.coerceIn(-4.2f, 4.2f)
    }

    fun fireLaser() {
        if (_uiState.value.isGameOver || _uiState.value.isPaused) return
        val currentZ = playerPos.z
        val currentX = playerPos.x
        val currentY = playerPos.y + playerElevation

        // Twin wing blaster bolts
        val leftBolt = LaserBolt(nextEntityId++, Vec3(currentX - 0.7f, currentY, currentZ + 1.2f), Vec3(0f, 0f, 75f))
        val rightBolt = LaserBolt(nextEntityId++, Vec3(currentX + 0.7f, currentY, currentZ + 1.2f), Vec3(0f, 0f, 75f))
        lasers.add(leftBolt)
        lasers.add(rightBolt)

        soundSynth.playLaser()
    }

    fun triggerJump() {
        if (playerElevation <= 0.05f) {
            playerElevation = 1.8f
            playerPitch = -0.2f
        }
    }

    /**
     * Main 60 FPS Game Physics & Update Tick
     */
    fun updateFrame(deltaSeconds: Float) {
        val state = _uiState.value
        if (state.isGameOver || state.isPaused) return

        val dt = deltaSeconds.coerceIn(0.001f, 0.05f)

        // FPS Calculation
        frameCount++
        fpsTimer += dt
        if (fpsTimer >= 0.5f) {
            val currentFps = (frameCount / fpsTimer).toInt()
            val frameMs = (fpsTimer / frameCount) * 1000f
            _uiState.update { it.copy(fps = currentFps, frameTimeMs = frameMs) }
            frameCount = 0
            fpsTimer = 0f
        }

        // Forward motion & speed calculation
        var forwardSpeed = state.speed
        if (state.isOverdrive) {
            forwardSpeed *= 1.8f
        }
        val distanceTraveled = forwardSpeed * dt
        val newZ = playerPos.z + distanceTraveled

        // Smooth steering physics towards target lane
        val diffX = targetLaneX - playerPos.x
        val moveX = diffX * min(1f, dt * 14f)
        val newX = playerPos.x + moveX

        // Ship banking roll calculation based on horizontal turn speed
        val targetRoll = (-diffX * 0.28f).coerceIn(-0.45f, 0.45f)
        playerRoll += (targetRoll - playerRoll) * min(1f, dt * 12f)

        // Player elevation decay (jump gravity)
        if (playerElevation > 0f) {
            playerElevation = max(0f, playerElevation - dt * 2.8f)
            playerPitch += (0f - playerPitch) * min(1f, dt * 6f)
        } else {
            playerPitch = 0f
        }

        playerPos = Vec3(newX, 0.4f, newZ)

        // Camera smoothly follows player with slight lag and elevation
        camera.position = Vec3(
            playerPos.x * 0.75f,
            playerPos.y + playerElevation + 2.4f,
            playerPos.z - 6.2f
        )
        camera.pitch = 0.12f + (playerElevation * 0.04f)
        camera.roll = playerRoll * 0.35f

        // Overdrive timer decay
        var isOverdrive = state.isOverdrive
        var overdriveTimeLeft = state.overdriveTimeLeft
        if (isOverdrive) {
            overdriveTimeLeft -= dt
            if (overdriveTimeLeft <= 0f) {
                isOverdrive = false
                overdriveTimeLeft = 0f
            }
        }

        // Update Projectiles (Lasers)
        val laserIter = lasers.iterator()
        while (laserIter.hasNext()) {
            val laser = laserIter.next()
            laser.position = laser.position + laser.velocity * dt
            laser.lifetime -= dt

            // Laser vs Obstacle collision check
            var hit = false
            for (obs in obstacles) {
                if (!obs.isDestroyed && obs.type != ObstacleType.SPEED_RING) {
                    val dist = (laser.position - obs.position).length()
                    if (dist < obs.radius + 0.6f) {
                        obs.hitPoints--
                        if (obs.hitPoints <= 0) {
                            obs.isDestroyed = true
                            soundSynth.playExplosion()
                            spawnExplosionParticles(obs.position, obs.radius)
                            _uiState.update {
                                val addedScore = (150 * it.multiplier).toInt()
                                it.copy(
                                    score = it.score + addedScore,
                                    dronesBlasted = it.dronesBlasted + 1,
                                    multiplier = min(5.0f, it.multiplier + 0.15f)
                                )
                            }
                        }
                        hit = true
                        break
                    }
                }
            }

            if (hit || laser.lifetime <= 0f) {
                laserIter.remove()
            }
        }

        // Update Obstacles & Rotations
        val obsIter = obstacles.iterator()
        while (obsIter.hasNext()) {
            val obs = obsIter.next()
            obs.rotation = obs.rotation + obs.rotSpeed * dt

            // Check collision with player
            if (!obs.isDestroyed && !state.isGameOver) {
                val playerActualPos = Vec3(playerPos.x, playerPos.y + playerElevation, playerPos.z)
                val dist = (playerActualPos - obs.position).length()

                if (obs.type == ObstacleType.SPEED_RING) {
                    // Flying through a speed ring awards multiplier & boost!
                    if (dist < obs.radius + 0.5f) {
                        obs.isDestroyed = true
                        soundSynth.playBoost()
                        spawnSpeedRingParticles(obs.position)
                        _uiState.update {
                            it.copy(
                                score = it.score + (300 * it.multiplier).toInt(),
                                multiplier = min(6.0f, it.multiplier + 0.5f),
                                speed = min(50f, it.speed + 1.5f)
                            )
                        }
                    }
                } else {
                    // Hazard collision
                    if (dist < obs.radius + 0.7f) {
                        if (isOverdrive) {
                            // Shatter obstacle in overdrive!
                            obs.isDestroyed = true
                            soundSynth.playExplosion()
                            spawnExplosionParticles(obs.position, obs.radius)
                        } else {
                            obs.isDestroyed = true
                            handlePlayerHit()
                        }
                    }
                }
            }

            // Clean up behind player
            if (obs.position.z < playerPos.z - 10f) {
                obsIter.remove()
            }
        }

        // Update Collectibles
        val colIter = collectibles.iterator()
        while (colIter.hasNext()) {
            val col = colIter.next()
            col.rotationY += dt * 3.2f

            if (!col.isCollected && !state.isGameOver) {
                val playerActualPos = Vec3(playerPos.x, playerPos.y + playerElevation, playerPos.z)
                val dist = (playerActualPos - col.position).length()

                if (dist < col.radius + 0.8f) {
                    col.isCollected = true
                    when (col.type) {
                        CollectibleType.ENERGY_CORE -> {
                            soundSynth.playPickup()
                            spawnCollectParticles(col.position, Color(0xFF00FFFF))
                            _uiState.update {
                                val gained = (50 * it.multiplier).toInt()
                                it.copy(
                                    score = it.score + gained,
                                    coresCollected = it.coresCollected + 1,
                                    multiplier = min(5.0f, it.multiplier + 0.05f)
                                )
                            }
                        }
                        CollectibleType.SHIELD_BOOST -> {
                            soundSynth.playPickup()
                            spawnCollectParticles(col.position, Color(0xFF00FF88))
                            _uiState.update {
                                it.copy(shields = min(it.maxShields, it.shields + 1))
                            }
                        }
                        CollectibleType.HYPER_OVERDRIVE -> {
                            soundSynth.playBoost()
                            spawnCollectParticles(col.position, Color(0xFFFFD700))
                            isOverdrive = true
                            overdriveTimeLeft = 4.5f
                        }
                    }
                }
            }

            if (col.position.z < playerPos.z - 10f) {
                colIter.remove()
            }
        }

        // Update 3D Particles
        val partIter = particles.iterator()
        while (partIter.hasNext()) {
            val p = partIter.next()
            p.position = p.position + p.velocity * dt
            p.life -= dt
            if (p.life <= 0f) {
                partIter.remove()
            }
        }

        // Spawn Thruster Exhaust Particles
        spawnThrusterParticles()

        // Procedural track generation ahead
        if (lastSpawnZ < playerPos.z + 130f) {
            spawnAhead(60f)
        }

        // Score increment by distance
        val newScore = state.score + (distanceTraveled * 2f * state.multiplier).toInt()
        val newDistance = state.distance + distanceTraveled
        val updatedHighScore = max(state.highScore, newScore)

        _uiState.update {
            it.copy(
                score = newScore,
                highScore = updatedHighScore,
                distance = newDistance,
                isOverdrive = isOverdrive,
                overdriveTimeLeft = overdriveTimeLeft,
                activeParticles = particles.size
            )
        }
    }

    private fun handlePlayerHit() {
        val state = _uiState.value
        soundSynth.playExplosion()
        spawnExplosionParticles(playerPos, 1.2f)

        if (state.shields > 1) {
            _uiState.update {
                it.copy(
                    shields = it.shields - 1,
                    multiplier = 1.0f // resets combo
                )
            }
        } else {
            // Game Over
            soundSynth.stopSynthwaveMusic()
            _uiState.update {
                it.copy(
                    shields = 0,
                    isGameOver = true,
                    highScore = max(it.highScore, it.score)
                )
            }
        }
    }

    private fun spawnAhead(spanZ: Float) {
        val targetZ = lastSpawnZ + spanZ
        var currentZ = lastSpawnZ
        val rand = Random

        while (currentZ < targetZ) {
            currentZ += rand.nextFloat() * 10f + 14f

            // Lanes: Left (-3.2f), Center (0f), Right (3.2f)
            val lanes = floatArrayOf(-3.0f, 0f, 3.0f)
            val laneIndex = rand.nextInt(3)
            val laneX = lanes[laneIndex]

            val rollType = rand.nextFloat()
            if (rollType < 0.35f) {
                // Hazard Drone
                obstacles.add(
                    Obstacle(
                        id = nextEntityId++,
                        type = ObstacleType.DRONE,
                        position = Vec3(laneX, 1.0f + rand.nextFloat() * 0.8f, currentZ),
                        rotSpeed = Vec3(rand.nextFloat() * 2f, rand.nextFloat() * 3f, 0f),
                        radius = 0.9f
                    )
                )
            } else if (rollType < 0.65f) {
                // Hazard Obelisk / Pyramid
                obstacles.add(
                    Obstacle(
                        id = nextEntityId++,
                        type = ObstacleType.PYRAMID_SPIKE,
                        position = Vec3(laneX, 0f, currentZ),
                        radius = 1.1f
                    )
                )
            } else if (rollType < 0.82f) {
                // Speed Arch Ring
                obstacles.add(
                    Obstacle(
                        id = nextEntityId++,
                        type = ObstacleType.SPEED_RING,
                        position = Vec3(laneX, 0f, currentZ),
                        radius = 1.4f
                    )
                )
            }

            // Also place collectibles in unoccupied lane or in air
            val collectLane = lanes[(laneIndex + 1 + rand.nextInt(2)) % 3]
            val collectType = when {
                rand.nextFloat() < 0.08f -> CollectibleType.HYPER_OVERDRIVE
                rand.nextFloat() < 0.18f -> CollectibleType.SHIELD_BOOST
                else -> CollectibleType.ENERGY_CORE
            }
            collectibles.add(
                Collectible(
                    id = nextEntityId++,
                    type = collectType,
                    position = Vec3(collectLane, 0.7f + rand.nextFloat() * 0.5f, currentZ + 4f)
                )
            )
        }
        lastSpawnZ = targetZ
    }

    private fun spawnThrusterParticles() {
        val shipColor = _uiState.value.currentShip.accentColor
        val pos = playerPos
        val elev = playerElevation

        // Left and right thruster jet streams
        val rand = Random
        val jitterX = (rand.nextFloat() - 0.5f) * 0.2f
        val jitterY = (rand.nextFloat() - 0.5f) * 0.2f

        particles.add(
            Particle3D(
                position = Vec3(pos.x - 0.4f + jitterX, pos.y + elev + 0.1f + jitterY, pos.z - 1.8f),
                velocity = Vec3(jitterX * 2f, jitterY * 2f, -12f - rand.nextFloat() * 8f),
                color = if (_uiState.value.isOverdrive) Color(0xFFFFE600) else shipColor,
                life = 0.25f,
                maxLife = 0.25f,
                size = 3.5f
            )
        )
        particles.add(
            Particle3D(
                position = Vec3(pos.x + 0.4f + jitterX, pos.y + elev + 0.1f + jitterY, pos.z - 1.8f),
                velocity = Vec3(jitterX * 2f, jitterY * 2f, -12f - rand.nextFloat() * 8f),
                color = if (_uiState.value.isOverdrive) Color(0xFFFFE600) else shipColor,
                life = 0.25f,
                maxLife = 0.25f,
                size = 3.5f
            )
        )
    }

    private fun spawnExplosionParticles(center: Vec3, radius: Float) {
        val rand = Random
        for (i in 0 until 18) {
            val vx = (rand.nextFloat() - 0.5f) * 16f
            val vy = (rand.nextFloat() - 0.3f) * 16f
            val vz = (rand.nextFloat() - 0.5f) * 16f
            val col = when (rand.nextInt(3)) {
                0 -> Color(0xFFFF0055)
                1 -> Color(0xFFFF7700)
                else -> Color(0xFFFFEA00)
            }
            particles.add(
                Particle3D(
                    position = center,
                    velocity = Vec3(vx, vy, vz),
                    color = col,
                    life = 0.45f + rand.nextFloat() * 0.25f,
                    maxLife = 0.7f,
                    size = 5.0f
                )
            )
        }
    }

    private fun spawnCollectParticles(center: Vec3, color: Color) {
        val rand = Random
        for (i in 0 until 12) {
            val vx = (rand.nextFloat() - 0.5f) * 8f
            val vy = (rand.nextFloat() - 0.5f) * 8f
            val vz = (rand.nextFloat() - 0.5f) * 8f
            particles.add(
                Particle3D(
                    position = center,
                    velocity = Vec3(vx, vy, vz),
                    color = color,
                    life = 0.35f,
                    maxLife = 0.35f,
                    size = 4f
                )
            )
        }
    }

    private fun spawnSpeedRingParticles(center: Vec3) {
        val rand = Random
        for (i in 0 until 16) {
            val angle = rand.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 8f + rand.nextFloat() * 10f
            particles.add(
                Particle3D(
                    position = center,
                    velocity = Vec3(cos(angle) * speed, sin(angle) * speed, 15f),
                    color = Color(0xFF00FFCC),
                    life = 0.5f,
                    maxLife = 0.5f,
                    size = 4.5f
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundSynth.stopSynthwaveMusic()
    }
}

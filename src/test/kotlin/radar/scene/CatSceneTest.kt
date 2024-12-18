package radar.scene

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import radar.collisionDetection.BruteForceCollisionDetection
import radar.generators.MoveGenerator
import kotlin.math.sqrt
import kotlin.random.Random
import radar.scene.*

class CatSceneTest {

    private lateinit var sceneConfig: SceneConfig
    private lateinit var particles: ArrayList<CatParticle>
    private lateinit var scene: CatScene

    @BeforeEach
    fun setUp() {
        sceneConfig = SceneConfig()
        sceneConfig.particleCount = 5
        sceneConfig.hissDist = 2.0
        sceneConfig.fightDist = 1.0
        particles = ArrayList()
        for (i in 0 until sceneConfig.particleCount) {
            particles.add(CatParticle(Point2D(Random.nextDouble(), Random.nextDouble())))
        }

        scene = CatScene(particles, sceneConfig)
    }

    @Test
    fun `test calcNewState returns FIGHT for close particles`() {
        val state = scene.calcNewState(0.5)
        assertEquals(CatStates.FIGHT, state)
    }

    @Test
    fun `test calcNewState returns CALM for distant particles`() {
        val state = scene.calcNewState(3.0)
        assertEquals(CatStates.CALM, state)
    }

    @Test
    fun test resetStates sets all to CALM()
    {
        particles[0].setCatState(CatStates.HISS)
        particles[1].setCatState(CatStates.FIGHT)
        scene.updateScene(MoveGenerator(sceneConfig))

        particles.forEach { particle ->
            assertEquals(CatStates.CALM, particle.state)
        }
    }
}

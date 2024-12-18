package radar.scene

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import radar.generators.MoveGenerator
import kotlin.random.Random

class CatSceneTest {

    private lateinit var sceneConfig: SceneConfig
    private lateinit var particles: ArrayList<CatParticle>
    private lateinit var scene: CatScene

    @BeforeEach
    fun setUp() {
        sceneConfig = SceneConfig()
        sceneConfig.particleCount = 5
        sceneConfig.hissDist = 2
        sceneConfig.fightDist = 1
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
    fun `test updateScene moves particles and updates states`() {
        val particle1 = CatParticle(Point2D(0.0, 0.0), CatStates.HISS)
        val particle2 = CatParticle(Point2D(1.5, 1.5), CatStates.FIGHT)
        particles = arrayListOf(particle1, particle2)
        scene = CatScene(particles, sceneConfig)
        scene.updateScene(MoveGenerator(sceneConfig))
        assertNotEquals(CatStates.CALM, particle1.state)
        assertNotEquals(CatStates.CALM, particle2.state)
    }
}

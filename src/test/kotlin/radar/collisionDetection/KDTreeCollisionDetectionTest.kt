package radar.collisionDetection

import CollisionDetection.Companion.THREAD_COUNT
import core.base.BaseCollisionDetection
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import radar.generators.CatGenerator
import radar.generators.MoveGenerator
import radar.scene.CatCollision
import radar.scene.CatEmitter
import radar.scene.CatParticle
import radar.scene.CatScene
import radar.scene.CatStates
import radar.scene.Offset2D
import radar.scene.Point2D
import radar.scene.SceneConfig
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class KDTreeCollisionDetectionTest {
    companion object {
        private const val COMPARE_WITH_BF_MAX_CAT_COUNT = 1000
        private const val COMPARE_WITH_BF_TEST_COUNT = 10000

        @JvmStatic
        fun particlesAndCollisionCount() =
            listOf(
                Arguments.of(
                    arrayListOf(
                        CatParticle(Point2D(0.0, 0.0)),
                        CatParticle(Point2D(1.0, 1.0)),
                        CatParticle(Point2D(4.0, 4.0)),
                    ),
                    1,
                    2,
                    SceneConfig().apply {
                        fightDist = 2
                        hissDist = 5
                        metric = SceneConfig.Companion.MetricType.EUCLIDEAN
                    },
                ),
                Arguments.of(
                    arrayListOf(
                        CatParticle(Point2D(0.0, 0.0)),
                        CatParticle(Point2D(1.0, 1.0)),
                        CatParticle(Point2D(4.0, 4.0)),
                    ),
                    1,
                    1,
                    SceneConfig().apply {
                        fightDist = 3
                        hissDist = 5
                        metric = SceneConfig.Companion.MetricType.MANHATTAN
                    },
                ),
            )

        @JvmStatic
        fun randomParticles(): List<Arguments> {
            val bruteForceCollisionDetection = BruteForceCollisionDetection()
            val workerPool: ExecutorService = Executors.newFixedThreadPool(THREAD_COUNT)
            val kdTreeCollisionDetection = KDTreeCollisionDetection(workerPool, THREAD_COUNT)
            val catEmitter = CatEmitter(CatGenerator())
            val config =
                SceneConfig().apply {
                    fightDist = 3
                    hissDist = 5
                    metric = SceneConfig.Companion.MetricType.EUCLIDEAN
                }

            fun generateCats(n: Int): ArrayList<CatParticle> {
                val catsList = ArrayList<CatParticle>(n)
                catsList.addAll(catEmitter.emit(n))
                return catsList
            }

            return mutableListOf<Arguments>().apply {
                repeat(COMPARE_WITH_BF_TEST_COUNT) {
                    add(
                        Arguments.of(
                            generateCats(Random.nextInt(COMPARE_WITH_BF_MAX_CAT_COUNT)),
                            config,
                            bruteForceCollisionDetection,
                            kdTreeCollisionDetection,
                        ),
                    )
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("particlesAndCollisionCount")
    fun `test findCollisions detects correct number of collisions`(
        particles: ArrayList<CatParticle>,
        minCollisionCount: Int,
        maxCollisionCount: Int,
        config: SceneConfig,
    ) {
        val scene =
            CatScene(
                particles,
                config,
                object : CatEmitter(
                    CatGenerator(),
                ) {
                    override fun emit(n: Int): Set<CatParticle> {
                        return setOf() // don't let cats spawn
                    }
                },
            )
        val collisions = scene.findCollisions()
        assertTrue {
            collisions.size in minCollisionCount..maxCollisionCount
        }
    }

    @ParameterizedTest
    @MethodSource("randomParticles")
    fun `test KDTree detects same fighters as brute force`(
        particles: ArrayList<CatParticle>,
        config: SceneConfig,
        bfCD: BruteForceCollisionDetection,
        kdTreeCD: KDTreeCollisionDetection,
    ) {
        fun getCollisions(
            cd: BaseCollisionDetection<CatScene, CatParticle, Point2D, Offset2D, CatCollision, MoveGenerator>,
        ): Array<CatCollision> {
            val scene =
                CatScene(
                    particles,
                    config,
                    object : CatEmitter(
                        CatGenerator(),
                    ) {
                        override fun emit(n: Int): Set<CatParticle> = setOf()
                    },
                    cd,
                )
            return scene.findCollisions()
        }

        fun Array<CatCollision>.onlyFightCollisions(): List<CatCollision> =
            this.filter {
                it.catState == CatStates.FIGHT
            }

        val bfFightCollisions = getCollisions(bfCD).onlyFightCollisions()
        val kdTreeFightCollisions = getCollisions(kdTreeCD).onlyFightCollisions()
        assertTrue {
            bfFightCollisions.all { bfCollision ->
                kdTreeFightCollisions.count { kdTreeCollision ->
                    kdTreeCollision.particle1 == bfCollision.particle1 &&
                            kdTreeCollision.particle2 == bfCollision.particle2 ||
                            kdTreeCollision.particle2 == bfCollision.particle1 &&
                            kdTreeCollision.particle1 == bfCollision.particle2
                } == 1
            }
        }
    }

    private val workerPool = Executors.newFixedThreadPool(10)
    private val collisionDetection = KDTreeCollisionDetection(workerPool, 10)

    @Test
    fun `test findCollisions with no particles`() {
        repeat(100) {
            val scene = CatScene(ArrayList<CatParticle>(), getSceneConfig())
            val collisions = collisionDetection.findCollisions(scene)

            // all cats must have calm state
            assertTrue(collisions.count { it ->
                it.particle1.state == CatStates.CALM && it.particle2.state == CatStates.CALM
            } == collisions.size)
        }
    }

    @Test
    fun `test with many particles`() {
        val particles = (1..10_000).map { id ->
            CatParticle(Point2D(Math.random() * 100, Math.random() * 100))
        } as ArrayList<CatParticle>
        val scene = CatScene(particles, getSceneConfig(20))
        val collisions = collisionDetection.findCollisions(scene)

        assertNotNull(collisions)
    }

    @Test
    fun `test batch size`() {
        var particles = ArrayList<CatParticle>(100)
        repeat(100) {
            particles.add(CatParticle(Point2D(Math.random() * 100, Math.random() * 100)))
        }
        val scene = CatScene(particles, getSceneConfig(10))

        // check that batchSize change after findCollisions
        CollisionDetection.batchSize = 1
        collisionDetection.findCollisions(scene)
        assertTrue(CollisionDetection.batchSize != 0 && CollisionDetection.batchSize > 0)
    }


    private fun getSceneConfig(hissDist: Int = 1): SceneConfig {
        val scene = SceneConfig()
        scene.hissDist = hissDist
        return scene
    }
}

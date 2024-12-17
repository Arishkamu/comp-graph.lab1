package radar.scene

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import radar.generators.CatGenerator

class CatEmitterTest {
    @Test
    fun `test emit generates correct number of particles`() {
        val generator = CatGenerator()
        val emitter = CatEmitter(particleGenerator = generator)
        val cats = emitter.emit(5)
        assertEquals(5, cats.size)
    }

    @Test
    fun `test emit generates particles at correct boundaries`() {
        val generator = CatGenerator()
        val emitter = CatEmitter(particleGenerator = generator)
        val particles = emitter.emit(100)

        for (particle in particles) {
            val position = particle.coordinates
            assertTrue(
                position.x == 0.0 || position.y == 0.0,
                "Частицы должны создаваться либо на вертикальной границе, либо на горизонтальной"
            )
        }
    }

    @Test
    fun `test emit generates unique particles`() {
        val generator = CatGenerator()
        val emitter = CatEmitter(particleGenerator = generator)
        val particles = emitter.emit(10)

        val uniquePositions = particles.map { it.coordinates }.toSet()
        assertEquals(
            particles.size, uniquePositions.size,
            "Все сгенерированные частицы должны быть уникальными по позиции"
        )
    }

    @Test
    fun `test emit does not generate particles when n is zero`() {
        val generator = CatGenerator()
        val emitter = CatEmitter(particleGenerator = generator)
        val particles = emitter.emit(0)
        assertTrue(particles.isEmpty(), "Должно быть создано 0 частиц, если n = 0")
    }
}

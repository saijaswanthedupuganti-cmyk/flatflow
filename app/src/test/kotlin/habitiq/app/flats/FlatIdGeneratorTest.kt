package habitiq.app.flats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FlatIdGeneratorTest {

    @Test
    fun `generated id has FLAT dash prefix and 4 character code`() {
        val id = generateFlatId(Random(seed = 42))
        assertTrue(id.startsWith("FLAT-"))
        assertEquals(9, id.length) // "FLAT-" (5) + 4 chars
    }

    @Test
    fun `generated id only uses the unambiguous alphabet`() {
        val id = generateFlatId(Random(seed = 1))
        val code = id.removePrefix("FLAT-")
        val allowed = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toSet()
        assertTrue(code.all { it in allowed })
    }

    @Test
    fun `same seed produces the same id (deterministic for testing)`() {
        val first = generateFlatId(Random(seed = 7))
        val second = generateFlatId(Random(seed = 7))
        assertEquals(first, second)
    }

    @Test
    fun `valid format is accepted`() {
        assertTrue(isValidFlatIdFormat("FLAT-A3B9"))
    }

    @Test
    fun `missing prefix is rejected`() {
        assertFalse(isValidFlatIdFormat("A3B9"))
    }

    @Test
    fun `wrong code length is rejected`() {
        assertFalse(isValidFlatIdFormat("FLAT-A3B"))
        assertFalse(isValidFlatIdFormat("FLAT-A3B99"))
    }

    @Test
    fun `ambiguous characters like O, 0, I, 1 are rejected`() {
        assertFalse(isValidFlatIdFormat("FLAT-O0I1"))
    }

    @Test
    fun `lowercase is rejected (codes are always uppercase)`() {
        assertFalse(isValidFlatIdFormat("flat-a3b9"))
    }
}

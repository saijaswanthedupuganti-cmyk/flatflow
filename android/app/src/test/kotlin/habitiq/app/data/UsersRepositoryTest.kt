package habitiq.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsersRepositoryTest {

    @Test
    fun `should create document when no existing data`() {
        assertTrue(shouldCreateDocument(existingData = null))
    }

    @Test
    fun `should not create document when data already exists`() {
        assertFalse(shouldCreateDocument(existingData = mapOf("email" to "a@b.com")))
    }

    @Test
    fun `should create document when existing data is empty map`() {
        assertTrue(shouldCreateDocument(existingData = emptyMap()))
    }
}

package habitiq.app.flats

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class FlatErrorMapperTest {

    @Test
    fun `flat not found maps to plain not-found message`() {
        assertEquals(
            "Flat not found. Check the invite code and try again.",
            mapFlatError(FlatNotFoundException())
        )
    }

    @Test
    fun `flat full maps to plain full message`() {
        assertEquals(
            "This flat is full (maximum 8 members).",
            mapFlatError(FlatFullException())
        )
    }

    @Test
    fun `already member maps to plain already-member message`() {
        assertEquals(
            "You are already a member of this flat.",
            mapFlatError(AlreadyMemberException())
        )
    }

    @Test
    fun `network error maps to connectivity message`() {
        assertEquals(
            "No internet connection. Please try again.",
            mapFlatError(IOException("network down"))
        )
    }

    @Test
    fun `unknown error maps to generic fallback message`() {
        assertEquals(
            "Something went wrong. Please try again.",
            mapFlatError(RuntimeException("something else"))
        )
    }
}

package habitiq.app.flats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class InviteCodesTest {

    @Test fun generatedCodesAreSixReadableCharacters() {
        val random = Random(7)
        repeat(200) {
            val code = generateInviteCode(random)
            assertEquals(InviteInput.Code(code), parseInviteInput(code))
            assertTrue(code.none { it in "01IO" })
        }
    }

    @Test fun forgivingAboutCaseSpacesAndDashes() {
        assertEquals(InviteInput.Code("K7QM2P"), parseInviteInput(" k7q-m2p "))
        assertEquals(InviteInput.Code("K7QM2P"), parseInviteInput("K7Q M2P"))
    }

    @Test fun oldFlatIdsAreLegacyNotCodes() {
        assertEquals(InviteInput.LegacyFlatId("FLAT-A3B9"), parseInviteInput("flat-a3b9"))
        assertEquals(InviteInput.LegacyFlatId("FLAT-A3B9K7"), parseInviteInput("FLAT-A3B9K7"))
    }

    @Test fun rejectsAnythingElse() {
        listOf("", "K7QM2", "K7QM2PX", "K7QM2O", "FLAT-", "hello!").forEach {
            assertEquals(it, InviteInput.Invalid, parseInviteInput(it))
        }
    }
}

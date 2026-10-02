package habitiq.app.lib

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyUtilsTest {
    @Test fun `formats whole rupees with Indian grouping`() {
        assertEquals("₹1,23,456", formatInr(123456.0))
    }

    @Test fun `retains paise instead of truncating`() {
        assertEquals("₹1,200.50", formatInr(1200.50))
    }

    @Test fun `uses magnitude for a signed balance label`() {
        assertEquals("₹999.25", formatInr(-999.25))
    }
}

class ParsePaiseTest {
    @org.junit.Test
    fun parsesPlainAndGroupedAmounts() {
        org.junit.Assert.assertEquals(120000L, parsePaise("1200"))
        org.junit.Assert.assertEquals(120000L, parsePaise("1,200"))
        org.junit.Assert.assertEquals(85050L, parsePaise(" 850.50 "))
    }

    @org.junit.Test
    fun rejectsBlankNegativeAndNonNumeric() {
        org.junit.Assert.assertNull(parsePaise(""))
        org.junit.Assert.assertNull(parsePaise("-5"))
        org.junit.Assert.assertNull(parsePaise("abc"))
    }

    @org.junit.Test
    fun customSharesThatSplitAThousandThreeWaysCanSumExactly() {
        // Floating point cannot guarantee this; whole paise can.
        val total = parsePaise("1000")!!
        val shares = listOf("333.34", "333.33", "333.33").map { parsePaise(it)!! }
        org.junit.Assert.assertEquals(total, shares.sum())
    }

    @org.junit.Test
    fun roundsExtraDecimalsHalfUp() {
        org.junit.Assert.assertEquals(1001L, parsePaise("10.005"))
    }
}

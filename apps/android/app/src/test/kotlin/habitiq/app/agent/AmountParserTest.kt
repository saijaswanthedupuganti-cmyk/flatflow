package habitiq.app.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountParserTest {
    private fun paise(raw: String) = findAmounts(normalizeUtterance(raw)).map { it.paise }

    @Test fun `normalises punctuation currency and apostrophes`() {
        assertEquals("bought milk for ₹60", normalizeUtterance("Bought milk for₹60!"))
        assertEquals("whats due today", normalizeUtterance("What's due today?"))
        assertEquals("rs 560 paid", normalizeUtterance("Rs.560 paid."))
        assertEquals("1,200 and 5.6k", normalizeUtterance("1,200, and 5.6k"))
    }

    @Test fun `digit amounts in every common form`() {
        assertEquals(listOf(56_000L), paise("₹560"))
        assertEquals(listOf(56_000L), paise("560"))
        assertEquals(listOf(56_000L), paise("560 rs"))
        assertEquals(listOf(56_000L), paise("rs. 560"))
        assertEquals(listOf(56_000L), paise("560 rupees"))
        assertEquals(listOf(120_000L), paise("1,200"))
        assertEquals(listOf(560_000L), paise("5.6k"))
        assertEquals(listOf(12_000_000L), paise("1.2 lakh"))
        assertEquals(listOf(4_550L), paise("45.50"))
    }

    @Test fun `number words`() {
        assertEquals(listOf(50_000L), paise("five hundred"))
        assertEquals(listOf(25_000L), paise("two hundred and fifty rupees"))
        assertEquals(listOf(300_000L), paise("three thousand"))
        assertEquals(listOf(6_000L), paise("sixty"))
        assertEquals(emptyList<Long>(), paise("one"))
    }

    @Test fun `non-money numbers are skipped`() {
        assertEquals(emptyList<Long>(), paise("2 bhk"))
        assertEquals(emptyList<Long>(), paise("on 4 oct"))
        assertEquals(emptyList<Long>(), paise("oct 4"))
        assertEquals(emptyList<Long>(), paise("3 days"))
        assertEquals(listOf(1_000_000L), paise("2 bhk under 10k"))
        assertEquals(emptyList<Long>(), paise("12 eggs"))
        assertEquals(emptyList<Long>(), paise("2 kg"))
        assertEquals(emptyList<Long>(), paise("3 bedroom"))
        assertEquals(emptyList<Long>(), paise("fifteen days"))
        assertEquals(emptyList<Long>(), paise("twenty litres"))
    }

    @Test fun `ranges point at the amount text`() {
        val text = normalizeUtterance("bought milk 60")
        val match = findAmounts(text).single()
        assertEquals("60", text.substring(match.range).trim())
    }
}

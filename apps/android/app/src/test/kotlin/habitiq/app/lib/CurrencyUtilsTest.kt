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

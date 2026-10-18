package habitiq.app.flats

import org.junit.Assert.assertEquals
import org.junit.Test

class EffectiveJoinModeTest {
    @Test fun `a flat listed in Discover always needs the admin's approval`() {
        assertEquals("approval", effectiveJoinMode("auto", vacancyActive = true))
        assertEquals("approval", effectiveJoinMode(null, vacancyActive = true))
    }

    @Test fun `an unlisted flat keeps its own join mode`() {
        assertEquals("auto", effectiveJoinMode("auto", vacancyActive = false))
        assertEquals("auto", effectiveJoinMode(null, vacancyActive = false))
        assertEquals("approval", effectiveJoinMode("approval", vacancyActive = false))
    }
}

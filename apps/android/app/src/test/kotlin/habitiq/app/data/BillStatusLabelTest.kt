package habitiq.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class BillStatusLabelTest {
    @Test fun `engine states map to the product vocabulary`() {
        assertEquals("Upcoming", billStatusLabel("pending"))
        assertEquals("Shares ready", billStatusLabel("split_generated"))
        assertEquals("Paid", billStatusLabel("paid"))
        assertEquals("Skipped", billStatusLabel("skipped"))
    }

    @Test fun `unknown states never leak raw engine text`() {
        assertEquals("Upcoming", billStatusLabel("something_new"))
    }
}

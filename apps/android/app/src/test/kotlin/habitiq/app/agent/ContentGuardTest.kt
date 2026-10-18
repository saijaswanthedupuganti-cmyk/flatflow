package habitiq.app.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContentGuardTest {
    @Test fun `illegal goods and services are refused`() {
        listOf("bought ganja 500", "Weed for 800 split with ravi", "paid 2000 for mdma", "spent 300 on charas",
            "bribe to watchman 500", "hawala 10000", "satta 1000", "bought a pistol 5000")
            .forEach { assertEquals(it, ContentGuard.REFUSAL, ContentGuard.reasonToBlock(it)) }
    }

    @Test fun `harassment and threats are refused`() {
        listOf("ravi is a useless idiot add 200", "I will kill priya", "beat him up 500")
            .forEach { assertEquals(it, ContentGuard.REFUSAL, ContentGuard.reasonToBlock(it)) }
    }

    @Test fun `everyday household words are not blocked`() {
        listOf("bought groceries 560", "paid ravi 200", "weeding the balcony plants 150", "coke and chips 120",
            "gas cylinder 1100", "pest control 900", "killed the cockroaches, bought spray 250", "what do i owe")
            .forEach { assertNull(it, ContentGuard.reasonToBlock(it)) }
    }

    @Test fun `household phrases that contain a blocked word are allowed`() {
        listOf("hash browns 180", "bought weed killer 350", "glue gun 400", "water gun for the kids 150",
            "medicines from the pharmacy 600", "drugs from the medical store 450", "I will kill the cockroaches tonight",
            "will kill the mosquitoes with a racket 300")
            .forEach { assertNull(it, ContentGuard.reasonToBlock(it)) }
    }

    @Test fun `allow-listed phrases do not launder a real refusal`() {
        listOf("glue gun and a pistol 5000", "hash browns and ganja 800", "I will kill the cockroaches and then kill her")
            .forEach { assertEquals(it, ContentGuard.REFUSAL, ContentGuard.reasonToBlock(it)) }
    }
}

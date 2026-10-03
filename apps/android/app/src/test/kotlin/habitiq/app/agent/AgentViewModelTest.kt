package habitiq.app.agent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeSpeech : SpeechSource {
    override val events = MutableSharedFlow<SpeechEvent>(extraBufferCapacity = 16)
    var started = 0
    var stopped = 0
    var released = false
    override fun start() { started++ }
    override fun stop() { stopped++ }
    override fun release() { released = true }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AgentViewModelTest {
    private val speech = FakeSpeech()
    private var household = Fixtures.state.copy(netBalances = mapOf("u1" to -350.0, "u2" to 350.0))
    private lateinit var vm: AgentViewModel

    @Before fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        vm = AgentViewModel({ household }, speech, { Fixtures.today })
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `voice question flows listening to checking to answer`() = runTest {
        vm.startListening()
        assertEquals(AgentUiState.Listening(""), vm.state.value)
        assertEquals(1, speech.started)
        advanceUntilIdle()
        speech.events.emit(SpeechEvent.Partial("what do"))
        advanceUntilIdle()
        assertEquals(AgentUiState.Listening("what do"), vm.state.value)
        speech.events.emit(SpeechEvent.Final("What do I owe?"))
        advanceUntilIdle()
        val s = vm.state.value as AgentUiState.Answer
        assertEquals("What do I owe?", s.transcript)
        assertEquals("You owe ₹350", s.answer.headline)
    }

    @Test fun `voice level follows the mic only while listening`() = runTest {
        vm.startListening()
        advanceUntilIdle()
        speech.events.emit(SpeechEvent.Level(0.7f))
        advanceUntilIdle()
        assertEquals(0.7f, vm.level.value, 0.001f)
        speech.events.emit(SpeechEvent.Final("what do i owe"))
        advanceUntilIdle()
        assertEquals(0f, vm.level.value, 0.001f)
        speech.events.emit(SpeechEvent.Level(0.9f))
        advanceUntilIdle()
        assertEquals(0f, vm.level.value, 0.001f)
    }

    @Test fun `lazy spend becomes an expense card`() = runTest {
        vm.submitText("I just spent 500")
        advanceUntilIdle()
        val card = (vm.state.value as AgentUiState.Preview).card as AgentCard.Expense
        assertEquals("Expense", card.title)
        assertEquals(50_000L, card.amountPaise)
    }

    @Test fun `typed expense shows a read-only preview`() = runTest {
        vm.submitText("bought milk 60")
        assertEquals(AgentUiState.Checking("bought milk 60"), vm.state.value)
        advanceUntilIdle()
        val s = vm.state.value as AgentUiState.Preview
        assertEquals("Add ₹60 for Milk, split 4 ways", s.summary)
        assertEquals(AgentCard.Expense("Milk", 6_000, "lifestyle", "You", listOf("You", "Ravi", "Priya", "Arjun"), true), s.card)
    }

    @Test fun `clarify then choose`() = runTest {
        household = household.copy(members = household.members + AgentMember("u5", "Ravi", "Ravi Teja"))
        vm.submitText("paid ravi 200")
        advanceUntilIdle()
        val c = vm.state.value as AgentUiState.Clarify
        assertEquals("Which Ravi?", c.question)
        vm.choose(c.options[1])
        val p = vm.state.value as AgentUiState.Preview
        assertEquals("Record ₹200 you paid Ravi", p.summary)
        assertEquals(AgentCard.Payment("You", "Ravi", 20_000), p.card)
    }

    @Test fun `misuse is refused before parsing`() = runTest {
        vm.submitText("bought ganja 500")
        advanceUntilIdle()
        assertEquals(AgentUiState.NotUnderstood("bought ganja 500", ContentGuard.REFUSAL), vm.state.value)
    }

    @Test fun `unknown request is not understood`() = runTest {
        vm.submitText("remind me to call mom")
        advanceUntilIdle()
        assertTrue(vm.state.value is AgentUiState.NotUnderstood)
    }

    @Test fun `unsupported plan shows its reason`() = runTest {
        household = HouseholdState.Empty.copy(myUid = "u1")
        vm.submitText("bought milk 60")
        advanceUntilIdle()
        assertEquals(AgentUiState.NotUnderstood("bought milk 60", "Join or create a flat first to track money."), vm.state.value)
    }

    @Test fun `no match keeps voice mode with a retry message`() = runTest {
        vm.startListening()
        advanceUntilIdle()
        speech.events.emit(SpeechEvent.Error(SpeechErrorKind.NoMatch))
        advanceUntilIdle()
        assertEquals(AgentUiState.Error("", "Didn't catch that. Try again or type it."), vm.state.value)
        assertEquals(AgentInputMode.Voice, vm.mode.value)
    }

    @Test fun `permission error switches to text mode`() = runTest {
        vm.startListening()
        advanceUntilIdle()
        speech.events.emit(SpeechEvent.Error(SpeechErrorKind.NoPermission))
        advanceUntilIdle()
        assertEquals(AgentInputMode.Text, vm.mode.value)
        assertEquals(AgentUiState.Idle("Mic access is off. You can type instead."), vm.state.value)
    }

    @Test fun `unavailable recogniser switches to text mode`() = runTest {
        vm.startListening()
        advanceUntilIdle()
        speech.events.emit(SpeechEvent.Error(SpeechErrorKind.Unavailable))
        advanceUntilIdle()
        assertEquals(AgentInputMode.Text, vm.mode.value)
        assertEquals(AgentUiState.Idle("Voice isn't available on this phone. You can type instead."), vm.state.value)
    }

    @Test fun `blank text does nothing and reset stops listening`() = runTest {
        vm.submitText("   ")
        assertEquals(AgentUiState.Idle(), vm.state.value)
        vm.startListening()
        vm.reset()
        assertEquals(AgentUiState.Idle(), vm.state.value)
        assertEquals(1, speech.stopped)
    }
}

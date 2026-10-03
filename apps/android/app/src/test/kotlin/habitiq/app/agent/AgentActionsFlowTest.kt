package habitiq.app.agent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class RecordingSpeech : SpeechSource {
    override val events = MutableSharedFlow<SpeechEvent>(extraBufferCapacity = 16)
    var started = 0
    override fun start() { started++ }
    override fun stop() {}
    override fun cancel() {}
    override fun release() {}
}

private class FakeActions(var ok: Boolean = true) : AgentActions {
    val ran = mutableListOf<AgentStep>()
    override suspend fun run(step: AgentStep): Boolean { ran += step; return ok }
}

/** Speaks instantly and runs the completion callback, like a TTS engine that finished. */
private class FakeSpeaker : AgentSpeaker {
    val said = mutableListOf<String>()
    override fun speak(text: String, onDone: () -> Unit) { said += text; onDone() }
    override fun stop() {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class AgentActionsFlowTest {
    private val speech = RecordingSpeech()
    private val actions = FakeActions()
    private val speaker = FakeSpeaker()
    private var planner: AgentPlanner? = null
    private val household = Fixtures.state.copy(
        tasks = listOf(AgentTask("t1", "Wash dishes", "u1", Fixtures.today, "daily", "pending")),
    )
    private lateinit var vm: AgentViewModel

    @Before fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        val delegating = object : AgentPlanner {
            override suspend fun plan(text: String, state: HouseholdState, today: java.time.LocalDate) = planner?.plan(text, state, today)
        }
        vm = AgentViewModel({ household }, speech, { Fixtures.today }, actions = actions, speaker = speaker, planner = delegating)
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `tapping save stores the expense and confirms`() = runTest {
        vm.submitText("bought milk 60")
        advanceUntilIdle()
        assertTrue(vm.state.value is AgentUiState.Preview)
        vm.confirm()
        advanceUntilIdle()
        assertEquals(listOf<AgentStep>(AgentStep.AddExpense("Milk", 6_000, "lifestyle", "u1", Fixtures.allUids)), actions.ran)
        assertEquals(AgentUiState.Done("bought milk 60", "Saved ₹60 for Milk."), vm.state.value)
    }

    @Test fun `a failed save says so and keeps nothing hidden`() = runTest {
        actions.ok = false
        vm.submitText("bought milk 60")
        advanceUntilIdle()
        vm.confirm()
        advanceUntilIdle()
        assertEquals(AgentUiState.Error("bought milk 60", "Couldn't save that. Check your connection and try again."), vm.state.value)
    }

    @Test fun `spoken request asks to confirm and a spoken yes saves`() = runTest {
        vm.startListening()
        runCurrent()
        speech.events.emit(SpeechEvent.Final("I spent 500 on groceries"))
        advanceUntilIdle()
        assertTrue(speaker.said.last().endsWith("Shall I save it?"))
        val preview = vm.state.value as AgentUiState.Preview
        assertTrue(preview.awaitingVoice)
        assertEquals(2, speech.started) // listening again for the answer
        speech.events.emit(SpeechEvent.Final("yes please"))
        advanceUntilIdle()
        assertEquals(1, actions.ran.size)
        assertTrue(vm.state.value is AgentUiState.Done)
        assertEquals("Saved ₹500 for Groceries.", speaker.said.last())
    }

    @Test fun `a spoken no cancels without saving`() = runTest {
        vm.startListening()
        runCurrent()
        speech.events.emit(SpeechEvent.Final("spent 500 on groceries"))
        advanceUntilIdle()
        speech.events.emit(SpeechEvent.Final("no cancel"))
        advanceUntilIdle()
        assertTrue(actions.ran.isEmpty())
        assertEquals(AgentUiState.Idle(), vm.state.value)
    }

    @Test fun `typed requests are not spoken`() = runTest {
        vm.submitText("what do i owe")
        advanceUntilIdle()
        assertTrue(speaker.said.isEmpty())
    }

    @Test fun `spoken questions are answered out loud`() = runTest {
        vm.startListening()
        runCurrent()
        speech.events.emit(SpeechEvent.Final("what do I owe"))
        advanceUntilIdle()
        assertEquals("You're all settled.", speaker.said.single())
    }

    @Test fun `task done is saved after confirm`() = runTest {
        vm.submitText("dishes done")
        advanceUntilIdle()
        vm.confirm()
        advanceUntilIdle()
        assertEquals(listOf<AgentStep>(AgentStep.CompleteTask("t1", "Wash dishes")), actions.ran)
        assertEquals("Nice. Marked Wash dishes as done.", (vm.state.value as AgentUiState.Done).message)
    }

    @Test fun `navigation requests open the screen`() = runTest {
        val opened = mutableListOf<AgentLink>()
        val job = launch { vm.navigation.collect { opened += it } }
        vm.submitText("I want to join a flat")
        advanceUntilIdle()
        assertEquals(listOf(AgentLink.JOIN_FLAT), opened)
        job.cancel()
    }

    @Test fun `unknown requests go to the planner`() = runTest {
        planner = object : AgentPlanner {
            override suspend fun plan(text: String, state: HouseholdState, today: java.time.LocalDate) =
                AgentPlan.Reply(AgentAnswer("Rent is due on the 5th.", emptyList(), null))
        }
        vm.submitText("when is rent due")
        advanceUntilIdle()
        assertEquals("Rent is due on the 5th.", (vm.state.value as AgentUiState.Answer).answer.headline)
    }

    @Test fun `planner failure falls back to a helpful message`() = runTest {
        planner = object : AgentPlanner {
            override suspend fun plan(text: String, state: HouseholdState, today: java.time.LocalDate): AgentPlan? = null
        }
        vm.submitText("remind me to call mom")
        advanceUntilIdle()
        assertTrue(vm.state.value is AgentUiState.NotUnderstood)
    }
}

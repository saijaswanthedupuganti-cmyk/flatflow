package habitiq.app.agent

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import habitiq.app.flat.FlatViewModel
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** Strips characters that text-to-speech reads awkwardly. */
internal fun cleanForSpeech(text: String): String =
    text.replace("“", "").replace("”", "").replace("→", "to").replace("·", ",").replace(Regex("""\s+"""), " ").trim()

/**
 * Android text-to-speech in Indian English. Speech requested before the engine is ready is queued;
 * if the engine can't start, callbacks still run so the conversation never stalls.
 */
class AndroidSpeaker(context: Context) : AgentSpeaker, TextToSpeech.OnInitListener {
    private val main = Handler(Looper.getMainLooper())
    private val callbacks = ConcurrentHashMap<String, () -> Unit>()
    private var ready = false
    private var failed = false
    private var queued: Pair<String, () -> Unit>? = null
    private val tts = TextToSpeech(context.applicationContext, this)

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            failed = true
            queued?.second?.let { main.post(it) }
            queued = null
            return
        }
        val india = Locale("en", "IN")
        tts.language = if (tts.isLanguageAvailable(india) >= TextToSpeech.LANG_AVAILABLE) india else Locale.UK
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) { finish(utteranceId) }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { finish(utteranceId) }
            override fun onError(utteranceId: String?, errorCode: Int) { finish(utteranceId) }
        })
        ready = true
        queued?.let { (text, done) -> speak(text, done) }
        queued = null
    }

    private fun finish(id: String?) {
        id?.let { callbacks.remove(it) }?.let { main.post(it) }
    }

    override fun speak(text: String, onDone: () -> Unit) {
        when {
            failed -> main.post(onDone)
            !ready -> queued = text to onDone
            else -> {
                val id = UUID.randomUUID().toString()
                callbacks[id] = onDone
                tts.speak(cleanForSpeech(text), TextToSpeech.QUEUE_FLUSH, null, id)
            }
        }
    }

    override fun stop() {
        callbacks.clear()
        queued = null
        if (ready) tts.stop()
    }

    fun shutdown() = tts.shutdown()
}

/** Runs approved agent steps through the same FlatViewModel functions the screens use. */
class FlatAgentActions(private val vm: FlatViewModel) : AgentActions {
    override suspend fun run(step: AgentStep): Boolean = when (step) {
        is AgentStep.AddExpense -> suspendCancellableCoroutine { cont ->
            vm.addExpense(
                description = step.title,
                amount = step.amountPaise / 100.0,
                splitAmong = step.splitAmongUids,
                category = step.category,
                paidBy = step.paidByUid,
            ) { ok -> if (cont.isActive) cont.resume(ok) }
        }
        is AgentStep.Settle -> {
            val me = vm.currentUser.value?.uid
            if (me == null) false else {
                val amount = step.amountPaise / 100.0
                if (step.fromUid == me) vm.recordManualSettlement(step.toUid, amount) else vm.recordMarkReceived(step.fromUid, amount)
                true
            }
        }
        is AgentStep.CompleteTask -> {
            val task = vm.tasks.value.find { it.taskId == step.taskId }
            if (task == null) false else { vm.completeTask(task); true }
        }
        is AgentStep.CreateTask -> suspendCancellableCoroutine { cont ->
            vm.createTask(
                name = step.name, frequency = step.frequency, priority = "medium",
                participantUids = vm.members.value.map { it.uid },
            ) { ok -> if (cont.isActive) cont.resume(ok) }
        }
        is AgentStep.SetAway -> { vm.toggleOutOfStation(step.away); true }
    }
}

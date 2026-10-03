package habitiq.app.ui.agent

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.agent.AgentCard
import habitiq.app.agent.AgentInputMode
import habitiq.app.agent.AgentLink
import habitiq.app.agent.AgentUiState
import habitiq.app.agent.ClarifyOption
import habitiq.app.ui.components.HqWordmark
import habitiq.app.ui.theme.HqDarkColors
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.hqReduceMotion

/** Cards inside the voice overlay draw as frosted glass instead of solid surfaces. */
internal val LocalAgentGlass = staticCompositionLocalOf { false }

private val White = Color.White
private val TextSoft = Color.White.copy(alpha = .72f)

internal fun Modifier.glass(shape: Shape, strong: Boolean = false): Modifier = this
    .clip(shape)
    .background(Brush.verticalGradient(listOf(White.copy(alpha = if (strong) .20f else .14f), White.copy(alpha = if (strong) .10f else .06f))))
    .border(1.dp, Brush.verticalGradient(listOf(White.copy(alpha = .34f), White.copy(alpha = .08f))), shape)

private data class Suggestion(val label: String, val icon: ImageVector, val tint: Color, val text: String?)

/** Starters shown while listening. A null [Suggestion.text] means "start talking with a hint". */
private val SUGGESTIONS = listOf(
    Suggestion("What do I need\nto do today?", Icons.Rounded.CalendarMonth, Color(0xFF4D9C8E), "What do I need to do today?"),
    Suggestion("Add an\nexpense", Icons.Rounded.Add, Color(0xFF3B6FF0), null),
    Suggestion("Who owes\nme money?", Icons.Rounded.Group, Color(0xFF7B5BE0), "Who owes me?"),
    Suggestion("What do I owe?", Icons.Rounded.CurrencyRupee, Color(0xFFC98A3E), "What do I owe?"),
)

/**
 * Full-screen voice experience over the (blurred) app: the orb rises out of the nav button, waves
 * follow the voice, the person's words appear live, and results come back as glass cards.
 * [micCenter] is the nav button's centre in root coordinates, for the rise-in.
 */
@Composable
fun VoiceOverlay(
    state: AgentUiState, mode: AgentInputMode, level: Float, micCenter: Offset?,
    onClose: () -> Unit, onTalk: () -> Unit, onStop: () -> Unit, onSubmit: (String) -> Unit,
    onChoose: (ClarifyOption) -> Unit, onOpen: (AgentLink) -> Unit, onUseText: () -> Unit, onUseVoice: () -> Unit,
    onAllowMic: () -> Unit,
    animate: Boolean = !hqReduceMotion(),
    onConfirm: () -> Unit = {},
    onCancelPreview: () -> Unit = {},
) {
    BackHandler(onBack = onClose)
    val enter = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) { if (animate) enter.animateTo(1f, spring(dampingRatio = 0.78f, stiffness = 260f)) }
    var hint by remember { mutableStateOf<String?>(null) }

    val mood = when (state) {
        is AgentUiState.Listening -> OrbMood.Listening
        is AgentUiState.Idle -> if (mode == AgentInputMode.Voice) OrbMood.Listening else OrbMood.Done
        is AgentUiState.Checking -> OrbMood.Thinking
        is AgentUiState.Preview -> if (state.awaitingVoice) OrbMood.Listening else if (state.saving) OrbMood.Thinking else OrbMood.Done
        else -> OrbMood.Done
    }
    val hasResult = state is AgentUiState.Answer || state is AgentUiState.Preview || state is AgentUiState.Clarify || state is AgentUiState.Done
    val orbSize by animateDpAsState(if (hasResult) 84.dp else if (mood == OrbMood.Thinking) 112.dp else 132.dp, tween(450, easing = FastOutSlowInEasing), label = "orbSize")
    val stageHeight by animateDpAsState(if (hasResult) 150.dp else 236.dp, tween(450, easing = FastOutSlowInEasing), label = "stage")

    CompositionLocalProvider(LocalHqColors provides HqDarkColors, LocalAgentGlass provides true) {
        Box(Modifier.fillMaxSize()) {
            // Deep navy veil over the blurred app; taps on it don't reach the app.
            Box(
                Modifier.fillMaxSize()
                    .graphicsLayer { alpha = enter.value.coerceIn(0f, 1f) }
                    .background(Brush.verticalGradient(listOf(VoiceColors.scrimTop.copy(alpha = .78f), VoiceColors.scrimBottom.copy(alpha = .86f))))
                    .pointerInput(Unit) { detectTapGestures { } },
            )
            Column(
                Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    HqWordmark(height = 30.dp, tint = White, modifier = Modifier.graphicsLayer { alpha = enter.value.coerceIn(0f, 1f) })
                    Spacer(Modifier.weight(1f))
                    if (mode == AgentInputMode.Voice) {
                        GlassCircle(Icons.Rounded.Keyboard, "Type instead", onUseText, size = 44)
                        Spacer(Modifier.width(10.dp))
                    }
                    GlassCircle(Icons.Rounded.Close, "Close", onClose)
                }

                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = if (hasResult) Arrangement.Top else Arrangement.Center,
                ) {
                    RisingOrb(level, orbSize, stageHeight, mood, animate, micCenter, enter.value)
                    Column(
                        Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AnimatedContent(
                            targetState = state,
                            contentKey = { it::class },
                            transitionSpec = { fadeIn(tween(260, delayMillis = 60)) togetherWith fadeOut(tween(140)) },
                            label = "voiceState",
                        ) { s -> StateContent(s, mode, hint, animate, onSubmit, onChoose, onOpen, onAllowMic) { h -> hint = h; onTalk() } }
                    }
                }

                BottomControls(state, mode, onClose, onTalk, onStop, onSubmit, onUseText, onUseVoice, onConfirm, onCancelPreview)
                // Leaves the frosted nav bar showing underneath, as in the reference.
                Spacer(Modifier.height(if (mode == AgentInputMode.Text) 16.dp else 108.dp))
            }
        }
    }
}

@Composable
private fun RisingOrb(level: Float, orbSize: androidx.compose.ui.unit.Dp, stageHeight: androidx.compose.ui.unit.Dp, mood: OrbMood, animate: Boolean, micCenter: Offset?, p: Float) {
    var stageCenter by remember { mutableStateOf<Offset?>(null) }
    VoiceOrbStage(
        level, orbSize, mood, animate,
        Modifier.fillMaxWidth().height(stageHeight)
            .onGloballyPositioned { stageCenter = it.boundsInRoot().center }
            .graphicsLayer {
                val from = micCenter
                val to = stageCenter
                if (from != null && to != null) {
                    translationX = (from.x - to.x) * (1f - p)
                    translationY = (from.y - to.y) * (1f - p)
                }
                val s = 0.3f + 0.7f * p
                scaleX = s; scaleY = s
                alpha = (p * 1.4f).coerceIn(0f, 1f)
            }
            .semantics { contentDescription = if (mood == OrbMood.Listening) "Listening" else "Oddroof" },
    )
}

@Composable
private fun StateContent(
    s: AgentUiState, mode: AgentInputMode, hint: String?, animate: Boolean,
    onSubmit: (String) -> Unit, onChoose: (ClarifyOption) -> Unit, onOpen: (AgentLink) -> Unit, onAllowMic: () -> Unit,
    onSuggestTalk: (String) -> Unit,
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        when (s) {
            is AgentUiState.Idle, is AgentUiState.Listening -> {
                val partial = (s as? AgentUiState.Listening)?.partial.orEmpty()
                val notice = (s as? AgentUiState.Idle)?.notice
                Title(if (mode == AgentInputMode.Text) "What do you need?" else "I’m listening…")
                Spacer(Modifier.height(8.dp))
                when {
                    partial.isNotEmpty() -> Text(
                        partial, style = TextStyle(fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
                        color = White, textAlign = TextAlign.Center, maxLines = 4,
                    )
                    notice != null -> {
                        Subtitle(notice)
                        Spacer(Modifier.height(12.dp))
                        GlassPill("Allow mic", Icons.Rounded.Mic, onClick = onAllowMic)
                    }
                    else -> Subtitle(hint ?: if (mode == AgentInputMode.Text) "Type it the way you’d say it." else "Tell me what you need.")
                }
                if (partial.isEmpty()) {
                    Spacer(Modifier.height(30.dp))
                    SuggestionGrid(animate, onSubmit, onSuggestTalk)
                }
            }
            is AgentUiState.Checking -> {
                Title("Checking your flat…")
                Spacer(Modifier.height(10.dp))
                Quote(s.transcript)
            }
            is AgentUiState.Answer -> {
                Quote(s.transcript)
                Spacer(Modifier.height(16.dp))
                AnswerCard(s.answer.headline, s.answer.lines)
                s.answer.link?.let { Spacer(Modifier.height(14.dp)); GlassPill(it.label, null) { onOpen(it) } }
            }
            is AgentUiState.Preview -> {
                Quote(s.transcript)
                Spacer(Modifier.height(16.dp))
                when (val card = s.card) {
                    is AgentCard.Expense -> ExpenseCard(card, animate)
                    is AgentCard.Payment -> PaymentCard(card, animate)
                    is AgentCard.Simple -> SimpleActionCard(card, animate)
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    when {
                        s.saving -> "Saving…"
                        s.awaitingVoice -> "Say “yes” to save, or “no” to cancel."
                        else -> "Tap Save to keep it."
                    },
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium), color = TextSoft, textAlign = TextAlign.Center,
                )
            }
            is AgentUiState.Done -> {
                Quote(s.transcript)
                Spacer(Modifier.height(18.dp))
                Box(
                    Modifier.size(64.dp).clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(Color(0xFF3CC3AA), Color(0xFF14937F)))),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Check, null, tint = White, modifier = Modifier.size(34.dp)) }
                Spacer(Modifier.height(14.dp))
                Title(s.message)
            }
            is AgentUiState.Clarify -> {
                Quote(s.transcript)
                Spacer(Modifier.height(16.dp))
                Title(s.question)
                Spacer(Modifier.height(18.dp))
                s.options.forEach { o ->
                    GlassPill(o.label, null, Modifier.fillMaxWidth()) { onChoose(o) }
                    Spacer(Modifier.height(10.dp))
                }
            }
            is AgentUiState.NotUnderstood -> {
                Quote(s.transcript)
                Spacer(Modifier.height(14.dp))
                Subtitle(s.message)
            }
            is AgentUiState.Error -> {
                if (s.transcript.isNotEmpty()) { Quote(s.transcript); Spacer(Modifier.height(14.dp)) }
                Title("Hmm.")
                Spacer(Modifier.height(8.dp))
                Subtitle(s.message)
            }
        }
    }
}

@Composable
private fun Title(text: String) = Text(
    text, style = TextStyle(fontSize = 31.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp, fontFamily = habitiq.app.ui.theme.HqDisplayFontFamily),
    color = White, textAlign = TextAlign.Center,
)

@Composable
private fun Subtitle(text: String) = Text(
    text, style = TextStyle(fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal), color = TextSoft, textAlign = TextAlign.Center,
)

@Composable
private fun Quote(text: String) = Text(
    "“$text”", style = TextStyle(fontSize = 18.sp, lineHeight = 25.sp, fontWeight = FontWeight.Medium),
    color = White.copy(alpha = .9f), textAlign = TextAlign.Center,
)

@Composable
private fun SuggestionGrid(animate: Boolean, onSubmit: (String) -> Unit, onSuggestTalk: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SUGGESTIONS.chunked(2).forEachIndexed { row, pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEachIndexed { col, s ->
                    val index = row * 2 + col
                    val p = remember { Animatable(if (animate) 0f else 1f) }
                    LaunchedEffect(Unit) {
                        if (animate) { kotlinx.coroutines.delay(180L + 70L * index); p.animateTo(1f, spring(dampingRatio = 0.75f, stiffness = 320f)) }
                    }
                    SuggestionChip(
                        s,
                        Modifier.weight(1f).graphicsLayer { alpha = p.value.coerceIn(0f, 1f); translationY = (1f - p.value) * 18.dp.toPx() },
                    ) { if (s.text != null) onSubmit(s.text) else onSuggestTalk("Say it like “Groceries 560” or “Paid Ravi 200”.") }
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(s: Suggestion, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Row(
        modifier.heightIn(min = 60.dp).glass(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 10.dp, end = 8.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(s.tint), contentAlignment = Alignment.Center) {
            Icon(s.icon, null, tint = White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(s.label, style = TextStyle(fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium), color = White, maxLines = 2)
    }
}

@Composable
private fun GlassCircle(icon: ImageVector, label: String, onClick: () -> Unit, size: Int = 52) {
    Box(
        Modifier.size(size.dp).glass(CircleShape, strong = true)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = White, modifier = Modifier.size((size * 0.44f).dp)) }
}

@Composable
private fun GlassPill(text: String, icon: ImageVector?, modifier: Modifier = Modifier, primary: Boolean = false, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val base = if (primary) Modifier.clip(shape).background(Brush.verticalGradient(listOf(VoiceColors.buttonTop, VoiceColors.buttonBottom)))
        .border(1.dp, White.copy(alpha = .25f), shape)
    else Modifier.glass(shape)
    Row(
        modifier.heightIn(min = 52.dp).then(base)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        icon?.let { Icon(it, null, tint = White, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(10.dp)) }
        Text(text, style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium), color = White)
    }
}

@Composable
private fun BottomControls(
    state: AgentUiState, mode: AgentInputMode,
    onClose: () -> Unit, onTalk: () -> Unit, onStop: () -> Unit, onSubmit: (String) -> Unit,
    onUseText: () -> Unit, onUseVoice: () -> Unit,
    onConfirm: () -> Unit, onCancelPreview: () -> Unit,
) {
    if (state is AgentUiState.Preview) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassPill("Cancel", Icons.Rounded.Close, onClick = onCancelPreview)
            GlassPill(if (state.saving) "Saving…" else "Save", Icons.Rounded.Check, primary = true, onClick = { if (!state.saving) onConfirm() })
        }
        return
    }
    if (mode == AgentInputMode.Text) {
        var text by rememberSaveable { mutableStateOf("") }
        val shape = RoundedCornerShape(28.dp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassCircle(Icons.Rounded.Mic, "Talk instead", onUseVoice)
            Box(Modifier.weight(1f).heightIn(min = 52.dp).glass(shape).padding(horizontal = 18.dp), contentAlignment = Alignment.CenterStart) {
                if (text.isEmpty()) Text("e.g. Spent 500 on milk", color = White.copy(alpha = .5f), style = TextStyle(fontSize = 16.sp), maxLines = 1)
                BasicTextField(
                    value = text, onValueChange = { text = it }, singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp, color = White), cursorBrush = SolidColor(White),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (text.isNotBlank()) { onSubmit(text); text = "" } }),
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Ask or tell Oddroof" },
                )
            }
            Box(
                Modifier.size(52.dp).clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(VoiceColors.buttonTop, VoiceColors.buttonBottom)))
                    .clickable(enabled = text.isNotBlank(), role = Role.Button, onClickLabel = "Send") { onSubmit(text); text = "" },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.AutoMirrored.Rounded.Send, "Send", tint = White, modifier = Modifier.size(22.dp)) }
        }
        return
    }
    val listening = state is AgentUiState.Listening
    val idle = state is AgentUiState.Idle
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        when {
            listening && (state as AgentUiState.Listening).partial.isNotEmpty() -> GlassPill("Done", null, onClick = onStop)
            listening || idle -> GlassPill("Cancel", Icons.Rounded.Close, onClick = onClose)
            state is AgentUiState.Checking -> GlassPill("Cancel", Icons.Rounded.Close, onClick = onClose)
            else -> GlassPill("Ask again", Icons.Rounded.Mic, primary = true, onClick = onTalk)
        }
    }
}

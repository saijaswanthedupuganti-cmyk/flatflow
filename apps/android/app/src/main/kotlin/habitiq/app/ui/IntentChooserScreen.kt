package habitiq.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import habitiq.app.R
import habitiq.app.discover.DiscoverMode
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqHeroBleedEffect
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.components.HqWordmark
import habitiq.app.ui.theme.HqEaseOut
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.hqReduceMotion

/** The starting options. Find a flat / Find a flatmate open Discover; Create and Join set up membership. */
enum class OnboardingIntent {
    MANAGE_FLAT,
    FIND_FLATMATE,
    JOIN_FLAT,
    FIND_FLAT,
    FIND_PERSON
}

private data class WelcomeSlide(val label: String, val title: String, val copy: String, val photo: Int)

/** Three plain slides: what Oddroof is, finding a flat or flatmate, and voice. Only real features. */
private val welcomeSlides = listOf(
    WelcomeSlide("Welcome to Oddroof", "The app for people\nwho share a flat.", OddroofValueSentence, R.drawable.home_hero),
    WelcomeSlide("Find a flat · Find a flatmate", "Find a flat or\na flatmate.", "Browse rooms near work or college, or list a room in your flat to find a flatmate.", R.drawable.slide2),
    WelcomeSlide("Just say it", "Tap the mic and\njust say it.", "Say “I spent 500 on groceries” and Oddroof adds the expense for you.", R.drawable.slide3),
)

/**
 * First-run slides: a full-bleed photo under a dark scrim, a short label, title and copy, progress dots and
 * one primary button ("Next", then "Get started" on the last slide). "I already have an account" opens Log in.
 */
@Composable
fun WelcomeScreen(onNext: () -> Unit, onLogin: () -> Unit) {
    var page by rememberSaveable { mutableStateOf(0) }
    BackHandler(enabled = page > 0) { page -= 1 }
    val c = LocalHqColors.current
    val reduce = hqReduceMotion()
    val last = page == welcomeSlides.lastIndex
    // Photo scrim: always dark so the white copy stays readable over any photo, in light and dark theme.
    val scrim = c.scrim
    HqHeroBleedEffect()
    Box(Modifier.fillMaxSize().background(scrim)) {
        AnimatedContent(
            targetState = page,
            transitionSpec = { fadeIn(tween(if (reduce) 0 else 600, easing = HqEaseOut)) togetherWith fadeOut(tween(if (reduce) 0 else 300)) },
            label = "welcomePhoto",
            modifier = Modifier.fillMaxSize(),
        ) { index ->
            Image(
                painterResource(welcomeSlides[index].photo), contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to scrim.copy(alpha = .50f), .30f to scrim.copy(alpha = .12f), .52f to scrim.copy(alpha = .62f), 1f to scrim.copy(alpha = .95f),
                ),
            ),
        )
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.lg),
        ) {
            Row(Modifier.fillMaxWidth().defaultMinSize(minHeight = HqSize.target), verticalAlignment = Alignment.CenterVertically) {
                HqWordmark(height = 26.dp, tint = c.onImage)
                Spacer(Modifier.weight(1f))
                HqTextButton(text = "Skip", onClick = onNext, color = c.onImage)
            }
            // The copy scrolls on its own if a very large font makes it taller than the space above the buttons.
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomStart) {
                AnimatedContent(
                    targetState = page,
                    transitionSpec = { (fadeIn(tween(if (reduce) 0 else 450, easing = HqEaseOut)) + slideInVertically(tween(if (reduce) 0 else 450, easing = HqEaseOut)) { if (reduce) 0 else it / 12 }) togetherWith fadeOut(tween(if (reduce) 0 else 120)) },
                    label = "welcomeCopy",
                ) { index ->
                    val slide = welcomeSlides[index]
                    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
                        Text(slide.label, style = HqType.labelMedium, color = c.onImage.copy(alpha = .9f), fontWeight = FontWeight.Bold)
                        Text(slide.title, style = HqType.display, color = c.onImage, modifier = Modifier.semantics { heading() })
                        Text(slide.copy, style = HqType.bodyLarge, color = c.onImage.copy(alpha = .9f))
                    }
                }
            }
            Spacer(Modifier.height(HqSpacing.xxl))
            Row(
                Modifier.fillMaxWidth().padding(bottom = HqSpacing.md)
                    .semantics { contentDescription = "Slide ${page + 1} of ${welcomeSlides.size}" },
                horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs),
            ) {
                welcomeSlides.indices.forEach { index ->
                    Box(
                        Modifier.size(HqSize.target, HqSpacing.xl).clip(CircleShape)
                            .clickable(role = Role.Button, onClickLabel = "Show slide ${index + 1}") { page = index }
                            .semantics { selected = index == page },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Box(
                            Modifier.size(width = if (index == page) HqSpacing.xxl else HqSpacing.sm, height = HqSpacing.sm).clip(CircleShape)
                                .background(if (index == page) c.onImage else c.onImage.copy(alpha = .4f)),
                        )
                    }
                }
            }
            HqButton(
                text = if (last) "Get started" else "Next",
                onClick = { if (last) onNext() else page += 1 },
                trailingIcon = HqIcons.Arrow,
            )
            HqTextButton(
                text = "I already have an account", onClick = onLogin, color = c.onImage,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = HqSpacing.xs),
            )
        }
    }
}

/**
 * First choice after creating an account: one question and the four ways to start ([StartChooser]). Find a flat
 * and Find a flatmate ask for city, budget and one preference, then open Discover with those filters applied.
 *
 * [onSaveProfile] and [onExploreLater] are no longer used (the profile and "done" steps were removed); they stay so
 * existing callers compile.
 */
@Composable
fun IntentChooserScreen(
    userName: String,
    onChoose: (OnboardingIntent) -> Unit,
    onSignOut: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onSaveProfile: (name: String, city: String, gender: String, onResult: (Boolean) -> Unit) -> Unit = { _, _, _, done -> done(true) },
    onOpenDiscover: (mode: DiscoverMode, city: String, budget: String, preference: String) -> Unit = { _, _, _, _ -> },
    @Suppress("UNUSED_PARAMETER") onExploreLater: () -> Unit = onSignOut
) {
    var step by rememberSaveable { mutableStateOf("goal") }
    BackHandler(enabled = step != "goal") { step = "goal" }
    var city by rememberSaveable { mutableStateOf("") }
    var budget by rememberSaveable { mutableStateOf("") }
    var room by rememberSaveable { mutableStateOf("Any") }
    var lifestyle by rememberSaveable { mutableStateOf("Flexible") }
    val c = LocalHqColors.current

    when (step) {
        "findFlat", "findPerson" -> {
            val people = step == "findPerson"
            val openDiscover = {
                onOpenDiscover(
                    if (people) DiscoverMode.FIND_A_PERSON else DiscoverMode.USE_A_FLAT,
                    city.trim(),
                    budget.trim(),
                    if (people) lifestyle else room,
                )
            }
            Column(Modifier.fillMaxSize().background(c.canvas).navigationBarsPadding()) {
                HqBackAppBar(title = if (people) "Find a flatmate" else "Find a flat", onBack = { step = "goal" })
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.screenHorizontal),
                ) {
                    Text(
                        if (people) "Tell us where your flat is. You can change this in Discover anytime."
                        else "Tell us where and your budget. You can change this in Discover anytime.",
                        style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(bottom = HqSpacing.xl),
                    )
                    HqTextField(value = city, onValueChange = { city = it }, label = "City or area", placeholder = "e.g. Hyderabad", leadingIcon = Icons.Filled.LocationOn)
                    Spacer(Modifier.height(HqSpacing.md))
                    HqTextField(
                        value = budget,
                        onValueChange = { budget = it },
                        label = if (people) "Monthly rent per person (₹)" else "Monthly budget (₹)",
                        placeholder = "e.g. 12000",
                        leadingIcon = Icons.Filled.CurrencyRupee,
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                        onImeAction = openDiscover,
                    )
                    Spacer(Modifier.height(HqSpacing.lg))
                    Text(if (people) "Lifestyle" else "Room", style = HqType.labelLarge, color = c.textSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm), modifier = Modifier.padding(top = HqSpacing.sm, bottom = HqSpacing.xl)) {
                        val options = if (people) listOf("Quiet", "Social", "Flexible") else listOf("Private", "Shared", "Any")
                        options.forEach { option ->
                            val isSelected = if (people) lifestyle == option else room == option
                            HqChip(label = option, selected = isSelected, onClick = {
                                if (people) lifestyle = option else room = option
                            })
                        }
                    }
                }
                HqButton(
                    text = if (people) "Show flatmates" else "Show flats",
                    onClick = openDiscover,
                    modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.md),
                )
            }
        }
        else -> {
            Column(
                Modifier.fillMaxSize().background(c.canvas).navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.xxl),
            ) {
                Text(
                    OddroofValueSentence, style = HqType.bodyMedium, color = c.textSecondary,
                    modifier = Modifier.padding(bottom = HqSpacing.lg),
                )
                StartChooser(
                    onFindFlat = { step = "findFlat" },
                    onFindPerson = { step = "findPerson" },
                    onCreateFlat = { onChoose(OnboardingIntent.MANAGE_FLAT) },
                    onJoinFlat = { onChoose(OnboardingIntent.JOIN_FLAT) },
                )
                Spacer(Modifier.height(HqSpacing.xxxl))
                HqTextButton(text = "Log out", onClick = onSignOut, color = c.textSecondary, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

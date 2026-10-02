package habitiq.app.ui


import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.border
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqHeroBleedEffect
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.width
import habitiq.app.ui.components.HqIllustration
import habitiq.app.ui.components.HqArt
import androidx.activity.compose.BackHandler
import habitiq.app.ui.components.HqWordmark
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import habitiq.app.R
import habitiq.app.discover.DiscoverMode
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqEaseOut
import habitiq.app.ui.theme.HqEnterDuration
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.hqReduceMotion
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/** The previous APK's starting options. Find a flat / Find a person explore Discover; Create and Join set up membership. */
enum class OnboardingIntent {
    MANAGE_FLAT,
    FIND_FLATMATE,
    JOIN_FLAT,
    FIND_FLAT,
    FIND_PERSON
}

private data class WelcomeSlide(val label: String, val title: String, val copy: String, val photo: Int)

private val welcomeSlides = listOf(
    WelcomeSlide("ONE HOME, IN SYNC", "Shared living,\nwithout the chasing.", "Keep tasks, rotations, daily expenses and monthly bills clear for everyone.", R.drawable.home_hero),
    WelcomeSlide("DISCOVER YOUR FIT", "Find the place \u2014 or\nperson \u2014 that feels right.", "Explore profiles, approximate locations and compatible ways of living.", R.drawable.slide2),
    WelcomeSlide("SEARCH AROUND YOUR LIFE", "Live closer to what\nmatters every day.", "Choose work, college or a landmark. Oddroof orders nearby homes around that place.", R.drawable.slide3),
)

/**
 * First-run slides from the Figma Make onboarding: a full-bleed photo, a dark gradient, tracked label, large
 * title and copy, progress dots and a pill Next button. Skip and "I already have an account" are kept.
 */
@Composable
fun WelcomeScreen(onNext: () -> Unit, onLogin: () -> Unit) {
    var page by rememberSaveable { mutableStateOf(0) }
    BackHandler(enabled = page > 0) { page -= 1 }
    val reduce = hqReduceMotion()
    val last = page == welcomeSlides.lastIndex
    val ink = Color(0xFF091917)
    HqHeroBleedEffect()
    Box(Modifier.fillMaxSize().background(ink)) {
        AnimatedContent(
            targetState = page,
            transitionSpec = { fadeIn(tween(if (reduce) 0 else 600, easing = HqEaseOut)) togetherWith fadeOut(tween(if (reduce) 0 else 300)) },
            label = "welcomePhoto",
            modifier = Modifier.fillMaxSize(),
        ) { index ->
            androidx.compose.foundation.Image(
                painterResource(welcomeSlides[index].photo), contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop, modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    0f to ink.copy(alpha = .52f), .38f to ink.copy(alpha = .10f), .68f to ink.copy(alpha = .72f), 1f to ink.copy(alpha = .97f),
                ),
            ),
        )
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    HqWordmark(height = 26.dp, tint = Color.White)
                }
                Box(
                    Modifier.defaultMinSize(minHeight = 48.dp).clip(CircleShape).clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onNext),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Skip", style = HqType.labelMedium, color = Color.White, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(CircleShape).background(ink.copy(alpha = .18f)).border(1.dp, Color.White.copy(alpha = .28f), CircleShape).padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
            Text(
                "%02d / 03".format(page + 1), style = HqType.titleMedium2, color = Color.White, fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.End).padding(top = 18.dp),
            )
            Spacer(Modifier.weight(1f))
            AnimatedContent(
                targetState = page,
                transitionSpec = { (fadeIn(tween(if (reduce) 0 else 450, easing = HqEaseOut)) + slideInVertically(tween(if (reduce) 0 else 450, easing = HqEaseOut)) { if (reduce) 0 else it / 12 }) togetherWith fadeOut(tween(if (reduce) 0 else 120)) },
                label = "welcomeCopy",
            ) { index ->
                val slide = welcomeSlides[index]
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(slide.label, style = HqType.labelSmall.copy(letterSpacing = 1.4.sp), color = Color(0xFF9BE5DC), fontWeight = FontWeight.ExtraBold)
                    Text(slide.title, style = HqType.display.copy(fontSize = 35.sp, lineHeight = 38.sp), color = Color.White)
                    Text(slide.copy, style = HqType.bodyLarge, color = Color.White.copy(alpha = .8f))
                }
            }
            Spacer(Modifier.height(28.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    welcomeSlides.indices.forEach { index ->
                        Box(
                            Modifier.size(width = if (index == page) 24.dp else 7.dp, height = 7.dp).clip(CircleShape)
                                .background(if (index == page) Color(0xFF14B8A6) else Color.White.copy(alpha = .35f))
                                .clickable(role = androidx.compose.ui.semantics.Role.Button) { page = index }
                                .semantics { contentDescription = "Show slide ${index + 1}" },
                        )
                    }
                }
                HqButton(
                    text = if (last) "Choose your path" else "Next",
                    onClick = { if (last) onNext() else page += 1 },
                    fullWidth = false,
                    trailingIcon = HqIcons.Arrow,
                )
            }
            HqTextButton(
                text = "I already have an account", onClick = onLogin, color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp),
            )
        }
    }
}

@Composable
fun IntentChooserScreen(
    userName: String,
    onChoose: (OnboardingIntent) -> Unit,
    onSignOut: () -> Unit,
    onSaveProfile: (name: String, city: String, gender: String, onResult: (Boolean) -> Unit) -> Unit = { _, _, _, done -> done(true) },
    onOpenDiscover: (mode: DiscoverMode, city: String, budget: String, preference: String) -> Unit = { _, _, _, _ -> },
    onExploreLater: () -> Unit = onSignOut
) {
    var step by rememberSaveable { mutableStateOf("goal") }
    var goalMode by rememberSaveable { mutableStateOf(DiscoverMode.USE_A_FLAT.name) }
    // Back from "done" returns to the search step it came from; every other step returns to the goal list.
    BackHandler(enabled = step != "goal") {
        step = if (step == "done") {
            if (goalMode == DiscoverMode.FIND_A_PERSON.name) "findPerson" else "findFlat"
        } else "goal"
    }
    var name by rememberSaveable { mutableStateOf(userName) }
    var city by rememberSaveable { mutableStateOf("") }
    var budget by rememberSaveable { mutableStateOf("") }
    var room by rememberSaveable { mutableStateOf("Any") }
    var lifestyle by rememberSaveable { mutableStateOf("Flexible") }
    var gender by rememberSaveable { mutableStateOf("Prefer not to say") }
    var profileSaving by rememberSaveable { mutableStateOf(false) }
    var profileSaved by rememberSaveable { mutableStateOf(false) }
    var profileSaveError by rememberSaveable { mutableStateOf<String?>(null) }
    val c = LocalHqColors.current
    val saveProfile = {
        profileSaving = true
        profileSaveError = null
        onSaveProfile(name, city, gender) { success ->
            profileSaving = false
            if (success) {
                profileSaved = true
                step = "goal"
            } else {
                profileSaveError = "Couldn't save your profile. Check your connection and try again."
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(c.canvas).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.xxl)
    ) {
        when (step) {
            "profile" -> {
                Text("Your profile", style = HqType.headlineMedium, color = c.textPrimary)
                Text("This helps the flat know who is joining.", style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(top = HqSpacing.sm, bottom = HqSpacing.xl))
                HqTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full name",
                    placeholder = "e.g. Priya Sharma",
                    leadingIcon = Icons.Filled.Badge,
                    capitalization = KeyboardCapitalization.Words,
                    contentType = ContentType.PersonFullName
                )
                Spacer(Modifier.height(HqSpacing.md))
                HqTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = "City / location",
                    placeholder = "e.g. Hyderabad",
                    leadingIcon = Icons.Filled.LocationOn,
                    imeAction = ImeAction.Done,
                    onImeAction = if (name.isNotBlank() && !profileSaving) saveProfile else null
                )
                Spacer(Modifier.height(HqSpacing.lg))
                Text("I'm a", style = HqType.labelLarge, color = c.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm), modifier = Modifier.padding(top = HqSpacing.sm)) {
                    listOf("Male", "Female", "Prefer not to say").forEach { option ->
                        HqChip(label = option, selected = gender == option, onClick = { gender = option })
                    }
                }
                Spacer(Modifier.height(HqSpacing.xxl))
                profileSaveError?.let {
                    Text(it, style = HqType.bodySmall, color = c.statusDangerFg, modifier = Modifier.padding(top = HqSpacing.sm))
                }
                HqButton(
                    text = if (profileSaving) "Saving…" else "Continue",
                    enabled = name.isNotBlank() && !profileSaving,
                    loading = profileSaving,
                    onClick = saveProfile
                )
            }
            "goal" -> {
                Text("What brings you to Oddroof?", style = HqType.display, color = c.textPrimary)
                Text("Choose the closest match. You can use every part of Oddroof later.", style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(top = HqSpacing.sm, bottom = HqSpacing.xl))
                IntentChoices(
                    onFindFlat = { step = "findFlat" },
                    onFindPerson = { step = "findPerson" },
                    onCreateFlat = { onChoose(OnboardingIntent.MANAGE_FLAT) },
                    onJoinFlat = { onChoose(OnboardingIntent.JOIN_FLAT) },
                )
                Spacer(Modifier.height(HqSpacing.xl))
                HqTextButton(text = "Sign out", onClick = onSignOut, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            "findFlat", "findPerson" -> {
                val people = step == "findPerson"
                val continueToDone = {
                    goalMode = if (people) DiscoverMode.FIND_A_PERSON.name else DiscoverMode.USE_A_FLAT.name
                    step = "done"
                }
                Text(if (people) "Looking for a flatmate" else "Looking for a place", style = HqType.headlineMedium, color = c.textPrimary)
                Spacer(Modifier.height(HqSpacing.lg))
                HqTextField(value = city, onValueChange = { city = it }, label = "City / area", placeholder = "e.g. Hyderabad", leadingIcon = Icons.Filled.LocationOn)
                Spacer(Modifier.height(HqSpacing.md))
                HqTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = "Monthly budget (₹)",
                    placeholder = "e.g. 12000",
                    leadingIcon = Icons.Filled.CurrencyRupee,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    onImeAction = continueToDone
                )
                Spacer(Modifier.height(HqSpacing.lg))
                Text(if (people) "Lifestyle" else "Room", style = HqType.labelLarge, color = c.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm), modifier = Modifier.padding(top = HqSpacing.sm)) {
                    val options = if (people) listOf("Quiet", "Social", "Flexible") else listOf("Private", "Shared", "Any")
                    options.forEach { option ->
                        val selected = if (people) lifestyle == option else room == option
                        HqChip(label = option, selected = selected, onClick = {
                            if (people) lifestyle = option else room = option
                        })
                    }
                }
                Spacer(Modifier.height(HqSpacing.xxl))
                HqButton(text = "Continue", onClick = continueToDone)
            }
            else -> {
                Image(
                    painter = painterResource(R.drawable.onboard_create),
                    contentDescription = "Your new home is ready",
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(HqSpacing.xl))
                Text("Welcome to Oddroof", style = HqType.headlineMedium, color = c.textPrimary)
                Text("Let's find the right people and places for you.", style = HqType.bodyLarge, color = c.textSecondary, modifier = Modifier.padding(top = HqSpacing.md, bottom = HqSpacing.lg))
                HqCard {
                    (listOf("Account created") + (if (profileSaved) listOf("Profile set up") else emptyList()) + listOf("Your goal is ready")).forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = HqSpacing.xs)) {
                            Icon(Icons.Filled.CheckCircle, null, tint = c.statusSuccessFg, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.size(HqSpacing.sm))
                            Text(item, style = HqType.titleSmall, color = c.textPrimary)
                        }
                    }
                }
                Spacer(Modifier.height(HqSpacing.lg))
                HqButton(text = "Go to Discover", onClick = {
                    onOpenDiscover(
                        runCatching { DiscoverMode.valueOf(goalMode) }.getOrDefault(DiscoverMode.USE_A_FLAT),
                        city.trim(),
                        budget.trim(),
                        if (goalMode == DiscoverMode.FIND_A_PERSON.name) lifestyle else room
                    )
                })
                Spacer(Modifier.height(HqSpacing.md))
                HqTextButton(text = "Explore later", onClick = onExploreLater, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

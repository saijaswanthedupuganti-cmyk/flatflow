package habitiq.app.ui

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

enum class OnboardingIntent {
    MANAGE_FLAT,
    FIND_FLATMATE,
    JOIN_FLAT,
    FIND_FLAT,
    FIND_PERSON
}

private val welcomePages = listOf(
    "A home for people who live together" to listOf(
        "Find verified flats and flatmates",
        "Live with like-minded people",
        "Manage tasks and expenses",
        "A safer, simpler way to share living"
    ),
    "People, places and better living" to listOf(
        "Whether you're finding a place, finding a flatmate, or managing your current flat",
        "Habitiq makes shared living simple"
    ),
    "Built for real life together" to listOf(
        "Verified homes and residents",
        "Clear preferences up front",
        "Easy join and invite flow",
        "Manage household tasks and bills"
    )
)

@Composable
fun WelcomeScreen(onNext: () -> Unit, onLogin: () -> Unit) {
    var page by rememberSaveable { mutableStateOf(0) }
    val c = LocalHqColors.current
    val reduce = hqReduceMotion()
    Column(
        Modifier.fillMaxSize().background(c.background).padding(HqSpacing.xxl)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            HqTextButton(text = "Skip", onClick = onNext)
        }
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                val enter = fadeIn(tween(HqEnterDuration, easing = HqEaseOut)) +
                    slideInHorizontally(tween(HqEnterDuration, easing = HqEaseOut)) { if (reduce) 0 else it / 10 }
                enter togetherWith fadeOut(tween(140, easing = HqEaseOut))
            },
            label = "welcomePage"
        ) { index ->
            val (title, bullets) = welcomePages[index]
            val art = if (index == 1) R.drawable.onboard_join else R.drawable.onboard_create
            Column {
                Image(
                    painter = painterResource(art),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(HqSpacing.lg))
                Text("habitiq", style = HqType.labelLarge, color = c.brandPrimary)
                Spacer(Modifier.height(HqSpacing.sm))
                Text(title, style = HqType.headlineMedium, color = c.textPrimary)
                Column(Modifier.padding(top = HqSpacing.md), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    bullets.forEach { line ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                            Icon(Icons.Filled.CheckCircle, null, tint = c.brandPrimary, modifier = Modifier.size(18.dp))
                            Text(line, style = HqType.bodyMedium, color = c.textSecondary)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.fillMaxWidth().padding(bottom = HqSpacing.md),
            horizontalArrangement = Arrangement.Center
        ) {
            welcomePages.indices.forEach { index ->
                Box(
                    Modifier.padding(horizontal = 4.dp).size(if (index == page) 18.dp else 7.dp, 7.dp)
                        .clip(CircleShape).background(if (index == page) c.brandPrimary else c.borderDefault)
                )
            }
        }
        HqButton(
            text = if (page == welcomePages.lastIndex) "Get started" else "Next",
            onClick = { if (page == welcomePages.lastIndex) onNext() else page += 1 }
        )
        HqTextButton(text = "I already have an account", onClick = onLogin, modifier = Modifier.align(Alignment.CenterHorizontally))
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

    Column(
        Modifier.fillMaxSize().background(c.background).verticalScroll(rememberScrollState()).padding(HqSpacing.xxl)
    ) {
        when (step) {
            "profile" -> {
                OnboardingStepLabel(3, "Set up your profile")
                Text("Your profile", style = HqType.headlineMedium, color = c.textPrimary)
                Text("This helps the flat know who is joining.", style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(top = HqSpacing.sm, bottom = HqSpacing.xl))
                Box(
                    Modifier.size(88.dp).clip(CircleShape).background(c.surfaceSubtle).align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.CameraAlt, "Add profile photo", tint = c.textSecondary, modifier = Modifier.size(34.dp)) }
                Spacer(Modifier.height(HqSpacing.xl))
                HqTextField(value = name, onValueChange = { name = it }, label = "Full name", leadingIcon = Icons.Filled.Badge)
                Spacer(Modifier.height(HqSpacing.md))
                HqTextField(value = city, onValueChange = { city = it }, label = "City / location", leadingIcon = Icons.Filled.LocationOn)
                Spacer(Modifier.height(HqSpacing.lg))
                Text("I'm a", style = HqType.labelLarge, color = c.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm), modifier = Modifier.padding(top = HqSpacing.sm)) {
                    listOf("Male", "Female", "Prefer not to say").forEach { option ->
                        HqChip(label = option, selected = gender == option, onClick = { gender = option })
                    }
                }
                Spacer(Modifier.height(HqSpacing.xxl))
                profileSaveError?.let {
                    Text(it, style = HqType.bodySmall, color = c.error, modifier = Modifier.padding(top = HqSpacing.sm))
                }
                HqButton(
                    text = if (profileSaving) "Saving…" else "Continue",
                    enabled = name.isNotBlank() && !profileSaving,
                    loading = profileSaving,
                    onClick = {
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
                )
            }
            "goal" -> {
                OnboardingStepLabel(1, "Choose your goal")
                Text("What are you here for?", style = HqType.headlineMedium, color = c.textPrimary)
                Text("You can change this later.", style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(top = HqSpacing.sm, bottom = HqSpacing.xl))
                IntentOptionCard(Icons.Default.Home, "Find a flat", "Look for rooms and places to move into.") { step = "findFlat" }
                Spacer(Modifier.height(HqSpacing.md))
                IntentOptionCard(Icons.Default.Person, "Find a person", "Look for potential flatmates.") { step = "findPerson" }
                Spacer(Modifier.height(HqSpacing.md))
                IntentOptionCard(Icons.Default.Group, "Create a flat", "Set up a shared household to manage together.") { onChoose(OnboardingIntent.MANAGE_FLAT) }
                Spacer(Modifier.height(HqSpacing.md))
                IntentOptionCard(Icons.Default.VpnKey, "Join a flat", "Have an invite code?") { onChoose(OnboardingIntent.JOIN_FLAT) }
                Spacer(Modifier.height(HqSpacing.xl))
                HqTextButton(text = "Sign out", onClick = onSignOut, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            "findFlat", "findPerson" -> {
                val people = step == "findPerson"
                OnboardingStepLabel(4, if (people) "Find a person" else "Find a flat")
                Text(if (people) "Looking for a flatmate" else "Looking for a place", style = HqType.headlineMedium, color = c.textPrimary)
                Spacer(Modifier.height(HqSpacing.lg))
                HqTextField(value = city, onValueChange = { city = it }, label = "City / area", leadingIcon = Icons.Filled.LocationOn)
                Spacer(Modifier.height(HqSpacing.md))
                HqTextField(value = budget, onValueChange = { budget = it }, label = "Monthly budget (₹)", leadingIcon = Icons.Filled.CurrencyRupee)
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
                HqButton(text = "Continue", onClick = {
                    goalMode = if (people) DiscoverMode.FIND_A_PERSON.name else DiscoverMode.USE_A_FLAT.name
                    step = "done"
                })
            }
            else -> {
                OnboardingStepLabel(5, "Start exploring")
                Image(
                    painter = painterResource(R.drawable.onboard_create),
                    contentDescription = "Your new home is ready",
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(HqSpacing.xl))
                Text("Welcome to Habitiq", style = HqType.headlineMedium, color = c.textPrimary)
                Text("Let's find the right people and places for you.", style = HqType.bodyLarge, color = c.textSecondary, modifier = Modifier.padding(top = HqSpacing.md, bottom = HqSpacing.lg))
                HqCard {
                    (listOf("Account created") + (if (profileSaved) listOf("Profile set up") else emptyList()) + listOf("Your goal is ready")).forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = HqSpacing.xs)) {
                            Icon(Icons.Filled.CheckCircle, null, tint = c.success, modifier = Modifier.size(20.dp))
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

@Composable
private fun IntentOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val c = LocalHqColors.current
    HqCard(variant = HqCardVariant.Interactive, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.lg)
        ) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(c.brandPrimaryContainer),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = c.brandPrimary, modifier = Modifier.size(HqIconSize.md)) }
            Column(Modifier.weight(1f)) {
                Text(title, style = HqType.titleMedium, color = c.textPrimary)
                Text(subtitle, style = HqType.bodySmall, color = c.textSecondary, modifier = Modifier.padding(top = HqSpacing.xs))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = c.textTertiary, modifier = Modifier.size(HqIconSize.sm))
        }
    }
}

@Composable
private fun OnboardingStepLabel(step: Int, label: String) {
    val c = LocalHqColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        modifier = Modifier.padding(bottom = HqSpacing.lg)
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(c.brandPrimaryContainer),
            contentAlignment = Alignment.Center
        ) { Text(step.toString(), style = HqType.titleSmall, color = c.brandPrimary) }
        Text(label, style = HqType.labelLarge, color = c.textSecondary)
    }
}

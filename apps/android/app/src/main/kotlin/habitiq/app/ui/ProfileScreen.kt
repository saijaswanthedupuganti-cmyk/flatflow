package habitiq.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import habitiq.app.data.SeekerProfile
import habitiq.app.discover.DiscoverFilterLogic
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.flats.launchShareInviteCode
import habitiq.app.ui.components.HqAvatar
import habitiq.app.ui.components.HqAvatarSize
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardSkeleton
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqConfirmDialog
import habitiq.app.ui.components.HqErrorState
import habitiq.app.ui.components.HqSkeletonBlock
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

private enum class ProfilePane { HOME, EDIT, FLAT, DISCOVERY }

@Composable
fun ProfileScreen(
    user: FirebaseUser?,
    flatViewModel: FlatViewModel,
    onOpenSettings: () -> Unit,
    onOpenMembers: () -> Unit,
    onOpenFlatSettings: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenFlatSwitcher: () -> Unit,
    onOpenMyPosts: () -> Unit = {},
    onStartOnboarding: () -> Unit = {},
    onNoFlatRemaining: () -> Unit = {},
    onSignOut: () -> Unit
) {
    val profile by flatViewModel.userProfile.collectAsStateWithLifecycleCompat()
    val flatInfo by flatViewModel.flatInfo.collectAsStateWithLifecycleCompat()
    val members by flatViewModel.members.collectAsStateWithLifecycleCompat()
    val isAdmin by flatViewModel.isAdmin.collectAsStateWithLifecycleCompat()
    val loading by flatViewModel.loading.collectAsStateWithLifecycleCompat()
    val error by flatViewModel.error.collectAsStateWithLifecycleCompat()
    val seekers by flatViewModel.seekerProfiles.collectAsStateWithLifecycleCompat()
    val uid = user?.uid.orEmpty()
    val myLooking = seekers.find { it.id == uid }

    var pane by remember { mutableStateOf(ProfilePane.HOME) }
    var showLeaveConfirm by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var awaitingLeave by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(profile?.activeFlatId, awaitingLeave) {
        if (awaitingLeave && profile?.activeFlatId == null) {
            awaitingLeave = false
            onNoFlatRemaining()
        }
    }

    when {
        error != null && profile == null && flatInfo == null -> ProfileErrorPane(
            onRetry = { flatViewModel.refresh() }
        )
        loading && profile == null && flatInfo == null -> ProfileSkeleton()
        pane == ProfilePane.EDIT -> EditAccountPane(
            user = user,
            currentName = profile?.displayName ?: user?.displayName.orEmpty(),
            onBack = { pane = ProfilePane.HOME },
            onSave = { name ->
                flatViewModel.updateDisplayName(name)
                pane = ProfilePane.HOME
            }
        )
        pane == ProfilePane.DISCOVERY -> DiscoveryProfilePane(
            looking = myLooking,
            onBack = { pane = ProfilePane.HOME },
            onSave = { city, lookingIn, budget, bio, gender, tags, active ->
                flatViewModel.updateSeekerProfile(city, lookingIn, budget, bio, gender, tags, active)
                pane = ProfilePane.HOME
            }
        )
        pane == ProfilePane.FLAT -> MyFlatPane(
            flatName = flatInfo?.name ?: "Your flat",
            flatId = flatInfo?.id,
            isAdmin = isAdmin,
            members = members,
            uid = uid,
            multiFlat = (profile?.flatIds?.size ?: 0) > 1,
            onBack = { pane = ProfilePane.HOME },
            onOpenMembers = onOpenMembers,
            onOpenFlatSettings = onOpenFlatSettings,
            onOpenActivity = onOpenActivity,
            onOpenFlatSwitcher = onOpenFlatSwitcher,
            onLeave = { showLeaveConfirm = true }
        )
        else -> ProfileHomePane(
            user = user,
            displayName = (profile?.displayName?.ifBlank { null } ?: user?.displayName).orEmpty()
                .ifBlank { user?.email?.substringBefore("@").orEmpty() },
            isAdmin = isAdmin,
            flatName = flatInfo?.name,
            memberCount = members.size,
            lookingActive = myLooking?.active == true,
            multiFlat = (profile?.flatIds?.size ?: 0) > 1,
            onEdit = { pane = ProfilePane.EDIT },
            onOpenFlat = {
                if (flatInfo == null) onStartOnboarding() else pane = ProfilePane.FLAT
            },
            onOpenDiscovery = { pane = ProfilePane.DISCOVERY },
            onOpenMyPosts = onOpenMyPosts,
            onOpenPreferences = onOpenSettings,
            onSignOut = { showSignOutConfirm = true }
        )
    }

    if (showLeaveConfirm) {
        HqConfirmDialog(
            title = "Leave this flat?",
            message = "Your tasks will be reassigned. You can rejoin with the invite code.",
            confirmLabel = "Leave",
            onConfirm = {
                showLeaveConfirm = false
                awaitingLeave = true
                flatViewModel.leaveFlat()
            },
            onDismiss = { showLeaveConfirm = false },
        )
    }
    if (showSignOutConfirm) {
        HqConfirmDialog(
            title = "Sign out?",
            message = "You'll need to sign in again to access Habitiq.",
            confirmLabel = "Sign out",
            onConfirm = {
                showSignOutConfirm = false
                onSignOut()
            },
            onDismiss = { showSignOutConfirm = false },
        )
    }
}

@Composable
private fun ProfileHomePane(
    user: FirebaseUser?,
    displayName: String,
    isAdmin: Boolean,
    flatName: String?,
    memberCount: Int,
    lookingActive: Boolean,
    multiFlat: Boolean,
    onEdit: () -> Unit,
    onOpenFlat: () -> Unit,
    onOpenDiscovery: () -> Unit,
    onOpenMyPosts: () -> Unit,
    onOpenPreferences: () -> Unit,
    onSignOut: () -> Unit
) {
    val c = LocalHqColors.current
    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(rememberScrollState())
            .padding(HqSpacing.xl)
    ) {
        Text("Profile", style = HqType.headlineLarge, color = c.textPrimary)
        Spacer(Modifier.height(HqSpacing.xxl))
        HqAvatar(
            name = displayName.ifBlank { user?.email.orEmpty() },
            size = HqAvatarSize.XL,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(HqSpacing.md))
        Text(displayName.ifBlank { "Habitiq" }, style = HqType.titleLarge, color = c.textPrimary, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(user?.email.orEmpty(), style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.align(Alignment.CenterHorizontally))
        if (flatName != null) {
            Text(
                if (isAdmin) "Admin · $flatName" else "Member · $flatName",
                style = HqType.bodySmall,
                color = c.textTertiary,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = HqSpacing.xs)
            )
        }
        Spacer(Modifier.height(HqSpacing.md))
        HqButton(
            text = "Edit profile",
            onClick = onEdit,
            variant = HqButtonVariant.Secondary,
            fullWidth = false,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(HqSpacing.xxxl))
        Text("MY FLAT", style = HqType.labelMedium, color = c.textTertiary)
        Spacer(Modifier.height(HqSpacing.sm))
        ProfileNavCard(
            icon = { Icon(Icons.Filled.Home, null, tint = c.brandPrimary, modifier = Modifier.size(HqIconSize.md)) },
            title = flatName ?: "No flat yet",
            subtitle = when {
                flatName == null -> "Create or join a flat to start"
                else -> {
                    val role = if (isAdmin) "Admin" else "Member"
                    val extra = if (multiFlat) " · tap to switch" else ""
                    "$role · $memberCount ${if (memberCount == 1) "member" else "members"}$extra"
                }
            },
            onClick = onOpenFlat
        )
        Spacer(Modifier.height(HqSpacing.xl))
        Text("DISCOVERY", style = HqType.labelMedium, color = c.textTertiary)
        Spacer(Modifier.height(HqSpacing.sm))
        ProfileNavCard(
            icon = { Icon(Icons.Filled.PersonSearch, null, tint = c.brandPrimary, modifier = Modifier.size(HqIconSize.md)) },
            title = "My Discovery profile",
            subtitle = if (lookingActive) "Looking post is visible" else "Help people understand if you’re a fit",
            onClick = onOpenDiscovery
        )
        Spacer(Modifier.height(HqSpacing.sm))
        ProfileNavCard(
            icon = { Icon(Icons.Filled.Home, null, tint = c.brandPrimary, modifier = Modifier.size(HqIconSize.md)) },
            title = "My posts",
            subtitle = "Create and manage Discovery posts",
            onClick = onOpenMyPosts
        )
        Spacer(Modifier.height(HqSpacing.xl))
        Text("APP", style = HqType.labelMedium, color = c.textTertiary)
        Spacer(Modifier.height(HqSpacing.sm))
        ProfileNavCard(
            icon = { Icon(Icons.Filled.Settings, null, tint = c.brandPrimary, modifier = Modifier.size(HqIconSize.md)) },
            title = "Preferences",
            subtitle = "App lock and account",
            onClick = onOpenPreferences
        )
        Spacer(Modifier.height(HqSpacing.xxxl))
        Text("ACCOUNT", style = HqType.labelMedium, color = c.textTertiary)
        Spacer(Modifier.height(HqSpacing.sm))
        // Sign out is reversible (unlike Leave Flat / Delete Account), so it stays a low-emphasis
        // neutral text action rather than a full Destructive button -- the confirm dialog above
        // is where the actual action happens.
        HqTextButton(text = "Sign out", onClick = onSignOut, color = c.textSecondary)
    }
}

@Composable
private fun ProfileNavCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val c = LocalHqColors.current
    HqCard(onClick = onClick) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)
        ) {
            icon()
            Column(Modifier.weight(1f)) {
                Text(title, style = HqType.titleMedium, color = c.textPrimary)
                Text(subtitle, style = HqType.bodySmall, color = c.textSecondary)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = c.textTertiary, modifier = Modifier.size(HqIconSize.sm))
        }
    }
}

@Composable
private fun EditAccountPane(
    user: FirebaseUser?,
    currentName: String,
    onBack: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    val c = LocalHqColors.current
    Column(Modifier.fillMaxSize().background(c.background)) {
        HqBackAppBar(title = "Edit profile", onBack = onBack)
        Column(Modifier.padding(horizontal = HqSpacing.xl)) {
            Text(
                "This is your Habitiq account — not your Discovery looking post.",
                style = HqType.bodyMedium,
                color = c.textSecondary
            )
            Spacer(Modifier.height(HqSpacing.xl))
            HqTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full name",
                singleLine = true
            )
            Spacer(Modifier.height(HqSpacing.md))
            HqTextField(
                value = user?.email.orEmpty(),
                onValueChange = {},
                label = "Email",
                enabled = false,
                helperText = "Email is tied to sign-in and can’t be changed here.",
                singleLine = true
            )
            if (!user?.photoUrl?.toString().isNullOrBlank()) {
                Text(
                    "Your photo comes from Google Sign-In. In-app photo upload isn’t available yet.",
                    style = HqType.caption,
                    color = c.textTertiary,
                    modifier = Modifier.padding(top = HqSpacing.sm)
                )
            }
            Spacer(Modifier.height(HqSpacing.xxl))
            HqButton(
                text = "Save changes",
                onClick = { onSave(name.trim()) },
                enabled = name.trim().isNotEmpty(),
            )
        }
    }
}

@Composable
private fun MyFlatPane(
    flatName: String,
    flatId: String?,
    isAdmin: Boolean,
    members: List<Member>,
    uid: String,
    multiFlat: Boolean,
    onBack: () -> Unit,
    onOpenMembers: () -> Unit,
    onOpenFlatSettings: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenFlatSwitcher: () -> Unit,
    onLeave: () -> Unit
) {
    val context = LocalContext.current
    val c = LocalHqColors.current
    Column(Modifier.fillMaxSize().background(c.background)) {
        HqBackAppBar(title = flatName, onBack = onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl)
        ) {
            Text(
                if (isAdmin) "Admin · ${members.size} members" else "${members.size} members",
                style = HqType.bodyMedium,
                color = c.textSecondary
            )
            Spacer(Modifier.height(HqSpacing.lg))
            members.forEach { member ->
                val you = member.uid == uid
                Text(
                    buildString {
                        append(member.nickname.ifBlank { "Member" })
                        if (you) append("  ·  You")
                        if (member.role.equals("admin", ignoreCase = true)) append("  ·  Admin")
                    },
                    style = HqType.bodyLarge,
                    color = c.textPrimary,
                    modifier = Modifier.padding(vertical = HqSpacing.xs)
                )
            }
            HqTextButton(text = "All members", onClick = onOpenMembers)
            if (!flatId.isNullOrBlank()) {
                Spacer(Modifier.height(HqSpacing.md))
                Text("Invite code", style = HqType.labelMedium, color = c.textTertiary)
                Text(flatId, style = HqType.titleMedium, color = c.brandPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    HqTextButton(text = "Copy", onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Habitiq invite", flatId))
                    })
                    HqTextButton(text = "Share", onClick = { launchShareInviteCode(context, flatName, flatId) })
                }
            }
            if (multiFlat) {
                HqTextButton(text = "Switch flat", onClick = onOpenFlatSwitcher)
            }
            if (isAdmin) {
                Spacer(Modifier.height(HqSpacing.sm))
                HqButton(text = "Flat settings", onClick = onOpenFlatSettings)
                HqTextButton(text = "Activity log", onClick = onOpenActivity)
            }
            Spacer(Modifier.height(HqSpacing.lg))
            HqButton(text = "Leave flat", onClick = onLeave, variant = HqButtonVariant.Destructive)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiscoveryProfilePane(
    looking: SeekerProfile?,
    onBack: () -> Unit,
    onSave: (city: String, lookingIn: String, budget: Double, bio: String, gender: String, tags: String, active: Boolean) -> Unit
) {
    var city by remember { mutableStateOf(looking?.city.orEmpty()) }
    var lookingIn by remember { mutableStateOf(looking?.lookingIn.orEmpty()) }
    var budget by remember { mutableStateOf(looking?.budget?.takeIf { it > 0 }?.toInt()?.toString().orEmpty()) }
    var bio by remember { mutableStateOf(looking?.bio.orEmpty()) }
    var gender by remember { mutableStateOf(looking?.gender?.ifBlank { "any" } ?: "any") }
    var tags by remember {
        mutableStateOf(
            looking?.lifestyleTags.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        )
    }
    var active by remember { mutableStateOf(looking?.active != false) }
    val identityOptions = listOf(
        "any" to "Prefer not to say",
        "female" to "Woman",
        "male" to "Man",
        "other" to "Non-binary / other"
    )

    val c = LocalHqColors.current
    Column(Modifier.fillMaxSize().background(c.background)) {
        HqBackAppBar(title = "Discovery profile", onBack = onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl)
        ) {
            Text(
                "This is what people see when they look for a flatmate — not your login email.",
                style = HqType.bodyMedium,
                color = c.textSecondary,
                modifier = Modifier.padding(bottom = HqSpacing.lg)
            )
            HqTextField(city, { city = it }, label = "City", singleLine = true)
            Spacer(Modifier.height(HqSpacing.sm))
            HqTextField(lookingIn, { lookingIn = it }, label = "Areas you want", singleLine = true)
            Spacer(Modifier.height(HqSpacing.sm))
            HqTextField(
                budget,
                { budget = it.filter { c2 -> c2.isDigit() } },
                label = "Budget (₹)",
                singleLine = true
            )
            Spacer(Modifier.height(HqSpacing.md))
            Text("Your information", style = HqType.titleSmall, color = c.textPrimary)
            Text("Optional. Used so others can understand you — not as a filter you set on people.", style = HqType.caption, color = c.textTertiary)
            Spacer(Modifier.height(HqSpacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                identityOptions.forEach { (value, label) ->
                    HqChip(label = label, selected = gender == value, onClick = { gender = value })
                }
            }
            Spacer(Modifier.height(HqSpacing.md))
            Text("Lifestyle", style = HqType.titleSmall, color = c.textPrimary)
            Spacer(Modifier.height(HqSpacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                DiscoverFilterLogic.lifestyleTagOptions.forEach { tag ->
                    HqChip(label = tag, selected = tag in tags, onClick = { tags = if (tag in tags) tags - tag else tags + tag })
                }
            }
            Spacer(Modifier.height(HqSpacing.sm))
            HqTextField(bio, { bio = it }, label = "About you", singleLine = false, minLines = 3)
            Spacer(Modifier.height(HqSpacing.md))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Visible in Discovery", style = HqType.titleSmall, color = c.textPrimary)
                    Text("When on, this looking post can appear in Find a person.", style = HqType.caption, color = c.textTertiary)
                }
                Switch(checked = active, onCheckedChange = { active = it })
            }
            Spacer(Modifier.height(HqSpacing.xl))
            HqButton(
                text = "Save Discovery profile",
                onClick = {
                    onSave(city, lookingIn, budget.toDoubleOrNull() ?: 0.0, bio, gender, tags.joinToString(","), active)
                },
            )
        }
    }
}

@Composable
private fun ProfileSkeleton() {
    val c = LocalHqColors.current
    Column(Modifier.fillMaxSize().background(c.background).padding(HqSpacing.xl)) {
        HqSkeletonBlock(modifier = Modifier.fillMaxWidth(0.4f), height = HqSpacing.xxl)
        Spacer(Modifier.height(HqSpacing.xxl))
        Box(Modifier.size(HqAvatarSize.XL.diameter).clip(CircleShape).background(c.surfaceDisabled).align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(HqSpacing.lg))
        HqSkeletonBlock(modifier = Modifier.fillMaxWidth(0.5f).align(Alignment.CenterHorizontally), height = 18.dp)
        Spacer(Modifier.height(HqSpacing.xxxl))
        HqCardSkeleton()
        Spacer(Modifier.height(HqSpacing.md))
        HqCardSkeleton()
    }
}

@Composable
private fun ProfileErrorPane(onRetry: () -> Unit) {
    HqErrorState(
        title = "Couldn't load your profile.",
        message = "Check your connection and try again.",
        onRetry = onRetry,
    )
}

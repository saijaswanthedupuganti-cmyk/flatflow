package habitiq.app.ui

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import habitiq.app.lib.pairwisePersonalBalances
import habitiq.app.lib.leaveFlatConsequences
import habitiq.app.lib.LeaveMember
import androidx.activity.compose.BackHandler
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
    val expenses by flatViewModel.expenses.collectAsStateWithLifecycleCompat()
    val settlements by flatViewModel.settlements.collectAsStateWithLifecycleCompat()
    val uid = user?.uid.orEmpty()
    val myLooking = seekers.find { it.id == uid }

    var pane by remember { mutableStateOf(ProfilePane.HOME) }
    BackHandler(enabled = pane != ProfilePane.HOME) { pane = ProfilePane.HOME }
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
        else -> {
            val displayName = (profile?.displayName?.ifBlank { null } ?: user?.displayName).orEmpty()
                .ifBlank { user?.email?.substringBefore("@").orEmpty() }
            val role = if (isAdmin) "Admin" else "Member"
            val multiFlat = (profile?.flatIds?.size ?: 0) > 1
            ProfileHomeContent(
                model = ProfileUiModel(
                    displayName = displayName,
                    email = user?.email.orEmpty(),
                    flatName = flatInfo?.name,
                    flatSupport = "$role · ${members.size} ${if (members.size == 1) "member" else "members"}" + if (multiFlat) " · switch flats" else "",
                    discoverySupport = if (myLooking?.active == true) "Your looking post is visible" else "Help people understand if you're a fit",
                ),
                onEdit = { pane = ProfilePane.EDIT },
                onOpenFlat = { if (flatInfo == null) onStartOnboarding() else pane = ProfilePane.FLAT },
                onOpenDiscovery = { pane = ProfilePane.DISCOVERY },
                onOpenMyPosts = onOpenMyPosts,
                onOpenPreferences = onOpenSettings,
                onSignOut = { showSignOutConfirm = true },
            )
        }
    }

    if (showLeaveConfirm) {
        HqConfirmDialog(
            title = "Leave this flat?",
            message = leaveFlatConsequences(
                members = members.map { LeaveMember(it.uid, it.role, "") },
                leaverUid = uid,
                hasUnsettledBalances = pairwisePersonalBalances(expenses, settlements, uid).isNotEmpty(),
            ),
            confirmLabel = "Leave flat",
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
            title = "Sign out of Oddroof?",
            message = "You'll need to sign in again to use Oddroof.",
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
private fun EditAccountPane(
    user: FirebaseUser?,
    currentName: String,
    onBack: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    val c = LocalHqColors.current
    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqBackAppBar(title = "Edit profile", onBack = onBack)
        Column(Modifier.padding(horizontal = HqSpacing.xl)) {
            Box(Modifier.fillMaxWidth().padding(top = 5.dp, bottom = 25.dp), contentAlignment = Alignment.Center) {
                habitiq.app.ui.components.HqAvatar(name.ifBlank { user?.email.orEmpty() }, size = habitiq.app.ui.components.HqAvatarSize.XL)
            }
            Text(
                "This is your Oddroof account — not your Discovery looking post.",
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
                    color = c.textMuted,
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
    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqBackAppBar(title = flatName, onBack = onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl).padding(bottom = HqSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.md),
        ) {
            // Figma flat-summary: a centred soft card with the flat, your role and the member avatars.
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(c.surfaceSubtle).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                habitiq.app.ui.components.HqIconTile(habitiq.app.ui.components.HqIcons.Home, habitiq.app.ui.components.HqTileTone.Teal)
                Text(flatName, style = HqType.titleMedium2, color = c.textPrimary, modifier = Modifier.padding(top = 10.dp))
                Text(if (isAdmin) "You're an admin" else "Member", style = HqType.bodyMedium, color = c.textSecondary)
                Row(Modifier.padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    members.take(5).forEach { member ->
                        habitiq.app.ui.components.HqAvatar(
                            member.nickname.ifBlank { "Member" }, size = habitiq.app.ui.components.HqAvatarSize.MD,
                            tone = habitiq.app.ui.components.hqToneFor(member.nickname, member.uid == uid),
                            modifier = Modifier.border(2.dp, Color.White, CircleShape),
                        )
                    }
                }
                Text("${members.size} ${if (members.size == 1) "member" else "members"}", style = HqType.labelMedium, color = c.textPrimary, fontWeight = FontWeight.Bold)
            }
            if (!flatId.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Invite code", style = HqType.labelMedium, color = c.textMuted)
                    Text(flatId, style = HqType.code, color = c.textBrand)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    HqButton(text = "Share invite", onClick = { launchShareInviteCode(context, flatName, flatId) }, variant = HqButtonVariant.Secondary, fullWidth = false)
                    HqTextButton(text = "Copy", onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Oddroof invite", flatId))
                    })
                }
            }
            habitiq.app.ui.components.HqMenuGroup(null) {
                habitiq.app.ui.components.HqMenuRow("Members", support = "${members.size} in this flat", lastRow = !isAdmin && !multiFlat, onClick = onOpenMembers)
                if (multiFlat) habitiq.app.ui.components.HqMenuRow("Switch flat", support = "Open another flat", lastRow = !isAdmin, onClick = onOpenFlatSwitcher)
                if (isAdmin) {
                    habitiq.app.ui.components.HqMenuRow("Flat settings", support = "Name, join mode and vacancy", onClick = onOpenFlatSettings)
                    habitiq.app.ui.components.HqMenuRow("Activity log", support = "Everything that happened in the flat", lastRow = true, onClick = onOpenActivity)
                }
            }
            Spacer(Modifier.height(HqSpacing.sm))
            habitiq.app.ui.components.HqDangerSoftButton("Leave flat", onLeave)
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
    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqBackAppBar(title = "Discovery Profile", onBack = onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl)
        ) {
            Text(
                "How you appear in Discover. This is not your login email.",
                style = HqType.bodyMedium,
                color = c.textSecondary,
                modifier = Modifier.padding(bottom = HqSpacing.lg)
            )
            HqTextField(city, { city = it }, label = "City", singleLine = true)
            Spacer(Modifier.height(HqSpacing.sm))
            HqTextField(lookingIn, { lookingIn = it }, label = "Looking in", singleLine = true)
            Spacer(Modifier.height(HqSpacing.sm))
            HqTextField(
                budget,
                { budget = it.filter { c2 -> c2.isDigit() } },
                label = "Monthly budget (₹)",
                singleLine = true
            )
            Spacer(Modifier.height(HqSpacing.md))
            habitiq.app.ui.components.HqSectionTitle("Your information")
            Text("Optional. Used so others can understand you — not as a filter you set on people.", style = HqType.caption, color = c.textMuted)
            Spacer(Modifier.height(HqSpacing.sm))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                identityOptions.forEach { (value, label) ->
                    HqChip(label = label, selected = gender == value, onClick = { gender = value })
                }
            }
            Spacer(Modifier.height(HqSpacing.md))
            habitiq.app.ui.components.HqSectionTitle("Lifestyle")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                DiscoverFilterLogic.lifestyleTagOptions.forEach { tag ->
                    HqChip(label = tag, selected = tag in tags, onClick = { tags = if (tag in tags) tags - tag else tags + tag })
                }
            }
            Spacer(Modifier.height(HqSpacing.sm))
            HqTextField(bio, { bio = it }, label = "About you", singleLine = false, minLines = 3)
            Spacer(Modifier.height(HqSpacing.md))
            habitiq.app.ui.components.HqSettingRow(
                title = "Visible in Discovery",
                support = "When on, your looking post appears in Find a flatmate.",
            ) {
                habitiq.app.ui.components.HqSwitch(checked = active, onCheckedChange = { active = it }, contentDescription = "Visible in Discovery")
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
    Column(Modifier.fillMaxSize().background(c.canvas).padding(HqSpacing.xl)) {
        HqSkeletonBlock(modifier = Modifier.fillMaxWidth(0.4f), height = HqSpacing.xxl)
        Spacer(Modifier.height(HqSpacing.xxl))
        Box(Modifier.size(HqAvatarSize.XL.diameter).clip(CircleShape).background(c.disabledBg).align(Alignment.CenterHorizontally))
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

package habitiq.app.ui

import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.components.HqTileTone
import habitiq.app.ui.components.HqDangerSoftButton
import habitiq.app.ui.components.HqMenuRow
import habitiq.app.ui.components.HqMenuGroup
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqPageHeader
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqListSkeleton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import habitiq.app.ui.components.HqAvatar
import habitiq.app.ui.components.HqAvatarSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * One gender list for every screen that asks for it (labels in this order). Stored values stay as they always
 * were ("female", "male", "other", "any"), so existing documents keep working; [normalizeGender] maps older or
 * free-text values ("Female", "Woman", "Prefer not to say", …) to one of these keys for display.
 */
val GenderOptions: List<Pair<String, String>> = listOf(
    "female" to "Woman",
    "male" to "Man",
    "other" to "Non-binary",
    "any" to "Prefer not to say",
)

/** Maps any stored gender value (old labels included) to a [GenderOptions] key. Unknown or blank means "any". */
fun normalizeGender(stored: String?): String = when (stored?.trim()?.lowercase()) {
    "female", "woman", "women", "f" -> "female"
    "male", "man", "men", "m" -> "male"
    "other", "non-binary", "nonbinary", "non-binary / other" -> "other"
    else -> "any"
}

/** Display label for a stored gender value. */
fun genderLabel(stored: String?): String = GenderOptions.first { it.first == normalizeGender(stored) }.second

/** Everything the Profile home shows. The person's real identity is the only visual anchor. */
data class ProfileUiModel(
    val displayName: String,
    val email: String,
    /** Null when the person has no flat yet; the row then offers to continue setup. */
    val flatName: String?,
    val flatSupport: String,
    val discoverySupport: String,
    /** True while the person has an active flat but its details haven't arrived yet. */
    val flatLoading: Boolean = false,
)

/**
 * Profile home, following the Figma Make Profile: page header with an edit action, an identity row, then
 * grouped menu cards (Your flat, Discover, Preferences) and a soft Log out button, set apart at the bottom.
 */
@Composable
fun ProfileHomeContent(
    model: ProfileUiModel,
    onEdit: () -> Unit,
    onOpenFlat: () -> Unit,
    onOpenDiscovery: () -> Unit,
    onOpenMyPosts: () -> Unit,
    onOpenPreferences: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalHqColors.current
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = HqSpacing.screenHorizontal)
            .padding(top = HqSpacing.sm, bottom = HqSpacing.screenEnd),
    ) {
        HqPageHeader(
            title = "Profile",
            action = {
                habitiq.app.ui.components.HqIconButton(icon = HqIcons.Edit, contentDescription = "Edit profile", onClick = onEdit)
            },
        )

        // Identity card: the real name (or email) only. With neither, the card asks for a name rather
        // than showing a placeholder as if it were data.
        val title = model.displayName.ifBlank { model.email }
        HqCard(
            modifier = Modifier.padding(top = HqSpacing.xs, bottom = HqSpacing.xxl),
            variant = HqCardVariant.Interactive,
            onClick = onEdit,
            padding = HqSpacing.component,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HqSpacing.related),
            ) {
                HqAvatar(name = title, size = HqAvatarSize.PROFILE)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                    Text(
                        title.ifBlank { "Add your name" },
                        style = HqType.titleMedium2,
                        color = if (title.isBlank()) c.textBrand else c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (model.email.isNotBlank() && model.email != title) {
                        Text(
                            model.email,
                            style = HqType.bodyMedium,
                            color = c.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Icon(HqIcons.Chevron, contentDescription = null, tint = c.iconDefault, modifier = Modifier.size(HqIconSize.sm))
            }
        }

        habitiq.app.ui.theme.HqFadeUp(index = 1) {
            when {
                // The flat is still loading: don't claim "No flat yet" before we know.
                model.flatLoading -> HqListSkeleton(rows = 1)
                model.flatName != null -> HqMenuGroup("Your flat") {
                    HqMenuRow(model.flatName, support = model.flatSupport, icon = HqIcons.Home, tone = HqTileTone.Teal, lastRow = true, onClick = onOpenFlat)
                }
                else -> HqMenuGroup("Your flat") {
                    HqMenuRow("No flat yet", support = "Create or join a flat", icon = HqIcons.Home, tone = HqTileTone.Teal, lastRow = true, onClick = onOpenFlat)
                }
            }
        }
        Spacer(Modifier.size(HqSpacing.xl))
        habitiq.app.ui.theme.HqFadeUp(index = 2) {
        HqMenuGroup("Discover") {
            HqMenuRow("Your Discover profile", support = model.discoverySupport, icon = HqIcons.Discover, tone = HqTileTone.Coral, onClick = onOpenDiscovery)
            HqMenuRow("My posts", support = "Rooms you listed and your looking post", icon = HqIcons.Receipt, tone = HqTileTone.Sand, lastRow = true, onClick = onOpenMyPosts)
        }
        }
        Spacer(Modifier.size(HqSpacing.xl))
        habitiq.app.ui.theme.HqFadeUp(index = 3) {
        HqMenuGroup("Preferences") {
            HqMenuRow("Privacy and security", support = "App lock and account", icon = HqIcons.Shield, tone = HqTileTone.Neutral, lastRow = true, onClick = onOpenPreferences)
        }
        }
        Spacer(Modifier.size(HqSpacing.xxl))
        HqDangerSoftButton("Log out", onSignOut)
    }
}

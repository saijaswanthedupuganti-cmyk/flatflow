package habitiq.app.ui

import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.components.HqTileTone
import habitiq.app.ui.components.HqDangerSoftButton
import habitiq.app.ui.components.HqMenuRow
import habitiq.app.ui.components.HqMenuGroup
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqPageHeader
import androidx.compose.ui.unit.dp
import androidx.compose.material3.IconButton
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
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqGroup
import habitiq.app.ui.components.HqNavRow
import habitiq.app.ui.components.HqRowDivider
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/** Everything the Profile home shows. The person's real identity is the only visual anchor. */
data class ProfileUiModel(
    val displayName: String,
    val email: String,
    /** Null when the person has no flat yet; the row then offers to continue setup. */
    val flatName: String?,
    val flatSupport: String,
    val discoverySupport: String,
)

/**
 * Profile home, following the Figma Make Profile: page header with an edit action, an identity row, then
 * grouped menu cards (My flat, Discovery, Preferences) and a soft sign-out button.
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
                IconButton(onClick = onEdit, modifier = Modifier.size(HqSize.target)) {
                    Icon(HqIcons.Edit, contentDescription = "Edit profile", tint = c.iconDefault, modifier = Modifier.size(HqIconSize.md))
                }
            },
        )

        Row(Modifier.padding(top = 4.dp, bottom = 28.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HqAvatar(name = model.displayName.ifBlank { model.email }, size = HqAvatarSize.PROFILE)
            Column(Modifier.weight(1f)) {
                Text(model.displayName.ifBlank { "Oddroof" }, style = HqType.titleMedium2, color = c.textPrimary)
                Text(model.email, style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(top = 3.dp))
            }
            HqButton(text = "Edit profile", onClick = onEdit, variant = HqButtonVariant.Secondary, fullWidth = false)
        }

        HqMenuGroup("My flat") {
            if (model.flatName != null) {
                HqMenuRow(model.flatName, support = model.flatSupport, icon = HqIcons.Home, tone = HqTileTone.Teal, lastRow = true, onClick = onOpenFlat)
            } else {
                HqMenuRow("You haven't joined a flat yet.", support = "Continue setup", icon = HqIcons.Home, tone = HqTileTone.Teal, lastRow = true, onClick = onOpenFlat)
            }
        }
        Spacer(Modifier.size(21.dp))
        HqMenuGroup("Discovery") {
            HqMenuRow("My Discovery Profile", support = model.discoverySupport, icon = HqIcons.Discover, tone = HqTileTone.Coral, onClick = onOpenDiscovery)
            HqMenuRow("My Posts", support = "Create and manage Discovery posts", icon = HqIcons.Receipt, tone = HqTileTone.Sand, lastRow = true, onClick = onOpenMyPosts)
        }
        Spacer(Modifier.size(21.dp))
        HqMenuGroup("Preferences") {
            HqMenuRow("Privacy & Security", support = "App lock and account", icon = HqIcons.Shield, tone = HqTileTone.Neutral, lastRow = true, onClick = onOpenPreferences)
        }
        Spacer(Modifier.size(24.dp))
        HqDangerSoftButton("Sign out", onSignOut)
    }
}

package habitiq.app.ui.components

import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * One reusable app bar for every screen. It follows the Figma page header: an optional back button, then a
 * large DM Sans title and trailing actions, on the canvas rather than in a Material bar.
 */
@Composable
private fun HqAppBarBase(
    title: String?,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val c = LocalHqColors.current
    Row(
        modifier.fillMaxWidth().background(c.canvas)
            .padding(start = if (onBack != null) 13.dp else 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(HqSize.target)) {
                Icon(HqIcons.Back, contentDescription = "Back", tint = c.iconDefault, modifier = Modifier.size(HqIconSize.md))
            }
        }
        if (title != null) {
            Text(title, style = HqType.titleLarge2, color = c.textPrimary, maxLines = 2, modifier = Modifier.weight(1f))
        } else {
            Spacer(Modifier.weight(1f))
        }
        actions()
    }
}

/** App-root screens (Home, Tasks, Expenses, Discover, Profile) -- title only, no back button. */
@Composable
fun HqRootAppBar(title: String, modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) =
    HqAppBarBase(title = title, modifier = modifier, onBack = null, actions = actions)

/** Standard back-navigable screen. */
@Composable
fun HqBackAppBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) =
    HqAppBarBase(title = title, modifier = modifier, onBack = onBack, actions = actions)

/** Back button with no title -- e.g. a screen whose title lives in the body instead of the bar. */
@Composable
fun HqTitleOnlyAppBar(onBack: () -> Unit, modifier: Modifier = Modifier) =
    HqAppBarBase(title = null, modifier = modifier, onBack = onBack)

/** Back-navigable screen with a single trailing action (e.g. overflow menu, save). */
@Composable
fun HqTitleWithActionAppBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier, action: @Composable RowScope.() -> Unit) =
    HqAppBarBase(title = title, modifier = modifier, onBack = onBack, actions = action)

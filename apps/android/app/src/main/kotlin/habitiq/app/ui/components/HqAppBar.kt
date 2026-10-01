package habitiq.app.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqTouchTarget
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * One reusable app bar covering the variants in the design doc section 42, instead of hand-built
 * back buttons per screen. All variants share the same title style, back-button placement, and
 * transparent-on-surface background.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HqAppBarBase(
    title: String?,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val c = LocalHqColors.current
    TopAppBar(
        title = { title?.let { Text(it, style = HqType.titleLarge, color = c.textPrimary) } },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(HqTouchTarget)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = c.textPrimary, modifier = Modifier.size(HqIconSize.md))
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = c.background,
            titleContentColor = c.textPrimary,
        ),
        modifier = modifier,
    )
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

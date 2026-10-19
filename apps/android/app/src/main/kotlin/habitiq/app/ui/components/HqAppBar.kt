package habitiq.app.ui.components

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
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
import androidx.compose.foundation.layout.defaultMinSize
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
            HqIconButton(icon = HqIcons.Back, contentDescription = "Back", onClick = onBack)
        }
        if (title != null) {
            Text(
                title, style = HqType.titleLarge2, color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
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

/**
 * THE app bar pattern for new and migrated screens (one pattern, two sizes):
 *
 * - Standard (`large = false`): 64dp bar on the canvas -- 48dp back arrow, an 18sp single-line title,
 *   trailing [actions] (each an [HqIconButton] or [HqTextButton]). Use on detail screens and forms.
 * - Large (`large = true`): the same 56dp row with back + actions, then a 28sp display title on its
 *   own line at the screen gutter. Use on a hierarchy's top screen and on screens whose title is
 *   the content (a task name).
 *
 * The title is a heading for TalkBack; the back arrow is announced "Back". [onBack] null = root screen.
 */
@Composable
fun HqAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    large: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val c = LocalHqColors.current
    val gutter = habitiq.app.ui.theme.HqSpacing.screenHorizontal
    androidx.compose.foundation.layout.Column(modifier.fillMaxWidth().background(c.canvas)) {
        Row(
            Modifier.fillMaxWidth()
                .defaultMinSize(minHeight = if (large) HqSize.appBarContent else 64.dp)
                .padding(start = if (onBack != null) habitiq.app.ui.theme.HqSpacing.xs else gutter, end = habitiq.app.ui.theme.HqSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(habitiq.app.ui.theme.HqSpacing.xs),
        ) {
            if (onBack != null) HqIconButton(icon = HqIcons.Back, contentDescription = "Back", onClick = onBack)
            if (!large) {
                Text(
                    title, style = HqType.titleSmall2, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            actions()
        }
        if (large) {
            Text(
                title, style = HqType.titleLarge2, color = c.textPrimary,
                modifier = Modifier.fillMaxWidth().padding(start = gutter, end = gutter, top = habitiq.app.ui.theme.HqSpacing.xs, bottom = habitiq.app.ui.theme.HqSpacing.lg).semantics { heading() },
            )
        }
    }
}

package habitiq.app.ui.components

import habitiq.app.ui.theme.hqPressable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqShadow
import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.hqShadow

/**
 * Figma search row: a 48dp bordered search field and a Filters button. The field is a real text input here
 * (Discover filters by typed area), and the button carries a count badge when filters are active. Below
 * 360dp, or at large font sizes, the button drops its label and keeps the icon, badge and spoken name.
 */
@Composable
fun HqSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onFilters: () -> Unit,
    activeFilters: Int,
    modifier: Modifier = Modifier,
) {
    val c = LocalHqColors.current
    val shape = HqShapes.control
    val searchFocus = androidx.compose.ui.platform.LocalFocusManager.current
    val compact = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 360 ||
        androidx.compose.ui.platform.LocalDensity.current.fontScale >= 1.5f
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        Row(
            Modifier.weight(1f).heightIn(min = HqSize.target).clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape)
                .padding(horizontal = HqSpacing.md, vertical = HqSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        ) {
            Icon(HqIcons.Search, null, tint = c.textMuted, modifier = Modifier.size(HqIconSize.sm))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) Text(placeholder, style = HqType.bodyMedium, color = c.textMuted, maxLines = 1)
                BasicTextField(
                    value = value, onValueChange = onValueChange, singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { searchFocus.clearFocus() }),
                    textStyle = HqType.bodyMedium.copy(color = c.textPrimary), cursorBrush = SolidColor(c.focus),
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = placeholder },
                )
            }
        }
        Box {
            Row(
                Modifier.heightIn(min = HqSize.target).defaultMinSize(minWidth = HqSize.target).clip(shape)
                    .background(if (activeFilters > 0) c.selectedBg else c.surfaceBase)
                    .border(1.dp, if (activeFilters > 0) c.selectedBorder else c.borderSubtle, shape)
                    .clickable(role = Role.Button, onClick = onFilters)
                    .semantics { contentDescription = if (activeFilters > 0) "Filters, $activeFilters on" else "Filters" }
                    .padding(horizontal = HqSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs, Alignment.CenterHorizontally),
            ) {
                Icon(HqIcons.Filter, null, tint = c.textPrimary, modifier = Modifier.size(HqIconSize.sm))
                if (!compact) {
                    Text("Filters", style = HqType.labelMedium, color = c.textPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.clearAndSetSemantics { })
                }
            }
            if (activeFilters > 0) {
                Box(
                    Modifier.align(Alignment.TopEnd).size(18.dp).clip(CircleShape).background(c.actionPrimaryBg).clearAndSetSemantics { },
                    contentAlignment = Alignment.Center,
                ) { Text("$activeFilters", style = HqType.labelSmall, color = c.actionPrimaryFg, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

/** One horizontally scrolling row of chips, as in the Figma Discover quick filters. */
@Composable
fun HqChipRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Row(modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
        content()
    }
}

/** Figma inbox button: a rounded square with a message glyph and an optional count in the alert colour. */
@Composable
fun HqInboxButton(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    val shape = HqShapes.control
    // The ripple is clipped to the visible rounded tile, never a bare grey square.
    Box(modifier.size(48.dp).semantics { contentDescription = if (count > 0) "Requests, $count new" else "Requests" }, contentAlignment = Alignment.Center) {
        Box(Modifier.size(42.dp).clip(shape).background(c.surfaceBase).border(BorderStroke(1.dp, c.borderSubtle), shape).hqPressable(onClick = onClick), contentAlignment = Alignment.Center) {
            Icon(HqIcons.Message, null, tint = c.textPrimary, modifier = Modifier.size(HqIconSize.md))
        }
        if (count > 0) {
            Box(Modifier.align(Alignment.TopEnd).padding(top = HqSpacing.xs / 2).size(18.dp).clip(CircleShape).background(c.actionDangerBg), contentAlignment = Alignment.Center) {
                Text("$count", style = HqType.labelSmall, color = c.actionDangerFg, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Header icon action in the same outlined 42dp tile as [HqInboxButton], so header actions match. */
@Composable
fun HqHeaderIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    val shape = HqShapes.control
    Box(modifier.size(HqSize.target), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(42.dp).clip(shape).background(c.surfaceBase).border(BorderStroke(1.dp, c.borderSubtle), shape)
                .hqPressable(onClick = onClick)
                .semantics { this.contentDescription = contentDescription },
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = c.textPrimary, modifier = Modifier.size(HqIconSize.md)) }
    }
}

/**
 * Figma `.listing-card` frame: a card-radius card with a photo slot on top and a padded body below.
 * [photo] fills the slot and can layer controls over it (the saved heart, a photo count).
 */
@Composable
fun HqListingFrame(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    photoHeight: Int = 178,
    photo: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit,
    body: @Composable () -> Unit,
) {
    val c = LocalHqColors.current
    val shape = HqShapes.card
    Column(
        modifier.fillMaxWidth()
            .hqShadow(HqShadow.Floating, shape)
            .clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape)
            .hqPressable(onClick = onClick),
    ) {
        Box(Modifier.fillMaxWidth().height(photoHeight.dp).background(c.selectedBg)) { photo() }
        Column(Modifier.padding(HqSpacing.lg), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) { body() }
    }
}


/**
 * For screens whose artwork runs under the status bar (Home hero, flat detail photo): tells the root to skip
 * its status-bar inset and switches the system icons to light while the screen is shown.
 */
/** Screens that currently draw under the status bar. Overlapping screens (e.g. during a navigation
 * transition) each hold a claim, so one leaving never turns the bleed off while another is shown. */
private var heroBleedClaims = 0

@Composable
fun HqHeroBleedEffect(lightIcons: Boolean = true) {
    val bleed = habitiq.app.ui.theme.LocalHeroBleed.current
    val view = androidx.compose.ui.platform.LocalView.current
    androidx.compose.runtime.DisposableEffect(Unit) {
        heroBleedClaims++
        bleed.value = true
        var ctx = view.context
        while (ctx is android.content.ContextWrapper && ctx !is android.app.Activity) ctx = ctx.baseContext
        val window = (ctx as? android.app.Activity)?.window
        val controller = window?.let { androidx.core.view.WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = !lightIcons
        onDispose {
            heroBleedClaims = (heroBleedClaims - 1).coerceAtLeast(0)
            if (heroBleedClaims == 0) {
                bleed.value = false
                controller?.isAppearanceLightStatusBars = true
            }
        }
    }
}

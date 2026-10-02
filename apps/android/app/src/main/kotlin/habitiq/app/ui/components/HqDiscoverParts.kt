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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Figma search row: a 44dp bordered search field and a Filters button. The field is a real text input here
 * (Discover filters by typed area), and the button carries a count badge when filters are active.
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
    val shape = RoundedCornerShape(13.dp)
    val searchFocus = androidx.compose.ui.platform.LocalFocusManager.current
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.weight(1f).height(48.dp).clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                Modifier.height(48.dp).clip(shape).background(if (activeFilters > 0) c.selectedBg else c.surfaceBase)
                    .border(1.dp, if (activeFilters > 0) c.selectedBorder else c.borderSubtle, shape)
                    .clickable(role = Role.Button, onClick = onFilters).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(HqIcons.Filter, null, tint = c.textPrimary, modifier = Modifier.size(HqIconSize.sm))
                Text("Filters", style = HqType.labelMedium, color = c.textPrimary, fontWeight = FontWeight.Bold)
            }
            if (activeFilters > 0) {
                Box(
                    Modifier.align(Alignment.TopEnd).padding(top = 0.dp).size(18.dp).clip(CircleShape).background(c.actionPrimaryBg),
                    contentAlignment = Alignment.Center,
                ) { Text("$activeFilters", style = HqType.labelSmall, color = c.actionPrimaryFg, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

/** One horizontally scrolling row of chips, as in the Figma Discover quick filters. */
@Composable
fun HqChipRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Row(modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        content()
    }
}

/** Figma inbox button: a rounded square with a message glyph and an optional coral count. */
@Composable
fun HqInboxButton(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(14.dp)
    // The ripple is clipped to the visible rounded tile, never a bare grey square.
    Box(modifier.size(48.dp).semantics { contentDescription = if (count > 0) "Connections, $count new" else "Connections" }, contentAlignment = Alignment.Center) {
        Box(Modifier.size(42.dp).clip(shape).background(c.surfaceBase).border(BorderStroke(1.dp, c.borderSubtle), shape).hqPressable(onClick = onClick), contentAlignment = Alignment.Center) {
            Icon(HqIcons.Message, null, tint = c.textPrimary, modifier = Modifier.size(HqIconSize.md))
        }
        if (count > 0) {
            Box(Modifier.align(Alignment.TopEnd).padding(top = 2.dp, end = 0.dp).size(18.dp).clip(CircleShape).background(c.brandCoral), contentAlignment = Alignment.Center) {
                Text("$count", style = HqType.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Header icon action in the same outlined 42dp tile as [HqInboxButton], so header actions match. */
@Composable
fun HqHeaderIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(14.dp)
    Box(modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(42.dp).clip(shape).background(c.surfaceBase).border(BorderStroke(1.dp, c.borderSubtle), shape)
                .hqPressable(onClick = onClick)
                .semantics { this.contentDescription = contentDescription },
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = c.textPrimary, modifier = Modifier.size(HqIconSize.md)) }
    }
}

/**
 * Figma `.listing-card` frame: a rounded 22dp card with a photo slot on top and a padded body below.
 * [photo] fills the slot; [badges] sits over the photo (photo count, "Listed today").
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
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier.fillMaxWidth()
            .shadow(4.dp, shape, ambientColor = c.textPrimary.copy(alpha = .07f), spotColor = c.textPrimary.copy(alpha = .07f))
            .clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape)
            .hqPressable(onClick = onClick),
    ) {
        Box(Modifier.fillMaxWidth().height(photoHeight.dp).background(c.selectedBg)) { photo() }
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { body() }
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

package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/** Continuous screen ground (`canvas`) (the status-bar inset comes from the app root). */
@Composable
fun FigmaScreenBackground(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxSize().background(LocalHqColors.current.canvas),
        content = content
    )
}

/**
 * Member picker avatar. Uses the shared selected-tint pair so avatar colour never reads as a
 * trust or role signal (design doc section 10.12). Selection adds a 2dp border and a check.
 */
@Composable
fun MemberInitialAvatar(
    name: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    val c = LocalHqColors.current
    val borderColor: Color = if (selected) c.selectedBorder else c.borderSubtle
    Box(
        modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(c.selectedBg)
            .border(if (selected) 2.dp else 1.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(memberInitials(name).ifEmpty { "?" }, color = c.selectedFg, style = HqType.labelMedium)
        if (selected) {
            Box(
                Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp)
                    .size(16.dp).clip(CircleShape).background(c.actionPrimaryBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, null, tint = c.actionPrimaryFg, modifier = Modifier.size(10.dp))
            }
        }
    }
}

fun memberInitials(name: String): String =
    name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")

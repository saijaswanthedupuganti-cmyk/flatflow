package habitiq.app.ui.figma

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/** Figma 64:36 — category label chip text (e.g. "Lifestyle"). */
@Composable
fun TaskCategoryLabel(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = LocalHqColors.current.textPrimary
) {
    Text(
        text = label,
        color = color,
        style = HqType.titleSmall,
        modifier = modifier
    )
}

val TaskCategories = listOf("Chores", "Lifestyle", "Bills", "Other")

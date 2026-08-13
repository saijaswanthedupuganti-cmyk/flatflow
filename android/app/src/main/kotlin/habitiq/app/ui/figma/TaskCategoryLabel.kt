package habitiq.app.ui.figma

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Figma 64:36 — category label chip text (e.g. "Lifestyle"). */
@Composable
fun TaskCategoryLabel(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF0F172A)
) {
    Text(
        text = label,
        color = color,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
        modifier = modifier
    )
}

val TaskCategories = listOf("Chores", "Lifestyle", "Bills", "Other")

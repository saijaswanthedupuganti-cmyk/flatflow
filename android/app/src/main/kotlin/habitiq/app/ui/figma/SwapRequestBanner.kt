package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.ui.theme.FigmaColors

@Composable
fun SwapRequestBanner(count: Int, onReview: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FigmaColors.PrimaryLight)
            .clickable(onClick = onReview)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.SwapHoriz, null, tint = FigmaColors.PrimaryInk, modifier = Modifier.size(20.dp))
            Text(
                "$count Pending Swap Request${if (count == 1) "" else "s"}",
                color = FigmaColors.PrimaryInk,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.26.sp
            )
        }
        Text(
            "Review",
            color = FigmaColors.PrimaryInk,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
            letterSpacing = 0.26.sp
        )
    }
}

@Composable
fun NextUpLabel(nextName: String) {
    Row {
        Text("Next up: ", color = FigmaColors.InkSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(nextName, color = FigmaColors.Ink, fontSize = 11.sp)
    }
}

@Composable
fun MemberNameLabel(name: String, modifier: Modifier = Modifier) {
    Text(name, color = FigmaColors.Ink, fontSize = 17.sp, fontWeight = FontWeight.Medium, modifier = modifier)
}

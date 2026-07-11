package habitiq.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val FlatOnboardingBackground = Color(0xFF0A0A0F)
val FlatOnboardingSubtitleColor = Color(0xFFB0B0B0)
val FlatOnboardingBenefitColor = Color(0xFFD0D0D0)

@Composable
fun FlatOnboardingHeader(
    accentColor: Color,
    imageRes: Int,
    titleLine1: String,
    titleLine2: String,
    subtitle: String,
    benefits: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )
            // Fades the illustration into the screen's background color so the hero
            // image reads as part of the page, not a pasted-in rectangle.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, FlatOnboardingBackground),
                            startY = 550f
                        )
                    )
            )
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text(titleLine1, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(titleLine2, color = accentColor, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(
                subtitle,
                color = FlatOnboardingSubtitleColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
            benefits.forEach { benefit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        benefit,
                        color = FlatOnboardingBenefitColor,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FlatRoleCallout(accentColor: Color, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = Color(0xFFE0E0E0), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

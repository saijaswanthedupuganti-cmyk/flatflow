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
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@Composable
fun FlatOnboardingHeader(
    accentColor: Color,
    imageRes: Int,
    titleLine1: String,
    titleLine2: String,
    subtitle: String,
    benefits: List<String>
) {
    val c = LocalHqColors.current
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
            // image reads as part of the page, not a pasted-in rectangle. Fraction-based
            // color stops (not startY/endY in raw px) so the fade point is consistent
            // across screen densities instead of the box's actual pixel height varying.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.45f to Color.Transparent,
                            1f to c.background
                        )
                    )
            )
        }
        Column(modifier = Modifier.padding(horizontal = HqSpacing.xxl, vertical = HqSpacing.xl)) {
            Text(titleLine1, color = c.textPrimary, style = HqType.headlineLarge)
            Text(titleLine2, color = accentColor, style = HqType.headlineLarge)
            Text(
                subtitle,
                color = c.textSecondary,
                style = HqType.bodyMedium,
                modifier = Modifier.padding(top = HqSpacing.sm, bottom = HqSpacing.lg)
            )
            benefits.forEach { benefit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = HqSpacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(HqIconSize.xs)
                    )
                    Text(
                        benefit,
                        color = c.textPrimary,
                        style = HqType.bodyMedium,
                        modifier = Modifier.padding(start = HqSpacing.sm)
                    )
                }
            }
        }
    }
}

@Composable
fun FlatRoleCallout(accentColor: Color, text: String) {
    val c = LocalHqColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(HqRadius.md))
            .padding(HqSpacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = c.textPrimary, style = HqType.labelLarge)
    }
}

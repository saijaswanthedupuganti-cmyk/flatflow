package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.ui.theme.FigmaColors

/** Figma 64:414 — Header - Top Navigation (Premium wizard). */
@Composable
fun FlatWizardPremiumHeader(
    currentStep: Int,
    totalSteps: Int = 5,
    onBack: () -> Unit,
    onSkip: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(Color(0xD9FAF8F5))
            .padding(start = 16.dp, end = 24.dp, top = 24.dp, bottom = 25.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            Modifier.size(56.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .clickable(onClick = onBack)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = FigmaColors.Ink,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(totalSteps) { index ->
                    val active = index < currentStep
                    Box(
                        Modifier
                            .height(2.dp)
                            .width(if (active && index == currentStep - 1) 40.dp else 12.dp)
                            .clip(RoundedCornerShape(9999.dp))
                            .background(if (active) FigmaColors.PrimaryDark else FigmaColors.SurfaceBorder)
                    )
                }
            }
            Text(
                "$currentStep OF $totalSteps",
                color = FigmaColors.InkSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.6.sp
            )
        }
        Box(Modifier.width(48.dp), contentAlignment = Alignment.CenterEnd) {
            if (onSkip != null) {
                Text(
                    "Skip",
                    color = FigmaColors.InkSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onSkip)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/** Figma 64:105/64:158 — Wizard header with segmented progress (5-step location flow). */
@Composable
fun FlatWizardLocationHeader(
    currentStep: Int,
    totalSteps: Int = 5,
    onBack: () -> Unit,
    onSkip: (() -> Unit)? = null,
    showHeaderSkip: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(FigmaColors.Background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(9999.dp)).clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = FigmaColors.Ink, modifier = Modifier.size(20.dp))
        }
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { index ->
                if (index > 0) Spacer(Modifier.width(4.dp))
                Box(
                    Modifier
                        .height(3.dp)
                        .width(32.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (index < currentStep) FigmaColors.Primary else FigmaColors.SurfaceBorder)
                )
            }
        }
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(40.dp)) {
            if (showHeaderSkip && onSkip != null) {
                Text(
                    "Skip",
                    color = FigmaColors.Primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.26.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onSkip)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Text(
                "$currentStep of $totalSteps",
                color = FigmaColors.InkSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** Figma 64:277 — Brilliant flow header (3-step). */
@Composable
fun FlatWizardBrilliantHeader(
    currentStep: Int,
    totalSteps: Int = 3,
    onBack: () -> Unit,
    onSkip: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(FigmaColors.Background.copy(alpha = 0.95f))
            .padding(horizontal = 21.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            Modifier.clip(RoundedCornerShape(9999.dp)).clickable(onClick = onBack).padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = FigmaColors.Ink, modifier = Modifier.size(16.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(totalSteps) { index ->
                Box(
                    Modifier
                        .height(4.dp)
                        .width(32.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(if (index < currentStep) FigmaColors.Primary else FigmaColors.BadgeNeutralBg)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Step $currentStep of $totalSteps",
                color = FigmaColors.InkSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.26.sp
            )
        }
        if (onSkip != null) {
            Text(
                "Skip",
                color = FigmaColors.InkSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.26.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onSkip)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        } else {
            Spacer(Modifier.width(40.dp))
        }
    }
}

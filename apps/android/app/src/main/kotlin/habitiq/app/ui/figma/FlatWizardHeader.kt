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
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/** Figma 64:414 — Header - Top Navigation (Premium wizard). */
@Composable
fun FlatWizardPremiumHeader(
    currentStep: Int,
    totalSteps: Int = 2,
    onBack: () -> Unit,
    onSkip: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val c = LocalHqColors.current
    Row(
        modifier
            .fillMaxWidth()
            .background(c.canvas)
            .padding(start = HqSpacing.lg, end = HqSpacing.xxl, top = HqSpacing.xxl, bottom = HqSpacing.xxl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            Modifier.size(56.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(HqRadius.full))
                    .clickable(onClick = onBack)
                    .padding(HqSpacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = c.textPrimary,
                    modifier = Modifier.size(HqIconSize.xs)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.xxl)) {
            Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs + 2.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(totalSteps) { index ->
                    val active = index < currentStep
                    Box(
                        Modifier
                            .height(2.dp)
                            .width(if (active && index == currentStep - 1) 40.dp else 12.dp)
                            .clip(RoundedCornerShape(HqRadius.full))
                            .background(if (active) c.actionPrimaryBg else c.borderSubtle)
                    )
                }
            }
            Text(
                "Step $currentStep of $totalSteps",
                color = c.textSecondary,
                style = HqType.labelMedium
            )
        }
        Box(Modifier.width(48.dp), contentAlignment = Alignment.CenterEnd) {
            if (onSkip != null) {
                Text(
                    "Skip",
                    color = c.textSecondary,
                    style = HqType.labelLarge,
                    modifier = Modifier
                        .clip(RoundedCornerShape(HqRadius.sm))
                        .clickable(onClick = onSkip)
                        .padding(horizontal = HqSpacing.sm, vertical = HqSpacing.xs)
                )
            }
        }
    }
}

/** Figma 64:105/64:158 — Wizard header with segmented progress (5-step location flow). */
@Composable
fun FlatWizardLocationHeader(
    currentStep: Int,
    totalSteps: Int = 2,
    onBack: () -> Unit,
    onSkip: (() -> Unit)? = null,
    showHeaderSkip: Boolean = false,
    modifier: Modifier = Modifier
) {
    val c = LocalHqColors.current
    Row(
        modifier
            .fillMaxWidth()
            .background(c.canvas)
            .padding(horizontal = HqSpacing.xl, vertical = HqSpacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(HqRadius.full)).clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.textPrimary, modifier = Modifier.size(HqIconSize.sm))
        }
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { index ->
                if (index > 0) Spacer(Modifier.width(HqSpacing.xs))
                Box(
                    Modifier
                        .height(3.dp)
                        .width(32.dp)
                        .clip(RoundedCornerShape(HqRadius.full))
                        .background(if (index < currentStep) c.actionPrimaryBg else c.borderSubtle)
                )
            }
        }
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(40.dp)) {
            if (showHeaderSkip && onSkip != null) {
                Text(
                    "Skip",
                    color = c.textBrand,
                    style = HqType.labelMedium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(HqRadius.sm))
                        .clickable(onClick = onSkip)
                        .padding(horizontal = HqSpacing.sm, vertical = HqSpacing.xs)
                )
            }
            Text(
                "Step $currentStep of $totalSteps",
                color = c.textSecondary,
                style = HqType.caption
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
    val c = LocalHqColors.current
    Row(
        modifier
            .fillMaxWidth()
            .background(c.canvas)
            .padding(horizontal = HqSpacing.xl, vertical = HqSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            Modifier.clip(RoundedCornerShape(HqRadius.full)).clickable(onClick = onBack).padding(HqSpacing.sm),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.textPrimary, modifier = Modifier.size(HqIconSize.xs))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            repeat(totalSteps) { index ->
                Box(
                    Modifier
                        .height(4.dp)
                        .width(32.dp)
                        .clip(RoundedCornerShape(HqRadius.full))
                        .background(if (index < currentStep) c.actionPrimaryBg else c.borderSubtle)
                )
            }
            Spacer(Modifier.width(HqSpacing.sm))
            Text(
                "Step $currentStep of $totalSteps",
                color = c.textSecondary,
                style = HqType.labelMedium
            )
        }
        if (onSkip != null) {
            Text(
                "Skip",
                color = c.textSecondary,
                style = HqType.labelMedium,
                modifier = Modifier
                    .clip(RoundedCornerShape(HqRadius.sm))
                    .clickable(onClick = onSkip)
                    .padding(horizontal = HqSpacing.sm, vertical = HqSpacing.xs)
            )
        } else {
            Spacer(Modifier.width(40.dp))
        }
    }
}

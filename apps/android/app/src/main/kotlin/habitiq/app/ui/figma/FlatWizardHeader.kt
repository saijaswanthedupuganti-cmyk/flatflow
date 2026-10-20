package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * One header for every create-flat step: a 48dp back button on the left, then the progress bar and
 * "Step X of Y". Skip never lives here; a step that can be skipped offers one Skip next to its main button.
 */
@Composable
private fun FlatWizardHeader(currentStep: Int, totalSteps: Int, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    Row(
        modifier.fillMaxWidth().background(c.canvas)
            .padding(start = HqSpacing.xs, end = HqSpacing.screenHorizontal, top = HqSpacing.sm, bottom = HqSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
    ) {
        habitiq.app.ui.components.HqIconButton(icon = HqIcons.Back, contentDescription = "Back", onClick = onBack)
        if (totalSteps <= 1) {
            Spacer(Modifier.weight(1f))
            return@Row
        }
        Row(
            Modifier.weight(1f).clearAndSetSemantics {},
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(totalSteps) { index ->
                Box(
                    Modifier.weight(1f).height(HqSpacing.xs).clip(RoundedCornerShape(HqRadius.full))
                        .background(if (index < currentStep) c.actionPrimaryBg else c.borderSubtle),
                )
            }
        }
        Spacer(Modifier.width(HqSpacing.sm))
        Text(
            "Create a flat · Step $currentStep of $totalSteps",
            color = c.textSecondary,
            style = HqType.labelMedium,
            modifier = Modifier.semantics { contentDescription = "Create a flat, step $currentStep of $totalSteps" },
        )
    }
}

/** Create a flat, step 1 (name and type). [onSkip] is ignored: the name is required. */
@Composable
fun FlatWizardPremiumHeader(
    currentStep: Int,
    totalSteps: Int = 2,
    onBack: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onSkip: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) = FlatWizardHeader(currentStep, totalSteps, onBack, modifier)

/** Create a flat, step 2 (location). Skip is offered once, under the main button, not in the header. */
@Composable
fun FlatWizardLocationHeader(
    currentStep: Int,
    totalSteps: Int = 2,
    onBack: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onSkip: (() -> Unit)? = null,
    @Suppress("UNUSED_PARAMETER") showHeaderSkip: Boolean = false,
    modifier: Modifier = Modifier
) = FlatWizardHeader(currentStep, totalSteps, onBack, modifier)

/** Single-step create-flat flow header. */
@Composable
fun FlatWizardBrilliantHeader(
    currentStep: Int,
    totalSteps: Int = 1,
    onBack: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onSkip: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) = FlatWizardHeader(currentStep, totalSteps, onBack, modifier)

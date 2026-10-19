package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/** Create a flat, step 1 of 2: name (required) and type. [onSkip] is kept for callers; the name cannot be skipped. */
@Composable
fun CreateFlatBasicsPremiumScreen(
    flatName: String,
    onFlatNameChange: (String) -> Unit,
    flatType: String,
    onFlatTypeChange: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalHqColors.current
    val types = listOf("apartment" to "Apartment", "house" to "Independent house")

    Box(modifier.fillMaxSize().background(c.canvas)) {
        Column(Modifier.fillMaxSize()) {
            FlatWizardPremiumHeader(currentStep = 1, onBack = onBack)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = HqSpacing.xxl)
                    .padding(top = HqSpacing.lg, bottom = HqSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(HqSpacing.xxxl)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    Text(
                        "Name your flat",
                        color = c.textPrimary,
                        style = HqType.headlineLarge
                    )
                    Text(
                        "Your flatmates will see this name.",
                        color = c.textSecondary,
                        style = HqType.bodyLarge
                    )
                }
                HqTextField(
                    value = flatName,
                    onValueChange = onFlatNameChange,
                    label = "Flat name",
                    placeholder = "e.g. The Penthouse, Cozy 3BHK",
                    leadingIcon = Icons.Filled.Home,
                    imeAction = ImeAction.Done,
                    onImeAction = if (flatName.isNotBlank()) onContinue else null
                )
                Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
                    Text(
                        "Flat type",
                        color = c.textPrimary,
                        style = HqType.labelLarge
                    )
                    types.forEach { (value, label) ->
                        val selected = flatType == value
                        FlatTypeOption(
                            label = label,
                            selected = selected,
                            onClick = { onFlatTypeChange(value) }
                        )
                    }
                }
            }
            // Sticky main action in the thumb zone. The name is required, so there is no Skip on this step.
            HqButton(
                text = "Continue",
                enabled = flatName.isNotBlank(),
                onClick = onContinue,
                modifier = Modifier.navigationBarsPadding().padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.md)
            )
        }
    }
}

@Composable
private fun FlatTypeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(HqRadius.md)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (selected) {
                    Modifier
                        .background(c.selectedBg)
                        .border(2.dp, c.actionPrimaryBg, shape)
                } else {
                    Modifier.border(1.dp, c.borderSubtle, shape)
                }
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = HqSpacing.xl, vertical = HqSpacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(HqIconSize.md)
                .clip(RoundedCornerShape(HqRadius.full))
                .then(
                    if (selected) {
                        Modifier.background(c.actionPrimaryBg)
                    } else {
                        Modifier.border(1.dp, c.borderSubtle, RoundedCornerShape(HqRadius.full))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(Icons.Filled.Check, null, tint = c.actionPrimaryFg, modifier = Modifier.size(12.dp))
            }
        }
        Spacer(Modifier.width(HqSpacing.xxl))
        Text(
            label,
            color = c.textPrimary,
            style = if (selected) HqType.titleMedium else HqType.bodyLarge
        )
    }
}

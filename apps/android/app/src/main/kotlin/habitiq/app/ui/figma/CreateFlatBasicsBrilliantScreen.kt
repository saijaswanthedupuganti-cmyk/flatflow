package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import habitiq.app.flats.FlatUiState
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqInlineError
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/** Figma 64:277 — Create a Flat: Basics (Brilliant & Story-driven), step 1 of 3. */
@Composable
fun CreateFlatBasicsBrilliantScreen(
    flatName: String,
    onFlatNameChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit,
    state: FlatUiState,
    onBack: () -> Unit,
    onCreateFlat: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalHqColors.current
    val loading = state is FlatUiState.Loading

    Box(modifier.fillMaxSize().background(c.canvas)) {
        Column(Modifier.fillMaxSize()) {
            FlatWizardBrilliantHeader(currentStep = 1, onBack = onBack, onSkip = onSkip)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = HqSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(HqSpacing.xxxl)
            ) {
                Spacer(Modifier.height(HqSpacing.sm))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(HqRadius.md))
                        .background(c.selectedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Home, null, tint = c.actionPrimaryBg, modifier = Modifier.size(HqIconSize.xl))
                }
                Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                    Text(
                        buildAnnotatedString {
                            append("Create your ")
                            withStyle(SpanStyle(color = c.textBrand, fontWeight = FontWeight.SemiBold)) {
                                append("shared home")
                            }
                        },
                        color = c.textPrimary,
                        style = HqType.headlineMedium
                    )
                    Text(
                        "Give your flat a name and tell us which city it's in.\nWe'll help you set up everything else later.",
                        color = c.textSecondary,
                        style = HqType.bodyMedium
                    )
                }
                HqTextField(value = flatName, onValueChange = onFlatNameChange, label = "Flat Name", placeholder = "Greenwood Heights")
                HqTextField(
                    value = city,
                    onValueChange = onCityChange,
                    label = "City",
                    placeholder = "Hyderabad",
                    helperText = "We'll use this to help you find the best local services."
                )
                HqCard {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(HqSpacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(HqIconSize.xl).clip(RoundedCornerShape(HqRadius.full)).background(c.actionPrimaryBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Lock, null, tint = c.actionPrimaryFg, modifier = Modifier.size(HqIconSize.sm))
                        }
                        Column {
                            Text("Your flat, your rules.", color = c.textPrimary, style = HqType.bodyLarge)
                            Text(
                                "Only people you invite can join your flat.",
                                color = c.textSecondary,
                                style = HqType.labelSmall
                            )
                        }
                    }
                }
                HqButton(
                    text = if (loading) "Creating…" else "Create Flat",
                    onClick = onCreateFlat,
                    enabled = flatName.isNotBlank(),
                    loading = loading,
                    leadingIcon = if (loading) null else Icons.Filled.Home,
                )
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    HqTextButton(text = "Skip for now", onClick = onSkip)
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Schedule, null, tint = c.textSecondary, modifier = Modifier.size(HqIconSize.xs))
                    Spacer(Modifier.width(HqSpacing.xs))
                    Text("Takes less than 10 seconds", color = c.textSecondary, style = HqType.labelSmall)
                }
                if (state is FlatUiState.Error) {
                    HqInlineError(state.message)
                }
                Spacer(Modifier.height(HqSpacing.xxl))
            }
        }
    }
}

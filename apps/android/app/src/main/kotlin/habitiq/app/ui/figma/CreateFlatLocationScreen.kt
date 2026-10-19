package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/** Create a flat, step 2 of 2: where the flat is. Optional; one Skip under the main button. */
@Composable
fun CreateFlatLocationScreen(
    area: String,
    onAreaChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit,
    pincode: String,
    onPincodeChange: (String) -> Unit,
    landmark: String,
    onLandmarkChange: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onSkip: (() -> Unit)? = null,
    showBottomSkip: Boolean = false,
    showHeaderSkip: Boolean = false,
    modifier: Modifier = Modifier
) {
    val c = LocalHqColors.current
    Column(modifier.fillMaxSize().background(c.canvas)) {
        FlatWizardLocationHeader(
            currentStep = 2,
            onBack = onBack,
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HqSpacing.xl)
                .padding(top = HqSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.xxxl)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                Text(
                    "Where is your flat?",
                    color = c.textPrimary,
                    style = HqType.headlineMedium
                )
                Text(
                    "Add the area and city. You can skip this and add it later in Flat settings.",
                    color = c.textSecondary,
                    style = HqType.bodyMedium
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xxl)) {
                MapLocationPicker(
                    onLocationPicked = { picked ->
                        if (picked.area.isNotBlank()) onAreaChange(picked.area)
                        if (picked.city.isNotBlank()) onCityChange(picked.city)
                    }
                )
                HqTextField(value = area, onValueChange = onAreaChange, label = "Area or locality", placeholder = "Madhapur", leadingIcon = Icons.Filled.LocationOn)
                HqTextField(value = city, onValueChange = onCityChange, label = "City", placeholder = "Hyderabad", leadingIcon = Icons.Filled.LocationCity)
                HqTextField(value = pincode, onValueChange = onPincodeChange, label = "Pincode", placeholder = "500081", leadingIcon = Icons.Filled.PinDrop,
                    keyboardType = KeyboardType.Number,
                    contentType = ContentType.PostalCode
                )
                HqTextField(
                    value = landmark,
                    onValueChange = onLandmarkChange,
                    label = "Nearby landmark (optional)",
                    placeholder = "e.g. DLF, Image Hospital",
                    leadingIcon = Icons.Filled.LocationOn,
                    imeAction = ImeAction.Done,
                    onImeAction = onContinue
                )
            }
            Spacer(Modifier.height(HqSpacing.xxl))
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(c.canvas)
                .border(1.dp, c.borderSubtle)
                .padding(horizontal = HqSpacing.xl)
                .padding(top = HqSpacing.md, bottom = HqSpacing.sm)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)
        ) {
            HqButton(text = "Create flat", onClick = onContinue)
            if (showBottomSkip && onSkip != null) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    HqTextButton(text = "Skip", onClick = onSkip)
                }
            }
        }
    }
}

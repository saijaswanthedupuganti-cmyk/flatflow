package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import habitiq.app.ui.theme.FigmaColors
import habitiq.app.ui.figma.MapLocationPicker

/** Figma 64:105 / 64:158 — Create a Flat: Location (step 2 of 5). */
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
    Column(modifier.fillMaxSize().background(FigmaColors.Background)) {
        FlatWizardLocationHeader(
            currentStep = 2,
            onBack = onBack,
            onSkip = onSkip,
            showHeaderSkip = showHeaderSkip
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 7.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    "Where is your flat located?",
                    color = FigmaColors.Ink,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 28.8.sp
                )
                Text(
                    "Add address to help others discover",
                    color = FigmaColors.InkSecondary,
                    fontSize = 15.sp,
                    lineHeight = 22.5.sp
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                MapLocationPicker(
                    onLocationPicked = { picked ->
                        if (picked.area.isNotBlank()) onAreaChange(picked.area)
                        if (picked.city.isNotBlank()) onCityChange(picked.city)
                    }
                )
                LocationField("Area / Locality", area, onAreaChange, "Madhapur")
                LocationField("City", city, onCityChange, "Hyderabad")
                LocationField("Pincode", pincode, onPincodeChange, "500081")
                LocationField(
                    label = buildAnnotatedString {
                        withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.SemiBold, color = FigmaColors.Ink)) {
                            append("Nearby Landmark ")
                        }
                        withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Normal, color = FigmaColors.InkSecondary)) {
                            append("(Optional)")
                        }
                    },
                    value = landmark,
                    onValueChange = onLandmarkChange,
                    placeholder = "Eg. DLF, Image Hospital"
                )
            }
            Spacer(Modifier.height(120.dp))
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(FigmaColors.Background)
                .border(1.dp, FigmaColors.SurfaceBorder)
                .padding(horizontal = 20.dp)
                .padding(top = 25.dp, bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FigmaColors.Primary)
                    .clickable(onClick = onContinue)
                    .padding(vertical = 17.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Continue",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.26.sp
                )
            }
            if (showBottomSkip && onSkip != null) {
                Text(
                    "Skip for now",
                    color = FigmaColors.InkSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.26.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSkip)
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LocationField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    LocationField(
        label = buildAnnotatedString { append(label) },
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder
    )
}

@Composable
private fun LocationField(
    label: androidx.compose.ui.text.AnnotatedString,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 13.sp, letterSpacing = 0.26.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = FigmaColors.InkSecondary, fontSize = 16.sp),
            cursorBrush = SolidColor(FigmaColors.Primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(FigmaColors.Surface)
                .border(1.dp, FigmaColors.SurfaceBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 17.dp, vertical = 14.dp),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = FigmaColors.InkSecondary, fontSize = 16.sp)
                    }
                    inner()
                }
            }
        )
    }
}

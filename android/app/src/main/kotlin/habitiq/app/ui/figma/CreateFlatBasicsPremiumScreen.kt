package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import habitiq.app.ui.theme.FigmaColors

/** Figma 64:349 — Create a Flat: Basics (Premium), step 1 of 5. */
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
    val types = listOf("apartment" to "Apartment", "house" to "Independent House")

    Box(modifier.fillMaxSize().background(FigmaColors.Background)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(390.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(FigmaColors.PrimaryDark.copy(alpha = 0.08f), Color.Transparent),
                        radius = 400f
                    )
                )
        )
        Column(Modifier.fillMaxSize()) {
            FlatWizardPremiumHeader(currentStep = 1, onBack = onBack, onSkip = onSkip)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Let's start with the basics",
                        color = FigmaColors.Ink,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.56).sp,
                        lineHeight = 33.6.sp
                    )
                    Text(
                        "Tell us about your flat",
                        color = FigmaColors.InkSecondary,
                        fontSize = 18.sp,
                        letterSpacing = (-0.4).sp,
                        lineHeight = 27.sp
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Flat Name",
                        color = FigmaColors.Ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.14.sp
                    )
                    FigmaTextField(
                        value = flatName,
                        onValueChange = onFlatNameChange,
                        placeholder = "e.g. The Penthouse, Cozy 3BHK"
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Flat Type",
                        color = FigmaColors.Ink,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.14.sp
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
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FigmaGradientButton(
                        text = "Continue",
                        enabled = flatName.isNotBlank(),
                        onClick = onContinue
                    )
                    Text(
                        "Skip for now",
                        color = FigmaColors.InkSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onSkip)
                            .padding(vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun FlatTypeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (selected) {
                    Modifier
                        .background(FigmaColors.Surface)
                        .border(2.dp, FigmaColors.PrimaryDark.copy(alpha = 0.2f), shape)
                } else {
                    Modifier.border(1.dp, FigmaColors.SurfaceBorder, shape)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 25.dp, vertical = if (selected) 26.dp else 27.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(9999.dp))
                .then(
                    if (selected) {
                        Modifier.background(FigmaColors.PrimaryDark)
                    } else {
                        Modifier.border(1.dp, Color(0xFFCBC4CF), RoundedCornerShape(9999.dp))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
        Spacer(Modifier.width(24.dp))
        Text(
            label,
            color = FigmaColors.Ink,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            letterSpacing = (-0.4).sp
        )
    }
}

@Composable
fun FigmaGradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9999.dp))
            .background(
                if (enabled) {
                    Brush.linearGradient(listOf(FigmaColors.PrimaryDark, FigmaColors.Primary))
                } else {
                    Brush.linearGradient(listOf(FigmaColors.PrimaryDark.copy(0.5f), FigmaColors.Primary.copy(0.5f)))
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.35.sp)
    }
}

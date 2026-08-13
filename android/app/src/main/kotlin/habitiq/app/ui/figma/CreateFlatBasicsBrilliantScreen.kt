package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.flats.FlatUiState
import androidx.compose.ui.text.style.TextAlign
import habitiq.app.ui.theme.FigmaColors

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
    val loading = state is FlatUiState.Loading

    Box(modifier.fillMaxSize().background(FigmaColors.Background)) {
        Box(
            Modifier
                .size(256.dp)
                .offset(x = 200.dp, y = (-128).dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(FigmaColors.PrimaryDark.copy(alpha = 0.2f))
        )
        Column(Modifier.fillMaxSize()) {
            FlatWizardBrilliantHeader(currentStep = 1, onBack = onBack, onSkip = onSkip)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(FigmaColors.PrimaryLight.copy(0.6f), FigmaColors.PrimaryDark.copy(0.15f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Home, null, tint = FigmaColors.PrimaryDark, modifier = Modifier.size(48.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(
                        buildAnnotatedString {
                            append("Create your ")
                            withStyle(SpanStyle(color = FigmaColors.Primary, fontWeight = FontWeight.SemiBold)) {
                                append("shared home")
                            }
                            append(" 🏡")
                        },
                        color = FigmaColors.Ink,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 28.8.sp
                    )
                    Text(
                        "Give your flat a name and tell us which city it's in.\nWe'll help you set up everything else later.",
                        color = FigmaColors.InkSecondary,
                        fontSize = 15.sp,
                        lineHeight = 22.5.sp
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Flat Name", color = FigmaColors.Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
                    FigmaTextField(value = flatName, onValueChange = onFlatNameChange, placeholder = "Greenwood Heights", fontSize = 15)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("City", color = FigmaColors.Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
                    Text(
                        "We'll use this to help you find the best local services.",
                        color = FigmaColors.InkSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    FigmaTextField(value = city, onValueChange = onCityChange, placeholder = "Hyderabad", fontSize = 15)
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FigmaColors.SurfaceMuted)
                        .border(1.dp, FigmaColors.SurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(17.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(9999.dp)).background(FigmaColors.PrimaryDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Lock, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("Your flat, your rules.", color = FigmaColors.Ink, fontSize = 16.sp)
                        Text(
                            "Only people you invite can join your flat.",
                            color = FigmaColors.InkSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9999.dp))
                        .background(FigmaColors.PrimaryDark)
                        .clickable(enabled = !loading && flatName.isNotBlank(), onClick = onCreateFlat)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Home, null, tint = FigmaColors.PrimaryMuted, modifier = Modifier.size(22.dp))
                        Text(
                            if (loading) "Creating…" else "Create Flat",
                            color = FigmaColors.PrimaryMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.26.sp
                        )
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = FigmaColors.PrimaryMuted, modifier = Modifier.size(16.dp))
                    }
                }
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
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Schedule, null, tint = FigmaColors.InkSecondary, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Takes less than 10 seconds", color = FigmaColors.InkSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                if (state is FlatUiState.Error) {
                    Text(state.message, color = FigmaColors.Error, fontSize = 13.sp)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

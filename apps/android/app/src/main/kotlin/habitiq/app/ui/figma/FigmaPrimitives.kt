package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.ui.theme.FigmaColors

@Composable
fun FigmaScreenBackground(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxSize().background(FigmaColors.Background).statusBarsPadding(),
        content = content
    )
}

@Composable
fun FigmaBackHeader(
    title: String,
    onBack: () -> Unit,
    subtitle: String? = null,
    centerTitle: Boolean = false
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = FigmaColors.Ink, modifier = Modifier.size(20.dp))
            }
            if (!centerTitle) {
                Spacer(Modifier.width(8.dp))
                Text(title, color = FigmaColors.Ink, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        if (centerTitle) {
            Text(
                title,
                color = FigmaColors.Ink,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
        subtitle?.let {
            Text(
                it,
                color = FigmaColors.InkSecondary,
                fontSize = 15.sp,
                lineHeight = 22.5.sp,
                textAlign = if (centerTitle) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun FigmaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 15
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = FigmaColors.Ink, fontSize = fontSize.sp),
        cursorBrush = SolidColor(FigmaColors.Primary),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FigmaColors.Surface)
            .border(1.dp, FigmaColors.SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 17.dp, vertical = 18.dp),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(placeholder, color = FigmaColors.Placeholder, fontSize = fontSize.sp)
                }
                inner()
            }
        }
    )
}

@Composable
fun FigmaChevronDown(modifier: Modifier = Modifier) {
    Icon(Icons.Filled.KeyboardArrowDown, null, tint = FigmaColors.InkSecondary, modifier = modifier.size(20.dp))
}

@Composable
fun MemberInitialAvatar(
    name: String,
    modifier: Modifier = Modifier,
    borderColor: Color = FigmaColors.SurfaceBorder,
    backgroundColor: Color = FigmaColors.PrimaryLight,
    textColor: Color = FigmaColors.Primary,
    selected: Boolean = false
) {
    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
    val border = if (selected) 2.dp else 1.dp
    val borderCol = if (selected) FigmaColors.Primary else borderColor
    Box(
        modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(border, borderCol, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(initials.ifEmpty { "?" }, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        if (selected) {
            Box(
                Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp)
                    .size(16.dp).clip(CircleShape).background(FigmaColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(10.dp))
            }
        }
    }
}

@Composable
fun FigmaPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) FigmaColors.Primary else FigmaColors.Primary.copy(0.5f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium)
    }
}

fun avatarPalette(index: Int): Triple<Color, Color, Color> {
    val palettes = listOf(
        Triple(FigmaColors.SuccessBg, FigmaColors.SuccessText, FigmaColors.SuccessText),
        Triple(FigmaColors.WarningBg, Color(0xFFE65100), Color(0xFFFF9800)),
        Triple(Color(0xFFF3E5F5), Color(0xFF7B1FA2), Color(0xFF9C27B0)),
        Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), Color(0xFF2196F3)),
        Triple(FigmaColors.PrimaryLight, FigmaColors.Primary, FigmaColors.Primary)
    )
    return palettes[index % palettes.size]
}

fun memberInitials(name: String): String =
    name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")

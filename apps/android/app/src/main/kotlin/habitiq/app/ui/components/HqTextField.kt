package habitiq.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * The one reusable text input (design doc section 21). Always shows a real label above the
 * field -- never relies on placeholder text alone as the label (section 21 "Do not rely only on
 * placeholder text"). Supports focus/error/disabled states and optional helper text.
 */
@Composable
fun HqTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    leadingIcon: ImageVector? = null,
    leadingIconDescription: String? = null,
) {
    val c = LocalHqColors.current
    val isError = errorText != null

    Column(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, style = HqType.labelLarge) },
            placeholder = placeholder?.let { { Text(it, style = HqType.bodyLarge, color = c.textTertiary) } },
            leadingIcon = leadingIcon?.let { icon ->
                { Icon(icon, contentDescription = leadingIconDescription, tint = c.textSecondary) }
            },
            enabled = enabled,
            isError = isError,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else minLines,
            textStyle = HqType.bodyLarge,
            shape = RoundedCornerShape(HqRadius.md),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.borderFocus,
                unfocusedBorderColor = c.borderDefault,
                disabledBorderColor = c.borderDisabled,
                errorBorderColor = c.error,
                focusedTextColor = c.textPrimary,
                unfocusedTextColor = c.textPrimary,
                cursorColor = c.brandPrimary,
                focusedLabelColor = c.brandPrimary,
                unfocusedLabelColor = c.textSecondary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        val caption = errorText ?: helperText
        if (caption != null) {
            Text(
                caption,
                style = HqType.bodySmall,
                color = if (isError) c.error else c.textTertiary,
                modifier = Modifier.fillMaxWidth().padding(top = HqSpacing.xs, start = HqSpacing.xs),
            )
        }
    }
}

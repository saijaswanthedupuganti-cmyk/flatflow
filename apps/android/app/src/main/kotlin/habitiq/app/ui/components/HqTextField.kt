package habitiq.app.ui.components

import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.autofill.ContentType
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
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
import habitiq.app.ui.theme.HqSize
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
    /** Keyboard action key. Use [ImeAction.Done] on the last field of a form. Multi-line fields always use Default (new line). */
    imeAction: ImeAction = ImeAction.Next,
    /** Overrides what the action key does, e.g. submit the form on Done. Next and Done keep their standard behaviour otherwise. */
    onImeAction: (() -> Unit)? = null,
    /** Autofill / password-manager hint, e.g. [ContentType.EmailAddress]. */
    contentType: ContentType? = null,
) {
    val c = LocalHqColors.current
    val isError = errorText != null
    var passwordVisible by remember { mutableStateOf(false) }
    // Free text starts sentences with a capital; credentials, numbers and codes never autocapitalise or autocorrect.
    val freeText = !isPassword && keyboardType == KeyboardType.Text

    Column(modifier.fillMaxWidth()) {
        // Figma `.field`: static label above the control (11sp semibold), not a floating label.
        Text(
            label,
            style = HqType.labelMedium,
            color = if (isError) c.statusDangerFg else if (enabled) c.textPrimary else c.disabledFg,
            modifier = Modifier.padding(bottom = 7.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder?.let { { Text(it, style = HqType.bodyLarge, color = c.textMuted) } },
            leadingIcon = leadingIcon?.let { icon ->
                { Icon(icon, contentDescription = leadingIconDescription, tint = c.iconDefault) }
            },
            trailingIcon = if (isPassword) {
                {
                    // Labelled, 48dp reveal control (design doc 10.3).
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = c.iconDefault,
                        )
                    }
                }
            } else null,
            enabled = enabled,
            isError = isError,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else minLines,
            textStyle = HqType.bodyLarge,
            shape = RoundedCornerShape(HqRadius.field),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                capitalization = if (freeText) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
                autoCorrectEnabled = freeText,
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                imeAction = if (singleLine) imeAction else ImeAction.Default,
            ),
            keyboardActions = if (onImeAction != null) {
                KeyboardActions(onNext = { onImeAction() }, onDone = { onImeAction() }, onGo = { onImeAction() }, onSend = { onImeAction() })
            } else KeyboardActions.Default,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = c.surfaceBase,
                unfocusedContainerColor = c.surfaceBase,
                errorContainerColor = c.surfaceBase,
                disabledContainerColor = c.disabledBg,
                focusedBorderColor = c.focus,
                unfocusedBorderColor = c.borderSubtle,
                disabledBorderColor = c.disabledBorder,
                errorBorderColor = c.statusDangerFg,
                focusedTextColor = c.textPrimary,
                unfocusedTextColor = c.textPrimary,
                errorTextColor = c.textPrimary,
                disabledTextColor = c.disabledFg,
                cursorColor = c.focus,
                errorCursorColor = c.statusDangerFg,
                focusedPlaceholderColor = c.textMuted,
                unfocusedPlaceholderColor = c.textMuted,
                disabledPlaceholderColor = c.disabledFg,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 51.dp)
                .then(if (contentType != null) Modifier.semantics { this.contentType = contentType } else Modifier),
        )
        val caption = errorText ?: helperText
        if (caption != null) {
            Text(
                caption,
                style = HqType.bodyMedium,
                color = if (isError) c.statusDangerFg else c.textSecondary,
                modifier = Modifier.fillMaxWidth().padding(top = HqSpacing.xs, start = HqSpacing.xs),
            )
        }
    }
}

package habitiq.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqMotion
import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.hqColorSpec

/**
 * The one text input (design doc section 21).
 *
 * - A real label sits above the field (never a floating or placeholder-only label).
 * - 56dp minimum, 16dp radius (same as buttons), visible 1dp control border (3:1) at rest.
 * - Focus: the border animates to 2dp in the focus colour on the fast colour tween.
 * - Error: red border + an inline row with an error icon and the message under the field; the
 *   message is announced politely and attached to the field for TalkBack.
 * - [prefix] / [suffix] for units (use [HqAmountField] for money: "₹" prefix, decimal keypad).
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
    /** Overrides the default capitalisation, e.g. Words for names, Characters for codes. */
    capitalization: KeyboardCapitalization? = null,
    /** Fixed text before the value, e.g. "₹". Read by TalkBack as part of the label. */
    prefix: String? = null,
    /** Fixed text after the value, e.g. "per month". */
    suffix: String? = null,
    /** Optional trailing control (a clear button, a picker icon). Must be its own 48dp target. */
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val c = LocalHqColors.current
    val isError = errorText != null
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    // Free text starts sentences with a capital; credentials, numbers and codes never autocapitalise or autocorrect.
    val freeText = !isPassword && keyboardType == KeyboardType.Text

    val borderColor by animateColorAsState(
        when {
            !enabled -> c.disabledBorder
            isError -> c.statusDangerFg
            focused -> c.focus
            else -> c.borderControl
        },
        hqColorSpec(), label = "fieldBorder",
    )
    val borderWidth by animateDpAsState(if ((focused || isError) && enabled) 2.dp else 1.dp, HqMotion.fastTween(), label = "fieldBorderW")
    val textColor = if (enabled) c.textPrimary else c.textPrimary.copy(alpha = 0.38f)
    val shape = HqShapes.field

    Column(modifier.fillMaxWidth()) {
        Text(
            label,
            style = HqType.labelMedium,
            color = if (isError) c.statusDangerFg else if (enabled) c.textPrimary else c.textPrimary.copy(alpha = 0.38f),
            // The field itself carries the label for TalkBack; reading this Text too would say it twice.
            modifier = Modifier.padding(bottom = HqSpacing.sm).clearAndSetSemantics {},
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else minLines,
            textStyle = HqType.bodyLarge.copy(color = textColor),
            cursorBrush = SolidColor(if (isError) c.statusDangerFg else c.focus),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                capitalization = capitalization ?: if (freeText) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
                autoCorrectEnabled = freeText,
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                imeAction = if (singleLine) imeAction else ImeAction.Default,
            ),
            keyboardActions = if (onImeAction != null) {
                KeyboardActions(onNext = { onImeAction() }, onDone = { onImeAction() }, onGo = { onImeAction() }, onSend = { onImeAction() }, onSearch = { onImeAction() })
            } else KeyboardActions(onDone = { focusManager.clearFocus() }, onSearch = { focusManager.clearFocus() }),
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxWidth()
                // Names the field for TalkBack (the visible label is a separate Text) and announces the error.
                .semantics {
                    contentDescription = if (prefix != null) "$label, $prefix" else label
                    if (errorText != null) error(errorText)
                }
                .then(if (contentType != null) Modifier.semantics { this.contentType = contentType } else Modifier),
            decorationBox = { inner ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = HqSize.input)
                        .clip(shape)
                        .background(if (enabled) c.surfaceBase else c.disabledBg)
                        .border(borderWidth, borderColor, shape)
                        .padding(start = if (leadingIcon != null) HqSpacing.md else HqSpacing.lg, end = if (isPassword || trailingContent != null) HqSpacing.xs else HqSpacing.lg),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            leadingIcon, contentDescription = leadingIconDescription, tint = c.iconDefault,
                            modifier = Modifier.padding(top = if (singleLine) 0.dp else HqSpacing.lg).size(HqIconSize.md),
                        )
                    }
                    if (prefix != null) {
                        Text(prefix, style = HqType.bodyLarge, color = c.textSecondary, modifier = Modifier.padding(top = if (singleLine) 0.dp else HqSpacing.lg).clearAndSetSemantics {})
                    }
                    Box(Modifier.weight(1f).padding(vertical = HqSpacing.lg)) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(placeholder, style = HqType.bodyLarge, color = c.textMuted, modifier = Modifier.clearAndSetSemantics {})
                        }
                        inner()
                    }
                    if (suffix != null) {
                        Text(suffix, style = HqType.bodyLarge, color = c.textSecondary, modifier = Modifier.padding(top = if (singleLine) 0.dp else HqSpacing.lg))
                    }
                    if (isPassword) {
                        // Labelled, 48dp reveal control (design doc 10.3).
                        HqIconButton(
                            icon = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            onClick = { passwordVisible = !passwordVisible },
                            enabled = enabled,
                        )
                    } else if (trailingContent != null) {
                        trailingContent()
                    }
                }
            },
        )
        if (errorText != null) {
            Row(
                Modifier.fillMaxWidth().padding(top = HqSpacing.sm).semantics { liveRegion = LiveRegionMode.Polite },
                horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs + 2.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(Icons.Outlined.ErrorOutline, null, tint = c.statusDangerFg, modifier = Modifier.padding(top = 2.dp).size(HqIconSize.xs))
                Text(errorText, style = HqType.bodyMedium, color = c.statusDangerFg)
            }
        } else if (helperText != null) {
            Text(
                helperText,
                style = HqType.bodyMedium,
                color = c.textSecondary,
                modifier = Modifier.fillMaxWidth().padding(top = HqSpacing.sm),
            )
        }
    }
}

/**
 * Money input: "₹" prefix, decimal keypad, and input limited to digits with at most two decimal
 * places (anything else is ignored as typed, never auto-corrected). [value] is the plain number
 * string ("1250.50"); format it for display elsewhere.
 */
@Composable
fun HqAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = "0",
    helperText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
    currencySymbol: String = "₹",
) {
    HqTextField(
        value = value,
        onValueChange = { raw -> if (isAmountInput(raw)) onValueChange(raw) },
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        helperText = helperText,
        errorText = errorText,
        enabled = enabled,
        keyboardType = KeyboardType.Decimal,
        imeAction = imeAction,
        onImeAction = onImeAction,
        prefix = currencySymbol,
    )
}

/** True for "", "12", "12.", "12.5", "12.50"; false for letters, a second dot or a third decimal. */
internal fun isAmountInput(raw: String): Boolean = raw.isEmpty() || Regex("^\\d{0,9}(\\.\\d{0,2})?$").matches(raw)

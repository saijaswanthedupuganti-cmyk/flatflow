package habitiq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Stored value for "available right away" on [HqDateField] when [immediateLabel] is set. */
const val DateImmediately = "Immediately"

/**
 * A tap-to-pick date field styled like [HqTextField]. Opens the Material calendar instead of asking
 * people to type a date. [value] is an ISO date (yyyy-MM-dd), blank, or [DateImmediately]; any other
 * legacy free text is shown as-is until a date is picked.
 *
 * Past days are disabled unless [allowPast]. When [immediateLabel] is set, the dialog offers it as a
 * shortcut that stores [DateImmediately].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HqDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Choose a date",
    helperText: String? = null,
    errorText: String? = null,
    allowPast: Boolean = false,
    immediateLabel: String? = null,
) {
    val c = LocalHqColors.current
    var open by remember { mutableStateOf(false) }
    val parsed = remember(value) { runCatching { LocalDate.parse(value.take(10)) }.getOrNull() }
    val display = when {
        parsed != null -> parsed.format(DisplayFormat)
        value.isNotBlank() -> value
        else -> null
    }
    val isError = errorText != null

    Column(modifier.fillMaxWidth()) {
        Text(
            label,
            style = HqType.labelMedium,
            color = if (isError) c.statusDangerFg else c.textPrimary,
            modifier = Modifier.padding(bottom = 7.dp),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 51.dp)
                .clip(RoundedCornerShape(HqRadius.field))
                .background(c.surfaceBase)
                .border(1.dp, if (isError) c.statusDangerFg else c.borderSubtle, RoundedCornerShape(HqRadius.field))
                .clickable(role = Role.Button) { open = true }
                .semantics { contentDescription = "$label: ${display ?: "not set"}. Opens a calendar." }
                .padding(start = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = c.iconDefault)
            Text(
                display ?: placeholder,
                style = HqType.bodyLarge,
                color = if (display != null) c.textPrimary else c.textMuted,
                modifier = Modifier.weight(1f).padding(vertical = 14.dp),
            )
            if (display != null) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear $label", tint = c.iconDefault)
                }
            }
        }
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

    if (open) {
        val todayUtc = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = parsed?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
            initialDisplayedMonthMillis = parsed?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli() ?: todayUtc,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = allowPast || utcTimeMillis >= todayUtc
                override fun isSelectableYear(year: Int) = allowPast || year >= LocalDate.now().year
            },
        )
        val colors = DatePickerDefaults.colors(
            containerColor = c.surfaceBase,
            titleContentColor = c.textSecondary,
            headlineContentColor = c.textPrimary,
            weekdayContentColor = c.textSecondary,
            navigationContentColor = c.textPrimary,
            dayContentColor = c.textPrimary,
            disabledDayContentColor = c.disabledFg,
            selectedDayContainerColor = c.actionPrimaryBg,
            selectedDayContentColor = c.actionPrimaryFg,
            todayContentColor = c.actionPrimaryBg,
            todayDateBorderColor = c.actionPrimaryBg,
            selectedYearContainerColor = c.actionPrimaryBg,
            selectedYearContentColor = c.actionPrimaryFg,
            currentYearContentColor = c.actionPrimaryBg,
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(
                    enabled = state.selectedDateMillis != null,
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            onValueChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString())
                        }
                        open = false
                    },
                ) { Text("Done", color = c.textBrand) }
            },
            dismissButton = {
                Row {
                    if (immediateLabel != null) {
                        TextButton(onClick = { onValueChange(DateImmediately); open = false }) {
                            Text(immediateLabel, color = c.textBrand)
                        }
                    }
                    TextButton(onClick = { open = false }) { Text("Cancel", color = c.textSecondary) }
                }
            },
            colors = colors,
        ) {
            DatePicker(state = state, colors = colors, showModeToggle = true)
        }
    }
}

private val DisplayFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())

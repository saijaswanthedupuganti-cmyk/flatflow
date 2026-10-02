package habitiq.app.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Consistently-styled SnackbarHost (design doc section 36). Use for a completed action, a saved
 * change, or a copied invite code -- not for critical decisions or information that must stay
 * visible (that's a dialog, see HqOverlays.kt).
 *
 * Usage: hold a `remember { SnackbarHostState() }` in the screen, call
 * `snackbarHostState.showSnackbar("Task completed")` on success, and place `HqSnackbarHost(state)`
 * in the Scaffold's `snackbarHost` slot.
 */
@Composable
fun HqSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    SnackbarHost(hostState, modifier = modifier) { data ->
        Snackbar(
            shape = RoundedCornerShape(HqRadius.control),
            // Inverse message surface, paired only with text.inverse (design doc section 5.2).
            containerColor = c.surfaceInverse,
            contentColor = c.textInverse,
        ) {
            Text(data.visuals.message, style = HqType.bodyMedium)
        }
    }
}

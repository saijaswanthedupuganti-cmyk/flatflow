package habitiq.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import habitiq.app.ui.theme.HabitiqTheme

@Composable
fun HabitiqApp() {
    HabitiqTheme {
        Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                Text("Habitiq — foundation scaffold")
            }
        }
    }
}

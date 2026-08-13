package habitiq.app.ui.figma

import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import habitiq.app.ui.theme.FigmaColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class PickedLocation(val lat: Double, val lng: Double, val area: String, val city: String)

@Composable
fun MapLocationPicker(
    initialLat: Double? = null,
    initialLng: Double? = null,
    onLocationPicked: (PickedLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val default = LatLng(initialLat ?: 17.4486, initialLng ?: 78.3908) // Hyderabad default
    var selected by remember { mutableStateOf(default) }
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selected, 14f)
    }

    Column(modifier) {
        Box(Modifier.fillMaxWidth().height(220.dp)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = camera,
                onMapClick = { latLng ->
                    selected = latLng
                }
            ) {
                Marker(state = MarkerState(position = selected), title = "Flat location")
            }
        }
        Text(
            "Tap map to set location · ${"%.4f".format(selected.latitude)}, ${"%.4f".format(selected.longitude)}",
            fontSize = 12.sp,
            color = FigmaColors.InkSecondary,
            modifier = Modifier.padding(8.dp)
        )
        FigmaPrimaryButton("Use this location", onClick = {
            val geocoder = Geocoder(context, Locale.getDefault())
            scope.launch {
                val addrs = withContext(Dispatchers.IO) {
                    runCatching { geocoder.getFromLocation(selected.latitude, selected.longitude, 1) }.getOrNull()
                }
                val first = addrs?.firstOrNull()
                val area = first?.subLocality ?: first?.thoroughfare ?: ""
                val city = first?.locality ?: first?.adminArea ?: ""
                onLocationPicked(PickedLocation(selected.latitude, selected.longitude, area, city))
            }
        })
    }
}

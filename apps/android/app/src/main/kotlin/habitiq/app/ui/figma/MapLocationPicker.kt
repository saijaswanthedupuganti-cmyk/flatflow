package habitiq.app.ui.figma

import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import java.util.Locale

data class PickedLocation(val lat: Double, val lng: Double, val area: String, val city: String)

@Composable
fun MapLocationPicker(
    initialLat: Double? = null,
    initialLng: Double? = null,
    onLocationPicked: (PickedLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalHqColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val default = LatLng(initialLat ?: 17.4486, initialLng ?: 78.3908) // Hyderabad default
    var selected by remember { mutableStateOf(default) }
    val markerState = remember(selected) { MarkerState(position = selected) }
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
                Marker(state = markerState, title = "Flat location")
            }
        }
        Text(
            "Tap map to set location · ${"%.4f".format(selected.latitude)}, ${"%.4f".format(selected.longitude)}",
            style = HqType.bodySmall,
            color = c.textSecondary,
            modifier = Modifier.padding(HqSpacing.sm)
        )
        HqButton(text = "Use this location", onClick = {
            val geocoder = Geocoder(context, Locale.getDefault())
            scope.launch {
                val addrs = reverseGeocode(geocoder, selected)
                val first = addrs?.firstOrNull()
                val area = first?.subLocality ?: first?.thoroughfare ?: ""
                val city = first?.locality ?: first?.adminArea ?: ""
                onLocationPicked(PickedLocation(selected.latitude, selected.longitude, area, city))
            }
        })
    }
}

private suspend fun reverseGeocode(geocoder: Geocoder, location: LatLng) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<android.location.Address>) {
                        if (continuation.isActive) continuation.resume(addresses)
                    }

                    override fun onError(errorMessage: String?) {
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            )
        }
    } else {
        withContext(Dispatchers.IO) {
            @Suppress("DEPRECATION")
            runCatching {
                geocoder.getFromLocation(location.latitude, location.longitude, 1)
            }.getOrNull()
        }
    }

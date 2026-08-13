package habitiq.app.flats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.analytics.AppAnalytics
import habitiq.app.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateFlatForm(
    val name: String = "",
    val flatType: String = "apartment",
    val city: String = "",
    val area: String = "",
    val pincode: String = "",
    val landmark: String = ""
)

class CreateFlatViewModel(
    private val authRepository: AuthRepository,
    private val flatsRepository: FlatsRepository,
    private val analytics: AppAnalytics = AppAnalytics()
) : ViewModel() {

    private val _state = MutableStateFlow<FlatUiState>(FlatUiState.Idle)
    val state: StateFlow<FlatUiState> = _state.asStateFlow()

    private val _createdFlatId = MutableStateFlow<String?>(null)
    val createdFlatId: StateFlow<String?> = _createdFlatId.asStateFlow()

    private val _form = MutableStateFlow(CreateFlatForm())
    val form: StateFlow<CreateFlatForm> = _form.asStateFlow()

    fun updateName(name: String) = _form.update { it.copy(name = name) }
    fun updateFlatType(flatType: String) = _form.update { it.copy(flatType = flatType) }
    fun updateCity(city: String) = _form.update { it.copy(city = city) }
    fun updateArea(area: String) = _form.update { it.copy(area = area) }
    fun updatePincode(pincode: String) = _form.update { it.copy(pincode = pincode) }
    fun updateLandmark(landmark: String) = _form.update { it.copy(landmark = landmark) }

    fun createFlat(flatName: String? = null) {
        val trimmedName = (flatName ?: _form.value.name).trim()
        if (trimmedName.isEmpty()) {
            _state.value = FlatUiState.Error("Give your flat a name first.")
            return
        }
        val user = authRepository.currentUser.value
        if (user == null) {
            _state.value = FlatUiState.Error("You must be signed in to create a flat.")
            return
        }
        _state.value = FlatUiState.Loading
        viewModelScope.launch {
            val nickname = user.displayName ?: user.email.orEmpty()
            val currentForm = _form.value.copy(name = trimmedName)
            val result = flatsRepository.createFlat(
                name = trimmedName,
                uid = user.uid,
                nickname = nickname,
                email = user.email.orEmpty(),
                flatType = currentForm.flatType,
                city = currentForm.city.takeIf { it.isNotBlank() },
                area = currentForm.area.takeIf { it.isNotBlank() },
                pincode = currentForm.pincode.takeIf { it.isNotBlank() },
                landmark = currentForm.landmark.takeIf { it.isNotBlank() }
            )
            result.onSuccess { flatId ->
                analytics.logFlatCreated()
                _createdFlatId.value = flatId
                _state.value = FlatUiState.Success
            }.onFailure { error ->
                _state.value = FlatUiState.Error(error.message ?: "Something went wrong. Please try again.")
            }
        }
    }
}

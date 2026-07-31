package habitiq.app.flats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.analytics.AppAnalytics
import habitiq.app.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateFlatViewModel(
    private val authRepository: AuthRepository,
    private val flatsRepository: FlatsRepository,
    private val analytics: AppAnalytics = AppAnalytics()
) : ViewModel() {

    private val _state = MutableStateFlow<FlatUiState>(FlatUiState.Idle)
    val state: StateFlow<FlatUiState> = _state

    private val _createdFlatId = MutableStateFlow<String?>(null)
    val createdFlatId: StateFlow<String?> = _createdFlatId

    fun createFlat(flatName: String) {
        val trimmedName = flatName.trim()
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
            val result = flatsRepository.createFlat(trimmedName, user.uid, nickname, user.email.orEmpty())
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

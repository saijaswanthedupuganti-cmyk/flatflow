package habitiq.app.flats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JoinFlatViewModel(
    private val authRepository: AuthRepository,
    private val flatsRepository: FlatsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<FlatUiState>(FlatUiState.Idle)
    val state: StateFlow<FlatUiState> = _state

    private val _joinedFlatId = MutableStateFlow<String?>(null)
    val joinedFlatId: StateFlow<String?> = _joinedFlatId

    fun joinFlat(code: String) {
        val normalizedCode = code.trim().uppercase()
        if (!isValidFlatIdFormat(normalizedCode)) {
            _state.value = FlatUiState.Error("That doesn't look like a valid invite code.")
            return
        }
        val user = authRepository.currentUser.value
        if (user == null) {
            _state.value = FlatUiState.Error("You must be signed in to join a flat.")
            return
        }
        _state.value = FlatUiState.Loading
        viewModelScope.launch {
            val nickname = user.displayName ?: user.email.orEmpty()
            val result = flatsRepository.joinFlat(normalizedCode, user.uid, nickname, user.email.orEmpty())
            result.onSuccess {
                _joinedFlatId.value = normalizedCode
                _state.value = FlatUiState.Success
            }.onFailure { error ->
                _state.value = FlatUiState.Error(error.message ?: "Something went wrong. Please try again.")
            }
        }
    }
}

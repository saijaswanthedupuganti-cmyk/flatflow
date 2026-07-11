package habitiq.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.data.UserProfile
import habitiq.app.data.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    fun signInWithEmail(email: String, password: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, password)
            onAuthResult(result)
        }
    }

    fun signInWithGoogleIdToken(idToken: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithGoogleIdToken(idToken)
            onAuthResult(result)
        }
    }

    private suspend fun onAuthResult(result: Result<Unit>) {
        result.onSuccess {
            val user = authRepository.currentUser.value
            val profileResult = if (user != null) {
                usersRepository.ensureUserDocument(
                    UserProfile(uid = user.uid, email = user.email.orEmpty(), displayName = user.displayName)
                )
            } else {
                Result.success(Unit)
            }
            profileResult.onSuccess {
                _state.value = AuthUiState.Success
            }.onFailure { error ->
                _state.value = AuthUiState.Error(error.message ?: "Something went wrong. Please try again.")
            }
        }.onFailure { error ->
            _state.value = AuthUiState.Error(error.message ?: "Something went wrong. Please try again.")
        }
    }
}

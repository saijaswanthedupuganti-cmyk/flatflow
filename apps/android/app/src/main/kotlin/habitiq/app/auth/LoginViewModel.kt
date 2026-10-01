package habitiq.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.analytics.AppAnalytics
import habitiq.app.analytics.METHOD_GOOGLE
import habitiq.app.analytics.METHOD_PASSWORD
import habitiq.app.data.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository,
    private val analytics: AppAnalytics = AppAnalytics()
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    fun signInWithEmail(email: String, password: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, password)
            onAuthResult(result, METHOD_PASSWORD)
        }
    }

    fun signInWithGoogleIdToken(idToken: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithGoogleIdToken(idToken)
            onAuthResult(result, METHOD_GOOGLE)
        }
    }

    fun onGoogleSignInFailed(message: String) {
        _state.value = AuthUiState.Error(message)
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _state.value = AuthUiState.Error("Enter your email first.")
            return
        }
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(email).fold(
                onSuccess = { _state.value = AuthUiState.Info("Password reset email sent. Check your inbox.") },
                onFailure = { _state.value = AuthUiState.Error(it.message ?: "Could not send reset email.") }
            )
        }
    }

    private suspend fun onAuthResult(result: Result<Unit>, method: String) {
        result.fold(
            onSuccess = {
                _state.value = completeAuthFlow(authRepository, usersRepository, analytics, method, isSignup = false)
            },
            onFailure = { error ->
                _state.value = AuthUiState.Error(error.message ?: "Something went wrong. Please try again.")
            }
        )
    }
}

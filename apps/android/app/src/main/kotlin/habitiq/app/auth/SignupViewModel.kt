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

class SignupViewModel(
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository,
    private val analytics: AppAnalytics = AppAnalytics()
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    fun signUpWithEmail(email: String, password: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.signUpWithEmail(email, password).fold(
                onSuccess = {
                    _state.value = completeAuthFlow(authRepository, usersRepository, analytics, METHOD_PASSWORD, isSignup = true)
                },
                onFailure = { error ->
                    _state.value = AuthUiState.Error(error.message ?: "Something went wrong. Please try again.")
                }
            )
        }
    }

    fun signInWithGoogleIdToken(idToken: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.signInWithGoogleIdToken(idToken).fold(
                onSuccess = {
                    _state.value = completeAuthFlow(authRepository, usersRepository, analytics, METHOD_GOOGLE, isSignup = true)
                },
                onFailure = { error ->
                    _state.value = AuthUiState.Error(error.message ?: "Something went wrong. Please try again.")
                }
            )
        }
    }

    fun onGoogleSignInFailed(message: String) {
        _state.value = AuthUiState.Error(message)
    }
}

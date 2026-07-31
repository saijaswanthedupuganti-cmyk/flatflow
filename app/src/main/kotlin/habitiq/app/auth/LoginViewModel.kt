package habitiq.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.analytics.AppAnalytics
import habitiq.app.analytics.METHOD_GOOGLE
import habitiq.app.analytics.METHOD_PASSWORD
import habitiq.app.data.UserProfile
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

    // Surfaces a failure from the Credential Manager step, before any Firebase call was made.
    fun onGoogleSignInFailed(message: String) {
        _state.value = AuthUiState.Error(message)
    }

    private suspend fun onAuthResult(result: Result<Unit>, method: String) {
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
                analytics.logLogin(method)
                _state.value = AuthUiState.Success
            }.onFailure {
                // UsersRepository surfaces the raw Firestore exception, so its message is not
                // safe to show the user.
                _state.value = AuthUiState.Error("Signed in, but we couldn't load your profile. Please try again.")
            }
        }.onFailure { error ->
            _state.value = AuthUiState.Error(error.message ?: "Something went wrong. Please try again.")
        }
    }
}

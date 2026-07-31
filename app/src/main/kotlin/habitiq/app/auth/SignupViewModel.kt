package habitiq.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.analytics.AppAnalytics
import habitiq.app.analytics.METHOD_PASSWORD
import habitiq.app.data.UserProfile
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
            val result = authRepository.signUpWithEmail(email, password)
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
                    analytics.logSignUp(METHOD_PASSWORD)
                    _state.value = AuthUiState.Success
                }.onFailure {
                    // UsersRepository surfaces the raw Firestore exception, so its message is not
                    // safe to show the user.
                    _state.value = AuthUiState.Error("Account created, but we couldn't set up your profile. Please try again.")
                }
            }.onFailure { error ->
                _state.value = AuthUiState.Error(error.message ?: "Something went wrong. Please try again.")
            }
        }
    }
}

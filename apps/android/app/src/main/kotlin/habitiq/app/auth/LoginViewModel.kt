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

    private val _reset = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    /** The forgot-password sheet's own state, kept apart from sign-in so the two never overwrite each other. */
    val reset: StateFlow<PasswordResetState> = _reset

    fun sendPasswordReset(email: String) {
        val address = email.trim()
        if (_reset.value is PasswordResetState.Sending) return
        if (!isPlausibleEmail(address)) {
            _reset.value = PasswordResetState.Error(
                if (address.isEmpty()) "Enter the email you signed up with." else "That email address doesn't look right. Check it and try again."
            )
            return
        }
        _reset.value = PasswordResetState.Sending
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(address).fold(
                onSuccess = { _reset.value = PasswordResetState.Sent(address, System.currentTimeMillis()) },
                onFailure = { _reset.value = PasswordResetState.Error(it.message ?: "Couldn't send the reset email right now. Please try again.") },
            )
        }
    }

    fun clearPasswordReset() { _reset.value = PasswordResetState.Idle }

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

sealed interface PasswordResetState {
    data object Idle : PasswordResetState
    data object Sending : PasswordResetState
    data class Sent(val email: String, val atMillis: Long) : PasswordResetState
    data class Error(val message: String) : PasswordResetState
}

/** Catches typos before Firebase does ("name@gmail", "name gmail.com"); Firebase still has the final say. */
fun isPlausibleEmail(email: String): Boolean =
    Regex("""^[^@\s]+@[^@\s]+\.[^@\s]{2,}$""").matches(email.trim())

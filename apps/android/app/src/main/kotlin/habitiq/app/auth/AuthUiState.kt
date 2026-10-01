package habitiq.app.auth

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Error(val message: String) : AuthUiState
    data class Info(val message: String) : AuthUiState
    /** [hasActiveFlat] true when user already belongs to a flat — skip intent chooser. */
    data class Success(val hasActiveFlat: Boolean) : AuthUiState
}

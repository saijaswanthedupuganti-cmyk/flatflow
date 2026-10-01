package habitiq.app.flats

sealed interface FlatUiState {
    data object Idle : FlatUiState
    data object Loading : FlatUiState
    data class Error(val message: String) : FlatUiState
    data object Success : FlatUiState
}

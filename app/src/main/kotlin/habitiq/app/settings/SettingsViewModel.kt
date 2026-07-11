package habitiq.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.auth.AuthRepository
import habitiq.app.data.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface DeleteAccountState {
    data object Idle : DeleteAccountState
    data object Deleting : DeleteAccountState
    data class Error(val message: String) : DeleteAccountState
    data object Deleted : DeleteAccountState
}

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _deleteState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteState: StateFlow<DeleteAccountState> = _deleteState

    fun deleteAccount() {
        val user = authRepository.currentUser.value
        if (user == null) {
            _deleteState.value = DeleteAccountState.Error("You must be signed in.")
            return
        }
        val uid = user.uid
        _deleteState.value = DeleteAccountState.Deleting
        viewModelScope.launch {
            authRepository.deleteAccount().fold(
                onSuccess = {
                    // Best-effort cleanup after the Auth account is gone -- a failure here
                    // shouldn't block the user from completing account deletion, since the
                    // Auth account (the part they actually asked to delete) already succeeded.
                    usersRepository.deleteUserData(uid)
                    _deleteState.value = DeleteAccountState.Deleted
                },
                onFailure = { error ->
                    _deleteState.value = DeleteAccountState.Error(
                        error.message ?: "Something went wrong. Please try again."
                    )
                }
            )
        }
    }
}

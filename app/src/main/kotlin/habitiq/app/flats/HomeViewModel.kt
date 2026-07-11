package habitiq.app.flats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.auth.AuthRepository
import habitiq.app.data.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface HomeFlatStatus {
    data object Loading : HomeFlatStatus
    data object NoFlat : HomeFlatStatus
    data class InFlat(val flatId: String) : HomeFlatStatus
}

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _flatStatus = MutableStateFlow<HomeFlatStatus>(HomeFlatStatus.Loading)
    val flatStatus: StateFlow<HomeFlatStatus> = _flatStatus

    init {
        checkFlatStatus()
    }

    fun checkFlatStatus() {
        val user = authRepository.currentUser.value
        if (user == null) {
            _flatStatus.value = HomeFlatStatus.NoFlat
            return
        }
        _flatStatus.value = HomeFlatStatus.Loading
        viewModelScope.launch {
            val activeFlatId = usersRepository.getActiveFlatId(user.uid)
            _flatStatus.value = if (activeFlatId != null) {
                HomeFlatStatus.InFlat(activeFlatId)
            } else {
                HomeFlatStatus.NoFlat
            }
        }
    }
}

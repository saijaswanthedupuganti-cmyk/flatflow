package habitiq.app.flats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface FlatHomeStatus {
    data object Loading : FlatHomeStatus
    data class Ready(val flat: FlatInfo) : FlatHomeStatus
    data class Error(val message: String) : FlatHomeStatus
}

class FlatHomeViewModel(
    private val flatId: String,
    private val flatsRepository: FlatsRepository,
    private val membersRepository: MembersRepository
) : ViewModel() {

    private val _status = MutableStateFlow<FlatHomeStatus>(FlatHomeStatus.Loading)
    val status: StateFlow<FlatHomeStatus> = _status

    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members

    private val _membersError = MutableStateFlow<String?>(null)
    val membersError: StateFlow<String?> = _membersError

    private var membersJob: Job? = null

    init {
        retry()
    }

    fun retry() {
        loadFlat()
        observeMembers()
    }

    private fun loadFlat() {
        _status.value = FlatHomeStatus.Loading
        viewModelScope.launch {
            flatsRepository.getFlat(flatId).fold(
                onSuccess = { _status.value = FlatHomeStatus.Ready(it) },
                onFailure = { error ->
                    _status.value = FlatHomeStatus.Error(
                        error.message ?: "Couldn't load your flat. Please try again."
                    )
                }
            )
        }
    }

    private fun observeMembers() {
        membersJob?.cancel()
        membersJob = viewModelScope.launch {
            membersRepository.observeMembers(flatId)
                // observeMembers closes the callbackFlow with the Firestore error, which makes
                // collect throw. Uncaught, that failure propagates out of viewModelScope.
                .catch { _membersError.value = "Couldn't load your roommates. Please try again." }
                .collect { members ->
                    _membersError.value = null
                    _members.value = members
                }
        }
    }
}

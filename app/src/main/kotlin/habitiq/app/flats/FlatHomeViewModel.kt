package habitiq.app.flats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FlatHomeViewModel(
    private val flatId: String,
    private val flatsRepository: FlatsRepository,
    private val membersRepository: MembersRepository
) : ViewModel() {

    private val _flat = MutableStateFlow<FlatInfo?>(null)
    val flat: StateFlow<FlatInfo?> = _flat

    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members

    init {
        viewModelScope.launch {
            flatsRepository.getFlat(flatId).onSuccess { _flat.value = it }
        }
        viewModelScope.launch {
            membersRepository.observeMembers(flatId).collect { _members.value = it }
        }
    }
}

package habitiq.app.flats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.analytics.AppAnalytics
import habitiq.app.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val INVALID_CODE = "That doesn't look like an invite code. It's 6 letters and numbers, like K7QM2P."

class JoinFlatViewModel(
    private val authRepository: AuthRepository,
    private val flatsRepository: FlatsRepository,
    private val analytics: AppAnalytics = AppAnalytics()
) : ViewModel() {

    private val _state = MutableStateFlow<FlatUiState>(FlatUiState.Idle)
    val state: StateFlow<FlatUiState> = _state

    private val _preview = MutableStateFlow<FlatInfo?>(null)
    val preview: StateFlow<FlatInfo?> = _preview

    private val _joinedFlatId = MutableStateFlow<String?>(null)
    val joinedFlatId: StateFlow<String?> = _joinedFlatId

    private val _pendingApproval = MutableStateFlow(false)
    val pendingApproval: StateFlow<Boolean> = _pendingApproval

    /**
     * The flat behind what was typed, and the code to join with. An old `FLAT-` code is the flat id, which
     * anyone who saw a Discover listing knows, so it only ever sends a request (no code to join with).
     */
    private suspend fun resolve(input: InviteInput): Result<Pair<String, String?>> = when (input) {
        is InviteInput.Code -> flatsRepository.resolveInviteCode(input.code).map { it to input.code }
        is InviteInput.LegacyFlatId -> Result.success(input.flatId to null)
        InviteInput.Invalid -> Result.failure(IllegalStateException(INVALID_CODE))
    }

    fun lookupFlat(code: String) {
        if (_state.value is FlatUiState.Loading) return
        val input = parseInviteInput(code)
        if (input == InviteInput.Invalid) {
            _preview.value = null
            _state.value = FlatUiState.Error(INVALID_CODE)
            return
        }
        _state.value = FlatUiState.Loading
        _preview.value = null
        viewModelScope.launch {
            resolve(input).mapCatching { (flatId, inviteCode) ->
                val info = flatsRepository.getFlat(flatId).getOrThrow()
                if (inviteCode == null) info.copy(joinMode = "approval") else info
            }.fold(
                onSuccess = { info ->
                    _preview.value = info
                    _state.value = FlatUiState.Idle
                },
                onFailure = { error ->
                    _preview.value = null
                    _state.value = FlatUiState.Error(error.message ?: "Couldn't check the code. Try again.")
                }
            )
        }
    }

    fun clearPreview() {
        _preview.value = null
        _state.value = FlatUiState.Idle
        _pendingApproval.value = false
    }

    fun joinFlat(code: String) {
        val input = parseInviteInput(code)
        if (input == InviteInput.Invalid) {
            _state.value = FlatUiState.Error(INVALID_CODE)
            return
        }
        if (_state.value is FlatUiState.Loading) return
        val user = authRepository.currentUser.value
        if (user == null) {
            _state.value = FlatUiState.Error("You must be signed in to join a flat.")
            return
        }
        _state.value = FlatUiState.Loading
        viewModelScope.launch {
            val (flatId, inviteCode) = resolve(input).getOrElse {
                _state.value = FlatUiState.Error(it.message ?: INVALID_CODE)
                return@launch
            }
            val nickname = user.displayName ?: user.email.orEmpty()
            val joinMode = if (inviteCode == null) "approval" else flatsRepository.getJoinMode(flatId).getOrElse { "auto" }
            if (joinMode == "approval") {
                flatsRepository.requestToJoin(flatId, user.uid, nickname, user.email.orEmpty()).fold(
                    onSuccess = {
                        _pendingApproval.value = true
                        _state.value = FlatUiState.Success
                    },
                    onFailure = { _state.value = FlatUiState.Error(it.message ?: "Request failed.") }
                )
            } else {
                val result = flatsRepository.joinFlat(flatId, user.uid, nickname, user.email.orEmpty(), inviteCode = inviteCode)
                result.onSuccess {
                    analytics.logFlatJoined()
                    _joinedFlatId.value = flatId
                    _state.value = FlatUiState.Success
                }.onFailure { error ->
                    _state.value = FlatUiState.Error(error.message ?: "Something went wrong. Please try again.")
                }
            }
        }
    }
}

package habitiq.app.flat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import habitiq.app.auth.AuthRepository
import habitiq.app.data.*
import habitiq.app.flats.FlatInfo
import habitiq.app.flats.FlatsRepository
import habitiq.app.flats.Member
import habitiq.app.flats.MembersRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

class FlatViewModel(
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository,
    private val flatsRepository: FlatsRepository,
    private val membersRepository: MembersRepository,
    private val tasksRepository: TasksRepository,
    private val expensesRepository: ExpensesRepository,
    private val activityRepository: ActivityRepository,
    private val swapRepository: SwapRepository,
    private val discoveryRepository: DiscoveryRepository
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser

    private val _flatId = MutableStateFlow<String?>(null)
    val flatId: StateFlow<String?> = _flatId.asStateFlow()

    private val _flatInfo = MutableStateFlow<FlatInfo?>(null)
    val flatInfo: StateFlow<FlatInfo?> = _flatInfo.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfileData?>(null)
    val userProfile: StateFlow<UserProfileData?> = _userProfile.asStateFlow()

    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members.asStateFlow()

    private val _tasks = MutableStateFlow<List<FlatTask>>(emptyList())
    val tasks: StateFlow<List<FlatTask>> = _tasks.asStateFlow()

    private val _expenses = MutableStateFlow<List<FlatExpense>>(emptyList())
    val expenses: StateFlow<List<FlatExpense>> = _expenses.asStateFlow()

    private val _swapRequests = MutableStateFlow<List<FlatSwapRequest>>(emptyList())
    val swapRequests: StateFlow<List<FlatSwapRequest>> = _swapRequests.asStateFlow()

    private val _vacancies = MutableStateFlow<List<VacancyListing>>(emptyList())
    val vacancies: StateFlow<List<VacancyListing>> = _vacancies.asStateFlow()

    private val _activity = MutableStateFlow<List<FlatActivity>>(emptyList())
    val activity: StateFlow<List<FlatActivity>> = _activity.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val showAddTaskTrigger = MutableStateFlow(false)
    val showAddExpenseTrigger = MutableStateFlow(false)

    val isAdmin: StateFlow<Boolean> = combine(currentUser, flatInfo) { user, flat ->
        user != null && flat != null && flat.adminUid == user.uid
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentMember: StateFlow<Member?> = combine(currentUser, members) { user, memberList ->
        user?.let { u -> memberList.find { it.uid == u.uid } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var flatObserveJob: Job? = null
    private var discoveryJob: Job? = null

    init {
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user == null) {
                    _flatId.value = null
                    _loading.value = false
                    return@collect
                }
                loadSession(user)
            }
        }
        discoveryJob = viewModelScope.launch {
            discoveryRepository.observeActiveVacancies().catch { emit(emptyList()) }
                .collect { _vacancies.value = it }
        }
    }

    private suspend fun loadSession(user: FirebaseUser) {
        _loading.value = true
        usersRepository.getUserProfile(user.uid).fold(
            onSuccess = { profile ->
                _userProfile.value = profile
                val activeId = profile.activeFlatId
                if (activeId != null) {
                    bindFlat(activeId)
                } else {
                    _flatId.value = null
                    _loading.value = false
                }
            },
            onFailure = {
                _error.value = it.message
                _loading.value = false
            }
        )
    }

    fun refresh() {
        val user = currentUser.value ?: return
        viewModelScope.launch { loadSession(user) }
    }

    private fun bindFlat(flatId: String) {
        _flatId.value = flatId
        flatObserveJob?.cancel()
        flatObserveJob = viewModelScope.launch {
            flatsRepository.getFlat(flatId).onSuccess { _flatInfo.value = it }
            combine(
                membersRepository.observeMembers(flatId).catch { emit(emptyList()) },
                tasksRepository.observeTasks(flatId).catch { emit(emptyList()) },
                expensesRepository.observeExpenses(flatId).catch { emit(emptyList()) },
                swapRepository.observeSwapRequests(flatId).catch { emit(emptyList()) },
                activityRepository.observeRecentActivity(flatId, 20).catch { emit(emptyList()) }
            ) { memberList, taskList, expenseList, swaps, act ->
                _members.value = memberList
                _tasks.value = taskList
                _expenses.value = expenseList
                _swapRequests.value = swaps
                _activity.value = act
                _loading.value = false
            }.collect()
        }
    }

    fun completeTask(task: FlatTask) {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        viewModelScope.launch {
            tasksRepository.completeTask(flat, task, _members.value, uid)
        }
    }

    fun createTask(name: String, frequency: String, priority: String, participantUids: List<String>) {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        val queue = if (participantUids.isNotEmpty()) participantUids else listOf(uid)
        val dueDate = Instant.now().plus(7, ChronoUnit.DAYS).toString()
        viewModelScope.launch {
            tasksRepository.createTask(
                flatId = flat,
                name = name,
                type = "rotating_duty",
                priority = priority,
                frequency = frequency,
                queueOrder = queue,
                dueDate = dueDate,
                adminId = uid
            )
        }
    }

    fun addExpense(description: String, amount: Double, splitAmong: List<String>) {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        val participants = if (splitAmong.isNotEmpty()) splitAmong else _members.value.map { it.uid }
        val perPerson = if (participants.isNotEmpty()) amount / participants.size else amount
        val splits = participants.associateWith { perPerson }
        val expense = FlatExpense(
            id = UUID.randomUUID().toString(),
            description = description.trim(),
            amount = amount,
            currency = "INR",
            paidBy = uid,
            splitAmong = participants,
            splitType = "equal",
            splits = splits,
            category = "other",
            date = LocalDate.now().toString(),
            createdBy = uid
        )
        viewModelScope.launch {
            expensesRepository.addExpense(flat, expense)
        }
    }

    fun createSwapRequest(taskId: String, toUserId: String) {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        viewModelScope.launch {
            swapRepository.createSwapRequest(flat, taskId, uid, toUserId)
        }
    }

    fun respondToSwap(requestId: String, accept: Boolean) {
        val flat = _flatId.value ?: return
        viewModelScope.launch {
            swapRepository.respondToSwap(flat, requestId, accept)
        }
    }

    fun updateDisplayName(name: String) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            usersRepository.updateProfile(uid, name).onSuccess {
                _userProfile.value = _userProfile.value?.copy(displayName = name.trim())
            }
        }
    }

    fun toggleOutOfStation(isOos: Boolean) {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        val status = if (isOos) "out_of_station" else "available"
        viewModelScope.launch {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("flats").document(flat).collection("members").document(uid)
                .update("status", status)
        }
    }

    fun onFlatCreated(flatId: String) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            usersRepository.setActiveFlat(uid, flatId)
            bindFlat(flatId)
        }
    }

    fun onFlatJoined(flatId: String) {
        onFlatCreated(flatId)
    }
}

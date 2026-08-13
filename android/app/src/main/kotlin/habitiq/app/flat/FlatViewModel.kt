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
import habitiq.app.lib.SuggestedSettlement
import habitiq.app.lib.computeMonthNetBalances
import habitiq.app.lib.currentMonthKey
import habitiq.app.services.FcmNotificationHelper
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    private val billsRepository: BillsRepository,
    private val activityRepository: ActivityRepository,
    private val swapRepository: SwapRepository,
    private val discoveryRepository: DiscoveryRepository,
    private val messagingRepository: MessagingRepository
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

    private val _recurringBills = MutableStateFlow<List<RecurringBill>>(emptyList())
    val recurringBills: StateFlow<List<RecurringBill>> = _recurringBills.asStateFlow()

    private val _billInstances = MutableStateFlow<List<BillInstance>>(emptyList())
    val billInstances: StateFlow<List<BillInstance>> = _billInstances.asStateFlow()

    private val _settlements = MutableStateFlow<List<Settlement>>(emptyList())
    val settlements: StateFlow<List<Settlement>> = _settlements.asStateFlow()

    private val _swapRequests = MutableStateFlow<List<FlatSwapRequest>>(emptyList())
    val swapRequests: StateFlow<List<FlatSwapRequest>> = _swapRequests.asStateFlow()

    private val _vacancies = MutableStateFlow<List<VacancyListing>>(emptyList())
    val vacancies: StateFlow<List<VacancyListing>> = _vacancies.asStateFlow()

    private val _seekerProfiles = MutableStateFlow<List<SeekerProfile>>(emptyList())
    val seekerProfiles: StateFlow<List<SeekerProfile>> = _seekerProfiles.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _activity = MutableStateFlow<List<FlatActivity>>(emptyList())
    val activity: StateFlow<List<FlatActivity>> = _activity.asStateFlow()

    private val _joinRequests = MutableStateFlow<List<JoinRequest>>(emptyList())
    val joinRequests: StateFlow<List<JoinRequest>> = _joinRequests.asStateFlow()

    private val _monthCycles = MutableStateFlow<List<MonthCycle>>(emptyList())
    val monthCycles: StateFlow<List<MonthCycle>> = _monthCycles.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val showAddTaskTrigger = MutableStateFlow(false)
    val showAddExpenseTrigger = MutableStateFlow(false)
    val showBillsTrigger = MutableStateFlow(false)

    val isAdmin: StateFlow<Boolean> = combine(currentUser, flatInfo) { user, flat ->
        user != null && flat != null && flat.adminUid == user.uid
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentMember: StateFlow<Member?> = combine(currentUser, members) { user, memberList ->
        user?.let { u -> memberList.find { it.uid == u.uid } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var flatObserveJob: Job? = null
    private var discoveryJob: Job? = null
    private var seekerJob: Job? = null
    private var chatJob: Job? = null

    init {
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user == null) {
                    _flatId.value = null
                    _loading.value = false
                    return@collect
                }
                loadSession(user)
                bindMessaging(user.uid)
            }
        }
        discoveryJob = viewModelScope.launch {
            discoveryRepository.observeActiveVacancies().catch { emit(emptyList()) }
                .collect { _vacancies.value = it }
        }
        seekerJob = viewModelScope.launch {
            messagingRepository.observeActiveSeekers().catch { emit(emptyList()) }
                .collect { _seekerProfiles.value = it }
        }
    }

    private fun bindMessaging(uid: String) {
        chatJob?.cancel()
        chatJob = viewModelScope.launch {
            messagingRepository.observeMessagesForUser(uid).catch { emit(emptyList()) }
                .collect { _chatMessages.value = it }
        }
    }

    fun observeConversation(partnerId: String): Flow<List<ChatMessage>> {
        val uid = currentUser.value?.uid ?: return flowOf(emptyList())
        return messagingRepository.observeConversation(uid, partnerId).catch { emit(emptyList()) }
    }

    private suspend fun loadSession(user: FirebaseUser) {
        _loading.value = true
        usersRepository.getUserProfile(user.uid).fold(
            onSuccess = { profile ->
                _userProfile.value = profile
                val activeId = profile.activeFlatId
                if (activeId != null) {
                    bindFlat(activeId)
                    FcmNotificationHelper.subscribeToFlatTopic(activeId)
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
                billsRepository.observeRecurringBills(flatId).catch { emit(emptyList()) },
                billsRepository.observeBillInstances(flatId).catch { emit(emptyList()) },
                billsRepository.observeSettlements(flatId).catch { emit(emptyList()) },
                swapRepository.observeSwapRequests(flatId).catch { emit(emptyList()) },
                activityRepository.observeRecentActivity(flatId, 20).catch { emit(emptyList()) },
                membersRepository.observeJoinRequests(flatId).catch { emit(emptyList()) },
                billsRepository.observeMonthCycles(flatId).catch { emit(emptyList()) }
            ) { values ->
                _members.value = values[0] as List<Member>
                _tasks.value = values[1] as List<FlatTask>
                _expenses.value = values[2] as List<FlatExpense>
                _recurringBills.value = values[3] as List<RecurringBill>
                _billInstances.value = values[4] as List<BillInstance>
                _settlements.value = values[5] as List<Settlement>
                _swapRequests.value = values[6] as List<FlatSwapRequest>
                _activity.value = values[7] as List<FlatActivity>
                _joinRequests.value = values[8] as List<JoinRequest>
                _monthCycles.value = values[9] as List<MonthCycle>
                _loading.value = false
            }.collect()
        }
    }

    fun computeNetBalances(month: String = currentMonthKey()): Map<String, Double> =
        computeMonthNetBalances(month, _expenses.value, _billInstances.value, _settlements.value, null)

    fun completeTask(task: FlatTask) {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        viewModelScope.launch {
            tasksRepository.completeTask(flat, task, _members.value, uid)
        }
    }

    fun createTask(name: String, frequency: String, priority: String, participantUids: List<String>, type: String = "rotating_duty") {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        val queue = if (participantUids.isNotEmpty()) participantUids else listOf(uid)
        val dueDate = Instant.now().plus(7, ChronoUnit.DAYS).toString()
        viewModelScope.launch {
            tasksRepository.createTask(
                flatId = flat, name = name, type = type, priority = priority,
                frequency = frequency, queueOrder = queue, dueDate = dueDate, adminId = uid
            )
        }
    }

    fun deleteTask(task: FlatTask) {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        viewModelScope.launch { tasksRepository.deleteTask(flat, task.taskId, uid, task.name) }
    }

    fun addExpense(
        description: String,
        amount: Double,
        splitAmong: List<String>,
        category: String = "other",
        currency: String = "INR",
        splitType: String = "equal",
        customSplits: Map<String, Double>? = null
    ) {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        val participants = if (splitAmong.isNotEmpty()) splitAmong else _members.value.map { it.uid }
        val splits = when {
            customSplits != null -> customSplits
            splitType == "equal" -> {
                val perPerson = if (participants.isNotEmpty()) amount / participants.size else amount
                participants.associateWith { perPerson }
            }
            else -> participants.associateWith { amount / participants.size.coerceAtLeast(1) }
        }
        val expense = FlatExpense(
            id = UUID.randomUUID().toString(), description = description.trim(), amount = amount,
            currency = currency, paidBy = uid, splitAmong = participants, splitType = splitType,
            splits = splits, category = category,
            date = LocalDate.now().toString(), createdBy = uid
        )
        viewModelScope.launch { expensesRepository.addExpense(flat, expense) }
    }

    fun updateExpense(expense: FlatExpense) {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        viewModelScope.launch { expensesRepository.updateExpense(flat, expense, uid) }
    }

    fun deleteExpense(expenseId: String) {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        viewModelScope.launch { expensesRepository.deleteExpense(flat, expenseId, uid) }
    }

    fun createRecurringBill(
        name: String, amount: Double?, billingDay: Int, isVariable: Boolean,
        memberUids: List<String>, collectorId: String? = null
    ) {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        val bill = RecurringBill(
            id = UUID.randomUUID().toString(), name = name.trim(), amount = amount,
            billingDay = billingDay.coerceIn(1, 28), rotationQueue = memberUids,
            participants = memberUids, isVariable = isVariable, active = true,
            createdBy = uid, createdAt = Instant.now().toString(),
            collectorId = collectorId ?: uid
        )
        viewModelScope.launch { billsRepository.createRecurringBill(flat, bill) }
    }

    fun generateBill(billId: String, amount: Double?) {
        val flat = _flatId.value ?: return
        val uid = currentUser.value?.uid ?: return
        val bill = _recurringBills.value.find { it.id == billId } ?: return
        viewModelScope.launch {
            billsRepository.generateBill(flat, bill, amount, uid, _members.value.map { it.uid })
        }
    }

    fun markBillPaid(instanceId: String) {
        val flat = _flatId.value ?: return
        viewModelScope.launch { billsRepository.markBillPaid(flat, instanceId) }
    }

    fun skipBillInstance(instanceId: String) {
        val flat = _flatId.value ?: return
        viewModelScope.launch { billsRepository.skipBillInstance(flat, instanceId) }
    }

    fun markBillCollected(instanceId: String, memberUid: String, collected: Boolean) {
        val flat = _flatId.value ?: return
        viewModelScope.launch { billsRepository.markBillCollected(flat, instanceId, memberUid, collected) }
    }

    fun closeMonth(month: String) {
        val flat = _flatId.value ?: return
        val uid = currentUser.value?.uid ?: return
        val nets = computeNetBalances(month)
        val monthExpenses = _expenses.value.filter { it.date.startsWith(month) }
        val monthBills = _billInstances.value.filter { it.month == month && it.status != "skipped" }
        val monthSettlements = _settlements.value.filter { (it.month ?: it.date.take(7)) == month }
        val cycle = MonthCycle(
            id = "${flat}_$month",
            month = month,
            status = "closed",
            closedAt = Instant.now().toString(),
            totalBillsINR = monthBills.sumOf { it.amount ?: 0.0 },
            totalExpensesINR = monthExpenses.sumOf { it.amount },
            totalSettledINR = monthSettlements.sumOf { it.amount },
            carryForwardOut = nets.filter { abs(it.value) > 0.5 }
        )
        viewModelScope.launch { billsRepository.closeMonth(flat, cycle, uid) }
    }

    fun recordSettlement(s: SuggestedSettlement) {
        val flat = _flatId.value ?: return
        val uid = currentUser.value?.uid ?: return
        val settlement = Settlement(
            id = UUID.randomUUID().toString(), fromUserId = s.fromUserId, toUserId = s.toUserId,
            amount = s.amount, date = LocalDate.now().toString(), month = currentMonthKey(),
            type = "immediate", createdAt = Instant.now().toString()
        )
        viewModelScope.launch { billsRepository.addSettlement(flat, settlement, uid) }
    }

    fun recordManualSettlement(toUserId: String, amount: Double) {
        val uid = currentUser.value?.uid ?: return
        recordSettlement(SuggestedSettlement(uid, toUserId, amount))
    }

    fun recordMarkReceived(fromUserId: String, amount: Double) {
        val uid = currentUser.value?.uid ?: return
        recordSettlement(SuggestedSettlement(fromUserId, uid, amount))
    }

    fun updateSeekerProfile(city: String, lookingIn: String, budget: Double, bio: String) {
        val user = currentUser.value ?: return
        val profile = SeekerProfile(
            id = user.uid, displayName = user.displayName.orEmpty(), photoUrl = user.photoUrl?.toString().orEmpty(),
            city = city, lookingIn = lookingIn, budget = budget, bio = bio, active = true,
            createdAt = Instant.now().toString()
        )
        viewModelScope.launch { messagingRepository.upsertSeekerProfile(profile) }
    }

    fun sendMessage(receiverId: String, content: String) {
        val uid = currentUser.value?.uid ?: return
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(), senderId = uid, receiverId = receiverId,
            flatId = _flatId.value, content = content.trim(), timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch { messagingRepository.sendMessage(msg) }
    }

    fun createSwapRequest(taskId: String, toUserId: String, isOOSRequest: Boolean = false) {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        viewModelScope.launch { swapRepository.createSwapRequest(flat, taskId, uid, toUserId, isOOSRequest) }
    }

    fun sendGoingAwayRequests(assignments: Map<String, String>) {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        viewModelScope.launch {
            assignments.forEach { (taskId, toUserId) ->
                swapRepository.createSwapRequest(flat, taskId, uid, toUserId, isOOSRequest = true)
            }
        }
    }

    fun respondToSwap(requestId: String, accept: Boolean) {
        val flat = _flatId.value ?: return
        val uid = currentUser.value?.uid ?: return
        val request = _swapRequests.value.find { it.id == requestId } ?: return
        val task = _tasks.value.find { it.taskId == request.taskId }
        val fromUser = _members.value.find { it.uid == request.fromUserId }
        viewModelScope.launch {
            swapRepository.respondToSwap(flat, requestId, accept)
            if (accept && task != null && fromUser != null) {
                tasksRepository.transferTask(flat, request.taskId, request.toUserId, request.fromUserId, task.name)
                activityRepository.addActivity(
                    flat, uid, "swap_resolved",
                    "accepted to cover ${task.name} for ${fromUser.nickname}"
                )
                if (request.isOOSRequest) {
                    val stillPending = _swapRequests.value.filter {
                        it.fromUserId == request.fromUserId && it.isOOSRequest && it.status == "pending" && it.id != requestId
                    }
                    if (stillPending.isEmpty()) {
                        membersRepository.updateMemberStatus(flat, request.fromUserId, "out_of_station")
                    }
                }
            } else if (!accept && task != null && fromUser != null) {
                activityRepository.addActivity(
                    flat, uid, "swap_resolved",
                    "declined to cover ${task.name} for ${fromUser.nickname}"
                )
            }
        }
    }

    fun switchFlat(newFlatId: String) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            val oldFlat = _flatId.value
            usersRepository.setActiveFlat(uid, newFlatId).onSuccess {
                if (oldFlat != null) FcmNotificationHelper.unsubscribeFromFlatTopic(oldFlat)
                FcmNotificationHelper.subscribeToFlatTopic(newFlatId)
                usersRepository.getUserProfile(uid).onSuccess { _userProfile.value = it }
                bindFlat(newFlatId)
            }
        }
    }

    fun leaveFlat() {
        val uid = currentUser.value?.uid ?: return
        val flat = _flatId.value ?: return
        viewModelScope.launch {
            usersRepository.leaveCurrentFlat(uid, flat).onSuccess { nextFlatId ->
                FcmNotificationHelper.unsubscribeFromFlatTopic(flat)
                if (nextFlatId != null) {
                    FcmNotificationHelper.subscribeToFlatTopic(nextFlatId)
                    bindFlat(nextFlatId)
                } else {
                    _flatId.value = null
                    _flatInfo.value = null
                    flatObserveJob?.cancel()
                }
                usersRepository.getUserProfile(uid).onSuccess { _userProfile.value = it }
            }
        }
    }

    fun kickMember(targetUid: String) {
        val flat = _flatId.value ?: return
        viewModelScope.launch { membersRepository.kickMember(flat, targetUid) }
    }

    fun approveJoinRequest(request: JoinRequest) {
        val flat = _flatId.value ?: return
        viewModelScope.launch {
            flatsRepository.joinFlat(flat, request.uid, request.nickname, request.email)
            firestoreUpdateJoinApproved(flat, request.id)
        }
    }

    private suspend fun firestoreUpdateJoinApproved(flatId: String, requestId: String) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("flats").document(flatId).collection("joinRequests").document(requestId)
            .update("status", "approved").await()
    }

    fun rejectJoinRequest(requestId: String) {
        val flat = _flatId.value ?: return
        viewModelScope.launch { membersRepository.rejectJoinRequest(flat, requestId) }
    }

    fun updateVacancy(vacancy: VacancyData) {
        val flat = _flatId.value ?: return
        viewModelScope.launch {
            flatsRepository.updateVacancy(flat, vacancy).onSuccess {
                _flatInfo.value = _flatInfo.value?.copy(vacancy = vacancy)
            }
        }
    }

    fun renameFlat(newName: String) {
        val flat = _flatId.value ?: return
        viewModelScope.launch {
            flatsRepository.renameFlat(flat, newName).onSuccess {
                _flatInfo.value = _flatInfo.value?.copy(name = newName.trim())
            }
        }
    }

    fun setJoinMode(mode: String) {
        val flat = _flatId.value ?: return
        viewModelScope.launch {
            flatsRepository.setJoinMode(flat, mode).onSuccess {
                _flatInfo.value = _flatInfo.value?.copy(joinMode = mode)
            }
        }
    }

    fun manuallyAssignTask(taskId: String, targetUserId: String) {
        val flat = _flatId.value ?: return
        val uid = currentUser.value?.uid ?: return
        val task = _tasks.value.find { it.taskId == taskId } ?: return
        val target = _members.value.find { it.uid == targetUserId } ?: return
        viewModelScope.launch {
            tasksRepository.manuallyAssignTask(flat, taskId, targetUserId, uid, task.name, target.nickname)
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
            FcmNotificationHelper.subscribeToFlatTopic(flatId)
            bindFlat(flatId)
        }
    }

    fun onFlatJoined(flatId: String) = onFlatCreated(flatId)
}

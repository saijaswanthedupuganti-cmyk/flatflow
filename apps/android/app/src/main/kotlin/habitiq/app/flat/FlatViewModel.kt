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
import habitiq.app.lib.defaultDueDateIso
import habitiq.app.services.FcmNotificationHelper
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.nio.charset.StandardCharsets

data class DiscoveryUploadProgress(val completedFiles: Int, val totalFiles: Int, val bytesTransferred: Long, val totalBytes: Long) {
    val fraction: Float
        get() {
            if (totalFiles <= 0) return 0f
            val current = if (totalBytes > 0) bytesTransferred.toFloat() / totalBytes else 0f
            return ((completedFiles + current) / totalFiles).coerceIn(0f, 1f)
        }
}
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

    private val _discoverVacanciesLoading = MutableStateFlow(true)
    val discoverVacanciesLoading: StateFlow<Boolean> = _discoverVacanciesLoading.asStateFlow()

    private val _discoverSeekersLoading = MutableStateFlow(true)
    val discoverSeekersLoading: StateFlow<Boolean> = _discoverSeekersLoading.asStateFlow()

    private val _discoverVacanciesError = MutableStateFlow<String?>(null)
    val discoverVacanciesError: StateFlow<String?> = _discoverVacanciesError.asStateFlow()

    private val _discoverSeekersError = MutableStateFlow<String?>(null)
    val discoverSeekersError: StateFlow<String?> = _discoverSeekersError.asStateFlow()

    private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val blockedUserIds: StateFlow<Set<String>> = _blockedUserIds.asStateFlow()

    private val _connections = MutableStateFlow<List<habitiq.app.discover.DiscoveryConnection>>(emptyList())
    val connections: StateFlow<List<habitiq.app.discover.DiscoveryConnection>> = _connections.asStateFlow()

    private val _connectionActionError = MutableStateFlow<String?>(null)
    val connectionActionError: StateFlow<String?> = _connectionActionError.asStateFlow()

    private val _connectionActionLoading = MutableStateFlow(false)
    val connectionActionLoading: StateFlow<Boolean> = _connectionActionLoading.asStateFlow()

    private val _discoveryPostLoading = MutableStateFlow(false)
    val discoveryPostLoading: StateFlow<Boolean> = _discoveryPostLoading.asStateFlow()

    private val _discoveryPostError = MutableStateFlow<String?>(null)
    val discoveryPostError: StateFlow<String?> = _discoveryPostError.asStateFlow()

    private val _discoveryUploadProgress = MutableStateFlow<DiscoveryUploadProgress?>(null)
    val discoveryUploadProgress: StateFlow<DiscoveryUploadProgress?> = _discoveryUploadProgress.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _activity = MutableStateFlow<List<FlatActivity>>(emptyList())
    val activity: StateFlow<List<FlatActivity>> = _activity.asStateFlow()

    private val _joinRequests = MutableStateFlow<List<JoinRequest>>(emptyList())
    val joinRequests: StateFlow<List<JoinRequest>> = _joinRequests.asStateFlow()

    /** Admin: members' vacancy posts waiting for approval. */
    private val _vacancyRequests = MutableStateFlow<List<habitiq.app.data.VacancyRequest>>(emptyList())
    val vacancyRequests: StateFlow<List<habitiq.app.data.VacancyRequest>> = _vacancyRequests.asStateFlow()

    /** The person's own vacancy request (pending / approved / declined), if they posted as a member. */
    private val _myVacancyRequest = MutableStateFlow<habitiq.app.data.VacancyRequest?>(null)
    val myVacancyRequest: StateFlow<habitiq.app.data.VacancyRequest?> = _myVacancyRequest.asStateFlow()

    private val _monthCycles = MutableStateFlow<List<MonthCycle>>(emptyList())
    val monthCycles: StateFlow<List<MonthCycle>> = _monthCycles.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val showAddTaskTrigger = MutableStateFlow(false)
    val showAddExpenseTrigger = MutableStateFlow(false)
    val showBillsTrigger = MutableStateFlow(false)
    /** Manage > Expenses scope: false = Daily splits, true = Monthly bills. Survives tab switches. */
    val expenseScopeMonthly = MutableStateFlow(false)
    /** Opens the add-bill form inside Monthly bills (from Quick add). */
    val showAddBillTrigger = MutableStateFlow(false)
    /** Manage Flat's "Payment due" attention card opens Expenses with the balance list pre-expanded. */
    val showBalancesTrigger = MutableStateFlow(false)

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
    private var connectionsJob: Job? = null
    private var blocksJob: Job? = null
    private var flatGeneration: Long = 0

    init {
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user == null) {
                    _flatId.value = null
                    clearFlatState()
                    _loading.value = false
                    return@collect
                }
                loadSession(user)
            }
        }
    }

    /** Discover, chat, and blocks stay off until that tab opens, so Home does not pay for them. */
    fun ensureDiscovery() {
        if (discoveryJob != null) return
        val uid = currentUser.value?.uid
        discoveryJob = viewModelScope.launch {
            discoveryRepository.observeActiveVacancies()
                .onStart { _discoverVacanciesLoading.value = true }
                .catch { e ->
                    _discoverVacanciesError.value = e.message ?: "Could not load vacancies"
                    _discoverVacanciesLoading.value = false
                }
                .collect {
                    _vacancies.value = it
                    _discoverVacanciesLoading.value = false
                    _discoverVacanciesError.value = null
                }
        }
        seekerJob = viewModelScope.launch {
            messagingRepository.observeActiveSeekers()
                .onStart { _discoverSeekersLoading.value = true }
                .catch { e ->
                    _discoverSeekersError.value = e.message ?: "Could not load seeker profiles"
                    _discoverSeekersLoading.value = false
                }
                .collect {
                    _seekerProfiles.value = it
                    _discoverSeekersLoading.value = false
                    _discoverSeekersError.value = null
                }
        }
        if (uid != null) {
            bindMessaging(uid)
            bindDiscoverySocial(uid)
        }
    }

    private fun bindMessaging(uid: String) {
        chatJob?.cancel()
        chatJob = viewModelScope.launch {
            messagingRepository.observeMessagesForUser(uid).catch { emit(emptyList()) }
                .collect { _chatMessages.value = it }
        }
    }

    private fun bindDiscoverySocial(uid: String) {
        connectionsJob?.cancel()
        blocksJob?.cancel()
        connectionsJob = viewModelScope.launch {
            discoveryRepository.observeConnections(uid).catch { emit(emptyList()) }
                .collect { _connections.value = it }
        }
        blocksJob = viewModelScope.launch {
            discoveryRepository.observeBlockedIds(uid).catch { emit(emptySet()) }
                .collect { ids -> _blockedUserIds.value = ids }
        }
    }

    fun connectionWith(partnerId: String): habitiq.app.discover.DiscoveryConnection? {
        val uid = currentUser.value?.uid ?: return null
        return _connections.value.find {
            (it.fromUid == uid && it.toUid == partnerId) || (it.fromUid == partnerId && it.toUid == uid)
        }
    }

    fun sendConnectionRequest(
        toUid: String,
        message: String,
        listingFlatId: String? = null,
        seekerId: String? = null,
        onResult: (Result<String>) -> Unit = {}
    ) {
        val uid = currentUser.value?.uid
        if (uid == null) {
            onResult(Result.failure(IllegalStateException("Sign in to send a request.")))
            return
        }
        if (toUid.isBlank() || toUid == uid) {
            onResult(Result.failure(IllegalArgumentException("Choose another Oddroof member.")))
            return
        }
        viewModelScope.launch {
            _connectionActionLoading.value = true
            _connectionActionError.value = null
            val result = discoveryRepository.sendConnectionRequest(uid, toUid, message, listingFlatId, seekerId)
                .onFailure { _connectionActionError.value = it.message ?: "Couldn't send the request." }
            _connectionActionLoading.value = false
            onResult(result)
        }
    }

    fun retryDiscovery() {
        discoveryJob?.cancel()
        seekerJob?.cancel()
        discoveryJob = null
        seekerJob = null
        ensureDiscovery()
    }

    fun respondToConnection(connectionId: String, accept: Boolean) {
        viewModelScope.launch {
            _connectionActionLoading.value = true
            discoveryRepository.updateConnectionStatus(
                connectionId,
                if (accept) habitiq.app.discover.ConnectionStatus.ACCEPTED
                else habitiq.app.discover.ConnectionStatus.DECLINED
            ).onFailure { _connectionActionError.value = it.message ?: "Couldn't update the request." }
            _connectionActionLoading.value = false
        }
    }

    fun clearConnectionActionError() {
        _connectionActionError.value = null
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
        val generation = ++flatGeneration
        _flatId.value = flatId
        _loading.value = true
        _error.value = null
        clearFlatState()
        flatObserveJob?.cancel()
        flatObserveJob = viewModelScope.launch {
            flatsRepository.getFlat(flatId)
                .onSuccess { if (generation == flatGeneration && _flatId.value == flatId) _flatInfo.value = it }
                .onFailure {
                    if (generation == flatGeneration) {
                        _error.value = it.message ?: "Couldn't load this flat."
                        _loading.value = false
                    }
                }
            fun <T> watch(label: String, flow: Flow<T>, assign: (T) -> Unit) {
                launch {
                    flow.catch {
                        if (generation == flatGeneration) {
                            _error.value = "Couldn't load $label. Try again."
                            _loading.value = false
                        }
                    }.collect { value ->
                        if (generation == flatGeneration && _flatId.value == flatId) {
                            assign(value)
                            _loading.value = false
                        }
                    }
                }
            }
            watch("members", membersRepository.observeMembers(flatId)) { _members.value = it }
            watch("tasks", tasksRepository.observeTasks(flatId)) { _tasks.value = it }
            watch("expenses", expensesRepository.observeExpenses(flatId)) { _expenses.value = it }
            watch("monthly bills", billsRepository.observeRecurringBills(flatId)) { _recurringBills.value = it }
            watch("bill instances", billsRepository.observeBillInstances(flatId)) { _billInstances.value = it }
            watch("settlements", billsRepository.observeSettlements(flatId)) { _settlements.value = it }
            watch("swap requests", swapRepository.observeSwapRequests(flatId)) { _swapRequests.value = it }
            watch("activity", activityRepository.observeRecentActivity(flatId, 20)) { _activity.value = it }
            watch("join requests", membersRepository.observeJoinRequests(flatId)) { _joinRequests.value = it }
            watch("month cycles", billsRepository.observeMonthCycles(flatId)) { _monthCycles.value = it }
            watch("vacancy requests", flatsRepository.observePendingVacancyRequests(flatId)) { _vacancyRequests.value = it }
            currentUser.value?.uid?.let { me ->
                watch("your vacancy", flatsRepository.observeMyVacancyRequest(flatId, me)) { _myVacancyRequest.value = it }
            }
        }
    }

    private fun clearFlatState() {
        _flatInfo.value = null
        _vacancyRequests.value = emptyList()
        _myVacancyRequest.value = null
        _members.value = emptyList()
        _tasks.value = emptyList()
        _expenses.value = emptyList()
        _recurringBills.value = emptyList()
        _billInstances.value = emptyList()
        _settlements.value = emptyList()
        _swapRequests.value = emptyList()
        _activity.value = emptyList()
        _joinRequests.value = emptyList()
        _monthCycles.value = emptyList()
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

    fun createTask(
        name: String,
        frequency: String,
        priority: String,
        participantUids: List<String>,
        type: String = "rotating_duty",
        dueDate: String? = null,
        notes: String = "",
        onDone: (Boolean) -> Unit = {}
    ) {
        val uid = currentUser.value?.uid ?: return onDone(false)
        val flat = _flatId.value ?: return onDone(false)
        val queue = if (participantUids.isNotEmpty()) participantUids else listOf(uid)
        val start = dueDate?.takeIf { it.isNotBlank() } ?: defaultDueDateIso(7)
        viewModelScope.launch {
            val result = tasksRepository.createTask(
                flatId = flat, name = name, type = type, priority = priority,
                frequency = frequency, queueOrder = queue, dueDate = start, adminId = uid, notes = notes
            )
            result.onFailure { _error.value = it.message ?: "Couldn't create the task." }
            onDone(result.isSuccess)
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
        customSplits: Map<String, Double>? = null,
        paidBy: String? = null,
        onDone: (Boolean) -> Unit = {}
    ) {
        val uid = currentUser.value?.uid ?: return onDone(false)
        val flat = _flatId.value ?: return onDone(false)
        // The payer defaults to the person recording the expense; createdBy always stays the recorder.
        val payer = paidBy ?: uid
        if (payer != uid && _members.value.none { it.uid == payer }) return onDone(false)
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
            currency = currency, paidBy = payer, splitAmong = participants, splitType = splitType,
            splits = splits, category = category,
            date = LocalDate.now().toString(), createdBy = uid
        )
        viewModelScope.launch {
            val result = expensesRepository.addExpense(flat, expense)
            result.onFailure { _error.value = it.message ?: "Couldn't add the expense." }
            onDone(result.isSuccess)
        }
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
            carryForwardOut = nets.filter { abs(it.value) >= habitiq.app.lib.BALANCE_EPSILON }
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

    fun updateSeekerProfile(
        city: String,
        lookingIn: String,
        budget: Double,
        bio: String,
        gender: String = "",
        lifestyleTags: String = "",
        active: Boolean = true,
        onSaved: () -> Unit = {}
    ) {
        val user = currentUser.value ?: return
        val existing = _seekerProfiles.value.find { it.id == user.uid }
        val profile = SeekerProfile(
            id = user.uid,
            displayName = user.displayName.orEmpty(),
            photoUrl = user.photoUrl?.toString().orEmpty(),
            city = city.trim().ifBlank { existing?.city.orEmpty() },
            lookingIn = lookingIn.trim().ifBlank { existing?.lookingIn.orEmpty() },
            budget = if (budget > 0) budget else existing?.budget ?: 0.0,
            bio = bio.trim().ifBlank { existing?.bio.orEmpty() },
            gender = gender.ifBlank { existing?.gender.orEmpty() },
            lifestyleTags = lifestyleTags.ifBlank { existing?.lifestyleTags.orEmpty() },
            active = active,
            createdAt = existing?.createdAt?.ifBlank { Instant.now().toString() } ?: Instant.now().toString()
        )
        viewModelScope.launch {
            _discoveryPostLoading.value = true
            _discoveryPostError.value = null
            messagingRepository.upsertSeekerProfile(profile)
                .onSuccess { onSaved() }
                .onFailure { _discoveryPostError.value = it.message ?: "Couldn't publish your looking post." }
            _discoveryPostLoading.value = false
        }
    }

    fun setSeekerActive(active: Boolean) {
        val user = currentUser.value ?: return
        val existing = _seekerProfiles.value.find { it.id == user.uid } ?: return
        viewModelScope.launch {
            messagingRepository.upsertSeekerProfile(existing.copy(active = active))
        }
    }

    fun blockUser(userId: String) {
        val uid = currentUser.value?.uid ?: return
        _blockedUserIds.value = _blockedUserIds.value + userId
        viewModelScope.launch {
            discoveryRepository.blockUser(uid, userId)
            val open = connectionWith(userId)
            if (open != null) {
                discoveryRepository.updateConnectionStatus(open.id, habitiq.app.discover.ConnectionStatus.BLOCKED)
            }
        }
    }

    fun reportUser(userId: String, reason: String, listingFlatId: String? = null) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            discoveryRepository.submitReport(
                habitiq.app.discover.DiscoveryReport(
                    id = UUID.randomUUID().toString(),
                    reporterUid = uid,
                    targetUid = userId,
                    listingFlatId = listingFlatId,
                    reason = reason,
                    createdAt = Instant.now().toString()
                )
            )
        }
    }

    fun sendMessage(receiverId: String, content: String, onResult: (Boolean) -> Unit = {}) {
        val uid = currentUser.value?.uid ?: return
        val trimmed = content.trim()
        val connection = connectionWith(receiverId)
        if (trimmed.isBlank() || trimmed.length > 2000 || connection == null ||
            connection.status !in setOf(
                habitiq.app.discover.ConnectionStatus.ACCEPTED,
                habitiq.app.discover.ConnectionStatus.CONVERSATION_OPEN,
                habitiq.app.discover.ConnectionStatus.MATCHED
            ) || receiverId in _blockedUserIds.value
        ) {
            onResult(false)
            return
        }
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(), senderId = uid, receiverId = receiverId,
            flatId = _flatId.value, content = trimmed, timestamp = System.currentTimeMillis(),
            connectionId = connection.id
        )
        viewModelScope.launch {
            messagingRepository.sendMessage(msg)
                .onSuccess { onResult(true) }
                .onFailure { onResult(false) }
        }
    }

    fun sendViewingRequest(
        receiverId: String,
        viewingTime: Long,
        note: String,
        onResult: (Boolean) -> Unit = {}
    ) {
        val uid = currentUser.value?.uid ?: return onResult(false)
        val connection = connectionWith(receiverId)
        if (viewingTime <= System.currentTimeMillis() || connection == null ||
            connection.status !in setOf(
                habitiq.app.discover.ConnectionStatus.ACCEPTED,
                habitiq.app.discover.ConnectionStatus.CONVERSATION_OPEN,
                habitiq.app.discover.ConnectionStatus.MATCHED
            ) || receiverId in _blockedUserIds.value
        ) {
            onResult(false)
            return
        }
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = uid,
            receiverId = receiverId,
            flatId = _flatId.value,
            content = note.trim().ifBlank { "I'd like to schedule a viewing." }.take(500),
            timestamp = System.currentTimeMillis(),
            connectionId = connection.id,
            isViewingRequest = true,
            viewingTime = viewingTime,
            viewingStatus = "proposed"
        )
        viewModelScope.launch {
            messagingRepository.sendMessage(msg)
                .onSuccess { onResult(true) }
                .onFailure { onResult(false) }
        }
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
            val joined = flatsRepository.joinFlat(flat, request.uid, request.nickname, request.email, linkUserProfile = false)
            if (joined.isSuccess) firestoreUpdateJoinApproved(flat, request.id)
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

    fun updateVacancy(vacancy: VacancyData, onSaved: () -> Unit = {}) {
        val flat = _flatId.value ?: return
        val health = habitiq.app.discover.FlatHealthComputer.compute(
            tasks = _tasks.value,
            settlements = _settlements.value,
            expensesCount = _expenses.value.size,
            activity = _activity.value,
            members = _members.value,
            nowIso = Instant.now().toString()
        )
        viewModelScope.launch {
            _discoveryPostLoading.value = true
            _discoveryPostError.value = null
            flatsRepository.updateVacancy(flat, vacancy, health)
                .onSuccess {
                    _flatInfo.value = _flatInfo.value?.copy(vacancy = vacancy)
                    onSaved()
                }
                .onFailure { _discoveryPostError.value = it.message ?: "Couldn't publish the vacancy." }
            _discoveryPostLoading.value = false
        }
    }

    fun publishVacancy(
        vacancy: VacancyData,
        photos: List<Uri>,
        contentResolver: android.content.ContentResolver? = null,
        onSaved: () -> Unit = {}
    ) {
        val flat = _flatId.value ?: return
        val me = currentUser.value?.uid ?: return
        // Members send the vacancy to the admin for approval; the admin publishes straight away.
        val asRequest = _flatInfo.value?.adminUid != me
        val myName = _members.value.firstOrNull { it.uid == me }?.nickname ?: _userProfile.value?.displayName.orEmpty()
        viewModelScope.launch {
            _discoveryPostLoading.value = true
            _discoveryPostError.value = null

            for (uri in photos) {
                val validation = habitiq.app.discover.DiscoveryMediaValidator.validate(contentResolver, uri)
                if (validation is habitiq.app.discover.DiscoveryMediaValidator.ValidationResult.Invalid) {
                    _discoveryPostError.value = validation.reason
                    _discoveryPostLoading.value = false
                    return@launch
                }
            }

            _discoveryUploadProgress.value = if (photos.isEmpty()) null else DiscoveryUploadProgress(0, photos.size, 0, 0)
            val uploadedRefs = mutableListOf<com.google.firebase.storage.StorageReference>()
            runCatching {
                val uploaded = photos.mapIndexed { index, uri ->
                    val stableId = UUID.nameUUIDFromBytes("$flat:${uri}".toByteArray(StandardCharsets.UTF_8))
                    val ref = FirebaseStorage.getInstance().reference
                        .child(if (asRequest) "flats/$flat/vacancyRequests/$me/$stableId" else "flats/$flat/vacancy/$stableId")
                    uploadedRefs.add(ref)
                    // storage.rules only accepts image/* — some pickers report no type, so set it.
                    val mime = contentResolver?.getType(uri)?.takeIf { it.startsWith("image/") } ?: "image/jpeg"
                    val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                        .setContentType(mime)
                        .build()
                    val task = ref.putFile(uri, metadata)
                    task.addOnProgressListener { snapshot ->
                        _discoveryUploadProgress.value = DiscoveryUploadProgress(
                            completedFiles = index,
                            totalFiles = photos.size,
                            bytesTransferred = snapshot.bytesTransferred,
                            totalBytes = snapshot.totalByteCount
                        )
                    }
                    task.await()
                    _discoveryUploadProgress.value = DiscoveryUploadProgress(index + 1, photos.size, 0, 0)
                    ref.downloadUrl.await().toString()
                }
                val saved = vacancy.copy(photoUrls = (uploaded + vacancy.photoUrls).distinct())
                val health = habitiq.app.discover.FlatHealthComputer.compute(
                    tasks = _tasks.value,
                    settlements = _settlements.value,
                    expensesCount = _expenses.value.size,
                    activity = _activity.value,
                    members = _members.value,
                    nowIso = Instant.now().toString()
                )
                if (asRequest) flatsRepository.submitVacancyRequest(flat, me, myName, saved).getOrThrow()
                else flatsRepository.updateVacancy(flat, saved.copy(postedBy = saved.postedBy ?: me), health).getOrThrow()
                saved
            }.onSuccess { saved ->
                if (asRequest) {
                    onSaved()
                } else if (_flatId.value == flat) {
                    _flatInfo.value = _flatInfo.value?.copy(vacancy = saved)
                    onSaved()
                }
            }.onFailure { ex ->
                android.util.Log.e("FlatViewModel", "publishVacancy failed", ex)
                uploadedRefs.forEach { ref ->
                    runCatching { ref.delete().await() }
                }
                _discoveryPostError.value = when (ex) {
                    is com.google.firebase.storage.StorageException -> when (ex.errorCode) {
                        com.google.firebase.storage.StorageException.ERROR_NOT_AUTHORIZED ->
                            "Photo upload was blocked. Try again, or post without photos."
                        com.google.firebase.storage.StorageException.ERROR_RETRY_LIMIT_EXCEEDED ->
                            "Couldn't upload one of the photos. Check your connection and try again."
                        else -> "Couldn't upload one of the photos (code ${ex.errorCode}). Try again."
                    }
                    else -> "Couldn't publish the vacancy. Your form is still here; try again."
                }
            }
            _discoveryPostLoading.value = false
            _discoveryUploadProgress.value = null
        }
    }

    /** Admin approves a member's vacancy: it goes live on Discover, credited to that member. */
    fun approveVacancyRequest(request: habitiq.app.data.VacancyRequest) {
        val flat = _flatId.value ?: return
        val health = habitiq.app.discover.FlatHealthComputer.compute(
            tasks = _tasks.value, settlements = _settlements.value, expensesCount = _expenses.value.size,
            activity = _activity.value, members = _members.value, nowIso = Instant.now().toString()
        )
        viewModelScope.launch {
            flatsRepository.approveVacancyRequest(flat, request, health)
                .onSuccess {
                    _flatInfo.value = _flatInfo.value?.copy(
                        vacancy = request.vacancy.copy(active = true, postStatus = "PUBLISHED", postedBy = request.requesterUid)
                    )
                }
                .onFailure { _error.value = it.message ?: "Couldn't approve the vacancy." }
        }
    }

    fun declineVacancyRequest(request: habitiq.app.data.VacancyRequest) {
        val flat = _flatId.value ?: return
        viewModelScope.launch {
            flatsRepository.declineVacancyRequest(flat, request.requesterUid)
                .onFailure { _error.value = it.message ?: "Couldn't decline the vacancy." }
        }
    }

    /** Pause / resume / close the live vacancy. Allowed for the admin and for the member who posted it. */
    fun setVacancyActive(active: Boolean, status: String) {
        val flat = _flatId.value ?: return
        viewModelScope.launch {
            flatsRepository.setVacancyActive(flat, active, status)
                .onSuccess {
                    _flatInfo.value = _flatInfo.value?.let { f -> f.copy(vacancy = f.vacancy?.copy(active = active, postStatus = status)) }
                }
                .onFailure { _discoveryPostError.value = "Couldn't update the post. Try again." }
        }
    }

    fun clearDiscoveryPostError() {
        _discoveryPostError.value = null
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

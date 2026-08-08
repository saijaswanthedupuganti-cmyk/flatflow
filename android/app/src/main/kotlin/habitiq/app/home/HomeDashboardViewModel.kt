package habitiq.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import habitiq.app.auth.AuthRepository
import habitiq.app.data.ActivityRepository
import habitiq.app.data.ExpensesRepository
import habitiq.app.data.FlatActivity
import habitiq.app.data.FlatTask
import habitiq.app.data.TasksRepository
import habitiq.app.data.UsersRepository
import habitiq.app.flats.FlatInfo
import habitiq.app.flats.FlatsRepository
import habitiq.app.flats.Member
import habitiq.app.flats.MembersRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class HomeViewMode { FLAT, MY }

sealed interface HomeDashboardStatus {
    data object Loading : HomeDashboardStatus
    data object NoFlat : HomeDashboardStatus
    data class Ready(val data: HomeDashboardData) : HomeDashboardStatus
    data class Error(val message: String) : HomeDashboardStatus
}

data class HomeDashboardData(
    val flat: FlatInfo,
    val members: List<Member>,
    val tasks: List<FlatTask>,
    val activity: List<FlatActivity>,
    val monthlySpent: Double,
    val isAdmin: Boolean,
    val currentUid: String,
    val displayName: String,
    val viewMode: HomeViewMode
) {
    val memberCount: Int get() = members.size.coerceAtLeast(flat.memberCount)

    val activeTasks: List<FlatTask>
        get() = tasks.filter { it.status == "pending" || it.status == "overdue" }

    val todaysTasks: List<FlatTask>
        get() {
            val today = LocalDate.now().toString()
            val filtered = activeTasks.filter { it.dueDate == today }
            return if (viewMode == HomeViewMode.MY) {
                filtered.filter { it.currentAssignedUserId == currentUid }
            } else filtered
        }

    val tasksTodayCount: Int get() = activeTasks.count { it.dueDate == LocalDate.now().toString() }

    val completedThisWeek: Int
        get() = tasks.count { task ->
            task.status == "completed" && isWithinLastWeek(task.dueDate)
        }

    val weeklyProgress: Int
        get() {
            val weekTasks = tasks.filter { isWithinLastWeek(it.dueDate) || it.status == "pending" }
            if (weekTasks.isEmpty()) return 0
            val done = weekTasks.count { it.status == "completed" }
            return ((done.toFloat() / weekTasks.size) * 100).toInt().coerceIn(0, 100)
        }

    private fun isWithinLastWeek(dateStr: String): Boolean {
        if (dateStr.isBlank()) return false
        return runCatching {
            val date = LocalDate.parse(dateStr)
            ChronoUnit.DAYS.between(date, LocalDate.now()) in 0..7
        }.getOrDefault(false)
    }
}

class HomeDashboardViewModel(
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository,
    private val flatsRepository: FlatsRepository,
    private val membersRepository: MembersRepository,
    private val tasksRepository: TasksRepository,
    private val activityRepository: ActivityRepository,
    private val expensesRepository: ExpensesRepository
) : ViewModel() {

    private val _status = MutableStateFlow<HomeDashboardStatus>(HomeDashboardStatus.Loading)
    val status: StateFlow<HomeDashboardStatus> = _status

    private val _viewMode = MutableStateFlow(HomeViewMode.FLAT)
    val viewMode: StateFlow<HomeViewMode> = _viewMode

    private var observeJob: Job? = null
    private var flatId: String? = null

    init {
        load()
    }

    fun load() {
        val user = authRepository.currentUser.value
        if (user == null) {
            _status.value = HomeDashboardStatus.NoFlat
            return
        }
        _status.value = HomeDashboardStatus.Loading
        viewModelScope.launch {
            usersRepository.getActiveFlatId(user.uid).fold(
                onSuccess = { activeFlatId ->
                    if (activeFlatId == null) {
                        _status.value = HomeDashboardStatus.NoFlat
                    } else {
                        flatId = activeFlatId
                        observeFlat(activeFlatId, user)
                    }
                },
                onFailure = { error ->
                    _status.value = HomeDashboardStatus.Error(
                        error.message ?: "Couldn't load your flat."
                    )
                }
            )
        }
    }

    fun setViewMode(mode: HomeViewMode) {
        _viewMode.value = mode
        val current = _status.value
        if (current is HomeDashboardStatus.Ready) {
            _status.value = current.copy(data = current.data.copy(viewMode = mode))
        }
    }

    private fun observeFlat(flatId: String, user: FirebaseUser) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            val monthPrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            combine(
                membersRepository.observeMembers(flatId).catch { emit(emptyList()) },
                tasksRepository.observeTasks(flatId).catch { emit(emptyList()) },
                activityRepository.observeRecentActivity(flatId).catch { emit(emptyList()) },
                expensesRepository.observeMonthlyExpenses(flatId, monthPrefix).catch {
                    emit(habitiq.app.data.ExpenseSummary(0.0, 0))
                }
            ) { members, tasks, activity, expenses ->
                val flatResult = flatsRepository.getFlat(flatId)
                val flat = flatResult.getOrNull()
                if (flat == null) {
                    HomeDashboardStatus.Error("Couldn't load flat details.")
                } else {
                    val displayName = user.displayName?.substringBefore(" ")
                        ?: user.email?.substringBefore("@")
                        ?: "there"
                    HomeDashboardStatus.Ready(
                        HomeDashboardData(
                            flat = flat,
                            members = members,
                            tasks = tasks,
                            activity = activity,
                            monthlySpent = expenses.totalSpent,
                            isAdmin = flat.adminUid == user.uid,
                            currentUid = user.uid,
                            displayName = displayName,
                            viewMode = _viewMode.value
                        )
                    )
                }
            }.collect { status ->
                _status.value = status
            }
        }
    }
}

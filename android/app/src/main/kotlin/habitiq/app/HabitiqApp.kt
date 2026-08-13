package habitiq.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import habitiq.app.auth.AuthRepository
import habitiq.app.auth.LoginViewModel
import habitiq.app.auth.SignupViewModel
import habitiq.app.data.ActivityRepository
import habitiq.app.data.BillsRepository
import habitiq.app.data.DiscoveryRepository
import habitiq.app.data.ExpensesRepository
import habitiq.app.data.MessagingRepository
import habitiq.app.data.SwapRepository
import habitiq.app.data.TasksRepository
import habitiq.app.data.UsersRepository
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.CreateFlatViewModel
import habitiq.app.flats.FlatsRepository
import habitiq.app.flats.HomeViewModel
import habitiq.app.flats.JoinFlatViewModel
import habitiq.app.flats.MembersRepository
import habitiq.app.home.HomeDashboardViewModel
import habitiq.app.settings.SettingsViewModel
import habitiq.app.ui.AppShell
import habitiq.app.ui.AppTab
import habitiq.app.ui.CreateFlatScreen
import habitiq.app.ui.BillsScreen
import habitiq.app.ui.DiscoverBoardScreen
import habitiq.app.ui.FigmaHomeScreen
import habitiq.app.ui.JoinFlatScreen
import habitiq.app.ui.LoginScreen
import habitiq.app.ui.ActivityLogScreen
import habitiq.app.ui.FlatSwitcherSheet
import habitiq.app.ui.ManageFlatScreen
import habitiq.app.ui.MembersScreen
import habitiq.app.ui.ManageTaskScreen
import habitiq.app.ui.SwapReviewSheet
import habitiq.app.ui.figma.CreateRecurringTaskScreen
import habitiq.app.ui.figma.CreateTaskTypeScreen
import habitiq.app.ui.figma.CreateTempTaskScreen
import habitiq.app.ui.figma.GoingAwayScreen
import habitiq.app.ui.figma.TaskDetailScreen
import habitiq.app.ui.figma.TaskStructure
import habitiq.app.ui.OnboardingScreen
import habitiq.app.ui.PlusActionSheet
import habitiq.app.ui.ProfileScreen
import habitiq.app.ui.SettingsScreen
import habitiq.app.ui.SignupScreen
import habitiq.app.ui.collectAsStateWithLifecycleCompat
import habitiq.app.ui.theme.HabitiqTheme

private object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val MAIN = "main"
    const val CREATE_FLAT = "create_flat"
    const val CREATE_FLAT_BRILLIANT = "create_flat_brilliant"
    const val JOIN_FLAT = "join_flat"
    const val SETTINGS = "settings"
}

private fun flatViewModelFactory(
    authRepository: AuthRepository,
    usersRepository: UsersRepository,
    flatsRepository: FlatsRepository,
    membersRepository: MembersRepository,
    tasksRepository: TasksRepository,
    expensesRepository: ExpensesRepository,
    billsRepository: BillsRepository,
    activityRepository: ActivityRepository,
    swapRepository: SwapRepository,
    discoveryRepository: DiscoveryRepository,
    messagingRepository: MessagingRepository
) = FlatViewModel(
    authRepository, usersRepository, flatsRepository, membersRepository,
    tasksRepository, expensesRepository, billsRepository, activityRepository,
    swapRepository, discoveryRepository, messagingRepository
)

@Composable
fun HabitiqApp() {
    val authRepository = remember { AuthRepository() }
    val usersRepository = remember { UsersRepository() }
    val flatsRepository = remember { FlatsRepository() }
    val membersRepository = remember { MembersRepository() }
    val activityRepository = remember { ActivityRepository() }
    val tasksRepository = remember { TasksRepository(activityRepository = activityRepository) }
    val expensesRepository = remember { ExpensesRepository(activityRepository = activityRepository) }
    val swapRepository = remember { SwapRepository() }
    val discoveryRepository = remember { DiscoveryRepository() }
    val billsRepository = remember { BillsRepository() }
    val messagingRepository = remember { MessagingRepository() }

    val navController = rememberNavController()
    val currentUser by authRepository.currentUser.collectAsStateWithLifecycleCompat()

    HabitiqTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = if (currentUser != null) Routes.MAIN else Routes.LOGIN
            ) {
                composable(Routes.LOGIN) {
                    val viewModel = remember { LoginViewModel(authRepository, usersRepository) }
                    LoginScreen(
                        viewModel = viewModel,
                        onSignedIn = {
                            navController.navigate(Routes.MAIN) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        },
                        onNavigateToSignup = { navController.navigate(Routes.SIGNUP) }
                    )
                }
                composable(Routes.SIGNUP) {
                    val viewModel = remember { SignupViewModel(authRepository, usersRepository) }
                    SignupScreen(
                        viewModel = viewModel,
                        onSignedUp = {
                            navController.navigate(Routes.MAIN) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.MAIN) {
                    val user = currentUser
                    if (user == null) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                        }
                        return@composable
                    }

                    val flatViewModel = viewModel {
                        flatViewModelFactory(
                            authRepository, usersRepository, flatsRepository, membersRepository,
                            tasksRepository, expensesRepository, billsRepository, activityRepository,
                            swapRepository, discoveryRepository, messagingRepository
                        )
                    }

                    val flatId by flatViewModel.flatId.collectAsStateWithLifecycleCompat()
                    val loading by flatViewModel.loading.collectAsStateWithLifecycleCompat()

                    if (!loading && flatId == null) {
                        val name = user.displayName?.split(" ")?.firstOrNull() ?: "there"
                        OnboardingScreen(
                            userName = name,
                            onCreateFlat = { navController.navigate(Routes.CREATE_FLAT) },
                            onCreateFlatBrilliant = { navController.navigate(Routes.CREATE_FLAT_BRILLIANT) },
                            onJoinFlat = { navController.navigate(Routes.JOIN_FLAT) }
                        )
                        return@composable
                    }

                    var selectedTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
                    var showPlusSheet by remember { mutableStateOf(false) }
                    var showFlatSwitcher by remember { mutableStateOf(false) }
                    var profileOverlay by rememberSaveable { mutableStateOf<String?>(null) }
                    var createTaskType by rememberSaveable { mutableStateOf("rotating_duty") }
                    var manageSubTab by rememberSaveable { mutableStateOf("Chores") }
                    var tasksOverlay by rememberSaveable { mutableStateOf<String?>(null) }
                    var selectedTaskId by rememberSaveable { mutableStateOf<String?>(null) }
                    var showSwapReview by remember { mutableStateOf(false) }

                    val homeViewModel = viewModel { HomeViewModel(authRepository, usersRepository) }
                    val dashboardViewModel = viewModel {
                        HomeDashboardViewModel(
                            authRepository,
                            usersRepository,
                            flatsRepository,
                            membersRepository,
                            tasksRepository,
                            activityRepository,
                            expensesRepository
                        )
                    }

                    val flatViewModelTasks by flatViewModel.tasks.collectAsStateWithLifecycleCompat()
                    val flatViewModelMembers by flatViewModel.members.collectAsStateWithLifecycleCompat()
                    val flatViewModelSwaps by flatViewModel.swapRequests.collectAsStateWithLifecycleCompat()
                    val flatViewModelUser by flatViewModel.currentUser.collectAsStateWithLifecycleCompat()

                    when (profileOverlay) {
                        "members" -> MembersScreen(flatViewModel, onBack = { profileOverlay = null })
                        "manage_flat" -> ManageFlatScreen(flatViewModel, onBack = { profileOverlay = null })
                        "activity" -> ActivityLogScreen(flatViewModel, onBack = { profileOverlay = null })
                    }

                    if (profileOverlay == null) {
                    when (tasksOverlay) {
                        "going_away" -> GoingAwayScreen(
                            viewModel = flatViewModel,
                            onBack = { tasksOverlay = null },
                            onSent = { tasksOverlay = null; selectedTab = AppTab.TASKS }
                        )
                        "create_type" -> CreateTaskTypeScreen(
                            onBack = { tasksOverlay = null },
                            onSelect = { type ->
                                createTaskType = if (type == TaskStructure.GROUP) "group_duty" else "rotating_duty"
                                tasksOverlay = when (type) {
                                    TaskStructure.RECURRING, TaskStructure.GROUP -> "create_recurring"
                                    TaskStructure.TEMP -> "create_temp"
                                }
                            }
                        )
                        "create_recurring" -> CreateRecurringTaskScreen(
                            viewModel = flatViewModel,
                            onBack = { tasksOverlay = "create_type" },
                            onCreated = { tasksOverlay = null; selectedTab = AppTab.TASKS },
                            taskType = createTaskType
                        )
                        "create_temp" -> CreateTempTaskScreen(
                            viewModel = flatViewModel,
                            onBack = { tasksOverlay = "create_type" },
                            onCreated = { tasksOverlay = null; selectedTab = AppTab.TASKS }
                        )
                        else -> if (selectedTaskId != null) {
                            val task = flatViewModelTasks.find { it.taskId == selectedTaskId }
                            if (task != null) {
                                TaskDetailScreen(
                                    task = task,
                                    members = flatViewModelMembers,
                                    currentUid = flatViewModelUser?.uid.orEmpty(),
                                    onBack = { selectedTaskId = null },
                                    onRequestSwap = { toUid ->
                                        flatViewModel.createSwapRequest(task.taskId, toUid)
                                        selectedTaskId = null
                                    }
                                )
                            } else {
                                selectedTaskId = null
                            }
                        } else {
                            AppShell(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                onPlusClick = { showPlusSheet = true }
                            ) {
                                when (selectedTab) {
                                    AppTab.HOME -> FigmaHomeScreen(
                                        user = user,
                                        homeViewModel = homeViewModel,
                                        dashboardViewModel = dashboardViewModel,
                                        flatViewModel = flatViewModel,
                                        onCreateFlat = { navController.navigate(Routes.CREATE_FLAT) },
                                        onJoinFlat = { navController.navigate(Routes.JOIN_FLAT) },
                                        onOpenFlatSwitcher = { showFlatSwitcher = true }
                                    )
                                    AppTab.DISCOVER -> DiscoverBoardScreen(flatViewModel)
                                    AppTab.TASKS -> ManageTaskScreen(
                                        flatViewModel,
                                        initialTab = manageSubTab,
                                        onOpenGoingAway = { tasksOverlay = "going_away" },
                                        onOpenTaskDetail = { selectedTaskId = it },
                                        onOpenCreateTask = { tasksOverlay = "create_type" },
                                        onReviewSwaps = { showSwapReview = true }
                                    )
                                    AppTab.PROFILE -> ProfileScreen(
                                        user = user,
                                        flatViewModel = flatViewModel,
                                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                                        onOpenMembers = { profileOverlay = "members" },
                                        onOpenManageFlat = { profileOverlay = "manage_flat" },
                                        onOpenActivity = { profileOverlay = "activity" },
                                        onOpenFlatSwitcher = { showFlatSwitcher = true },
                                        onSignOut = {
                                            authRepository.signOut()
                                            navController.navigate(Routes.LOGIN) {
                                                popUpTo(Routes.MAIN) { inclusive = true }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    }

                    FlatSwitcherSheet(
                        viewModel = flatViewModel,
                        visible = showFlatSwitcher,
                        onDismiss = { showFlatSwitcher = false },
                        onCreateFlat = { navController.navigate(Routes.CREATE_FLAT) },
                        onJoinFlat = { navController.navigate(Routes.JOIN_FLAT) }
                    )

                    if (showSwapReview) {
                        SwapReviewSheet(
                            swaps = flatViewModelSwaps,
                            tasks = flatViewModelTasks,
                            members = flatViewModelMembers,
                            uid = flatViewModelUser?.uid.orEmpty(),
                            onAccept = { flatViewModel.respondToSwap(it, true) },
                            onReject = { flatViewModel.respondToSwap(it, false) },
                            onDismiss = { showSwapReview = false }
                        )
                    }

                    PlusActionSheet(
                        visible = showPlusSheet,
                        onDismiss = { showPlusSheet = false },
                        onAddTask = {
                            showPlusSheet = false
                            selectedTab = AppTab.TASKS
                            manageSubTab = "Chores"
                            tasksOverlay = "create_type"
                        },
                        onAddExpense = {
                            showPlusSheet = false
                            selectedTab = AppTab.TASKS
                            manageSubTab = "Money"
                            flatViewModel.showAddExpenseTrigger.value = true
                        },
                        onBillsSettlements = {
                            showPlusSheet = false
                            selectedTab = AppTab.TASKS
                            manageSubTab = "Bills"
                            flatViewModel.showBillsTrigger.value = true
                        },
                        onInviteRoommate = {
                            showPlusSheet = false
                            selectedTab = AppTab.HOME
                        }
                    )
                }
                composable(Routes.CREATE_FLAT) {
                    val createVm = viewModel { CreateFlatViewModel(authRepository, flatsRepository) }
                    val flatVm: FlatViewModel = viewModel(
                        viewModelStoreOwner = navController.getBackStackEntry(Routes.MAIN)
                    ) {
                        FlatViewModel(
                            authRepository, usersRepository, flatsRepository, membersRepository,
                            tasksRepository, expensesRepository, billsRepository, activityRepository,
                            swapRepository, discoveryRepository, messagingRepository
                        )
                    }
                    CreateFlatScreen(
                        viewModel = createVm,
                        onDone = { flatId ->
                            flatVm.onFlatCreated(flatId)
                            navController.popBackStack(Routes.MAIN, false)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.CREATE_FLAT_BRILLIANT) {
                    val createVm = viewModel { CreateFlatViewModel(authRepository, flatsRepository) }
                    val flatVm: FlatViewModel = viewModel(
                        viewModelStoreOwner = navController.getBackStackEntry(Routes.MAIN)
                    ) {
                        FlatViewModel(
                            authRepository, usersRepository, flatsRepository, membersRepository,
                            tasksRepository, expensesRepository, billsRepository, activityRepository,
                            swapRepository, discoveryRepository, messagingRepository
                        )
                    }
                    CreateFlatScreen(
                        viewModel = createVm,
                        brilliantFlow = true,
                        onDone = { flatId ->
                            flatVm.onFlatCreated(flatId)
                            navController.popBackStack(Routes.MAIN, false)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.JOIN_FLAT) {
                    val joinVm = viewModel { JoinFlatViewModel(authRepository, flatsRepository) }
                    val flatVm: FlatViewModel = viewModel(
                        viewModelStoreOwner = navController.getBackStackEntry(Routes.MAIN)
                    ) {
                        FlatViewModel(
                            authRepository, usersRepository, flatsRepository, membersRepository,
                            tasksRepository, expensesRepository, billsRepository, activityRepository,
                            swapRepository, discoveryRepository, messagingRepository
                        )
                    }
                    JoinFlatScreen(
                        viewModel = joinVm,
                        onJoined = { flatId ->
                            flatVm.onFlatJoined(flatId)
                            navController.popBackStack(Routes.MAIN, false)
                        }
                    )
                }
                composable(Routes.SETTINGS) {
                    val viewModel = viewModel { SettingsViewModel(authRepository, usersRepository) }
                    SettingsScreen(
                        user = currentUser,
                        viewModel = viewModel,
                        onSignOut = {
                            authRepository.signOut()
                            navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                        },
                        onAccountDeleted = {
                            navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                        }
                    )
                }
            }
        }
    }
}

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
import habitiq.app.data.DiscoveryRepository
import habitiq.app.data.ExpensesRepository
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
import habitiq.app.ui.DiscoverScreen
import habitiq.app.ui.FigmaHomeScreen
import habitiq.app.ui.JoinFlatScreen
import habitiq.app.ui.LoginScreen
import habitiq.app.ui.ManageTaskScreen
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
    const val JOIN_FLAT = "join_flat"
    const val SETTINGS = "settings"
}

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
                        FlatViewModel(
                            authRepository,
                            usersRepository,
                            flatsRepository,
                            membersRepository,
                            tasksRepository,
                            expensesRepository,
                            activityRepository,
                            swapRepository,
                            discoveryRepository
                        )
                    }

                    val flatId by flatViewModel.flatId.collectAsStateWithLifecycleCompat()
                    val loading by flatViewModel.loading.collectAsStateWithLifecycleCompat()

                    if (!loading && flatId == null) {
                        val name = user.displayName?.split(" ")?.firstOrNull() ?: "there"
                        OnboardingScreen(
                            userName = name,
                            onCreateFlat = { navController.navigate(Routes.CREATE_FLAT) },
                            onJoinFlat = { navController.navigate(Routes.JOIN_FLAT) }
                        )
                        return@composable
                    }

                    var selectedTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
                    var showPlusSheet by remember { mutableStateOf(false) }
                    var manageSubTab by rememberSaveable { mutableStateOf("Chores") }

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
                                onCreateFlat = { navController.navigate(Routes.CREATE_FLAT) },
                                onJoinFlat = { navController.navigate(Routes.JOIN_FLAT) }
                            )
                            AppTab.DISCOVER -> DiscoverScreen(flatViewModel)
                            AppTab.TASKS -> ManageTaskScreen(flatViewModel, initialTab = manageSubTab)
                            AppTab.PROFILE -> ProfileScreen(
                                user = user,
                                flatViewModel = flatViewModel,
                                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                                onSignOut = {
                                    authRepository.signOut()
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo(Routes.MAIN) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }

                    PlusActionSheet(
                        visible = showPlusSheet,
                        onDismiss = { showPlusSheet = false },
                        onAddTask = {
                            showPlusSheet = false
                            selectedTab = AppTab.TASKS
                            manageSubTab = "Chores"
                            flatViewModel.showAddTaskTrigger.value = true
                        },
                        onAddExpense = {
                            showPlusSheet = false
                            selectedTab = AppTab.TASKS
                            manageSubTab = "Money"
                            flatViewModel.showAddExpenseTrigger.value = true
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
                            authRepository,
                            usersRepository,
                            flatsRepository,
                            membersRepository,
                            tasksRepository,
                            expensesRepository,
                            activityRepository,
                            swapRepository,
                            discoveryRepository
                        )
                    }
                    CreateFlatScreen(
                        viewModel = createVm,
                        onDone = { flatId ->
                            flatVm.onFlatCreated(flatId)
                            navController.popBackStack(Routes.MAIN, false)
                        }
                    )
                }
                composable(Routes.JOIN_FLAT) {
                    val joinVm = viewModel { JoinFlatViewModel(authRepository, flatsRepository) }
                    val flatVm: FlatViewModel = viewModel(
                        viewModelStoreOwner = navController.getBackStackEntry(Routes.MAIN)
                    ) {
                        FlatViewModel(
                            authRepository,
                            usersRepository,
                            flatsRepository,
                            membersRepository,
                            tasksRepository,
                            expensesRepository,
                            activityRepository,
                            swapRepository,
                            discoveryRepository
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

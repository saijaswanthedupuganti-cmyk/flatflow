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
import habitiq.app.data.ExpensesRepository
import habitiq.app.data.TasksRepository
import habitiq.app.data.UsersRepository
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
import habitiq.app.ui.PlusActionSheet
import habitiq.app.ui.ProfileScreen
import habitiq.app.ui.SettingsScreen
import habitiq.app.ui.SignupScreen
import habitiq.app.ui.TasksManageScreen
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
    val tasksRepository = remember { TasksRepository() }
    val activityRepository = remember { ActivityRepository() }
    val expensesRepository = remember { ExpensesRepository() }

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

                    var selectedTab by rememberSaveable { mutableStateOf(AppTab.HOME) }
                    var showPlusSheet by remember { mutableStateOf(false) }

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
                            AppTab.DISCOVER -> DiscoverScreen()
                            AppTab.MANAGE -> TasksManageScreen()
                            AppTab.PROFILE -> ProfileScreen(
                                user = user,
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
                            selectedTab = AppTab.MANAGE
                        },
                        onAddExpense = {
                            showPlusSheet = false
                            selectedTab = AppTab.MANAGE
                        },
                        onInviteRoommate = {
                            showPlusSheet = false
                            selectedTab = AppTab.HOME
                        }
                    )
                }
                composable(Routes.CREATE_FLAT) {
                    val viewModel = viewModel { CreateFlatViewModel(authRepository, flatsRepository) }
                    CreateFlatScreen(
                        viewModel = viewModel,
                        onDone = { navController.popBackStack(Routes.MAIN, false) }
                    )
                }
                composable(Routes.JOIN_FLAT) {
                    val viewModel = viewModel { JoinFlatViewModel(authRepository, flatsRepository) }
                    JoinFlatScreen(
                        viewModel = viewModel,
                        onJoined = { navController.popBackStack(Routes.MAIN, false) }
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

package habitiq.app

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import habitiq.app.auth.AuthRepository
import habitiq.app.auth.LoginViewModel
import habitiq.app.auth.SignupViewModel
import habitiq.app.data.UsersRepository
import habitiq.app.flats.CreateFlatViewModel
import habitiq.app.flats.FlatHomeViewModel
import habitiq.app.flats.FlatsRepository
import habitiq.app.flats.HomeViewModel
import habitiq.app.flats.JoinFlatViewModel
import habitiq.app.flats.MembersRepository
import habitiq.app.ui.CreateFlatScreen
import habitiq.app.ui.FlatHomeScreen
import habitiq.app.ui.HomeScreen
import habitiq.app.ui.JoinFlatScreen
import habitiq.app.ui.LoginScreen
import habitiq.app.ui.SignupScreen
import habitiq.app.ui.theme.HabitiqTheme

private object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val CREATE_FLAT = "createFlat"
    const val JOIN_FLAT = "joinFlat"
    const val FLAT_HOME = "flatHome/{flatId}"
    fun flatHome(flatId: String) = "flatHome/$flatId"
}

@Composable
fun HabitiqApp() {
    val authRepository = remember { AuthRepository() }
    val usersRepository = remember { UsersRepository() }
    val flatsRepository = remember { FlatsRepository() }
    val membersRepository = remember { MembersRepository() }
    val navController = rememberNavController()
    val currentUser by authRepository.currentUser.collectAsState()
    // Computed once, not re-derived from the live currentUser flow above: NavHost rebuilds
    // its graph (resetting the back stack) whenever startDestination changes, which would
    // race with the explicit navigate() calls below on every sign-in/sign-out. Session-start
    // routing should happen exactly once; all subsequent transitions go through navigate().
    val startDestination = remember { if (authRepository.currentUser.value != null) Routes.HOME else Routes.LOGIN }

    HabitiqTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(Routes.LOGIN) {
                    val viewModel = remember { LoginViewModel(authRepository, usersRepository) }
                    LoginScreen(
                        viewModel = viewModel,
                        onSignedIn = { navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                        onNavigateToSignup = { navController.navigate(Routes.SIGNUP) }
                    )
                }
                composable(Routes.SIGNUP) {
                    val viewModel = remember { SignupViewModel(authRepository, usersRepository) }
                    SignupScreen(
                        viewModel = viewModel,
                        onSignedUp = { navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } }
                    )
                }
                composable(Routes.HOME) {
                    val homeViewModel = remember { HomeViewModel(authRepository, usersRepository) }
                    HomeScreen(
                        user = currentUser,
                        homeViewModel = homeViewModel,
                        onSignOut = {
                            authRepository.signOut()
                            navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
                        },
                        onCreateFlat = { navController.navigate(Routes.CREATE_FLAT) },
                        onJoinFlat = { navController.navigate(Routes.JOIN_FLAT) },
                        onViewFlat = { flatId -> navController.navigate(Routes.flatHome(flatId)) }
                    )
                }
                composable(Routes.CREATE_FLAT) {
                    val viewModel = remember { CreateFlatViewModel(authRepository, flatsRepository) }
                    CreateFlatScreen(
                        viewModel = viewModel,
                        onDone = { navController.popBackStack() }
                    )
                }
                composable(Routes.JOIN_FLAT) {
                    val viewModel = remember { JoinFlatViewModel(authRepository, flatsRepository) }
                    JoinFlatScreen(
                        viewModel = viewModel,
                        onJoined = { flatId ->
                            navController.navigate(Routes.flatHome(flatId)) {
                                popUpTo(Routes.HOME) { inclusive = false }
                            }
                        }
                    )
                }
                composable(
                    route = Routes.FLAT_HOME,
                    arguments = listOf(navArgument("flatId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val flatId = backStackEntry.arguments?.getString("flatId") ?: return@composable
                    val viewModel = remember(flatId) { FlatHomeViewModel(flatId, flatsRepository, membersRepository) }
                    FlatHomeScreen(viewModel = viewModel, currentUid = currentUser?.uid.orEmpty())
                }
            }
        }
    }
}

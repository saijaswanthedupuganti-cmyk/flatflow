package habitiq.app

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import habitiq.app.auth.AuthRepository
import habitiq.app.auth.LoginViewModel
import habitiq.app.auth.SignupViewModel
import habitiq.app.data.UsersRepository
import habitiq.app.ui.HomeScreen
import habitiq.app.ui.LoginScreen
import habitiq.app.ui.SignupScreen
import habitiq.app.ui.theme.HabitiqTheme

private object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
}

@Composable
fun HabitiqApp() {
    val authRepository = remember { AuthRepository() }
    val usersRepository = remember { UsersRepository() }
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
                    HomeScreen(user = currentUser, onSignOut = {
                        authRepository.signOut()
                        navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
                    })
                }
            }
        }
    }
}

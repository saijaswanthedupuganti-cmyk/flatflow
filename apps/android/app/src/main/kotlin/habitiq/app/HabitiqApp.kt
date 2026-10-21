package habitiq.app

import habitiq.app.discover.DiscoverFlags
import habitiq.app.ui.CreateOption
import habitiq.app.ui.ShellCreate
import habitiq.app.ui.theme.HqLayout
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.compose.animation.togetherWith
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
import habitiq.app.discover.DiscoverMode
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.CreateFlatViewModel
import habitiq.app.flats.FlatsRepository
import habitiq.app.flats.HomeViewModel
import habitiq.app.flats.JoinFlatViewModel
import habitiq.app.flats.MembersRepository
import habitiq.app.flats.launchShareInviteCode
import habitiq.app.home.AdminHomeMode
import habitiq.app.home.HomeDashboardViewModel
import habitiq.app.settings.SettingsViewModel
import habitiq.app.ui.AppShell
import habitiq.app.ui.AppTab
import habitiq.app.ui.CreateFlatScreen
import habitiq.app.ui.BillsScreen
import habitiq.app.ui.DiscoverBoardScreen
import habitiq.app.ui.ExpensesScreen
import habitiq.app.ui.FigmaHomeScreen
import habitiq.app.ui.IntentChooserScreen
import habitiq.app.ui.JoinFlatScreen
import habitiq.app.ui.LoginScreen
import habitiq.app.ui.ActivityLogScreen
import habitiq.app.ui.FlatSwitcherSheet
import habitiq.app.ui.FlatSettingsScreen
import habitiq.app.ui.MembersScreen
import habitiq.app.ui.ManageFlatHub
import habitiq.app.ui.ManageFlatArea
import habitiq.app.ui.OnboardingIntent
import habitiq.app.ui.SwapReviewSheet
import habitiq.app.ui.figma.GoingAwayScreen
import habitiq.app.ui.figma.TaskDetailScreen
import habitiq.app.ui.figma.AddTaskScreen
import habitiq.app.ui.ProfileScreen
import habitiq.app.ui.SettingsScreen
import habitiq.app.ui.SignupScreen
import habitiq.app.ui.WelcomeScreen
import habitiq.app.ui.collectAsStateWithLifecycleCompat
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.theme.HabitiqTheme

private object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val INTENT_CHOOSER = "intent_chooser"
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

    val appContext = LocalContext.current.applicationContext
    val appPreferences = remember { habitiq.app.settings.AppPreferences(appContext) }
    val welcomeScope = rememberCoroutineScope()

    val navController = rememberNavController()
    val currentUser by authRepository.currentUser.collectAsStateWithLifecycleCompat()

    var startupResolved by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf(Routes.WELCOME) }
    var startupError by remember { mutableStateOf<String?>(null) }
    var startupAttempt by remember { mutableStateOf(0) }
    var mainInitialTab by rememberSaveable { mutableStateOf(AppTab.HOME.name) }
    var mainDiscoverModeName by rememberSaveable { mutableStateOf(DiscoverMode.USE_A_FLAT.name) }
    var mainDiscoverCity by rememberSaveable { mutableStateOf("") }
    var mainDiscoverBudget by rememberSaveable { mutableStateOf("") }
    var mainDiscoverPreference by rememberSaveable { mutableStateOf("") }
    var createForDiscover by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(currentUser, startupAttempt) {
        startupError = null
        if (currentUser == null) {
            val seen = runCatching { appPreferences.welcomeSeen.first() }.getOrDefault(false)
            startDestination = if (seen) Routes.LOGIN else Routes.WELCOME
            startupResolved = true
            return@LaunchedEffect
        }
        startupResolved = false
        // Anyone who has signed in on this install is not a first-time user.
        runCatching { appPreferences.setWelcomeSeen() }
        usersRepository.getActiveFlatId(currentUser!!.uid).fold(
            onSuccess = { flatId ->
                startDestination = if (flatId != null) Routes.MAIN else Routes.INTENT_CHOOSER
                startupResolved = true
            },
            onFailure = {
                startupError = "Couldn't load your profile. Try again."
                startupResolved = true
            }
        )
    }

    fun navigateAfterAuth(hasActiveFlat: Boolean) {
        val dest = if (hasActiveFlat) Routes.MAIN else Routes.INTENT_CHOOSER
        navController.navigate(dest) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    fun signOut() {
        authRepository.signOut()
        startupResolved = false
        navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    // Home's photo hero bleeds under the status bar; it flips this on while it is on screen.
    val heroBleed = remember { mutableStateOf(false) }
    // Once per process: rotation or theme changes never replay the intro.
    var introDone by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    val rootFocusManager = androidx.compose.ui.platform.LocalFocusManager.current
    HabitiqTheme {
        // The single status-bar inset for every screen; individual screens must not add their own.
        // Centre and cap the content column on tablets/foldables (design doc 7.2); phones are unaffected.
        Box(Modifier.fillMaxSize().background(LocalHqColors.current.canvas), contentAlignment = Alignment.TopCenter) {
        androidx.compose.runtime.CompositionLocalProvider(habitiq.app.ui.theme.LocalHeroBleed provides heroBleed) {
        Surface(
            modifier = Modifier.widthIn(max = HqLayout.contentMax).fillMaxSize()
                .then(if (heroBleed.value) Modifier else Modifier.statusBarsPadding()).imePadding()
                // Android convention: tapping empty space dismisses the keyboard. Child clickables consume their own taps.
                .pointerInput(Unit) { detectTapGestures { rootFocusManager.clearFocus() } },
            color = LocalHqColors.current.canvas,
        ) {
          habitiq.app.ui.components.HqTopBarHost {
            if (!introDone) {
                // Plays the full brand intro on cold start; it finishes only once startup has resolved.
                habitiq.app.ui.components.HqLoadingScreen(ready = startupResolved, onFinished = { introDone = true })
            } else if (!startupResolved) {
                // A retry after a startup error: a quiet spinner, never a second full intro.
                Box(Modifier.fillMaxSize().background(LocalHqColors.current.canvas), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LocalHqColors.current.actionPrimaryBg)
                }
            } else if (startupError != null) {
                Column(
                    Modifier.fillMaxSize().background(LocalHqColors.current.canvas).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.padding(80.dp))
                    Text(startupError ?: "", color = LocalHqColors.current.textPrimary, style = HqType.bodyLarge)
                    HqTextButton(text = "Try again", onClick = { startupAttempt += 1 })
                }
            } else {
                val t = habitiq.app.ui.theme.rememberHqTransitions()
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    enterTransition = { t.pushEnter },
                    exitTransition = { t.pushExit },
                    popEnterTransition = { t.popEnter },
                    popExitTransition = { t.popExit },
                ) {
                    composable(Routes.WELCOME) {
                        WelcomeScreen(
                            onNext = {
                                welcomeScope.launch { appPreferences.setWelcomeSeen() }
                                navController.navigate(Routes.SIGNUP)
                            },
                            onLogin = {
                                welcomeScope.launch { appPreferences.setWelcomeSeen() }
                                navController.navigate(Routes.LOGIN)
                            }
                        )
                    }
                    composable(Routes.LOGIN) {
                        val viewModel = remember { LoginViewModel(authRepository, usersRepository) }
                        LoginScreen(
                            viewModel = viewModel,
                            onSignedIn = { hasActiveFlat -> navigateAfterAuth(hasActiveFlat) },
                            onNavigateToSignup = { navController.navigate(Routes.SIGNUP) }
                        )
                    }
                    composable(Routes.SIGNUP) {
                        val viewModel = remember { SignupViewModel(authRepository, usersRepository) }
                        SignupScreen(
                            viewModel = viewModel,
                            onSignedUp = { hasActiveFlat -> navigateAfterAuth(hasActiveFlat) },
                            onNavigateToLogin = { navController.popBackStack() }
                        )
                    }
                    composable(Routes.INTENT_CHOOSER) {
                        val user = currentUser
                        if (user == null) {
                            LaunchedEffect(Unit) { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } }
                            return@composable
                        }
                        val name = user.displayName?.split(" ")?.firstOrNull() ?: "there"
                        val profileScope = rememberCoroutineScope()
                        IntentChooserScreen(
                            userName = name,
                            onChoose = { intent ->
                                when (intent) {
                                    OnboardingIntent.MANAGE_FLAT -> {
                                        createForDiscover = false
                                        navController.navigate(Routes.CREATE_FLAT)
                                    }
                                    OnboardingIntent.FIND_FLATMATE, OnboardingIntent.FIND_PERSON -> {
                                        mainInitialTab = AppTab.DISCOVER.name
                                        mainDiscoverModeName = DiscoverMode.FIND_A_PERSON.name
                                        navController.navigate(Routes.MAIN) {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                        }
                                    }
                                    OnboardingIntent.FIND_FLAT -> {
                                        mainInitialTab = AppTab.DISCOVER.name
                                        mainDiscoverModeName = DiscoverMode.USE_A_FLAT.name
                                        navController.navigate(Routes.MAIN) {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                        }
                                    }
                                    OnboardingIntent.JOIN_FLAT -> navController.navigate(Routes.JOIN_FLAT)
                                }
                            },
                            onSignOut = { signOut() },
                            onSaveProfile = { profileName, city, gender, onResult ->
                                profileScope.launch {
                                    usersRepository.updateProfile(user.uid, profileName, city, gender)
                                        .fold(onSuccess = { onResult(true) }, onFailure = { onResult(false) })
                                }
                            },
                            onOpenDiscover = { mode, city, budget, preference ->
                                mainInitialTab = AppTab.DISCOVER.name
                                mainDiscoverModeName = mode.name
                                mainDiscoverCity = city
                                mainDiscoverBudget = budget
                                mainDiscoverPreference = preference
                                navController.navigate(Routes.MAIN) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            },
                            onExploreLater = {
                                navController.navigate(Routes.MAIN) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Routes.MAIN) {
                        val user = currentUser
                        if (user == null) {
                            LaunchedEffect(Unit) { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } }
                            return@composable
                        }

                        val flatViewModel = viewModel {
                            flatViewModelFactory(
                                authRepository, usersRepository, flatsRepository, membersRepository,
                                tasksRepository, expensesRepository, billsRepository, activityRepository,
                                swapRepository, discoveryRepository, messagingRepository
                            )
                        }

                        var selectedTab by rememberSaveable {
                            mutableStateOf(
                                runCatching { AppTab.valueOf(mainInitialTab) }.getOrDefault(AppTab.HOME)
                            )
                        }
                        var discoverMode by rememberSaveable {
                            mutableStateOf(
                                runCatching { DiscoverMode.valueOf(mainDiscoverModeName) }
                                    .getOrDefault(DiscoverMode.USE_A_FLAT)
                            )
                        }
                        var showFlatSwitcher by remember { mutableStateOf(false) }
                        var profileOverlay by rememberSaveable { mutableStateOf<String?>(null) }
                        // Save confirmations ("Task added", "Expense added", ...) outlive the form that closed.
                        val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
                        val snackbarScope = rememberCoroutineScope()
                        val snackbarController = remember { habitiq.app.ui.components.HqSnackbarController(snackbarHostState, snackbarScope) }
                        var adminHomeMode by rememberSaveable { mutableStateOf(AdminHomeMode.TASKS) }
                        var tasksOverlay by rememberSaveable { mutableStateOf<String?>(null) }
                        var selectedTaskId by rememberSaveable { mutableStateOf<String?>(null) }
                        var showSwapReview by remember { mutableStateOf(false) }
                        var showVacancyReview by remember { mutableStateOf(false) }
                        var discoverOpenCreate by remember { mutableStateOf(false) }
                        var discoverCreateType by remember { mutableStateOf<String?>(null) }
                        var discoverOpenPosts by remember { mutableStateOf(false) }
                        // Discover forms and detail screens take the whole screen: no nav pill or + over their bottom button.
                        var discoverImmersive by remember { mutableStateOf(false) }
                        val notificationTarget by habitiq.app.services.NotificationNav.pending.collectAsStateWithLifecycleCompat()
                        LaunchedEffect(notificationTarget) {
                            if (habitiq.app.services.NotificationNav.opensMyPosts(notificationTarget)) {
                                selectedTab = AppTab.DISCOVER
                                discoverOpenPosts = true
                            }
                            if (notificationTarget != null) habitiq.app.services.NotificationNav.pending.value = null
                        }
                        var manageAreaName by rememberSaveable { mutableStateOf(ManageFlatArea.HUB.name) }

                        val homeViewModel = viewModel { HomeViewModel(authRepository, usersRepository) }
                        val dashboardViewModel = viewModel {
                            HomeDashboardViewModel(
                                authRepository, usersRepository, flatsRepository, membersRepository,
                                tasksRepository, activityRepository, expensesRepository
                            )
                        }

                        val flatViewModelTasks by flatViewModel.tasks.collectAsStateWithLifecycleCompat()
                        val shellIsAdmin by flatViewModel.isAdmin.collectAsStateWithLifecycleCompat()
                        val flatViewModelMembers by flatViewModel.members.collectAsStateWithLifecycleCompat()
                        val flatViewModelSwaps by flatViewModel.swapRequests.collectAsStateWithLifecycleCompat()
                        val flatViewModelUser by flatViewModel.currentUser.collectAsStateWithLifecycleCompat()
                        val flatInfo by flatViewModel.flatInfo.collectAsStateWithLifecycleCompat()
                        val triggerBills by flatViewModel.showBillsTrigger.collectAsStateWithLifecycleCompat()
                        val shareContext = LocalContext.current

                        // Add-expense is opened inside ExpensesScreen via showAddExpenseTrigger.
                        // Do not steal it into a full-screen overlay (that hid Manage Flat).
                        // Monthly bills is a scope inside Manage > Expenses, not a separate screen.
                        fun openMonthlyBills() {
                            selectedTab = AppTab.MONEY
                            manageAreaName = ManageFlatArea.EXPENSES.name
                            flatViewModel.expenseScopeMonthly.value = true
                        }
                        val appContext = androidx.compose.ui.platform.LocalContext.current.applicationContext
                        val agentViewModel: habitiq.app.agent.AgentViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                            habitiq.app.agent.AgentViewModel(
                                household = {
                                    habitiq.app.agent.householdStateOf(
                                        myUid = flatViewModel.currentUser.value?.uid.orEmpty(),
                                        flat = flatViewModel.flatInfo.value,
                                        members = flatViewModel.members.value,
                                        tasks = flatViewModel.tasks.value,
                                        // Same all-time personal view as the Expenses and Home screens, so answers never contradict them.
                                        myBalances = habitiq.app.lib.pairwisePersonalBalances(
                                            flatViewModel.expenses.value, flatViewModel.settlements.value,
                                            flatViewModel.currentUser.value?.uid.orEmpty(),
                                        ),
                                        billInstances = flatViewModel.billInstances.value,
                                        vacancies = flatViewModel.vacancies.value,
                                        month = habitiq.app.lib.currentMonthKey(),
                                    )
                                },
                                speech = habitiq.app.agent.AndroidVoiceInput(appContext),
                                actions = habitiq.app.agent.FlatAgentActions(flatViewModel),
                                speaker = habitiq.app.agent.AndroidSpeaker(appContext),
                                // Anything the rules don't understand goes to Gemini via Firebase AI Logic (no key in the app).
                                planner = habitiq.app.agent.GeminiPlanner(appContext),
                                // Play AI-content policy: in-app reporting of Gemini replies, reviewed from Firestore.
                                reportAi = { transcript, reply ->
                                    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                    if (uid != null) {
                                        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("aiReports").add(
                                            mapOf(
                                                "reporterUid" to uid, "request" to transcript, "reply" to reply,
                                                "appVersion" to runCatching {
                                                    appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
                                                }.getOrNull().orEmpty(),
                                                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                            )
                                        ) // queued offline too; nothing to wait for
                                    }
                                },
                                prepareFlatSearch = {
                                    flatViewModel.ensureDiscovery()
                                    // Wait for the first batch of listings, but never hold the answer more than 4s.
                                    kotlinx.coroutines.withTimeoutOrNull(4_000) {
                                        flatViewModel.discoverVacanciesLoading.first { !it }
                                    }
                                },
                            )
                        }
                        // Leaving the app closes the mic and stops speech; nothing listens in the background.
                        val agentLifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current
                        DisposableEffect(agentLifecycle, agentViewModel) {
                            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                                if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) agentViewModel.onBackground()
                            }
                            agentLifecycle.lifecycle.addObserver(observer)
                            onDispose { agentLifecycle.lifecycle.removeObserver(observer) }
                        }
                        val agentState by agentViewModel.state.collectAsStateWithLifecycleCompat()
                        val agentMode by agentViewModel.mode.collectAsStateWithLifecycleCompat()
                        val agentLevel by agentViewModel.level.collectAsStateWithLifecycleCompat()
                        val agentSmartNote by agentViewModel.smartNote.collectAsStateWithLifecycleCompat()
                        var agentOpen by rememberSaveable { mutableStateOf(false) }
                        fun openAgentLink(link: habitiq.app.agent.AgentLink) {
                            agentOpen = false
                            when (link) {
                                habitiq.app.agent.AgentLink.BALANCES -> {
                                    selectedTab = AppTab.MONEY
                                    manageAreaName = ManageFlatArea.EXPENSES.name
                                    flatViewModel.expenseScopeMonthly.value = false
                                    flatViewModel.showBalancesTrigger.value = true
                                }
                                habitiq.app.agent.AgentLink.EXPENSES -> {
                                    selectedTab = AppTab.MONEY
                                    manageAreaName = ManageFlatArea.EXPENSES.name
                                    flatViewModel.expenseScopeMonthly.value = false
                                }
                                habitiq.app.agent.AgentLink.TASKS -> {
                                    selectedTab = AppTab.TASKS
                                    manageAreaName = ManageFlatArea.TASKS.name
                                }
                                habitiq.app.agent.AgentLink.BILLS -> openMonthlyBills()
                                habitiq.app.agent.AgentLink.DISCOVER -> selectedTab = AppTab.DISCOVER
                                habitiq.app.agent.AgentLink.JOIN_FLAT -> navController.navigate(Routes.JOIN_FLAT)
                                habitiq.app.agent.AgentLink.CREATE_FLAT -> navController.navigate(Routes.CREATE_FLAT)
                                habitiq.app.agent.AgentLink.MEMBERS -> profileOverlay = "members"
                                habitiq.app.agent.AgentLink.PROFILE -> selectedTab = AppTab.PROFILE
                                habitiq.app.agent.AgentLink.HOME -> selectedTab = AppTab.HOME
                            }
                        }
                        // "Join a flat", "open expenses": the agent asks, the shell takes the person there.
                        LaunchedEffect(agentViewModel) { agentViewModel.navigation.collect { openAgentLink(it) } }
                        val micPermission = androidx.activity.compose.rememberLauncherForActivityResult(
                            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                        ) { granted ->
                            if (granted) agentViewModel.startListening()
                            else {
                                // Denied with no rationale to show = "Don't ask again": only Settings can turn it back on.
                                val activity = generateSequence(shareContext) { (it as? android.content.ContextWrapper)?.baseContext }
                                    .filterIsInstance<android.app.Activity>().firstOrNull()
                                val permanent = activity != null && !androidx.core.app.ActivityCompat
                                    .shouldShowRequestPermissionRationale(activity, android.Manifest.permission.RECORD_AUDIO)
                                if (permanent) agentViewModel.useTextMode(habitiq.app.agent.MIC_BLOCKED, habitiq.app.agent.MicAction.Settings)
                                else agentViewModel.useTextMode(habitiq.app.agent.MIC_OFF, habitiq.app.agent.MicAction.Ask)
                            }
                        }
                        fun talk() {
                            val granted = androidx.core.content.ContextCompat.checkSelfPermission(appContext, android.Manifest.permission.RECORD_AUDIO) ==
                                android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (granted) agentViewModel.startListening() else micPermission.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                        LaunchedEffect(triggerBills) {
                            if (triggerBills) {
                                adminHomeMode = AdminHomeMode.EXPENSES
                                openMonthlyBills()
                            }
                        }

                        // One back handler for everything layered over the shell, innermost first. Screens
                        // rendered below register their own handlers after this one, so theirs take priority.
                        // Last step: any tab other than Home returns to Home before the app exits.
                        BackHandler(
                            enabled = profileOverlay != null || tasksOverlay != null ||
                                selectedTaskId != null ||
                                selectedTab != AppTab.HOME
                        ) {
                            when {
                                profileOverlay != null -> profileOverlay = null
                                tasksOverlay != null -> tasksOverlay = null
                                selectedTaskId != null -> selectedTaskId = null
                                else -> selectedTab = AppTab.HOME
                            }
                        }

                        // Overlays slide in from the right when going deeper and back out when returning
                        // (Android shared-axis X). The key captures the layer state so the outgoing screen
                        // keeps rendering what it showed while it animates away.
                        val overlayKey = OverlayKey(profileOverlay, tasksOverlay, selectedTaskId)
                        val overlayT = habitiq.app.ui.theme.rememberHqTransitions()
                        androidx.compose.runtime.CompositionLocalProvider(habitiq.app.ui.components.LocalHqSnackbar provides snackbarController) {
                        androidx.compose.animation.AnimatedContent(
                            targetState = overlayKey,
                            transitionSpec = {
                                if (targetState.depth >= initialState.depth) {
                                    overlayT.pushEnter togetherWith overlayT.pushExit
                                } else {
                                    overlayT.popEnter togetherWith overlayT.popExit
                                }
                            },
                            label = "overlay",
                        ) { k ->
                            when (k.profile) {
                                "members" -> MembersScreen(flatViewModel, onBack = { profileOverlay = null })
                                "flat_settings" -> FlatSettingsScreen(flatViewModel, onBack = { profileOverlay = null }, onListRoom = {
                                    profileOverlay = null
                                    selectedTab = AppTab.DISCOVER
                                    discoverCreateType = "VACANCY"; discoverOpenCreate = true
                                })
                                "activity" -> ActivityLogScreen(flatViewModel, onBack = { profileOverlay = null })
                            }

                            if (k.profile == null) {
                                when (k.tasks) {
                                    "going_away" -> GoingAwayScreen(
                                        viewModel = flatViewModel,
                                        onBack = { tasksOverlay = null },
                                        onSent = {
                                            tasksOverlay = null
                                            selectedTab = AppTab.TASKS
                                            manageAreaName = ManageFlatArea.TASKS.name
                                        }
                                    )
                                    // One form for every task; after a save it closes onto Tasks with a snackbar.
                                    "add_task" -> AddTaskScreen(
                                        viewModel = flatViewModel,
                                        onBack = { tasksOverlay = null },
                                        onAdded = {
                                            tasksOverlay = null
                                            selectedTab = AppTab.TASKS
                                            manageAreaName = ManageFlatArea.TASKS.name
                                            snackbarController.show("Task added", actionLabel = "Undo") { flatViewModel.undoLastAddedTask() }
                                        }
                                    )
                                    else -> if (k.taskId != null) {
                                        val task = flatViewModelTasks.find { it.taskId == k.taskId }
                                        if (task != null) {
                                            TaskDetailScreen(
                                                task = task,
                                                members = flatViewModelMembers,
                                                currentUid = flatViewModelUser?.uid.orEmpty(),
                                                onBack = { selectedTaskId = null },
                                                onRequestSwap = { toUid ->
                                                    flatViewModel.createSwapRequest(task.taskId, toUid)
                                                    selectedTaskId = null
                                                },
                                                onComplete = {
                                                    flatViewModel.completeTask(task)
                                                    selectedTaskId = null
                                                },
                                                onOpenAway = {
                                                    selectedTaskId = null
                                                    tasksOverlay = "going_away"
                                                },
                                            )
                                        } else {
                                            selectedTaskId = null
                                        }
                                    } else {
                                        // The plus adds flat things everywhere except Discover, where it posts to
                                        // Discover; mixing both in one menu confused testers (18 Oct). Flat actions
                                        // appear only in a flat; tasks and bills only for admins. The menu reads
                                        // bottom-up from the button, so the most used action (expense) goes last.
                                        val inFlat = flatInfo != null
                                        val onDiscover = selectedTab == AppTab.DISCOVER
                                        val createOptions = buildList {
                                            if (!onDiscover && inFlat && shellIsAdmin) add(CreateOption("Add bill") { openMonthlyBills(); flatViewModel.showAddBillTrigger.value = true })
                                            if (!onDiscover && inFlat && shellIsAdmin) add(CreateOption("Add task") { tasksOverlay = "add_task" })
                                            if (!onDiscover && inFlat) add(CreateOption("Add expense") {
                                                selectedTab = AppTab.MONEY
                                                manageAreaName = ManageFlatArea.EXPENSES.name
                                                flatViewModel.expenseScopeMonthly.value = false
                                                flatViewModel.showAddExpenseTrigger.value = true
                                            })
                                            // Any member can post a vacancy; a member's post waits for admin approval.
                                            if (onDiscover && inFlat) add(CreateOption("List a room") {
                                                selectedTab = AppTab.DISCOVER
                                                discoverCreateType = "VACANCY"; discoverOpenCreate = true
                                            })
                                            if (onDiscover && DiscoverFlags.LOOKING_POSTS) add(CreateOption("I'm looking for a room") {
                                                selectedTab = AppTab.DISCOVER
                                                discoverCreateType = "LOOKING"; discoverOpenCreate = true
                                            })
                                        }
                                        val createAction: ShellCreate = when (createOptions.size) {
                                            0 -> ShellCreate.None
                                            else -> ShellCreate.Menu(createOptions)
                                        }
                                        AppShell(
                                            selectedTab = selectedTab,
                                            chromeHidden = selectedTab == AppTab.DISCOVER && discoverImmersive,
                                            createAction = createAction,
                                            onMic = {
                                                agentOpen = true
                                                flatViewModel.ensureDiscovery() // flat-search answers need vacancies loaded
                                                if (agentMode == habitiq.app.agent.AgentInputMode.Voice) talk()
                                            },
                                            overlayOpen = agentOpen,
                                            overlay = { micCenter ->
                                                habitiq.app.ui.agent.VoiceOverlay(
                                                    state = agentState,
                                                    mode = agentMode,
                                                    level = agentLevel,
                                                    micCenter = micCenter,
                                                    onClose = { agentViewModel.reset(); agentOpen = false },
                                                    onTalk = { talk() },
                                                    onStop = { agentViewModel.stopListening() },
                                                    onSubmit = { agentViewModel.submitText(it) },
                                                    onChoose = { agentViewModel.choose(it) },
                                                    onUseText = { agentViewModel.useTextMode() },
                                                    onUseVoice = { agentViewModel.useVoiceMode(); talk() },
                                                    onAllowMic = { action ->
                                                        if (action == habitiq.app.agent.MicAction.Settings) {
                                                            shareContext.startActivity(
                                                                android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                                    .setData(android.net.Uri.fromParts("package", shareContext.packageName, null))
                                                                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                            )
                                                        } else micPermission.launch(android.Manifest.permission.RECORD_AUDIO)
                                                    },
                                                    onOpen = { link -> agentViewModel.reset(); openAgentLink(link) },
                                                    onConfirm = { agentViewModel.confirm() },
                                                    onCancelPreview = { agentViewModel.cancelPreview() },
                                                    smartNote = agentSmartNote,
                                                    onReport = { agentViewModel.reportReply() },
                                                )
                                            },
                                            onTabSelected = {
                                                selectedTab = it
                                            }
                                        ) {
                                            when (selectedTab) {
                                                AppTab.HOME -> FigmaHomeScreen(
                                                    homeViewModel = homeViewModel,
                                                    dashboardViewModel = dashboardViewModel,
                                                    flatViewModel = flatViewModel,
                                                    adminHomeMode = adminHomeMode,
                                                    onAdminHomeModeChange = { adminHomeMode = it },
                                                    onOpenBills = {
                                                        adminHomeMode = AdminHomeMode.EXPENSES
                                                        openMonthlyBills()
                                                    },
                                                    onStartOnboarding = {
                                                        navController.navigate(Routes.INTENT_CHOOSER) {
                                                            popUpTo(Routes.MAIN) { inclusive = true }
                                                        }
                                                    },
                                                    onOpenFlatSwitcher = { showFlatSwitcher = true },
                                                    onOpenTasks = {
                                                        selectedTab = AppTab.TASKS
                                                        manageAreaName = ManageFlatArea.TASKS.name
                                                    },
                                                    onOpenTaskDetail = { selectedTaskId = it },
                                                    onOpenExpenses = {
                                                        selectedTab = AppTab.MONEY
                                                        manageAreaName = ManageFlatArea.EXPENSES.name
                                                    },
                                                    onReviewJoinRequests = { profileOverlay = "members" },
                                                    onReviewSwapRequests = { showSwapReview = true },
                                                    onOpenDiscover = { selectedTab = AppTab.DISCOVER },
                                                    // No-flat Home: each choice goes straight where it says, no second chooser.
                                                    onCreateFlat = { createForDiscover = false; navController.navigate(Routes.CREATE_FLAT) },
                                                    onJoinFlat = { navController.navigate(Routes.JOIN_FLAT) },
                                                    onFindFlat = { discoverMode = DiscoverMode.USE_A_FLAT; selectedTab = AppTab.DISCOVER },
                                                    onFindFlatmate = { discoverMode = DiscoverMode.FIND_A_PERSON; selectedTab = AppTab.DISCOVER },
                                                    onOpenMembers = { profileOverlay = "members" },
                                                    onOpenActivity = { profileOverlay = "activity" },
                                                    onAddTask = { tasksOverlay = "add_task" },
                                                    onAddExpense = {
                                                        selectedTab = AppTab.MONEY
                                                        manageAreaName = ManageFlatArea.EXPENSES.name
                                                        flatViewModel.expenseScopeMonthly.value = false
                                                        flatViewModel.showAddExpenseTrigger.value = true
                                                    },
                                                    onAddBill = { openMonthlyBills(); flatViewModel.showAddBillTrigger.value = true },
                                                    onOpenProfile = { selectedTab = AppTab.PROFILE },
                                                    onReviewVacancies = { showVacancyReview = true },
                                                )
                                                AppTab.DISCOVER -> DiscoverBoardScreen(
                                                    flatViewModel,
                                                    initialMode = discoverMode,
                                                    initialCity = mainDiscoverCity,
                                                    initialBudget = mainDiscoverBudget,
                                                    initialPreference = mainDiscoverPreference,
                                                    openCreatePost = discoverOpenCreate,
                                                    createPostType = discoverCreateType,
                                                    onCreatePostConsumed = { discoverOpenCreate = false },
                                                    openMyPosts = discoverOpenPosts,
                                                    onMyPostsConsumed = { discoverOpenPosts = false },
                                                    onImmersiveChange = { discoverImmersive = it }
                                                )
                                                AppTab.TASKS, AppTab.MONEY -> ManageFlatHub(
                                                    viewModel = flatViewModel,
                                                    area = if (selectedTab == AppTab.MONEY) ManageFlatArea.EXPENSES else ManageFlatArea.TASKS,
                                                    onAreaChange = {
                                                        manageAreaName = it.name
                                                        selectedTab = if (it == ManageFlatArea.EXPENSES) AppTab.MONEY else AppTab.TASKS
                                                    },
                                                    onOpenGoingAway = { tasksOverlay = "going_away" },
                                                    onOpenTaskDetail = { selectedTaskId = it },
                                                    onOpenCreateTask = { tasksOverlay = "add_task" },
                                                    onReviewSwaps = { showSwapReview = true },
                                                    onOpenBills = { openMonthlyBills() },
                                                    onOpenActivity = { profileOverlay = "activity" }
                                                )
                                                AppTab.PROFILE -> ProfileScreen(
                                                    user = user,
                                                    flatViewModel = flatViewModel,
                                                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                                                    onOpenMembers = { profileOverlay = "members" },
                                                    onOpenFlatSettings = { profileOverlay = "flat_settings" },
                                                    onOpenActivity = { profileOverlay = "activity" },
                                                    onOpenFlatSwitcher = { showFlatSwitcher = true },
                                                    onOpenMyPosts = {
                                                        selectedTab = AppTab.DISCOVER
                                                        discoverOpenPosts = true
                                                    },
                                                    onStartOnboarding = {
                                                        navController.navigate(Routes.INTENT_CHOOSER) {
                                                            popUpTo(Routes.MAIN) { inclusive = true }
                                                        }
                                                    },
                                                    onNoFlatRemaining = {
                                                        homeViewModel.checkFlatStatus()
                                                        dashboardViewModel.load()
                                                        navController.navigate(Routes.INTENT_CHOOSER) {
                                                            popUpTo(Routes.MAIN) { inclusive = true }
                                                        }
                                                    },
                                                    onSignOut = { signOut() }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                        }
                        }
                        androidx.compose.foundation.layout.Box(
                            androidx.compose.ui.Modifier.fillMaxSize().then(androidx.compose.ui.Modifier.navigationBarsPadding())
                                .padding(start = habitiq.app.ui.theme.HqSpacing.lg, end = habitiq.app.ui.theme.HqSpacing.lg, bottom = habitiq.app.ui.theme.HqSize.navigationContent + habitiq.app.ui.theme.HqSpacing.xxl),
                            contentAlignment = androidx.compose.ui.Alignment.BottomCenter,
                        ) { habitiq.app.ui.components.HqSnackbarHost(snackbarHostState) }
                        FlatSwitcherSheet(
                            viewModel = flatViewModel,
                            visible = showFlatSwitcher,
                            onDismiss = { showFlatSwitcher = false },
                            onCreateFlat = { navController.navigate(Routes.CREATE_FLAT) },
                            onJoinFlat = { navController.navigate(Routes.JOIN_FLAT) }
                        )

                        if (showVacancyReview) {
                            val pendingVacancies by flatViewModel.vacancyRequests.collectAsStateWithLifecycleCompat()
                            val reviewFlat by flatViewModel.flatInfo.collectAsStateWithLifecycleCompat()
                            habitiq.app.ui.discover.VacancyReviewSheet(
                                requests = pendingVacancies,
                                onApprove = { r, done -> flatViewModel.approveVacancyRequest(r, done) },
                                onDecline = { r, done -> flatViewModel.declineVacancyRequest(r, done) },
                                onDismiss = { showVacancyReview = false },
                                hasLiveVacancy = reviewFlat?.vacancy?.active == true,
                            )
                        }
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

                    }
                    composable(Routes.CREATE_FLAT) { backStackEntry ->
                        val graphEntry = remember(backStackEntry) { navController.getBackStackEntry(navController.graph.id) }
                        val createVm = viewModel { CreateFlatViewModel(authRepository, flatsRepository, usersRepository) }
                        val flatVm: FlatViewModel = viewModel(viewModelStoreOwner = graphEntry) {
                            flatViewModelFactory(
                                authRepository, usersRepository, flatsRepository, membersRepository,
                                tasksRepository, expensesRepository, billsRepository, activityRepository,
                                swapRepository, discoveryRepository, messagingRepository
                            )
                        }
                        CreateFlatScreen(
                            viewModel = createVm,
                            onDone = { flatId ->
                                flatVm.onFlatCreated(flatId)
                                if (createForDiscover) {
                                    mainInitialTab = AppTab.DISCOVER.name
                                    mainDiscoverModeName = DiscoverMode.FIND_A_PERSON.name
                                } else {
                                    mainInitialTab = AppTab.HOME.name
                                }
                                createForDiscover = false
                                navController.navigate(Routes.MAIN) {
                                    popUpTo(Routes.INTENT_CHOOSER) { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Routes.CREATE_FLAT_BRILLIANT) { backStackEntry ->
                        val graphEntry = remember(backStackEntry) { navController.getBackStackEntry(navController.graph.id) }
                        val createVm = viewModel { CreateFlatViewModel(authRepository, flatsRepository, usersRepository) }
                        val flatVm: FlatViewModel = viewModel(viewModelStoreOwner = graphEntry) {
                            flatViewModelFactory(
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
                                if (createForDiscover) {
                                    mainInitialTab = AppTab.DISCOVER.name
                                    mainDiscoverModeName = DiscoverMode.FIND_A_PERSON.name
                                } else {
                                    mainInitialTab = AppTab.HOME.name
                                }
                                createForDiscover = false
                                navController.navigate(Routes.MAIN) {
                                    popUpTo(Routes.INTENT_CHOOSER) { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Routes.JOIN_FLAT) { backStackEntry ->
                        val graphEntry = remember(backStackEntry) { navController.getBackStackEntry(navController.graph.id) }
                        val joinVm = viewModel { JoinFlatViewModel(authRepository, flatsRepository) }
                        val flatVm: FlatViewModel = viewModel(viewModelStoreOwner = graphEntry) {
                            flatViewModelFactory(
                                authRepository, usersRepository, flatsRepository, membersRepository,
                                tasksRepository, expensesRepository, billsRepository, activityRepository,
                                swapRepository, discoveryRepository, messagingRepository
                            )
                        }
                        JoinFlatScreen(
                            viewModel = joinVm,
                            onJoined = { flatId ->
                                flatVm.onFlatJoined(flatId)
                                navController.navigate(Routes.MAIN) {
                                    popUpTo(Routes.INTENT_CHOOSER) { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Routes.SETTINGS) {
                        val viewModel = viewModel { SettingsViewModel(authRepository, usersRepository) }
                        SettingsScreen(
                            user = currentUser,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onSignOut = { signOut() },
                            onAccountDeleted = { signOut() }
                        )
                    }
                }
            }
          }
        }
        }
        }
    }
}

/** Snapshot of the layers drawn over the shell, used to animate between them. */
private data class OverlayKey(val profile: String?, val tasks: String?, val taskId: String?) {
    val depth: Int get() = listOf(profile, tasks, taskId).count { it != null } +
        (if (tasks == "add_task") 1 else 0)
}

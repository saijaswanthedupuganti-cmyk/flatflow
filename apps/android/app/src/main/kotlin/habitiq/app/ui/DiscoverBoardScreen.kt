package habitiq.app.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import habitiq.app.data.SeekerProfile
import habitiq.app.discover.Compatibility
import habitiq.app.discover.ConnectionStatus
import habitiq.app.discover.DiscoverFilterLogic
import habitiq.app.discover.DiscoverFlags
import habitiq.app.discover.DiscoverMode
import habitiq.app.discover.DiscoverRanking
import habitiq.app.discover.SeekerFilters
import habitiq.app.discover.TrustCopy
import habitiq.app.discover.VacancyFilters
import habitiq.app.flat.FlatViewModel
import habitiq.app.settings.AppPreferences
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.discover.BlockConfirmation
import habitiq.app.ui.discover.ConnectionRequestSheet
import habitiq.app.ui.discover.CreateDiscoveryPostScreen
import habitiq.app.ui.discover.DiscoverFilterSheet
import habitiq.app.ui.discover.DiscoveryEmpty
import habitiq.app.ui.discover.FindFlatmateDiscoverContent
import habitiq.app.ui.discover.FlatListingDetailScreen
import habitiq.app.ui.discover.FlatmateProfileScreen
import habitiq.app.ui.discover.MyPostsScreen
import habitiq.app.ui.discover.ReportSheet
import habitiq.app.ui.discover.TrustConsentDialog
import habitiq.app.ui.discover.UseAFlatDiscoverContent
import habitiq.app.ui.theme.HabitiqTheme
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.HqFadeUp
import habitiq.app.ui.theme.LocalHqColors
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun BiometricLockScreen(activity: FragmentActivity, onUnlocked: () -> Unit) {
    var status by remember { mutableStateOf("Tap to unlock with biometrics") }
    val executor = remember { ContextCompat.getMainExecutor(activity) }

    fun authenticate() {
        val can = BiometricManager.from(activity).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (can != BiometricManager.BIOMETRIC_SUCCESS) {
            status = "Biometrics unavailable — tap to continue"
            onUnlocked()
            return
        }
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onUnlocked() }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { status = errString.toString() }
            override fun onAuthenticationFailed() { status = "Try again" }
        })
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Habitiq Vault")
                .setSubtitle("Verify to access your flat")
                .setNegativeButtonText("Cancel")
                .build()
        )
    }

    // Deliberately its own dark "vault" look regardless of the surrounding (light) app theme --
    // not the light HqColorScheme other Discover screens use.
    HabitiqTheme(dark = true) {
        val c = LocalHqColors.current
        Box(Modifier.fillMaxSize().background(c.background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HqSpacing.lg)) {
                Text("Habitiq is locked", style = HqType.headlineSmall, color = c.textPrimary)
                Text(status, style = HqType.bodyMedium, color = c.textSecondary)
                HqButton(text = "Unlock", onClick = { authenticate() }, fullWidth = false)
            }
        }
    }
    LaunchedEffect(Unit) { authenticate() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverBoardScreen(
    viewModel: FlatViewModel,
    initialMode: DiscoverMode = DiscoverMode.USE_A_FLAT,
    initialCity: String = "",
    initialBudget: String = "",
    initialPreference: String = "",
    initialSurface: String = "entry",
    openCreatePost: Boolean = false,
    onCreatePostConsumed: () -> Unit = {},
    openMyPosts: Boolean = false,
    onMyPostsConsumed: () -> Unit = {},
    onRootBack: (() -> Unit)? = null
) {
    val c = LocalHqColors.current
    LaunchedEffect(Unit) { viewModel.ensureDiscovery() }

    if (!DiscoverFlags.ENABLED) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Discovery is turned off in this build.", style = HqType.bodyMedium, color = c.textSecondary)
        }
        return
    }

    val vacancies by viewModel.vacancies.collectAsStateWithLifecycleCompat()
    val seekers by viewModel.seekerProfiles.collectAsStateWithLifecycleCompat()
    val messages by viewModel.chatMessages.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val vacanciesLoading by viewModel.discoverVacanciesLoading.collectAsStateWithLifecycleCompat()
    val vacanciesError by viewModel.discoverVacanciesError.collectAsStateWithLifecycleCompat()
    val seekersLoading by viewModel.discoverSeekersLoading.collectAsStateWithLifecycleCompat()
    val seekersError by viewModel.discoverSeekersError.collectAsStateWithLifecycleCompat()
    val connections by viewModel.connections.collectAsStateWithLifecycleCompat()
    val blockedIds by viewModel.blockedUserIds.collectAsStateWithLifecycleCompat()
    val connectionLoading by viewModel.connectionActionLoading.collectAsStateWithLifecycleCompat()
    val connectionError by viewModel.connectionActionError.collectAsStateWithLifecycleCompat()
    val discoveryPostLoading by viewModel.discoveryPostLoading.collectAsStateWithLifecycleCompat()
    val discoveryPostError by viewModel.discoveryPostError.collectAsStateWithLifecycleCompat()
    val discoveryUploadProgress by viewModel.discoveryUploadProgress.collectAsStateWithLifecycleCompat()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycleCompat()
    val flatInfo by viewModel.flatInfo.collectAsStateWithLifecycleCompat()

    var mode by remember(initialMode) { mutableStateOf(initialMode) }
    var vacancyFilters by remember(initialCity, initialBudget, initialPreference) {
        mutableStateOf(
            VacancyFilters(
                cityArea = initialCity,
                rentMax = initialBudget,
                roomType = when (initialPreference.lowercase()) {
                    "private" -> "private"
                    "shared" -> "shared"
                    else -> "any"
                }
            )
        )
    }
    var seekerFilters by remember(initialCity, initialBudget, initialPreference) {
        mutableStateOf(
            SeekerFilters(
                cityArea = initialCity,
                budgetMax = initialBudget,
                lifestyleTags = initialPreference.takeIf { it.isNotBlank() && !it.equals("flexible", true) }?.let(::setOf).orEmpty()
            )
        )
    }
    var showFilterSheet by remember { mutableStateOf(false) }
    var surface by rememberSaveable(initialSurface) { mutableStateOf(initialSurface) }
    var chooseQuery by rememberSaveable { mutableStateOf("") }
    var selectedFlatId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedSeekerId by rememberSaveable { mutableStateOf<String?>(null) }
    var chatPartnerId by rememberSaveable { mutableStateOf<String?>(null) }
    var chatContext by rememberSaveable { mutableStateOf("") }
    var connectTargetUid by remember { mutableStateOf<String?>(null) }
    var requestSentName by remember { mutableStateOf<String?>(null) }
    var connectName by remember { mutableStateOf("") }
    var connectContext by remember { mutableStateOf("") }
    var connectFlatId by remember { mutableStateOf<String?>(null) }
    var connectSeekerId by remember { mutableStateOf<String?>(null) }
    var reportTarget by remember { mutableStateOf<Pair<String, String?>?>(null) }
    var blockTarget by remember { mutableStateOf<Pair<String, String>?>(null) }

    val uid = currentUser?.uid.orEmpty()
    val mySeeker = seekers.find { it.id == uid }
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    val trustConsent by prefs.discoveryTrustConsent.collectAsState(initial = false)
    val trustPrompted by prefs.discoveryTrustPrompted.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    LaunchedEffect(openCreatePost) {
        if (openCreatePost) {
            surface = "create"
            onCreatePostConsumed()
        }
    }
    LaunchedEffect(openMyPosts) {
        if (openMyPosts) {
            surface = "posts"
            onMyPostsConsumed()
        }
    }

    val filteredVacancies = remember(vacancies, vacancyFilters, mySeeker) {
        DiscoverRanking.sortVacancies(
            DiscoverFilterLogic.applyVacancyFilters(vacancies, vacancyFilters),
            vacancyFilters,
            mySeeker
        )
    }
    val filteredSeekers = remember(seekers, seekerFilters, uid, blockedIds, flatInfo) {
        DiscoverRanking.sortSeekers(
            DiscoverFilterLogic.applySeekerFilters(seekers, seekerFilters, uid, blockedIds),
            seekerFilters,
            flatInfo?.let { listOfNotNull(it.vacancy?.city, it.vacancy?.area).joinToString(" ") }
        )
    }

    fun statusFor(partnerId: String): ConnectionStatus {
        if (partnerId in blockedIds) return ConnectionStatus.BLOCKED
        val conn = connections.find {
            (it.fromUid == uid && it.toUid == partnerId) || (it.fromUid == partnerId && it.toUid == uid)
        }
        if (conn != null) return conn.status
        val hasChat = messages.any {
            (it.senderId == uid && it.receiverId == partnerId) || (it.senderId == partnerId && it.receiverId == uid)
        }
        return if (hasChat) ConnectionStatus.CONVERSATION_OPEN else ConnectionStatus.NONE
    }

    fun incomingId(partnerId: String): String? {
        return connections.find { it.fromUid == partnerId && it.toUid == uid && it.status == ConnectionStatus.REQUEST_SENT }?.id
    }

    fun canOpenChat(partnerId: String): Boolean {
        return when (statusFor(partnerId)) {
            ConnectionStatus.ACCEPTED, ConnectionStatus.CONVERSATION_OPEN, ConnectionStatus.MATCHED -> true
            else -> false
        }
    }

    if (showFilterSheet) {
        DiscoverFilterSheet(
            mode = mode,
            vacancyFilters = vacancyFilters,
            seekerFilters = seekerFilters,
            onVacancyFiltersChange = { vacancyFilters = it },
            onSeekerFiltersChange = { seekerFilters = it },
            onDismiss = { showFilterSheet = false },
            onApply = { showFilterSheet = false },
            onClear = {
                if (mode == DiscoverMode.USE_A_FLAT) vacancyFilters = VacancyFilters()
                else seekerFilters = SeekerFilters()
            }
        )
    }

    if (connectTargetUid != null) {
        ConnectionRequestSheet(
            toName = connectName,
            contextLine = connectContext,
            sending = connectionLoading,
            error = connectionError,
            onSend = { msg ->
                val sentTo = connectName
                viewModel.sendConnectionRequest(
                    toUid = connectTargetUid!!,
                    message = msg,
                    listingFlatId = connectFlatId,
                    seekerId = connectSeekerId,
                    onResult = { result ->
                        if (result.isSuccess) {
                            connectTargetUid = null
                            requestSentName = sentTo
                        }
                    }
                )
            },
            onDismiss = { connectTargetUid = null; viewModel.clearConnectionActionError() }
        )
    }

    reportTarget?.let { (target, listingId) ->
        ReportSheet(
            title = "Why are you reporting?",
            onSubmit = { viewModel.reportUser(target, it, listingId) },
            onDismiss = { reportTarget = null }
        )
    }
    blockTarget?.let { (id, name) ->
        BlockConfirmation(name, onConfirm = { viewModel.blockUser(id) }, onDismiss = { blockTarget = null })
    }
    requestSentName?.let { name ->
        HqFadeUp(modifier = Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().background(c.background).padding(HqSpacing.xxl),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(92.dp).clip(CircleShape).background(c.successContainer),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.CheckCircle, null, tint = c.success, modifier = Modifier.size(54.dp)) }
                Spacer(Modifier.height(HqSpacing.xl))
                Text("Request sent!", style = HqType.headlineMedium, color = c.textPrimary)
                Text(
                    "You'll be notified when $name responds.",
                    style = HqType.bodyLarge,
                    color = c.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = HqSpacing.sm, bottom = HqSpacing.xl)
                )
                HqButton(text = "View profile", onClick = { requestSentName = null })
                Spacer(Modifier.height(HqSpacing.md))
                HqButton(
                    text = if (connectSeekerId != null) "Back to people" else "Back to Discover",
                    onClick = { requestSentName = null; surface = "browse" },
                    variant = HqButtonVariant.Secondary
                )
            }
        }
        return
    }
    if (!trustPrompted) {
        TrustConsentDialog(
            onAllow = { scope.launch { prefs.setDiscoveryTrustConsent(true) } },
            onNotNow = { scope.launch { prefs.setDiscoveryTrustConsent(false) } }
        )
    }

    when (surface) {
        "create" -> CreateDiscoveryPostScreen(
            isAdmin = isAdmin,
            flat = flatInfo,
            existingVacancy = flatInfo?.vacancy,
            onPublishVacancy = { vacancy, photos ->
                viewModel.publishVacancy(vacancy, photos, context.contentResolver) { surface = "published" }
            },
            onPublishLooking = { city, looking, budget, bio, gender, tags ->
                viewModel.updateSeekerProfile(city, looking, budget, bio, gender, tags, active = true) {
                    surface = "published"
                }
            },
            onBack = { if (initialSurface != "entry" && onRootBack != null) onRootBack() else surface = "entry" },
            publishing = discoveryPostLoading,
            uploadProgress = discoveryUploadProgress?.fraction,
            publishError = discoveryPostError,
            onClearError = viewModel::clearDiscoveryPostError
        )
        "posts" -> MyPostsScreen(
            isAdmin = isAdmin,
            flat = flatInfo,
            vacancy = flatInfo?.vacancy,
            mySeeker = mySeeker,
            incomingCount = connections.count { it.toUid == uid && it.status == ConnectionStatus.REQUEST_SENT },
            onPauseVacancy = {
                val v = flatInfo?.vacancy ?: return@MyPostsScreen
                viewModel.updateVacancy(v.copy(active = false, postStatus = "PAUSED"))
            },
            onResumeVacancy = {
                val v = flatInfo?.vacancy ?: return@MyPostsScreen
                viewModel.updateVacancy(v.copy(active = true, postStatus = "PUBLISHED"))
            },
            onCloseVacancy = {
                val v = flatInfo?.vacancy ?: return@MyPostsScreen
                viewModel.updateVacancy(v.copy(active = false, postStatus = "CLOSED"))
            },
            onPauseLooking = { viewModel.setSeekerActive(false) },
            onResumeLooking = { viewModel.setSeekerActive(true) },
            onEdit = { surface = "create" },
            onViewConnections = { surface = "inbox" },
            onBack = { if (initialSurface != "entry" && onRootBack != null) onRootBack() else surface = "entry" }
        )
        "inbox" -> ConnectionInboxScreen(
            uid = uid,
            connections = connections,
            seekers = seekers,
            vacancies = vacancies,
            onOpenChat = { partner, label ->
                if (canOpenChat(partner)) {
                    chatPartnerId = partner
                    chatContext = label
                    surface = "chat"
                }
            },
            onAccept = { viewModel.respondToConnection(it, true) },
            onDecline = { viewModel.respondToConnection(it, false) },
            onBack = { if (initialSurface != "entry" && onRootBack != null) onRootBack() else surface = "entry" }
        )
        "flat" -> {
            val listing = vacancies.find { it.flatId == selectedFlatId }
            if (listing == null) {
                surface = "browse"
            } else {
                val partner = listing.adminUid
                FlatListingDetailScreen(
                    listing = listing,
                    trust = TrustCopy.forVacancy(listing.memberCount, trustConsent),
                    signals = Compatibility.vacancySignals(listing, mySeeker, vacancyFilters),
                    connectionStatus = statusFor(partner),
                    incomingRequestId = incomingId(partner),
                    blocked = partner in blockedIds,
                    isOwnListing = partner == uid,
                    interestedCount = connections.count {
                        it.listingFlatId == listing.flatId &&
                            it.status != ConnectionStatus.DECLINED &&
                            it.status != ConnectionStatus.BLOCKED
                    },
                    interestedPeople = if (partner == uid) {
                        connections.filter {
                            it.listingFlatId == listing.flatId &&
                                it.toUid == uid &&
                                it.status != ConnectionStatus.DECLINED
                        }.map { conn ->
                            val name = seekers.find { it.id == conn.fromUid }?.displayName?.ifBlank { null }
                                ?: "Habitiq user"
                            conn.fromUid to name
                        }
                    } else emptyList(),
                    onOpenInterested = { personId ->
                        selectedSeekerId = personId
                        surface = "person"
                    },
                    onBack = { surface = "browse" },
                    onConnect = {
                        connectTargetUid = partner
                        connectName = listing.flatName
                        connectContext = "${listing.area} · ${listing.city}".trim(' ', '·')
                        connectFlatId = listing.flatId
                        connectSeekerId = null
                    },
                    onMessage = {
                        chatPartnerId = partner
                        chatContext = listing.flatName
                        surface = "chat"
                    },
                    onAccept = { viewModel.respondToConnection(it, true) },
                    onDecline = { viewModel.respondToConnection(it, false) },
                    onReport = { reportTarget = partner to listing.flatId },
                    onBlock = { blockTarget = partner to listing.flatName }
                )
            }
        }
        "person" -> {
            val seeker = seekers.find { it.id == selectedSeekerId }
            if (seeker == null) {
                surface = "browse"
            } else {
                FlatmateProfileScreen(
                    seeker = seeker,
                    trust = TrustCopy.forSeeker(trustConsent),
                    signals = Compatibility.seekerSignals(seeker, seekerFilters, flatInfo?.vacancy?.city).map { it.label },
                    connectionStatus = statusFor(seeker.id),
                    incomingRequestId = incomingId(seeker.id),
                    blocked = seeker.id in blockedIds,
                    isSelf = seeker.id == uid,
                    onBack = { surface = "browse" },
                    onConnect = {
                        connectTargetUid = seeker.id
                        connectName = seeker.displayName.ifBlank { "this person" }
                        connectContext = seeker.lookingIn.ifBlank { seeker.city }
                        connectFlatId = null
                        connectSeekerId = seeker.id
                    },
                    onMessage = {
                        chatPartnerId = seeker.id
                        chatContext = seeker.lookingIn.ifBlank { "Looking for a flat" }
                        surface = "chat"
                    },
                    onAccept = { viewModel.respondToConnection(it, true) },
                    onDecline = { viewModel.respondToConnection(it, false) },
                    onReport = { reportTarget = seeker.id to null },
                    onBlock = { blockTarget = seeker.id to seeker.displayName.ifBlank { "this person" } }
                )
            }
        }
        "chat" -> {
            val partner = chatPartnerId
            if (partner == null) surface = "browse"
            else ChatScreen(
                viewModel = viewModel,
                partnerId = partner,
                contextLabel = chatContext,
                onBack = { surface = if (initialSurface == "inbox") "inbox" else "browse"; chatPartnerId = null },
                onReport = { reportTarget = partner to null },
                onBlock = {
                    blockTarget = partner to chatContext.ifBlank { "this person" }
                }
            )
        }
        "entry" -> DiscoverEntry(
            isAdmin = isAdmin,
            onFindPlace = { mode = DiscoverMode.USE_A_FLAT; surface = "choose" },
            onFindPerson = { mode = DiscoverMode.FIND_A_PERSON; surface = "choose" },
            onLocation = { city ->
                mode = DiscoverMode.USE_A_FLAT
                vacancyFilters = vacancyFilters.copy(cityArea = city)
                surface = "browse"
            },
            onPost = { surface = "create" },
            onPosts = { surface = "posts" },
            onInbox = { surface = "inbox" }
        )
        "choose" -> DiscoverChoose(
            findingPerson = mode == DiscoverMode.FIND_A_PERSON,
            query = chooseQuery,
            onQuery = { chooseQuery = it },
            onBack = { surface = "entry" },
            onContinue = {
                if (mode == DiscoverMode.FIND_A_PERSON) {
                    seekerFilters = seekerFilters.copy(cityArea = chooseQuery.trim())
                } else {
                    vacancyFilters = vacancyFilters.copy(cityArea = chooseQuery.trim())
                }
                surface = "browse"
            }
        )
        "published" -> Column(
            Modifier.fillMaxSize().background(c.background).padding(HqSpacing.xxl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(104.dp).clip(CircleShape).background(c.successContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = c.success, modifier = Modifier.size(62.dp))
            }
            Spacer(Modifier.height(HqSpacing.xl))
            Text("Your listing is live!", style = HqType.headlineMedium, color = c.textPrimary)
            Text(
                "People looking for a flatmate can now discover your home and send a request.",
                style = HqType.bodyLarge,
                color = c.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = HqSpacing.lg)
            )
            HqButton(text = "Manage post", onClick = { surface = "posts" })
            Spacer(Modifier.height(HqSpacing.md))
            HqButton(text = "Back to Discover", onClick = { surface = "entry" }, variant = HqButtonVariant.Secondary)
        }
        else -> Column(Modifier.fillMaxSize().background(c.background)) {
            DiscoverHeader(
                onBack = { surface = "entry" },
                onPost = { surface = "create" },
                onMyPosts = { surface = "posts" },
                onInbox = { surface = "inbox" },
                inboxCount = connections.count { it.toUid == uid && it.status == ConnectionStatus.REQUEST_SENT }
            )
            Column(Modifier.padding(horizontal = HqSpacing.lg)) {
                DiscoverModeToggle(mode, DiscoverFlags.FIND_FLATMATE) { mode = it }
                Spacer(Modifier.height(HqSpacing.xs))
                Text(
                    when (mode) {
                        DiscoverMode.USE_A_FLAT -> "Find a place to live — flats, PGs and open rooms."
                        DiscoverMode.FIND_A_PERSON -> "Find people who may fit a household. Not the same search as flats."
                    },
                    style = HqType.bodySmall,
                    color = c.textSecondary
                )
            }
            Spacer(Modifier.height(HqSpacing.sm))
            when (mode) {
                DiscoverMode.USE_A_FLAT -> UseAFlatDiscoverContent(
                    vacancies = vacancies,
                    filteredVacancies = filteredVacancies,
                    filters = vacancyFilters,
                    onFiltersChange = { vacancyFilters = it },
                    isLoading = vacanciesLoading,
                    loadError = vacanciesError,
                    onRetry = viewModel::retryDiscovery,
                    onOpenFilters = { showFilterSheet = true },
                    onOpenListing = {
                        selectedFlatId = it.flatId
                        surface = "flat"
                    }
                )
                DiscoverMode.FIND_A_PERSON -> {
                    if (!DiscoverFlags.FIND_FLATMATE) {
                        FindPersonStub()
                    } else {
                        FindFlatmateDiscoverContent(
                            seekers = seekers.filter { it.id != uid },
                            filtered = filteredSeekers,
                            filters = seekerFilters,
                            onFiltersChange = { seekerFilters = it },
                            isLoading = seekersLoading,
                            loadError = seekersError,
                            onRetry = viewModel::retryDiscovery,
                            viewerCity = flatInfo?.vacancy?.city,
                            trustConsent = trustConsent,
                            onOpenFilters = { showFilterSheet = true },
                            onOpenProfile = {
                                selectedSeekerId = it.id
                                surface = "person"
                            },
                            onConnect = { seeker ->
                                connectTargetUid = seeker.id
                                connectName = seeker.displayName.ifBlank { "this person" }
                                connectContext = seeker.lookingIn.ifBlank { seeker.city }
                                connectFlatId = null
                                connectSeekerId = seeker.id
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverEntry(
    isAdmin: Boolean,
    onFindPlace: () -> Unit,
    onFindPerson: () -> Unit,
    onLocation: (String) -> Unit,
    onPost: () -> Unit,
    onPosts: () -> Unit,
    onInbox: () -> Unit
) {
    val c = LocalHqColors.current
    Column(Modifier.fillMaxSize().background(c.background).verticalScroll(rememberScrollState()).padding(HqSpacing.xl)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            HqTextButton(text = "Posts", onClick = onPosts)
            HqTextButton(text = "Inbox", onClick = onInbox)
        }
        Text("Discover", style = HqType.headlineLarge, color = c.textPrimary)
        Text("Find your next place or the right people to live with.", style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(bottom = HqSpacing.lg))
        DiscoverChoiceCard(Icons.Filled.Home, "Find a place", "Flats and rooms with space.") { onFindPlace() }
        Spacer(Modifier.height(HqSpacing.md))
        DiscoverChoiceCard(Icons.Filled.Person, "Find a person", "Flatmates and households.") { onFindPerson() }
        Spacer(Modifier.height(HqSpacing.xl))
        Text("Popular locations", style = HqType.titleSmall, color = c.textPrimary)
        Column(Modifier.padding(top = HqSpacing.sm), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            listOf("Hyderabad", "Gachibowli", "Hitech City", "Kondapur").chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    row.forEach { city ->
                        HqChip(label = city, selected = false, onClick = { onLocation(city) })
                    }
                }
            }
        }
        if (isAdmin) {
            Spacer(Modifier.height(HqSpacing.xl))
            DiscoverChoiceCard(Icons.Filled.Add, "Post a vacancy", "List a room in your flat.") { onPost() }
        }
    }
}

@Composable
private fun DiscoverChoiceCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val c = LocalHqColors.current
    HqCard(variant = HqCardVariant.Interactive, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
            Icon(icon, null, tint = c.brandPrimary)
            Column {
                Text(title, style = HqType.titleMedium, color = c.textPrimary)
                Text(subtitle, style = HqType.bodySmall, color = c.textSecondary)
            }
        }
    }
}

@Composable
private fun DiscoverChoose(
    findingPerson: Boolean,
    query: String,
    onQuery: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val c = LocalHqColors.current
    Column(Modifier.fillMaxSize().background(c.background)) {
        HqBackAppBar(title = if (findingPerson) "Find a person" else "Find a place", onBack = onBack)
        Column(Modifier.padding(HqSpacing.xl), verticalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
            Text(
                if (findingPerson) "Search by location or lifestyle." else "Search flats, rooms and landmarks.",
                style = HqType.bodyMedium,
                color = c.textSecondary
            )
            HqTextField(value = query, onValueChange = onQuery, label = "Search", placeholder = "City, area or landmark")
            Text("Recent searches", style = HqType.titleSmall, color = c.textPrimary)
            listOf("2 BHK", "Private room", "Hyderabad").forEach { hint ->
                HqChip(label = hint, selected = query == hint, onClick = { onQuery(hint) })
            }
            Spacer(Modifier.height(HqSpacing.lg))
            HqButton(text = "Continue", onClick = onContinue)
        }
    }
}

@Composable
private fun DiscoverHeader(onBack: () -> Unit, onPost: () -> Unit, onMyPosts: () -> Unit, onInbox: () -> Unit, inboxCount: Int) {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth().padding(start = HqSpacing.xl, end = HqSpacing.md, top = HqSpacing.xl, bottom = HqSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HqTextButton(text = "←", onClick = onBack)
        Column(Modifier.weight(1f)) {
            Text("Discover", style = HqType.headlineLarge, color = c.textPrimary)
            Text("Find your next place or the right people to live with", style = HqType.bodyMedium, color = c.textSecondary)
        }
        HqTextButton(text = "Post", onClick = onPost)
        HqTextButton(text = "Mine", onClick = onMyPosts)
        BadgedBox(badge = { if (inboxCount > 0) Badge { Text(inboxCount.toString()) } }) {
            HqTextButton(text = "Inbox", onClick = onInbox)
        }
    }
}

@Composable
private fun FindPersonStub() {
    val c = LocalHqColors.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HqSpacing.md),
            modifier = Modifier.padding(HqSpacing.xxxl)
        ) {
            Text("Find a flatmate", style = HqType.headlineSmall, color = c.textPrimary, textAlign = TextAlign.Center)
            Text("This side of Discovery is feature-flagged off in this build.", style = HqType.bodyMedium, color = c.textSecondary, textAlign = TextAlign.Center)
        }
    }
}

/** No Hq segmented-control component exists yet -- kept custom, restyled with tokens. */
@Composable
private fun DiscoverModeToggle(mode: DiscoverMode, findPersonEnabled: Boolean, onModeChange: (DiscoverMode) -> Unit) {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(HqRadius.lg)).background(c.surfaceSubtle).padding(HqSpacing.xs)
    ) {
        DiscoverModeChip(
            title = "Find Flat",
            subtitle = "Places",
            selected = mode == DiscoverMode.USE_A_FLAT,
            onClick = { onModeChange(DiscoverMode.USE_A_FLAT) },
            modifier = Modifier.weight(1f)
        )
        DiscoverModeChip(
            title = "Find Flatmate",
            subtitle = if (findPersonEnabled) "People" else "Soon",
            selected = mode == DiscoverMode.FIND_A_PERSON,
            onClick = { onModeChange(DiscoverMode.FIND_A_PERSON) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DiscoverModeChip(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalHqColors.current
    val bg = if (selected) c.brandPrimary else Color.Transparent
    Column(
        modifier
            .clip(RoundedCornerShape(HqRadius.md))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = HqSpacing.sm, horizontal = HqSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = HqType.labelMedium, color = if (selected) c.onBrandPrimary else c.textPrimary)
        Text(subtitle, style = HqType.caption, color = if (selected) c.onBrandPrimary.copy(alpha = 0.8f) else c.textTertiary)
    }
}

@Composable
private fun ConnectionInboxScreen(
    uid: String,
    connections: List<habitiq.app.discover.DiscoveryConnection>,
    seekers: List<SeekerProfile>,
    vacancies: List<habitiq.app.data.VacancyListing>,
    onOpenChat: (String, String) -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onBack: () -> Unit
) {
    val c = LocalHqColors.current
    var filter by rememberSaveable { mutableStateOf("all") }
    fun label(partner: String): String {
        return vacancies.find { it.adminUid == partner }?.flatName
            ?: seekers.find { it.id == partner }?.displayName?.ifBlank { null }
            ?: "Connection"
    }
    Column(Modifier.fillMaxSize().background(c.background)) {
        HqTextButton(text = "← Back", onClick = onBack, modifier = Modifier.padding(HqSpacing.sm))
        Text("Interested people", style = HqType.headlineMedium, color = c.textPrimary, modifier = Modifier.padding(horizontal = HqSpacing.xl))
        Text("Review requests before you open a conversation.", style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(horizontal = HqSpacing.xl, vertical = HqSpacing.xs))
        val visible = connections.filter {
            when (filter) {
                "new" -> it.toUid == uid && it.status == ConnectionStatus.REQUEST_SENT
                "accepted" -> it.status == ConnectionStatus.ACCEPTED || it.status == ConnectionStatus.CONVERSATION_OPEN
                else -> true
            }
        }
        Row(Modifier.padding(horizontal = HqSpacing.xl, vertical = HqSpacing.sm), horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            HqChip("All (${connections.size})", selected = filter == "all", onClick = { filter = "all" })
            HqChip("New (${connections.count { it.toUid == uid && it.status == ConnectionStatus.REQUEST_SENT }})", selected = filter == "new", onClick = { filter = "new" })
            HqChip("Accepted", selected = filter == "accepted", onClick = { filter = "accepted" })
        }
        if (visible.isEmpty()) {
            DiscoveryEmpty(
                title = if (filter == "new") "No new requests" else "Nothing here yet",
                body = "Interested people will appear here when they contact you."
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(HqSpacing.lg), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                items(visible, key = { it.id }) { conn ->
                    val partner = conn.partnerId(uid)
                    HqCard {
                        Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                            Text(label(partner), style = HqType.titleMedium, color = c.textPrimary)
                            Text(conn.status.name.replace('_', ' ').lowercase(), style = HqType.caption, color = c.textTertiary)
                            if (conn.message.isNotBlank()) Text(conn.message, style = HqType.bodySmall, color = c.textSecondary)
                            if (conn.toUid == uid && conn.status == ConnectionStatus.REQUEST_SENT) {
                                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                                    HqButton(text = "Accept", onClick = { onAccept(conn.id) }, fullWidth = false)
                                    HqButton(text = "Decline", onClick = { onDecline(conn.id) }, variant = HqButtonVariant.Secondary, fullWidth = false)
                                }
                            } else if (conn.status == ConnectionStatus.ACCEPTED || conn.status == ConnectionStatus.CONVERSATION_OPEN) {
                                HqTextButton(text = "Message", onClick = { onOpenChat(partner, label(partner)) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(
    viewModel: FlatViewModel,
    partnerId: String,
    contextLabel: String = "",
    onBack: () -> Unit,
    onReport: () -> Unit = {},
    onBlock: () -> Unit = {}
) {
    val c = LocalHqColors.current
    val messages by viewModel.observeConversation(partnerId).collectAsStateWithLifecycle(initialValue = emptyList())
    val seekers by viewModel.seekerProfiles.collectAsStateWithLifecycleCompat()
    val vacancies by viewModel.vacancies.collectAsStateWithLifecycleCompat()
    val uid by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    var text by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var showSchedule by rememberSaveable { mutableStateOf(false) }
    val partnerName = vacancies.find { it.adminUid == partnerId }?.flatName
        ?: seekers.find { it.id == partnerId }?.displayName?.takeIf { it.isNotBlank() }
        ?: "Habitiq user"
    val threadLabel = contextLabel.ifBlank {
        vacancies.find { it.adminUid == partnerId }?.let { "${it.area} · ${it.city}".trim(' ', '·') }.orEmpty()
    }
    val isFlatConversation = vacancies.any { it.adminUid == partnerId }

    if (showSchedule) {
        ScheduleViewingScreen(
            listingName = partnerName,
            onBack = { showSchedule = false },
            onConfirm = { timestamp, note ->
                viewModel.sendViewingRequest(partnerId, timestamp, note) { sent ->
                    if (sent) showSchedule = false
                }
            }
        )
        return
    }

    Column(Modifier.fillMaxSize().background(c.background)) {
        HqBackAppBar(
            title = partnerName,
            onBack = onBack,
            actions = {
                HqTextButton(text = "Report", onClick = onReport)
                Spacer(Modifier.width(HqSpacing.sm))
                HqTextButton(text = "Block", onClick = onBlock)
            }
        )
        if (threadLabel.isNotBlank()) {
            Text(threadLabel, style = HqType.bodySmall, color = c.textSecondary, modifier = Modifier.padding(horizontal = HqSpacing.lg))
        }
        if (isFlatConversation) {
            HqButton(
                text = "Schedule a viewing",
                onClick = { showSchedule = true },
                variant = HqButtonVariant.Secondary,
                fullWidth = false,
                modifier = Modifier.padding(horizontal = HqSpacing.lg, vertical = HqSpacing.sm)
            )
        }
        Surface(Modifier.fillMaxWidth().padding(horizontal = HqSpacing.lg, vertical = HqSpacing.sm), color = c.warningContainer, shape = RoundedCornerShape(HqRadius.md)) {
            Row(Modifier.padding(HqSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = c.warning, modifier = Modifier.size(HqIconSize.sm))
                Spacer(Modifier.width(HqSpacing.sm))
                Text("Stay safe. Don’t send money before viewing. Never share sensitive personal information.", style = HqType.caption, color = c.textPrimary)
            }
        }
        Spacer(Modifier.height(HqSpacing.sm))
        LazyColumn(
            Modifier.weight(1f).padding(horizontal = HqSpacing.lg),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)
        ) {
            items(messages.reversed(), key = { it.id }) { msg ->
                val mine = msg.senderId == uid?.uid
                Box(Modifier.fillMaxWidth(), contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart) {
                    Surface(color = if (mine) c.brandPrimary else c.surface, shape = RoundedCornerShape(HqRadius.md)) {
                        if (msg.isViewingRequest && msg.viewingTime != null) {
                            Column(Modifier.padding(HqSpacing.md), verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                                    Icon(Icons.Filled.CalendarMonth, null, tint = if (mine) c.onBrandPrimary else c.brandPrimary)
                                    Text("Viewing request", style = HqType.titleSmall, color = if (mine) c.onBrandPrimary else c.textPrimary)
                                }
                                Text(
                                    java.time.Instant.ofEpochMilli(msg.viewingTime).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("EEE, d MMM · h:mm a")),
                                    style = HqType.labelLarge,
                                    color = if (mine) c.onBrandPrimary else c.brandPrimary
                                )
                                Text(msg.content, color = if (mine) c.onBrandPrimary else c.textSecondary, style = HqType.bodySmall)
                            }
                        } else {
                            Text(msg.content, Modifier.padding(HqSpacing.sm), color = if (mine) c.onBrandPrimary else c.textPrimary, style = HqType.bodyMedium)
                        }
                    }
                }
            }
        }
        Row(Modifier.padding(HqSpacing.lg).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            HqTextField(
                value = text,
                onValueChange = { text = it },
                label = "Message",
                placeholder = "Message…",
                modifier = Modifier.weight(1f)
            )
            HqButton(
                text = if (sending) "…" else "Send",
                enabled = text.isNotBlank() && !sending,
                fullWidth = false,
                onClick = {
                    if (text.isNotBlank()) {
                        sending = true
                        viewModel.sendMessage(partnerId, text) { sent ->
                            if (sent) text = ""
                            sending = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ScheduleViewingScreen(
    listingName: String,
    onBack: () -> Unit,
    onConfirm: (Long, String) -> Unit
) {
    val c = LocalHqColors.current
    val dates = remember { (1L..4L).map { LocalDate.now().plusDays(it) } }
    val times = remember { listOf("10:00", "11:00", "12:00", "14:00", "16:00") }
    var selectedDate by rememberSaveable { mutableStateOf(dates.first().toString()) }
    var selectedTime by rememberSaveable { mutableStateOf("11:00") }
    var note by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(c.background)) {
        HqBackAppBar(title = "Schedule a viewing", onBack = onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(HqSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.xl)
        ) {
            HqCard {
                Text(listingName, style = HqType.titleLarge, color = c.textPrimary)
                Text("Choose a time to visit this home.", style = HqType.bodySmall, color = c.textSecondary)
            }
            Text("Select date", style = HqType.titleMedium, color = c.textPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                dates.forEach { date ->
                    HqChip(
                        label = date.format(DateTimeFormatter.ofPattern("EEE\nd MMM")),
                        selected = selectedDate == date.toString(),
                        onClick = { selectedDate = date.toString() }
                    )
                }
            }
            Text("Select time", style = HqType.titleMedium, color = c.textPrimary)
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                items(times) { time ->
                    HqChip(label = LocalTime.parse(time).format(DateTimeFormatter.ofPattern("h:mm a")), selected = selectedTime == time, onClick = { selectedTime = time })
                }
            }
            HqTextField(
                value = note,
                onValueChange = { note = it.take(300) },
                label = "Message (optional)",
                placeholder = "Looking forward to the visit!",
                leadingIcon = Icons.Filled.Schedule,
                singleLine = false,
                minLines = 3
            )
            HqButton(
                text = "Confirm viewing",
                onClick = {
                    val timestamp = LocalDate.parse(selectedDate).atTime(LocalTime.parse(selectedTime))
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    onConfirm(timestamp, note)
                }
            )
        }
    }
}

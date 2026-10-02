package habitiq.app.ui

import androidx.compose.foundation.layout.Box
import habitiq.app.ui.theme.HqSize
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.activity.compose.BackHandler
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
                .setTitle("Oddroof Vault")
                .setSubtitle("Verify to access your flat")
                .setNegativeButtonText("Cancel")
                .build()
        )
    }

    // The lock screen renders outside HabitiqApp, so it resolves the system theme itself.
    HabitiqTheme {
        val c = LocalHqColors.current
        Box(Modifier.fillMaxSize().background(c.canvas), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HqSpacing.lg)) {
                Text("Oddroof is locked", style = HqType.headlineSmall, color = c.textPrimary)
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
    initialSurface: String = "browse",
    openCreatePost: Boolean = false,
    createPostType: String? = null,
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
    val savedIds by prefs.savedFlats(uid).collectAsState(initial = emptySet())
    val savedScope = androidx.compose.runtime.rememberCoroutineScope()
    var flatFrom by rememberSaveable { mutableStateOf("browse") }
    // Where the person profile / chat were opened from, so back returns there instead of browse.
    var personFrom by rememberSaveable { mutableStateOf("browse") }
    var chatFrom by rememberSaveable { mutableStateOf("browse") }
    val trustPrompted by prefs.discoveryTrustPrompted.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    // System back / swipe-back steps out of whichever Discover screen is open, exactly like the
    // on-screen back arrows. Declared before the screens it covers so their own handlers win.
    BackHandler(enabled = requestSentName != null || surface != "browse") {
        when {
            requestSentName != null -> requestSentName = null
            surface == "create" || surface == "posts" || surface == "inbox" ->
                if (initialSurface != "browse" && onRootBack != null) onRootBack() else surface = "browse"
            surface == "flat" -> { surface = flatFrom; flatFrom = "browse" }
            surface == "person" -> { surface = personFrom; personFrom = "browse" }
            surface == "saved" || surface == "map" -> surface = "browse"
            surface == "chat" -> { surface = chatFrom; chatFrom = "browse"; chatPartnerId = null }
            surface == "choose" && initialSurface == "entry" -> surface = "entry"
            else -> surface = "browse"
        }
    }

    var createInitialType by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(openCreatePost) {
        if (openCreatePost) {
            createInitialType = createPostType
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
                Modifier.fillMaxSize().background(c.canvas).padding(HqSpacing.xxl),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(92.dp).clip(CircleShape).background(c.statusSuccessBg),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.CheckCircle, null, tint = c.statusSuccessFg, modifier = Modifier.size(54.dp)) }
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
            initialType = createInitialType,
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
            onBack = { if (initialSurface != "browse" && onRootBack != null) onRootBack() else surface = "browse" },
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
            onBack = { if (initialSurface != "browse" && onRootBack != null) onRootBack() else surface = "browse" }
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
                    chatFrom = "inbox"
                    surface = "chat"
                }
            },
            onAccept = { viewModel.respondToConnection(it, true) },
            onDecline = { viewModel.respondToConnection(it, false) },
            onBack = { if (initialSurface != "browse" && onRootBack != null) onRootBack() else surface = "browse" }
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
                                ?: "Oddroof user"
                            conn.fromUid to name
                        }
                    } else emptyList(),
                    onOpenInterested = { personId ->
                        selectedSeekerId = personId
                        personFrom = "flat"
                        surface = "person"
                    },
                    onBack = { surface = flatFrom; flatFrom = "browse" },
                    saved = listing.flatId in savedIds,
                    onToggleSave = {
                        savedScope.launch { prefs.setFlatSaved(uid, listing.flatId, listing.flatId !in savedIds) }
                    },
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
                        chatFrom = "flat"
                        surface = "chat"
                    },
                    onAccept = { viewModel.respondToConnection(it, true) },
                    onDecline = { viewModel.respondToConnection(it, false) },
                    onReport = { reportTarget = partner to listing.flatId },
                    onBlock = { blockTarget = partner to listing.flatName }
                )
            }
        }
        "saved" -> habitiq.app.ui.discover.SavedFlatsScreen(
            saved = vacancies.filter { it.flatId in savedIds },
            onOpen = { selectedFlatId = it.flatId; flatFrom = "saved"; surface = "flat" },
            onRemove = { l -> savedScope.launch { prefs.setFlatSaved(uid, l.flatId, false) } },
            onExplore = { surface = "browse" },
            onBack = { surface = "browse" },
        )
        "map" -> habitiq.app.ui.discover.WorkplaceMapScreen(
            onPicked = { area, city ->
                vacancyFilters = vacancyFilters.copy(cityArea = area.ifBlank { city })
                mode = DiscoverMode.USE_A_FLAT
                surface = "browse"
            },
            onBack = { surface = "browse" },
        )
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
                    onBack = { surface = personFrom; personFrom = "browse" },
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
                        chatFrom = "person"
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
                onBack = { surface = chatFrom; chatFrom = "browse"; chatPartnerId = null },
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
            onBack = { surface = if (initialSurface == "entry") "entry" else "browse" },
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
            Modifier.fillMaxSize().background(c.canvas).padding(HqSpacing.xxl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(104.dp).clip(CircleShape).background(c.statusSuccessBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = c.statusSuccessFg, modifier = Modifier.size(62.dp))
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
            HqButton(text = "Back to Discover", onClick = { surface = "browse" }, variant = HqButtonVariant.Secondary)
        }
        else -> Column(Modifier.fillMaxSize().background(c.canvas)) {
            val flatList = androidx.compose.foundation.lazy.rememberLazyListState()
            val personList = androidx.compose.foundation.lazy.rememberLazyListState()
            val activeList = if (mode == DiscoverMode.USE_A_FLAT) flatList else personList
            val pinPx = with(androidx.compose.ui.platform.LocalDensity.current) { 72.dp.toPx() }
            val discoverPinned by androidx.compose.runtime.remember(activeList) {
                androidx.compose.runtime.derivedStateOf { activeList.firstVisibleItemIndex > 0 || activeList.firstVisibleItemScrollOffset > pinPx }
            }
            habitiq.app.ui.components.HqPinnedTopBar(
                pinned = discoverPinned,
                title = "Discover",
                actions = {
                    habitiq.app.ui.components.HqHeaderIconButton(habitiq.app.ui.components.HqIcons.Heart, "Saved flats", { surface = "saved" })
                    habitiq.app.ui.components.HqInboxButton(
                        count = connections.count { it.toUid == uid && it.status == ConnectionStatus.REQUEST_SENT },
                        onClick = { surface = "inbox" },
                    )
                },
            )
            val discoverTop: @Composable () -> Unit = {
            DiscoverHeader(
                onBack = { surface = "browse" },
                onMyPosts = { surface = "posts" },
                onInbox = { surface = "inbox" },
                inboxCount = connections.count { it.toUid == uid && it.status == ConnectionStatus.REQUEST_SENT },
                onSaved = { surface = "saved" },
            )
            Column(Modifier.padding(horizontal = HqSpacing.screenHorizontal)) {
                DiscoverModeToggle(mode, DiscoverFlags.FIND_FLATMATE) { mode = it }
                Spacer(Modifier.height(HqSpacing.sm))
                Text(
                    when (mode) {
                        DiscoverMode.USE_A_FLAT -> "Find a place to live — flats, PGs and open rooms."
                        DiscoverMode.FIND_A_PERSON -> "People looking for a home, matched to how you live."
                    },
                    style = HqType.bodySmall,
                    color = c.textSecondary
                )
            }
            Spacer(Modifier.height(HqSpacing.sm))
            }
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
                    },
                    onMyPosts = { surface = "posts" },
                    onNearPlace = { surface = "map" },
                    header = discoverTop,
                    listState = flatList,
                )
                DiscoverMode.FIND_A_PERSON -> {
                    if (!DiscoverFlags.FIND_FLATMATE) {
                        Column { discoverTop(); FindPersonStub() }
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
                            onMyPosts = { surface = "posts" },
                            header = discoverTop,
                            listState = personList,
                            onOpenProfile = {
                                selectedSeekerId = it.id
                                personFrom = "browse"
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
    Column(Modifier.fillMaxSize().background(c.canvas).verticalScroll(rememberScrollState()).padding(HqSpacing.xl)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            HqTextButton(text = "Posts", onClick = onPosts)
            HqTextButton(text = "Inbox", onClick = onInbox)
        }
        Text("Discover", style = HqType.headlineLarge, color = c.textPrimary)
        Text("Find your next place or the right people to live with.", style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(bottom = HqSpacing.lg))
        androidx.compose.foundation.layout.Row(
            Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.related),
        ) {
            habitiq.app.ui.IntentTile("Find a place", "Flats and rooms with space.", habitiq.app.ui.components.HqArt.SearchHouse, onFindPlace, Modifier.weight(1f))
            habitiq.app.ui.IntentTile("Find a person", "Flatmates and households.", habitiq.app.ui.components.HqArt.IntentFlatmate, onFindPerson, Modifier.weight(1f))
        }
        Spacer(Modifier.height(HqSpacing.xl))
        Text("Search by area", style = HqType.titleSmall, color = c.textPrimary)
        habitiq.app.ui.components.HqChipFlow(Modifier.padding(top = HqSpacing.sm)) {
            listOf("Hyderabad", "Gachibowli", "Hitech City", "Kondapur").forEach { city ->
                HqChip(label = city, selected = false, onClick = { onLocation(city) })
            }
        }
        if (isAdmin) {
            Spacer(Modifier.height(HqSpacing.xl))
            habitiq.app.ui.components.HqGroup {
                habitiq.app.ui.components.HqNavRow(title = "Post a vacancy", support = "List a room in your flat.", onClick = onPost)
            }
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
            Icon(icon, null, tint = c.actionPrimaryBg)
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
    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqBackAppBar(title = if (findingPerson) "Find a person" else "Find a place", onBack = onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(HqSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.md)
        ) {
            Text(
                if (findingPerson) "Search by location or lifestyle." else "Search flats, rooms and landmarks.",
                style = HqType.bodyMedium,
                color = c.textSecondary
            )
            HqTextField(
                value = query,
                onValueChange = onQuery,
                label = "Search",
                placeholder = "City, area or landmark",
                imeAction = androidx.compose.ui.text.input.ImeAction.Search,
                onImeAction = onContinue
            )
            Text("Recent searches", style = HqType.titleSmall, color = c.textPrimary)
            habitiq.app.ui.components.HqChipFlow {
                listOf("2 BHK", "Private room", "Hyderabad").forEach { hint ->
                    HqChip(label = hint, selected = query == hint, onClick = { onQuery(hint) })
                }
            }
            Spacer(Modifier.height(HqSpacing.lg))
            HqButton(text = "Continue", onClick = onContinue)
        }
    }
}

@Composable
private fun DiscoverHeader(onBack: () -> Unit, onMyPosts: () -> Unit, onInbox: () -> Unit, inboxCount: Int, onSaved: () -> Unit = {}) {
    // Figma Discover header: title and subtitle with the connections inbox on the right. Posting lives on
    // the shell's + (vacancy / looking) and "My posts" sits on the list's section title.
    habitiq.app.ui.components.HqPageHeader(
        title = "Discover",
        subtitle = "Find a place or the right people.",
        pinOnScroll = false,
        modifier = Modifier.padding(start = HqSpacing.screenHorizontal, end = HqSpacing.screenHorizontal, top = HqSpacing.sm),
        action = {
            habitiq.app.ui.components.HqHeaderIconButton(habitiq.app.ui.components.HqIcons.Heart, "Saved flats", onSaved)
            habitiq.app.ui.components.HqInboxButton(count = inboxCount, onClick = onInbox)
        },
    )
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

/** The two faces of Discover: finding a flat and finding a person. */
@Composable
private fun DiscoverModeToggle(mode: DiscoverMode, findPersonEnabled: Boolean, onModeChange: (DiscoverMode) -> Unit) {
    habitiq.app.ui.components.HqSegmentedControl(
        options = listOf("Find a Flat", if (findPersonEnabled) "Find a Flatmate" else "Find a Flatmate (soon)"),
        selectedIndex = if (mode == DiscoverMode.USE_A_FLAT) 0 else 1,
        onSelect = { onModeChange(if (it == 0) DiscoverMode.USE_A_FLAT else DiscoverMode.FIND_A_PERSON) },
    )
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
    // Figma Connections: Requests (incoming), Sent (waiting), Connected (open conversations).
    val incoming = connections.filter { it.toUid == uid && it.status == ConnectionStatus.REQUEST_SENT }
    val sent = connections.filter { it.toUid != uid && it.status == ConnectionStatus.REQUEST_SENT }
    val connected = connections.filter { it.status == ConnectionStatus.ACCEPTED || it.status == ConnectionStatus.CONVERSATION_OPEN || it.status == ConnectionStatus.MATCHED }
    Column(Modifier.fillMaxSize().background(c.canvas).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.screenHorizontal).padding(bottom = HqSpacing.screenEnd)) {
        habitiq.app.ui.components.HqPageHeader(
            title = "Connections",
            subtitle = "Your Discovery requests and conversations",
            onBack = onBack,
            modifier = Modifier.padding(top = HqSpacing.sm),
        )
        if (connections.isEmpty()) {
            DiscoveryEmpty(
                title = "Nothing here yet",
                body = "Interested people will appear here when they contact you."
            )
        }
        if (incoming.isNotEmpty()) {
            habitiq.app.ui.components.HqSectionTitle("Requests")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                incoming.forEach { conn ->
                    val partner = conn.partnerId(uid)
                    val name = label(partner)
                    val shape = RoundedCornerShape(18.dp)
                    Row(
                        Modifier.fillMaxWidth().clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape).padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(11.dp), verticalAlignment = Alignment.Top,
                    ) {
                        habitiq.app.ui.components.HqAvatar(name, size = habitiq.app.ui.components.HqAvatarSize.MD, tone = habitiq.app.ui.components.hqToneFor(name, false))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(name, style = HqType.rowTitle, color = c.textPrimary)
                            if (conn.message.isNotBlank()) Text(conn.message, style = HqType.bodyMedium, color = c.textSecondary)
                            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                                HqButton(text = "Accept", onClick = { onAccept(conn.id) }, fullWidth = false)
                                HqButton(text = "Decline", onClick = { onDecline(conn.id) }, variant = HqButtonVariant.Tertiary, fullWidth = false)
                            }
                        }
                    }
                }
            }
        }
        if (sent.isNotEmpty()) {
            habitiq.app.ui.components.HqSectionTitle("Sent")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sent.forEach { conn ->
                    val name = label(conn.partnerId(uid))
                    habitiq.app.ui.components.HqCalloutCard(
                        title = name, support = "Waiting for a reply",
                        icon = habitiq.app.ui.components.HqIcons.Clock, tone = habitiq.app.ui.components.HqTileTone.Neutral, onClick = {},
                    )
                }
            }
        }
        if (connected.isNotEmpty()) {
            habitiq.app.ui.components.HqSectionTitle("Connected")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                connected.forEach { conn ->
                    val partner = conn.partnerId(uid)
                    val name = label(partner)
                    val shape = RoundedCornerShape(18.dp)
                    Row(
                        Modifier.fillMaxWidth().clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape)
                            .clickable(role = androidx.compose.ui.semantics.Role.Button) { onOpenChat(partner, name) }.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,
                    ) {
                        habitiq.app.ui.components.HqAvatar(name, size = habitiq.app.ui.components.HqAvatarSize.MD, tone = habitiq.app.ui.components.hqToneFor(name, false))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(name, style = HqType.rowTitle, color = c.textPrimary)
                            Text(if (conn.status == ConnectionStatus.MATCHED) "Matched" else "Tap to message", style = HqType.bodyMedium, color = c.textSecondary)
                        }
                        Icon(habitiq.app.ui.components.HqIcons.Chevron, null, tint = c.iconDefault, modifier = Modifier.size(HqIconSize.sm))
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
    // Registered after Discover's outer handler, so back closes the schedule sub-screen first.
    BackHandler(enabled = showSchedule) { showSchedule = false }
    val partnerName = vacancies.find { it.adminUid == partnerId }?.flatName
        ?: seekers.find { it.id == partnerId }?.displayName?.takeIf { it.isNotBlank() }
        ?: "Oddroof user"
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

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        habitiq.app.ui.components.HqPageHeader(
            title = partnerName,
            subtitle = threadLabel.takeIf { it.isNotBlank() }?.let { "Interested in: $it" },
            onBack = onBack,
            modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.sm, bottom = 0.dp),
            action = {
                HqTextButton(text = "Report", onClick = onReport)
                HqTextButton(text = "Block", onClick = onBlock)
            },
        )
        if (isFlatConversation) {
            HqButton(
                text = "Schedule a viewing",
                onClick = { showSchedule = true },
                variant = HqButtonVariant.Secondary,
                fullWidth = false,
                modifier = Modifier.padding(horizontal = HqSpacing.lg, vertical = HqSpacing.sm)
            )
        }
        Surface(Modifier.fillMaxWidth().padding(horizontal = HqSpacing.lg, vertical = HqSpacing.sm), color = c.statusWarningBg, shape = RoundedCornerShape(HqRadius.md)) {
            Row(Modifier.padding(HqSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = c.statusWarningFg, modifier = Modifier.size(HqIconSize.sm))
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
                    Surface(
                        color = if (mine) c.actionPrimaryBg else c.surfaceSubtle,
                        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = if (mine) 18.dp else 4.dp, bottomEnd = if (mine) 4.dp else 18.dp),
                    ) {
                        if (msg.isViewingRequest && msg.viewingTime != null) {
                            Column(Modifier.padding(HqSpacing.md), verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                                    Icon(Icons.Filled.CalendarMonth, null, tint = if (mine) c.actionPrimaryFg else c.actionPrimaryBg)
                                    Text("Viewing request", style = HqType.titleSmall, color = if (mine) c.actionPrimaryFg else c.textPrimary)
                                }
                                Text(
                                    java.time.Instant.ofEpochMilli(msg.viewingTime).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("EEE, d MMM · h:mm a")),
                                    style = HqType.labelLarge,
                                    color = if (mine) c.actionPrimaryFg else c.actionPrimaryBg
                                )
                                Text(msg.content, color = if (mine) c.actionPrimaryFg else c.textSecondary, style = HqType.bodySmall)
                            }
                        } else {
                            Text(msg.content, Modifier.padding(horizontal = 14.dp, vertical = 10.dp), color = if (mine) c.actionPrimaryFg else c.textPrimary, style = HqType.bodyMedium)
                        }
                    }
                }
            }
        }
        // Figma composer: a rounded field with a round send button on the right.
        Row(
            Modifier.padding(HqSpacing.lg).fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(c.surfaceSubtle).padding(start = 18.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        ) {
            Box(Modifier.weight(1f).padding(vertical = 10.dp)) {
                if (text.isEmpty()) Text("Write a message", style = HqType.bodyLarge, color = c.textMuted)
                androidx.compose.foundation.text.BasicTextField(
                    value = text, onValueChange = { text = it }, textStyle = HqType.bodyLarge.copy(color = c.textPrimary),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(c.focus), modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences
                    ),
                    maxLines = 5,
                )
            }
            val canSend = text.isNotBlank() && !sending
            Box(
                Modifier.size(HqSize.target).clip(CircleShape).background(if (canSend) c.actionPrimaryBg else c.disabledBg)
                    .clickable(enabled = canSend, role = androidx.compose.ui.semantics.Role.Button) {
                        if (text.isNotBlank()) {
                            sending = true
                            viewModel.sendMessage(partnerId, text) { sent ->
                                if (sent) text = ""
                                sending = false
                            }
                        }
                    }
                    .semantics { contentDescription = "Send message" },
                contentAlignment = Alignment.Center,
            ) { Icon(habitiq.app.ui.components.HqIcons.Arrow, null, tint = if (canSend) c.actionPrimaryFg else c.disabledFg, modifier = Modifier.size(HqIconSize.md)) }
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

    Column(Modifier.fillMaxSize().background(c.canvas)) {
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

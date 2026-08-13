package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import habitiq.app.data.ChatMessage
import habitiq.app.data.SeekerProfile
import habitiq.app.flat.FlatViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import habitiq.app.ui.theme.FigmaColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    prompt.authenticate(BiometricPrompt.PromptInfo.Builder()
      .setTitle("Habitiq Vault")
      .setSubtitle("Verify to access your flat")
      .setNegativeButtonText("Cancel")
      .build())
  }

  Box(Modifier.fillMaxSize().background(Color(0xFF0F172A)), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Icon(Icons.Filled.Fingerprint, null, tint = FigmaColors.Primary, modifier = Modifier.size(64.dp))
      Text("Habitiq is locked", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
      Text(status, color = Color(0xFF94A3B8), fontSize = 14.sp)
      Button(onClick = { authenticate() }, colors = ButtonDefaults.buttonColors(containerColor = FigmaColors.Primary)) {
        Text("Unlock")
      }
    }
  }
  LaunchedEffect(Unit) { authenticate() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverBoardScreen(viewModel: FlatViewModel) {
    val vacancies by viewModel.vacancies.collectAsStateWithLifecycleCompat()
    val seekers by viewModel.seekerProfiles.collectAsStateWithLifecycleCompat()
    val messages by viewModel.chatMessages.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    var tab by remember { mutableStateOf("Vacancies") }
    var cityFilter by remember { mutableStateOf("") }
    var chatPartnerId by remember { mutableStateOf<String?>(null) }
    var showSeekerForm by remember { mutableStateOf(false) }

    if (chatPartnerId != null) {
        ChatScreen(viewModel, chatPartnerId!!, onBack = { chatPartnerId = null })
        return
    }
    if (showSeekerForm) {
        SeekerProfileForm(viewModel, onBack = { showSeekerForm = false }, onSaved = { showSeekerForm = false })
        return
    }

    val uid = currentUser?.uid.orEmpty()
    val chatPartners = remember(messages, uid) {
        messages.map { if (it.senderId == uid) it.receiverId else it.senderId }.distinct()
    }

    Column(Modifier.fillMaxSize().background(FigmaColors.Background)) {
        Text("Discover", modifier = Modifier.padding(20.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FigmaColors.Ink)
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Vacancies", "Seekers", "Chats").forEach { t ->
                FilterChip(selected = tab == t, onClick = { tab = t }, label = { Text(t, fontSize = 12.sp) })
            }
        }
        if (tab != "Chats") {
            OutlinedTextField(value = cityFilter, onValueChange = { cityFilter = it },
                modifier = Modifier.padding(16.dp).fillMaxWidth(), label = { Text("Filter city/area") }, singleLine = true)
        }
        when (tab) {
            "Vacancies" -> {
                val filtered = vacancies.filter { cityFilter.isBlank() || it.city.contains(cityFilter, true) || it.area.contains(cityFilter, true) }
                if (filtered.isEmpty()) Text("No active vacancies.", modifier = Modifier.padding(16.dp), color = FigmaColors.InkSecondary)
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered, key = { it.flatId }) { VacancyCard(it) }
                }
            }
            "Seekers" -> {
                Row(Modifier.padding(horizontal = 16.dp)) {
                    TextButton(onClick = { showSeekerForm = true }) { Text("Publish my seeker profile") }
                }
                val filtered = seekers.filter { it.id != uid && (cityFilter.isBlank() || it.city.contains(cityFilter, true) || it.lookingIn.contains(cityFilter, true)) }
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered, key = { it.id }) { seeker ->
                        SeekerCard(seeker, onChat = { chatPartnerId = seeker.id })
                    }
                }
            }
            else -> {
                if (chatPartners.isEmpty()) Text("No chats yet. Message a seeker or vacancy poster.", modifier = Modifier.padding(16.dp), color = FigmaColors.InkSecondary)
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(chatPartners) { partnerId ->
                        val partner = seekers.find { it.id == partnerId }
                        val last = messages.filter { it.senderId == partnerId || it.receiverId == partnerId }.maxByOrNull { it.timestamp }
                        Card(onClick = { chatPartnerId = partnerId }, colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(partner?.displayName ?: partnerId, fontWeight = FontWeight.Bold)
                                last?.let { Text(it.content.take(60), fontSize = 12.sp, color = FigmaColors.InkSecondary) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeekerCard(seeker: SeekerProfile, onChat: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)) {
        Column(Modifier.padding(12.dp)) {
            Text(seeker.displayName, fontWeight = FontWeight.Bold)
            Text("${seeker.city} → ${seeker.lookingIn}", fontSize = 13.sp, color = FigmaColors.InkSecondary)
            Text("Budget ₹${seeker.budget.toInt()}", fontSize = 13.sp)
            if (seeker.bio.isNotBlank()) Text(seeker.bio, fontSize = 12.sp, color = FigmaColors.InkMuted)
            TextButton(onClick = onChat) { Text("Chat") }
        }
    }
}

@Composable
private fun SeekerProfileForm(viewModel: FlatViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    var city by remember { mutableStateOf("") }
    var lookingIn by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(FigmaColors.Background).padding(20.dp)) {
        Text("My seeker profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(city, { city = it }, Modifier.fillMaxWidth(), label = { Text("Current city") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(lookingIn, { lookingIn = it }, Modifier.fillMaxWidth(), label = { Text("Looking in") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(budget, { budget = it }, Modifier.fillMaxWidth(), label = { Text("Max budget (₹)") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(bio, { bio = it }, Modifier.fillMaxWidth(), label = { Text("Bio") })
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            viewModel.updateSeekerProfile(city, lookingIn, budget.toDoubleOrNull() ?: 0.0, bio)
            onSaved()
        }, modifier = Modifier.fillMaxWidth()) { Text("Publish") }
        TextButton(onClick = onBack) { Text("Cancel") }
    }
}

@Composable
fun ChatScreen(viewModel: FlatViewModel, partnerId: String, onBack: () -> Unit) {
    val messages by viewModel.observeConversation(partnerId).collectAsStateWithLifecycle(initialValue = emptyList())
    val seekers by viewModel.seekerProfiles.collectAsStateWithLifecycleCompat()
    val uid by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    var text by remember { mutableStateOf("") }
    val partnerName = seekers.find { it.id == partnerId }?.displayName ?: partnerId
    val fmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(Modifier.fillMaxSize().background(FigmaColors.Background)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Back") }
            Text(partnerName, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), reverseLayout = true, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(messages.reversed(), key = { it.id }) { msg ->
                val mine = msg.senderId == uid?.uid
                Box(Modifier.fillMaxWidth(), contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart) {
                    Surface(color = if (mine) FigmaColors.Primary else FigmaColors.Surface, shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(10.dp)) {
                            Text(msg.content, color = if (mine) Color.White else FigmaColors.Ink, fontSize = 14.sp)
                            Text(fmt.format(Date(msg.timestamp)), fontSize = 10.sp, color = if (mine) Color.White.copy(0.7f) else FigmaColors.InkMuted)
                        }
                    }
                }
            }
        }
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f), placeholder = { Text("Message…") })
            IconButton(onClick = {
                if (text.isNotBlank()) { viewModel.sendMessage(partnerId, text); text = "" }
            }) { Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = FigmaColors.Primary) }
        }
    }
}

@Composable
private fun VacancyCard(listing: habitiq.app.data.VacancyListing) {
    Card(colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)) {
        Column(Modifier.padding(12.dp)) {
            Text(listing.flatName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("${listing.area}, ${listing.city}", fontSize = 13.sp, color = FigmaColors.InkSecondary)
            listing.rentPerHead?.let { Text("₹${it.toInt()}/head", fontSize = 13.sp) }
            if (listing.about.isNotBlank()) Text(listing.about, fontSize = 12.sp, color = FigmaColors.InkMuted)
        }
    }
}

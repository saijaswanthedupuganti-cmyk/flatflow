package habitiq.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.firebase.messaging.FirebaseMessaging
import habitiq.app.data.MessagingRepository
import habitiq.app.services.FcmNotificationHelper
import habitiq.app.settings.AppPreferences
import habitiq.app.ui.BiometricLockScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appPreferences = AppPreferences(this)
        FcmNotificationHelper.createNotificationChannel(this)
        FcmNotificationHelper.requestNotificationPermissionIfNeeded(this)

        setContent {
            var isLocked by remember { mutableStateOf(false) }
            val biometricEnabled by appPreferences.isBiometricLockEnabled.collectAsStateWithLifecycle(initialValue = false)
            val lifecycleOwner = LocalLifecycleOwner.current

            LaunchedEffect(biometricEnabled) {
                if (biometricEnabled) isLocked = true
            }

            DisposableEffect(lifecycleOwner, biometricEnabled) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP && biometricEnabled) {
                        isLocked = true
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            if (isLocked && biometricEnabled) {
                BiometricLockScreen(activity = this, onUnlocked = { isLocked = false })
            } else {
                HabitiqApp()
            }
        }

        registerFcmToken()
    }

    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result ?: return@addOnCompleteListener
            CoroutineScope(Dispatchers.IO).launch {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                MessagingRepository().saveFcmToken(uid, token)
            }
        }
    }
}

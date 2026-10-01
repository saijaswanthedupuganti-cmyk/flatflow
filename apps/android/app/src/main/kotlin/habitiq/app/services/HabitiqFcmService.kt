package habitiq.app.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import habitiq.app.data.MessagingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HabitiqFcmService : FirebaseMessagingService() {
    private val messagingRepository = MessagingRepository()

    // Keep the token callback until the shared backend migrates to FID-based targeting.
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed")
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            messagingRepository.saveFcmToken(uid, token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        val title = data["title"] ?: remoteMessage.notification?.title ?: "Habitiq"
        val body = data["body"] ?: remoteMessage.notification?.body ?: "New flat update"
        val type = data["type"] ?: "info"
        FcmNotificationHelper.showLocalNotification(this, title, body, type)
    }

    companion object {
        private const val TAG = "HabitiqFcmService"
    }
}

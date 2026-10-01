package habitiq.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import habitiq.app.MainActivity
import java.util.UUID

object FcmNotificationHelper {
    private const val TAG = "FcmNotificationHelper"
    const val CHANNEL_ID = "flat_alerts_channel"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Flatmate Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Chores, bills, and expense updates" }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun subscribeToFlatTopic(flatId: String) {
        if (flatId.isBlank()) return
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("flat_$flatId")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) Log.d(TAG, "Subscribed to flat_$flatId")
                    else Log.e(TAG, "Topic subscribe failed", task.exception)
                }
        } catch (e: Exception) {
            Log.w(TAG, "FCM not available: ${e.message}")
        }
    }

    fun unsubscribeFromFlatTopic(flatId: String) {
        if (flatId.isBlank()) return
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("flat_$flatId")
        } catch (_: Exception) { }
    }

    fun requestNotificationPermissionIfNeeded(activity: MainActivity, onResult: (Boolean) -> Unit = {}) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            onResult(true)
        } else {
            onResult(true)
        }
    }

    fun showLocalNotification(context: Context, title: String, body: String, type: String = "info") {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("notification_type", type)
        }
        val pending = PendingIntent.getActivity(
            context, UUID.randomUUID().hashCode(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}

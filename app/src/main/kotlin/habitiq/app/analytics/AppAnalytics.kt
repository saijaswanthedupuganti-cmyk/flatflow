package habitiq.app.analytics

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

const val METHOD_PASSWORD = "password"
const val METHOD_GOOGLE = "google"

private const val EVENT_FLAT_CREATED = "flat_created"
private const val EVENT_FLAT_JOINED = "flat_joined"
private const val EVENT_ACCOUNT_DELETED = "account_deleted"

// Params carry non-identifying labels only -- never email, uid, nickname or flat id.
class AppAnalytics(private val analytics: FirebaseAnalytics = Firebase.analytics) {

    fun logSignUp(method: String) =
        analytics.logEvent(
            FirebaseAnalytics.Event.SIGN_UP,
            Bundle().apply { putString(FirebaseAnalytics.Param.METHOD, method) }
        )

    fun logLogin(method: String) =
        analytics.logEvent(
            FirebaseAnalytics.Event.LOGIN,
            Bundle().apply { putString(FirebaseAnalytics.Param.METHOD, method) }
        )

    fun logFlatCreated() = analytics.logEvent(EVENT_FLAT_CREATED, null)

    fun logFlatJoined() = analytics.logEvent(EVENT_FLAT_JOINED, null)

    fun logAccountDeleted() = analytics.logEvent(EVENT_ACCOUNT_DELETED, null)
}

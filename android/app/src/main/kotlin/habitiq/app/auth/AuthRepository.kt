package habitiq.app.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<Unit> = runCatching {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        Unit
    }.recoverCatching { throw IllegalStateException(mapAuthError(reported(it)), it) }

    suspend fun signInWithEmail(email: String, password: String): Result<Unit> = runCatching {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
        Unit
    }.recoverCatching { throw IllegalStateException(mapAuthError(reported(it)), it) }

    suspend fun signInWithGoogleIdToken(idToken: String): Result<Unit> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).await()
        Unit
    }.recoverCatching { throw IllegalStateException(mapAuthError(reported(it)), it) }

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> = runCatching {
        firebaseAuth.currentUser?.delete()?.await()
        Unit
    }.recoverCatching { throw IllegalStateException(mapAuthError(reported(it)), it) }

    // The user only ever sees the mapped message, so without this the underlying failure would
    // leave no trace anywhere. Returns the failure unchanged so it can still be mapped.
    private fun reported(error: Throwable): Exception {
        val exception = error as Exception
        if (!isExpectedAuthError(exception)) {
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
        return exception
    }
}

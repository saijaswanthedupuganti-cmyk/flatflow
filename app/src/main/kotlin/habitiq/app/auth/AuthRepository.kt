package habitiq.app.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
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
    }.recoverCatching { throw IllegalStateException(mapAuthError(it as Exception), it) }

    suspend fun signInWithEmail(email: String, password: String): Result<Unit> = runCatching {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
        Unit
    }.recoverCatching { throw IllegalStateException(mapAuthError(it as Exception), it) }

    suspend fun signInWithGoogleIdToken(idToken: String): Result<Unit> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).await()
        Unit
    }.recoverCatching { throw IllegalStateException(mapAuthError(it as Exception), it) }

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> = runCatching {
        firebaseAuth.currentUser?.delete()?.await()
        Unit
    }.recoverCatching { throw IllegalStateException(mapAuthError(it as Exception), it) }
}

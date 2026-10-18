package habitiq.app.auth

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class AuthErrorMapperTest {

    @Test
    fun `recent login required maps to a plain re-authenticate message`() {
        val exception = FirebaseAuthRecentLoginRequiredException("ERROR_REQUIRES_RECENT_LOGIN", "stale session")
        assertEquals(
            "Please sign out and sign back in, then try deleting your account again.",
            mapAuthError(exception)
        )
    }

    @Test
    fun `invalid credentials maps to plain wrong password message`() {
        val exception = FirebaseAuthInvalidCredentialsException("ERROR_WRONG_PASSWORD", "bad creds")
        assertEquals("Incorrect email or password.", mapAuthError(exception))
    }

    @Test
    fun `user collision maps to account exists message`() {
        val exception = FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "exists")
        assertEquals("An account already exists with this email.", mapAuthError(exception))
    }

    @Test
    fun `weak password maps to plain weak password message`() {
        val exception = FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "weak", "PASSWORD_DOES_NOT_MEET_REQUIREMENTS")
        assertEquals("Password is too weak — use at least 6 characters.", mapAuthError(exception))
    }

    @Test
    fun `network error maps to connectivity message`() {
        val exception = IOException("network down")
        assertEquals("No internet connection. Please try again.", mapAuthError(exception))
    }

    @Test
    fun `unknown error maps to generic fallback message`() {
        val exception = RuntimeException("something else")
        assertEquals("Something went wrong. Please try again.", mapAuthError(exception))
    }

    @Test
    fun `offline firebase calls say no internet, not something went wrong`() {
        assertEquals("No internet connection. Please try again.", mapAuthError(com.google.firebase.FirebaseNetworkException("offline")))
    }

    @Test
    fun `reset errors use reset wording`() {
        assertEquals(
            "That email address doesn't look right. Check it and try again.",
            mapResetError(FirebaseAuthInvalidCredentialsException("ERROR_INVALID_EMAIL", "bad"))
        )
        assertEquals("No internet connection. Connect and try again.", mapResetError(com.google.firebase.FirebaseNetworkException("offline")))
        assertEquals(
            "Too many reset requests. Wait a few minutes, then try again.",
            mapResetError(com.google.firebase.FirebaseTooManyRequestsException("slow down"))
        )
    }

    @Test
    fun `email typos are caught before sending`() {
        listOf("sai@gmail.com", " sai.j+1@habitiq.app ").forEach { assertEquals(it, true, isPlausibleEmail(it)) }
        listOf("", "sai", "sai@gmail", "sai gmail.com", "sai@@gmail.com", "sai@gmail.c").forEach { assertEquals(it, false, isPlausibleEmail(it)) }
    }
}

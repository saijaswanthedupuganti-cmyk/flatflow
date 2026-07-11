package habitiq.app.auth

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class AuthErrorMapperTest {

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
}

package habitiq.app.auth

import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.io.IOException

fun mapAuthError(exception: Exception): String = when (exception) {
    is FirebaseAuthRecentLoginRequiredException -> "Please sign out and sign back in, then try deleting your account again."
    // FirebaseAuthWeakPasswordException is a subclass of FirebaseAuthInvalidCredentialsException
    // in the real SDK, so it must be checked first or it's caught by the parent branch below.
    is FirebaseAuthWeakPasswordException -> "Password is too weak — use at least 6 characters."
    is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
    is FirebaseAuthInvalidUserException -> "No account found with this email."
    is FirebaseAuthUserCollisionException -> "An account already exists with this email."
    // Credential Manager (Google Sign-In) failures, before any Firebase call is made.
    is NoCredentialException -> "No Google account found on this device. Add one in Settings, then try again."
    is GetCredentialProviderConfigurationException -> "Google Sign-In isn't set up on this device. Try updating Google Play Services."
    is GetCredentialInterruptedException -> "Sign-in was interrupted. Please try again."
    is GetCredentialUnsupportedException -> "Google Sign-In isn't supported on this device."
    is GoogleIdTokenParsingException -> "Google Sign-In returned an unexpected response. Please try again."
    is IOException -> "No internet connection. Please try again."
    else -> "Something went wrong (${exception::class.simpleName}: ${exception.message}). Please try again."
}

package habitiq.app.auth

import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
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
    is FirebaseAuthWeakPasswordException -> "Password is too weak. Use at least 8 characters with a letter, a number and a special character."
    is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
    is FirebaseAuthInvalidUserException -> "No account found with this email."
    is FirebaseAuthUserCollisionException -> "An account already exists with this email."
    // Credential Manager (Google Sign-In) failures, before any Firebase call is made.
    is NoCredentialException -> "No Google account found on this device. Add one in Settings, then try again."
    is GetCredentialProviderConfigurationException -> "Google Sign-In isn't set up on this device. Try updating Google Play Services."
    is GetCredentialInterruptedException -> "Sign-in was interrupted. Please try again."
    is GetCredentialUnsupportedException -> "Google Sign-In isn't supported on this device."
    is GoogleIdTokenParsingException -> "Google Sign-In returned an unexpected response. Please try again."
    // Firebase reports "offline" as its own type, not an IOException.
    is FirebaseNetworkException, is IOException -> "No internet connection. Please try again."
    is FirebaseTooManyRequestsException -> "Too many tries from this phone. Wait a few minutes and try again."
    else -> "Something went wrong. Please try again."
}

/** Forgot-password failures. Sign-in wording ("Incorrect email or password") makes no sense here. */
fun mapResetError(exception: Exception): String = when (exception) {
    is FirebaseAuthInvalidUserException -> "No account uses that email. Check the spelling, or create an account instead."
    is FirebaseAuthInvalidCredentialsException -> "That email address doesn't look right. Check it and try again."
    is FirebaseNetworkException, is IOException -> "No internet connection. Connect and try again."
    is FirebaseTooManyRequestsException -> "Too many reset requests. Wait a few minutes, then try again."
    else -> "Couldn't send the reset email right now. Please try again."
}

// Outcomes the user caused and can fix. Reporting them as Crashlytics non-fatals would bury the
// real defects. GoogleIdTokenParsingException is deliberately absent: a malformed token means the
// Sign-In integration itself is broken, which is worth a report.
fun isExpectedAuthError(exception: Exception): Boolean = when (exception) {
    is FirebaseAuthRecentLoginRequiredException,
    is FirebaseAuthInvalidCredentialsException,
    is FirebaseAuthInvalidUserException,
    is FirebaseAuthUserCollisionException,
    is FirebaseNetworkException,
    is FirebaseTooManyRequestsException,
    is NoCredentialException,
    is GetCredentialProviderConfigurationException,
    is GetCredentialInterruptedException,
    is GetCredentialUnsupportedException,
    is IOException -> true
    else -> false
}

package habitiq.app.auth

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
    is IOException -> "No internet connection. Please try again."
    else -> "Something went wrong. Please try again."
}

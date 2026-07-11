package habitiq.app.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

suspend fun launchGoogleSignIn(context: Context, webClientId: String): String? {
    val option = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(option)
        .build()

    return try {
        val response = CredentialManager.create(context).getCredential(context, request)
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(response.credential.data)
        googleIdTokenCredential.idToken
    } catch (e: GetCredentialCancellationException) {
        null
    }
}

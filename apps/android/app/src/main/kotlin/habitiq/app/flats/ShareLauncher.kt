package habitiq.app.flats

import android.content.Context
import android.content.Intent

/** [inviteCode] is the flat's invite code from inviteCodeFor, never its id. */
fun launchShareInviteCode(context: Context, flatName: String, inviteCode: String) {
    val message = "Join my flat \"$flatName\" on Oddroof. In the app, tap Join a flat and enter $inviteCode."
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    val chooser = Intent.createChooser(sendIntent, "Share invite code")
    context.startActivity(chooser)
}

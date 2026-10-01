package habitiq.app.flats

import android.content.Context
import android.content.Intent

fun launchShareInviteCode(context: Context, flatName: String, flatId: String) {
    val message = "Join my flat \"$flatName\" on Habitiq! Use code $flatId to join."
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    val chooser = Intent.createChooser(sendIntent, "Share invite code")
    context.startActivity(chooser)
}

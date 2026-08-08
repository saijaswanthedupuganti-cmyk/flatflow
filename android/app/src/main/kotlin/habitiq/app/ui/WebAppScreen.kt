package habitiq.app.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import habitiq.app.ui.theme.HabitiqBrand

/** Production web app — full product surface until native parity is complete. */
const val HABITIQ_WEB_URL = "https://habitiq.app"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebAppScreen(startUrl: String = HABITIQ_WEB_URL) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var canGoBack by remember { mutableStateOf(false) }

    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HabitiqBrand.Canvas)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(0xFF0C0B0F.toInt())

                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        mediaPlaybackRequiresUserGesture = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                        cacheMode = WebSettings.LOAD_DEFAULT
                        // Strip WebView marker so Google OAuth is less likely to block the session.
                        userAgentString = userAgentString.replace("; wv", "")
                    }

                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest
                        ): Boolean {
                            val uri = request.url ?: return false
                            val host = uri.host.orEmpty()
                            val scheme = uri.scheme.orEmpty()

                            // Keep Habitiq + Firebase auth domains inside the WebView.
                            if (scheme == "http" || scheme == "https") {
                                if (host.endsWith("habitiq.app") ||
                                    host.endsWith("habitiq.in") ||
                                    host.endsWith("firebaseapp.com") ||
                                    host.endsWith("googleapis.com") ||
                                    host.endsWith("gstatic.com") ||
                                    host.endsWith("google.com") ||
                                    host.endsWith("google.co.in") ||
                                    host.endsWith("accounts.youtube.com")
                                ) {
                                    return false
                                }
                                // External https → system browser
                                return openExternal(view, uri)
                            }

                            // tel:, mailto:, intent:, etc.
                            return openExternal(view, uri)
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            loading = true
                            loadError = null
                            canGoBack = view?.canGoBack() == true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            loading = false
                            canGoBack = view?.canGoBack() == true
                            CookieManager.getInstance().flush()
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            loading = false
                            loadError = description ?: "Could not load Habitiq"
                        }
                    }

                    loadUrl(startUrl)
                    webView = this
                }
            },
            update = { view ->
                webView = view
                canGoBack = view.canGoBack()
            }
        )

        if (loading && loadError == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HabitiqBrand.Canvas.copy(alpha = 0.92f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text(
                    "Habitiq",
                    color = HabitiqBrand.Ink,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Smart living, managed.",
                    color = HabitiqBrand.Ink.copy(alpha = 0.65f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Spacer(Modifier.height(28.dp))
                CircularProgressIndicator(color = HabitiqBrand.Primary, strokeWidth = 3.dp)
            }
        }

        loadError?.let { message ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HabitiqBrand.Canvas)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text("Couldn't reach Habitiq", color = HabitiqBrand.Ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(message, color = HabitiqBrand.Error, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                Text(
                    "Check your internet connection, then retry.",
                    color = HabitiqBrand.Ink.copy(alpha = 0.65f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(Modifier.height(20.dp))
                TextButton(onClick = {
                    loadError = null
                    loading = true
                    webView?.loadUrl(startUrl)
                }) {
                    Text("Retry", color = HabitiqBrand.PrimarySoft)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.stopLoading()
        }
    }
}

private fun openExternal(view: WebView, uri: Uri): Boolean {
    return try {
        view.context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

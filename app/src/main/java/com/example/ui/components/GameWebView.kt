package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.GameViewModel

private const val GAME_ASSET_URL = "file:///android_asset/game/index.html"

class AndroidGameBridge(
    private val mainHandler: Handler,
    private val viewModel: GameViewModel
) {
    @JavascriptInterface
    fun onEngineReady() {
        mainHandler.post { viewModel.onEngineReady() }
    }

    @JavascriptInterface
    fun onEngineError(message: String) {
        mainHandler.post { viewModel.onEngineError(message) }
    }

    @JavascriptInterface
    fun onHudUpdate(json: String) {
        mainHandler.post { viewModel.onHudUpdate(json) }
    }

    @JavascriptInterface
    fun onPauseRequested(unused: String) {
        mainHandler.post { viewModel.onPauseRequestedFromJs() }
    }

    @JavascriptInterface
    fun onGameOver(json: String) {
        mainHandler.post { viewModel.onGameOverFromJs(json) }
    }
}

fun mapAndroidKeyCodeToJs(keyCode: Int): String? = when (keyCode) {
    KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> "ArrowLeft"
    KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> "ArrowRight"
    KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> "ArrowUp"
    KeyEvent.KEYCODE_SPACE -> "Space"
    KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> "ArrowDown"
    KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT, KeyEvent.KEYCODE_E -> "ShiftLeft"
    KeyEvent.KEYCODE_C -> "KeyC"
    KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_ESCAPE -> "Escape"
    else -> null
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GameWebView(
    viewModel: GameViewModel,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#140B08"))
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            isFocusable = true
            isFocusableInTouchMode = true
            isHapticFeedbackEnabled = false

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                @Suppress("DEPRECATION")
                allowFileAccessFromFileURLs = true
                @Suppress("DEPRECATION")
                allowUniversalAccessFromFileURLs = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                cacheMode = WebSettings.LOAD_DEFAULT
                useWideViewPort = true
                loadWithOverviewMode = true
            }

            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {}
            addJavascriptInterface(AndroidGameBridge(mainHandler, viewModel), "AndroidBridge")
            loadUrl(GAME_ASSET_URL)
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            webView.requestFocus()
        }
    }

    LaunchedEffect(webView) {
        viewModel.jsCommands.collect { script ->
            if (script == "__RELOAD__") {
                webView.loadUrl(GAME_ASSET_URL)
            } else {
                webView.evaluateJavascript(script, null)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.removeJavascriptInterface("AndroidBridge")
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}

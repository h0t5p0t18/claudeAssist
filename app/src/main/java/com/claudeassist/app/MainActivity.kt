package com.mak.claudeassist

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mak.claudeassist.databinding.ActivityMainBinding
import java.io.ByteArrayInputStream

/**
 * claudeAssist – ein schlanker, datenschutzfreundlicher WebView-Wrapper für claude.ai
 *
 * Inspiriert von gptAssist / geminiAssist (woheller69, GPLv3).
 * Blockiert alle Requests, die nicht zur Funktion von claude.ai selbst nötig sind
 * (Tracker, Analytics, Werbe-Endpunkte Dritter), lässt sich aber per Klick
 * umschalten, falls das Blocking den Login oder eine Funktion behindert.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var blockingEnabled = true
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    companion object {
        private const val TARGET_URL = "https://claude.ai/"

        // Domains, die für den Betrieb von claude.ai selbst benötigt werden.
        // Alles außerhalb dieser Liste wird bei aktiviertem Blocking verworfen.
        private val ALLOWED_DOMAIN_SUFFIXES = listOf(
            "claude.ai",
            "anthropic.com",
            "cloudflare.com",
            "cloudflareinsights.com", // Cloudflare Challenge/CDN, für Login teils nötig
            "gstatic.com",            // Google reCAPTCHA/Login-Flows
            "google.com",             // Google-Login (OAuth)
            "googleapis.com",
            "googleusercontent.com",
            "accounts.google.com"
        )

        // Explizit bekannte Tracking-/Analytics-Domains, die auch bei
        // ansonsten erlaubten Ober-Domains geblockt werden.
        private val BLOCKED_KEYWORDS = listOf(
            "google-analytics.com",
            "doubleclick.net",
            "segment.io",
            "segment.com",
            "mixpanel.com",
            "amplitude.com",
            "sentry.io",
            "fullstory.com",
            "hotjar.com",
            "intercom.io",
            "facebook.com",
            "facebook.net"
        )
    }

    private val fileChooserLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = if (result.resultCode == Activity.RESULT_OK) result.data else null
            val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, data)
            filePathCallback?.onReceiveValue(uris)
            filePathCallback = null
        }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        setupToggleButton()

        if (savedInstanceState == null) {
            binding.webview.loadUrl(TARGET_URL)
        }
    }

    private fun setupWebView() {
        val webView: WebView = binding.webview
        val settings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.mediaPlaybackRequiresUserGesture = false
        settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
        settings.userAgentString = settings.userAgentString + " claudeAssist/1.0"

        // Cookies persistieren, damit der Login erhalten bleibt
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                if (blockingEnabled && shouldBlock(request.url.toString())) {
                    return emptyResponse()
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                // Alles innerhalb der App im WebView öffnen, keine externen Browser-Sprünge
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                this@MainActivity.filePathCallback = filePathCallback
                val intent = fileChooserParams.createIntent()
                return try {
                    fileChooserLauncher.launch(intent)
                    true
                } catch (e: Exception) {
                    this@MainActivity.filePathCallback = null
                    false
                }
            }

            // Mikrofonzugriff für Sprachfunktionen erlauben
            override fun onPermissionRequest(request: PermissionRequest) {
                runOnUiThread { request.grant(request.resources) }
            }
        }
    }

    private fun shouldBlock(url: String): Boolean {
        val lower = url.lowercase()

        // Bekannte Tracker immer blocken, unabhängig von der Ober-Domain
        if (BLOCKED_KEYWORDS.any { lower.contains(it) }) return true

        // Wenn die Domain zu einer der erlaubten Suffixe gehört, nicht blocken
        val host = Uri.parse(url).host ?: return true
        val isAllowed = ALLOWED_DOMAIN_SUFFIXES.any { suffix ->
            host == suffix || host.endsWith(".$suffix")
        }
        return !isAllowed
    }

    private fun emptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "utf-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupToggleButton() {
        updateToggleLabel()

        binding.toggleButton.setOnClickListener {
            blockingEnabled = !blockingEnabled
            updateToggleLabel()
            binding.webview.reload()
            Toast.makeText(
                this,
                if (blockingEnabled) getString(R.string.blocking_on)
                else getString(R.string.blocking_off),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.toggleButton.setOnLongClickListener {
            confirmReset()
            true
        }

        // Button nach oben wegwischen, um ihn auszublenden (wie bei gptAssist)
        var startY = 0f
        binding.toggleButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    false
                }
                MotionEvent.ACTION_UP -> {
                    if (startY - event.rawY > 80) {
                        v.visibility = View.GONE
                    }
                    false
                }
                else -> false
            }
        }
    }

    private fun updateToggleLabel() {
        binding.toggleButton.text =
            if (blockingEnabled) getString(R.string.block_on_label)
            else getString(R.string.block_off_label)
    }

    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle(R.string.reset_title)
            .setMessage(R.string.reset_message)
            .setPositiveButton(R.string.reset_confirm) { _, _ -> resetSession() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun resetSession() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        binding.webview.clearCache(true)
        binding.webview.clearHistory()
        android.webkit.WebStorage.getInstance().deleteAllData()
        binding.webview.loadUrl(TARGET_URL)
    }

    override fun onBackPressed() {
        if (binding.webview.canGoBack()) {
            binding.webview.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

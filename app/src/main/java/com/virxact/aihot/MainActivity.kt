package com.virxact.aihot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var errorView: View
    private lateinit var errorTitle: TextView
    private lateinit var errorMessage: TextView
    private lateinit var retryButton: MaterialButton

    private var isNetworkAvailable = true
    private var isErrorShowing = false
    private var isFirstLoad = true

    private val connectivityManager: ConnectivityManager
        get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val wasOffline = !isNetworkAvailable
            isNetworkAvailable = true
            if (wasOffline && isErrorShowing) {
                runOnUiThread {
                    hideError()
                    webView.reload()
                }
            }
        }

        override fun onLost(network: Network) {
            isNetworkAvailable = false
        }

        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
        ) {
            isNetworkAvailable = capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep splash until WebView loads the page
        splashScreen.setKeepOnScreenCondition { isFirstLoad }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        initViews()
        setupWebView()
        setupSwipeRefresh()
        setupBackPress()
        registerNetworkCallback()
    }

    private fun initViews() {
        webView = findViewById(R.id.webView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)
        errorView = findViewById(R.id.errorView)
        errorTitle = findViewById(R.id.errorTitle)
        errorMessage = findViewById(R.id.errorMessage)
        retryButton = findViewById(R.id.retryButton)

        retryButton.setOnClickListener {
            if (isNetworkAvailable) {
                hideError()
                webView.reload()
            }
        }

        // Apply edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(swipeRefresh) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }
    }

    private fun setupWebView() {
        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                mixedContentMode =
                    android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                userAgentString = buildCustomUserAgent()
                cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                allowFileAccess = false
                allowContentAccess = false
            }

            webViewClient = AIHotWebClient()
            webChromeClient = AIHotChromeClient()

            // Load the site
            loadUrl("https://aihot.virxact.com/")
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.apply {
            setColorSchemeResources(
                R.color.swipe_refresh_1,
                R.color.swipe_refresh_2,
                R.color.swipe_refresh_3
            )
            setProgressBackgroundColorSchemeColor(
                resources.getColor(R.color.surface_dark, theme)
            )
            setOnRefreshListener {
                if (isNetworkAvailable) {
                    webView.reload()
                } else {
                    isRefreshing = false
                }
            }
        }
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    // Disable this callback so the system handles it
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        // Check initial state
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        isNetworkAvailable = capabilities?.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) ?: false
    }

    private fun showError(errorCode: Int, description: String) {
        if (isErrorShowing) return
        isErrorShowing = true

        errorTitle.text = when {
            !isNetworkAvailable -> getString(R.string.no_network)
            errorCode == ERROR_TIMEOUT -> "请求超时"
            errorCode == ERROR_HOST_LOOKUP -> "无法连接到服务器"
            else -> getString(R.string.error_title)
        }

        errorMessage.text = when {
            !isNetworkAvailable -> getString(R.string.error_message)
            else -> description.ifEmpty { getString(R.string.error_message) }
        }

        errorView.visibility = View.VISIBLE
        webView.visibility = View.GONE
    }

    private fun hideError() {
        isErrorShowing = false
        errorView.visibility = View.GONE
        webView.visibility = View.VISIBLE
    }

    private fun buildCustomUserAgent(): String {
        val defaultUA = WebView(this).settings.userAgentString ?: ""
        return "$defaultUA AIHot-Android/1.0"
    }

    /**
     * Custom WebViewClient with error handling
     */
    private inner class AIHotWebClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (!isErrorShowing) {
                progressBar.visibility = View.VISIBLE
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressBar.visibility = View.GONE
            swipeRefresh.isRefreshing = false
            isFirstLoad = false

            if (isErrorShowing && isNetworkAvailable) {
                hideError()
            }
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            // Only show error for main frame failures
            if (request?.isForMainFrame == true && error != null) {
                showError(error.errorCode, error.description?.toString() ?: "")
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url?.toString() ?: return false

            // Keep all aihot.virxact.com URLs in the WebView
            if (url.contains("aihot.virxact.com")) {
                return false
            }

            // Open external links in the browser
            try {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(url)
                )
                startActivity(intent)
            } catch (e: Exception) {
                // No browser found, load in WebView
                return false
            }
            return true
        }
    }

    /**
     * Custom WebChromeClient for progress tracking
     */
    private inner class AIHotChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            if (!isErrorShowing) {
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.VISIBLE
                }
            }
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            // Could update action bar title here if desired
        }
    }

    override fun onDestroy() {
        unregisterNetworkCallbackSafe()
        webView.apply {
            stopLoading()
            destroy()
        }
        super.onDestroy()
    }

    private fun unregisterNetworkCallbackSafe() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
            // Already unregistered
        }
    }
}

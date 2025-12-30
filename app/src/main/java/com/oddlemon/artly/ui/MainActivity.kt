package com.oddlemon.artly.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.oddlemon.artly.R
import com.oddlemon.artly.util.NetworkMonitor
import com.oddlemon.artly.util.WebAppInterface
import com.oddlemon.artly.util.WebViewDownloadListener
import com.oddlemon.artly.util.applyAppWebViewSettings

class MainActivity : AppCompatActivity() {

    private val baseUrl = "https://artly.soundgram.co.kr/"

    private lateinit var myWebView: WebView
    private var backPressedTime: Long = 0
    private lateinit var layoutOffline: LinearLayout
    private lateinit var btnRetry: Button

    private lateinit var networkMonitor: NetworkMonitor

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_main)

        layoutOffline = findViewById(R.id.layoutOffline)
        btnRetry = findViewById(R.id.btnRetry)
        myWebView = findViewById(R.id.webview)

        myWebView.setBackgroundColor(Color.TRANSPARENT)
        myWebView.applyAppWebViewSettings(this)
        myWebView.setDownloadListener(WebViewDownloadListener(this))

        // 일단 출시 전까지는 이렇게 해결 ㅠㅠ
        myWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                val fixMultipartScript = """
                    (function() {
                        var originalSetRequestHeader = XMLHttpRequest.prototype.setRequestHeader;
                        XMLHttpRequest.prototype.setRequestHeader = function(header, value) {
                            if (header && header.toLowerCase() === 'content-type' && value.indexOf('multipart/form-data') !== -1) {
                                return; 
                            }
                            originalSetRequestHeader.apply(this, arguments);
                        };
                    })();
                """.trimIndent()

                view?.evaluateJavascript(fixMultipartScript, null)
            }
        }

        myWebView.addJavascriptInterface(WebAppInterface(this), "Android")

        networkMonitor = NetworkMonitor(this)

        networkMonitor.onNetworkChanged = { isConnected ->
            runOnUiThread {
                handleNetworkChange(isConnected)
            }
        }

        btnRetry.setOnClickListener {
            if (networkMonitor.isConnected()) {
                handleNetworkChange(true)
            } else {
                Toast.makeText(this, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        if (networkMonitor.isConnected()) {
            if (myWebView.url.isNullOrEmpty()) {
                myWebView.loadUrl(baseUrl)
            }
            handleNetworkChange(true)
        } else {
            handleNetworkChange(false)
        }

        handleSystemBarInsets()
        setupBackPress()
        askNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        networkMonitor.startMonitoring()
    }

    override fun onPause() {
        super.onPause()
        networkMonitor.stopMonitoring()
    }

    private fun handleNetworkChange(isConnected: Boolean) {
        if (isConnected) {
            layoutOffline.visibility = View.GONE
            myWebView.visibility = View.VISIBLE

            if (myWebView.url.isNullOrEmpty()) {
                myWebView.loadUrl(baseUrl)
            }
        } else {
            layoutOffline.visibility = View.VISIBLE
            myWebView.visibility = View.GONE
        }
    }

    private fun handleSystemBarInsets() {
        val rootView = findViewById<ViewGroup>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupBackPress() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (myWebView.canGoBack()) {
                    myWebView.goBack()
                } else {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime > backPressedTime + 2000) {
                        backPressedTime = currentTime
                        Toast.makeText(this@MainActivity, R.string.app_end, Toast.LENGTH_SHORT).show()
                        return
                    } else {
                        finish()
                    }
                }
            }
        }
        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
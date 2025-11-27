package com.oddlemon.artly.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.oddlemon.artly.R
import com.oddlemon.artly.util.WebAppInterface
import com.oddlemon.artly.util.applyAppWebViewSettings

class MainActivity : AppCompatActivity() {

    private val baseUrl = "https://artly.soundgram.co.kr/"

    private lateinit var myWebView: WebView
    private var backPressedTime: Long = 0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "알림 권한 허용됨")
        } else {
            Log.w("FCM", "알림 권한 거부됨")
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_main)

        myWebView = findViewById(R.id.webview)
        myWebView.setBackgroundColor(Color.TRANSPARENT)

        myWebView.applyAppWebViewSettings(this)


        handleSystemBarInsets()
        setupBackPress()
        askNotificationPermission()

        myWebView.loadUrl(baseUrl)
        myWebView.addJavascriptInterface(WebAppInterface(this), "Android")

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
                        Toast.makeText(this@MainActivity,R.string.app_end, Toast.LENGTH_SHORT).show()
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
        // 안드로이드 13 (API 33) 이상인지 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // 이미 권한이 있음
                Log.d("FCM", "이미 알림 권한이 있습니다.")
            } else {
                // 권한이 없어서 팝업 띄움
                Log.d("FCM", "알림 권한 요청 팝업 띄움")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
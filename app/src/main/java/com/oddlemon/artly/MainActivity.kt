package com.oddlemon.artly

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.oddlemon.artly.BuildConfig

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myWebView = WebView(this)
        setContentView(myWebView)

        myWebView.loadUrl(BuildConfig.ARTLY_URL)

        myWebView.settings.javaScriptEnabled = true

        myWebView.webViewClient = WebViewClient()

    }
}
package com.oddlemon.artly.util

import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import com.oddlemon.artly.ui.MainActivity

fun WebView.applyAppWebViewSettings(activity: MainActivity) {
    this.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        cacheMode = WebSettings.LOAD_DEFAULT
        loadWithOverviewMode = true
        useWideViewPort = true
        textZoom = 100
        allowFileAccess = true
        setGeolocationEnabled(true)
        setSupportMultipleWindows(false)
        mediaPlaybackRequiresUserGesture = true
        setGeolocationDatabasePath(context.filesDir.path)
    }

    this.webChromeClient = WebChromeClient(activity)
}
package com.oddlemon.artly.util

import android.content.Context
import android.webkit.JavascriptInterface
import android.util.Log
import com.oddlemon.artly.util.TokenManager // 💡 TokenManager import

class WebAppInterface(private val context: Context) {
    companion object {
        private const val TAG = "WebAppInterface"
    }

    @JavascriptInterface
    fun setUserId(userId: String) {
        Log.d(TAG, "WebView로부터 userId 수신: $userId")
        val id = userId.toIntOrNull() ?: 0
        TokenManager.saveUserId(context, id)
    }

    @JavascriptInterface
    fun getFcmToken(): String? {
        val token = TokenManager.getToken(context)
        Log.d(TAG, "WebView에서 FCM 토큰 요청. 결과: ${if (token != null) "발급됨" else "없음"}")
        return token
    }
}
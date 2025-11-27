package com.oddlemon.artly.util

import android.content.Context
import android.util.Log
import com.oddlemon.artly.data.module.ApiModule
import com.oddlemon.artly.data.request.RegisterTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TokenManager {
    private const val TAG = "TokenManager"
    private const val PREF_NAME = "firebaseToken"
    private const val KEY_TOKEN = "firebaseToken"
    private const val KEY_USER_ID = "userId"

    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_TOKEN, token)
            apply()
        }
    }

    fun getToken(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)
    }

    fun saveUserId(context: Context, userId: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putInt(KEY_USER_ID, userId)
            apply()
        }
    }

    fun getUserId(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_USER_ID, -1)
    }

    fun sendTokenToServer(context: Context, userId: Int, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = RegisterTokenRequest(
                    user_id = userId,
                    fcm_token = token
                )

                val response = ApiModule.artlyService.registerToken(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Log.d(TAG, "토큰 등록 성공: ${body?.message}")
                        saveUserId(context, userId)
                    } else {
                        Log.e(TAG, "토큰 등록 실패: ${response.code()} - ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "토큰 전송 중 오류 발생", e)
            }
        }
    }
}

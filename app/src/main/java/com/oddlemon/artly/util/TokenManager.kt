package com.oddlemon.artly.util

import android.content.Context
import android.util.Log
import com.oddlemon.artly.data.module.ApiModule
import com.oddlemon.artly.data.request.RegisterTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object TokenManager {
    private const val TAG = "TokenManager"
    private const val PREF_NAME = "firebaseToken"
    private const val KEY_TOKEN = "firebaseToken"
    private const val KEY_USER_ID = "userId"

    // 토큰 저장
    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_TOKEN, token)
            apply()
        }
    }

    // 토큰 가져오기
    fun getToken(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)
    }

    // 유저 ID 저장
    fun saveUserId(context: Context, userId: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putInt(KEY_USER_ID, userId)
            apply()
        }
    }

    // 유저 ID 가져오기
    fun getUserId(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_USER_ID, -1)
    }

    // 서버로 토큰 전송 (Context 인자 삭제함 -> 불필요)
    fun sendTokenToServer(userId: Int, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = RegisterTokenRequest(
                    user_id = userId,
                    fcm_token = token
                )

                val response = ApiModule.artlyService.registerToken(request)

                if (response.isSuccessful) {
                    Log.d(TAG, "토큰 서버 전송 성공: ${response.body()?.message}")
                    // 여기서 saveUserId를 또 할 필요 없음 (이미 되어있음)
                } else {
                    Log.e(TAG, "토큰 서버 전송 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "토큰 전송 중 에러", e)
            }
        }
    }
}
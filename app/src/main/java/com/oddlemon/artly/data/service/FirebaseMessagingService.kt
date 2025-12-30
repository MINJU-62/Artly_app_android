package com.oddlemon.artly.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.oddlemon.artly.R
import com.oddlemon.artly.ui.MainActivity
import com.oddlemon.artly.util.TokenManager
import java.util.concurrent.atomic.AtomicInteger

class FirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_Service"
        private const val CHANNEL_ID = "fcm_channel"
        private const val CHANNEL_NAME = "FCM 알림"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New Token: $token")

        // 토큰 저장
        TokenManager.saveToken(this, token)

        // 저장된 userId가 있으면 서버로 전송
        val userId = TokenManager.getUserId(this)
        if (userId != -1) {
            TokenManager.sendTokenToServer(userId, token)
        } else {
            Log.d(TAG, "userId가 없습니다. 로그인 후 토큰이 전송됩니다.")
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "From: ${message.from}")

        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
        }

        message.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.title, it.body, message.data)
        }
    }

    private fun sendNotification(title: String?, body: String?, data: Map<String, String>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            for ((key, value) in data) {
                putExtra(key, value)
            }
        }

        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val notificationId = NotificationID.atomicId.incrementAndGet()
        val pendingIntent = PendingIntent.getActivity(this, notificationId, intent, pendingIntentFlag)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}

object NotificationID {
    val atomicId = AtomicInteger(0)
}
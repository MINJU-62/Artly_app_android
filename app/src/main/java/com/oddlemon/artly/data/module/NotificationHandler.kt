package com.oddlemon.artly.data.module

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.oddlemon.artly.ui.MainActivity
import java.util.concurrent.atomic.AtomicInteger

class NotificationHandler(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "fcm_channel"
        private const val CHANNEL_NAME = "FCM 알림"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(title: String?, body: String?, data: Map<String, String>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
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
        val pendingIntent = PendingIntent.getActivity(context, notificationId, intent, pendingIntentFlag)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title) // 제목 설정
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}

// 알림 ID를 중복 되지 않도록
object NotificationID {
    val atomicId = AtomicInteger(0)
}
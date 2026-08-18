package com.example.lionideaton.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

private const val CHANNEL_ID = "skin_basket_reminders"
private const val KEY_TITLE = "title"
private const val KEY_MESSAGE = "message"
private const val KEY_NOTIFICATION_ID = "notificationId"

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val message = inputData.getString(KEY_MESSAGE) ?: ""
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, 0)
        showReminderNotification(applicationContext, notificationId, title, message)
        return Result.success()
    }

    companion object {
        fun inputData(title: String, message: String, notificationId: Int) = workDataOf(
            KEY_TITLE to title,
            KEY_MESSAGE to message,
            KEY_NOTIFICATION_ID to notificationId
        )
    }
}

fun ensureReminderChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "Skin Basket 알림", NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)
    }
}

private fun showReminderNotification(context: Context, notificationId: Int, title: String, message: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle(title)
        .setContentText(message)
        .setAutoCancel(true)
        .build()
    NotificationManagerCompat.from(context).notify(notificationId, notification)
}

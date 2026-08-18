package com.example.lionideaton.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

// WorkManager's minimum periodic interval is 15 minutes — real "식사 마친 시간"/scheduled-time
// reminders would need exact-alarm scheduling instead, but this is enough to demo actual
// notifications firing without that extra complexity.
enum class ReminderType(val workName: String, val notificationId: Int, val title: String, val message: String) {
    MEAL_LOG(
        workName = "meal_log_reminder",
        notificationId = 1001,
        title = "식사 기록할 시간이에요",
        message = "오늘 먹은 음식을 기록해보세요!"
    ),
    SKIN_PHOTO(
        workName = "skin_photo_reminder",
        notificationId = 1002,
        title = "피부 사진 촬영 리마인더",
        message = "오늘의 피부 상태를 기록해보세요!"
    )
}

object ReminderScheduler {
    private val INTERVAL = 15L to TimeUnit.MINUTES

    fun schedule(context: Context, type: ReminderType) {
        ensureReminderChannel(context)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(INTERVAL.first, INTERVAL.second)
            .setInputData(ReminderWorker.inputData(type.title, type.message, type.notificationId))
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            type.workName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context, type: ReminderType) {
        WorkManager.getInstance(context).cancelUniqueWork(type.workName)
    }
}

package com.dtetu.gptfriend.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Manages scheduling of periodic "miss you" notifications
 */
object NotificationScheduler {
    
    private const val WORK_NAME = "miss_you_notifications"

    /**
     * Schedule periodic notifications every minute
     */
    fun scheduleNotifications(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<MissYouNotificationWorker>(
            1, TimeUnit.MINUTES // Every 1 minute
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Cancel all scheduled notifications
     */
    fun cancelNotifications(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Check if notifications are currently scheduled
     */
    fun areNotificationsScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WORK_NAME).get()
        return workInfos.any { !it.state.isFinished }
    }
}

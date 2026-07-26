package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.*
import com.example.data.model.Bill
import java.util.concurrent.TimeUnit

object NotificationHelper {
    private const val CHANNEL_ID = "bill_reminders"
    private const val CHANNEL_NAME = "Bill Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications for upcoming bill payments"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleBillNotification(context: Context, bill: Bill) {
        if (!bill.isNotificationEnabled || bill.isPaid) {
            cancelBillNotification(context, bill.id)
            return
        }

        val now = System.currentTimeMillis()
        val delay = bill.dueDate - now

        if (delay > 0) {
            val data = workDataOf(
                "bill_name" to bill.name,
                "bill_amount" to bill.amount,
                "bill_id" to bill.id
            )

            val notificationRequest = OneTimeWorkRequestBuilder<BillNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("bill_${bill.id}")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "bill_notification_${bill.id}",
                ExistingWorkPolicy.REPLACE,
                notificationRequest
            )
        }
    }

    fun cancelBillNotification(context: Context, billId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork("bill_notification_$billId")
    }
}

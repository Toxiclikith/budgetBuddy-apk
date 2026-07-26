package com.example.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.MainActivity
import java.util.Locale

class BillNotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val billName = inputData.getString("bill_name") ?: "Upcoming Bill"
        val billAmount = inputData.getDouble("bill_amount", 0.0)
        val billId = inputData.getInt("bill_id", 0)

        showNotification(billName, billAmount, billId)
        return Result.success()
    }

    private fun showNotification(name: String, amount: Double, id: Int) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            id, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(applicationContext, "bill_reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using standard icon for now
            .setContentTitle("Bill Reminder: $name")
            .setContentText("Your payment of $${String.format(Locale.getDefault(), "%.2f", amount)} is due today!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(id, builder.build())
        }
    }
}

package se.umu.calu0217.smartcalendar.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import se.umu.calu0217.smartcalendar.data.ReminderWorker

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: return
        WorkManager.getInstance(ctx).enqueue(
            OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(workDataOf("title" to title))
                .build()
        )
    }
}

package com.jiuntian.hydratedme.broadcast

import android.app.IntentService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.jiuntian.hydratedme.R
import com.jiuntian.hydratedme.controller.DrinkRecordsController
import com.jiuntian.hydratedme.controller.PetDataController
import com.jiuntian.hydratedme.controller.ScheduleController
import com.jiuntian.hydratedme.model.DrinkType
import com.jiuntian.hydratedme.util.Util
import java.util.*

class DoneACupReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null) {
            // Handle the action to set the Medicine Administered
            if (intent.action!!.equals(context.getString(R.string.action_water_drinked), ignoreCase = true)) {

                val extras = intent.extras
                if (extras != null) {

                    val notificationId = extras.getInt(NOTIFICATION_ID)

                    val reminderId = extras.getInt("Id")
//                    val medicineAdministered = extras.getBoolean("drinked")

                    // Lookup the reminder for sanity
                    val reminderData = Util.getReminderById(reminderId)

                    // Update the database
                    val cupSize : Int = Util.getPreference(context, context.getString(R.string.cup_size))

                    //DrinkRecordsController.addDrinkRecord(Date(), cupSize, DrinkType.WATER, context)
                    val workManager = WorkManager.getInstance(context)
                    val doneACupWorkRequest = DoneACupIntentWorker.buildWorkRequest(Date(), cupSize)
//                    workManager.enqueueUniqueWork(DoneACupIntentWorker.UNIQUE_WORK_NAME, ExistingWorkPolicy.APPEND, doneACupWorkRequest)
                    workManager.enqueue(doneACupWorkRequest)

                    // Remove the alarm
                    ScheduleController.removeAlarmsForReminder(context, reminderData)

                    // finally, cancel the notification
                    if (notificationId != -1) {
                        val notificationManager = NotificationManagerCompat.from(context)
                        notificationManager.cancel(notificationId)
                    }
                }
            }
        }
    }

    class DoneACupIntentWorker(context : Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
        companion object {
            private const val CUP_SIZE = "CUP_SIZE"
            private const val DATE = "DATE"
            const val UNIQUE_WORK_NAME = "DONE_A_CUP_INTENT_WORKER"

            fun buildWorkRequest(date: Date, cupSize: Int): OneTimeWorkRequest {
                val data = Data.Builder()
                    .putLong(DATE, date.time)
                    .putInt(CUP_SIZE,cupSize)
                    .build()
                return OneTimeWorkRequestBuilder<DoneACupIntentWorker>().apply { setInputData(data) }.build()
            }
        }

        override fun doWork(): Result {
            Log.d("broadcast", "worker doWork")
            val cupSize: Int = inputData.getInt(CUP_SIZE, 0)
            val date = Date(inputData.getLong(DATE, 0L))
            DrinkRecordsController.addDrinkRecord(date, cupSize, DrinkType.WATER, this.applicationContext)
            return Result.success()
        }

    }
}
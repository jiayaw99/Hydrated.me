package com.jiuntian.hydratedme.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver
import com.jiuntian.hydratedme.R
import com.jiuntian.hydratedme.controller.ScheduleController
import com.jiuntian.hydratedme.util.Util

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("alarm", "alarm received, creating notification")
        if (intent.action != null) {
            if (intent.action!!.equals(context.getString(R.string.action_notify_drink_water), ignoreCase = true)) {
                if (intent.extras != null) {
                    val pm : PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                    val wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "alarm:wakelock")
                    wakeLock.acquire(5000L /*1 second*/)

                    val reminderData = Util.getReminderById(intent.extras!!.getInt("Id"))
                    ScheduleController.createNotificationForWater(context, reminderData)

                    // set the alarm again
                    ScheduleController.scheduleAlarmsForReminder(context, reminderData)
                    wakeLock.release()
                }
            }
        }
    }
}
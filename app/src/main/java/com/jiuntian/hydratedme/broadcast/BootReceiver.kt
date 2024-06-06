package com.jiuntian.hydratedme.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jiuntian.hydratedme.controller.ScheduleController

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED"){
            // schedule push notification
            ScheduleController.scheduleAlarmsForData(context)
        }
    }
}
package com.jiuntian.hydratedme.controller

import android.app.*
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.jiuntian.hydratedme.R
import com.jiuntian.hydratedme.activity.MainActivity
import com.jiuntian.hydratedme.broadcast.AlarmReceiver
import com.jiuntian.hydratedme.broadcast.DoneACupReceiver
import com.jiuntian.hydratedme.model.ReminderData
import com.jiuntian.hydratedme.util.Util
import java.util.*
import kotlin.collections.ArrayList

object ScheduleController {
    private const val ADMINISTER_REQUEST_CODE = 2019

    fun getNextIntakeTime(context: Context) : ReminderData{
        val wakeUpTime = Util.getTimePreference(context, context.getString(R.string.wake_up_time))
        val sleepTime = Util.getTimePreference(context, context.getString(R.string.sleep_time))
        val schedules: ArrayList<ReminderData> = getSchedules(wakeUpTime, sleepTime)
        var currentHour = Date().hours
        var prevHour = 0

        if (currentHour < schedules[0].hour) {
            currentHour += 24
        }

        for (schedule in schedules) {
            var thisHour = schedule.hour
            if(prevHour>=schedule.hour){
                thisHour += 24
            }
            if(thisHour > currentHour){
                return schedule
            }
            prevHour = schedule.hour
        }
        return schedules[0]
    }

    fun scheduleAlarmsForData(context: Context) {
        val wakeUpTime = Util.getTimePreference(context, context.getString(R.string.wake_up_time))
        val sleepTime = Util.getTimePreference(context, context.getString(R.string.sleep_time))
        val schedules: ArrayList<ReminderData> = getSchedules(wakeUpTime, sleepTime)

        for (schedule in schedules) {
            scheduleAlarmsForReminder(context, ReminderData(schedule.hour, schedule.minute))
        }
    }

    private fun getSchedules(wakeUpTime: IntArray, sleepTime: IntArray): ArrayList<ReminderData> {
        val wakeUpHour = wakeUpTime[0]
        var sleepHour = sleepTime[0]
        if (sleepHour >= 0) {
            sleepHour += 24
        }
        val result = ArrayList<ReminderData>()
        for (i in wakeUpHour..sleepHour step 2) {
            val hour = i % 24
            val min = wakeUpTime[1]
            result.add(ReminderData(hour, min))
        }
        return result
    }

    // new try

    fun createNotificationChannel(
        context: Context, importance: Int, showBadge: Boolean,
        name: String, description: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val channelId = "${context.packageName}-$name"
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setShowBadge(showBadge)
            channel.enableVibration(true)
            channel.setSound(soundUri, null)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createSampleDataNotification(context: Context, title: String, message: String,
                                     bigText: String, autoCancel: Boolean) {
        val channelId = "${context.packageName}-${context.getString(R.string.app_name)}"
        val notificationBuilder = NotificationCompat.Builder(context, channelId).apply{
            setSmallIcon(R.drawable.ic_notification_icon_24)
            setContentTitle(title)
            setContentText(message)
            setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            priority = NotificationCompat.PRIORITY_HIGH
            setAutoCancel(autoCancel)
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            setContentIntent(pendingIntent)
        }
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1001, notificationBuilder.build())
    }

    fun scheduleAlarmsForReminder(context: Context, reminderData: ReminderData) {
        // get the AlarmManager reference
        val alarmMgr = context.getSystemService(ALARM_SERVICE) as AlarmManager

        // Schedule the alarms
        // get the PendingIntent for the alarm
        val alarmIntent = createPendingIntent(context, reminderData)

        // schedule the alarm
        scheduleAlarm(reminderData, alarmIntent, alarmMgr)
    }

    private fun scheduleAlarm(reminderData: ReminderData, alarmIntent: PendingIntent?, alarmMgr: AlarmManager) {
        val datetimeToAlarm = Calendar.getInstance(Locale.getDefault())
        datetimeToAlarm.timeInMillis = System.currentTimeMillis()

//        if (datetimeToAlarm.get(Calendar.HOUR_OF_DAY) > reminderData.hour || // cond 1
//            (datetimeToAlarm.get(Calendar.HOUR_OF_DAY) == reminderData.hour &&
//                datetimeToAlarm.get(Calendar.MINUTE) > reminderData.minute)  ) {
//            datetimeToAlarm.add(Calendar.DAY_OF_MONTH, 1)
//        }

        datetimeToAlarm.set(Calendar.HOUR_OF_DAY, reminderData.hour)
        datetimeToAlarm.set(Calendar.MINUTE, reminderData.minute)
        datetimeToAlarm.set(Calendar.SECOND, 0)
        datetimeToAlarm.set(Calendar.MILLISECOND, 0)

        if (datetimeToAlarm.timeInMillis < System.currentTimeMillis()) {
            datetimeToAlarm.add(Calendar.DAY_OF_MONTH, 1)
        }

//        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
//            datetimeToAlarm.timeInMillis, AlarmManager.INTERVAL_DAY, alarmIntent)
        alarmMgr.setExact(AlarmManager.RTC_WAKEUP,
            datetimeToAlarm.timeInMillis, alarmIntent)
    }

    fun removeAlarmsForReminder(context: Context, reminderData: ReminderData) {
        val intent = Intent(context.applicationContext, AlarmReceiver::class.java)
        intent.action = context.getString(R.string.action_notify_drink_water)
        intent.putExtra("Id", reminderData.id)

        // type must be unique so Intent.filterEquals passes the check to make distinct PendingIntents
        // Schedule the alarms based on the days to administer the medicine

        val type = "${reminderData.hour}-${reminderData.minute}-water"

        intent.type = type
        val alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmMgr = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmMgr.cancel(alarmIntent)
    }

    private fun createPendingIntent(context: Context, reminderData: ReminderData): PendingIntent? {
        val intent = Intent(context.applicationContext, AlarmReceiver::class.java).apply {
            //
            action = context.getString(R.string.action_notify_drink_water)
            //
            type = "${reminderData.hour}-${reminderData.minute}-water"
            //
            putExtra("Id", reminderData.id)
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    fun createNotificationForWater(context: Context, reminderData: ReminderData) {
        Log.d("notification", "createNotificationForWater")
        val groupBuilder = buildGroupNotification(context, reminderData)
        val notificationBuilder = buildNotificationForWater(context, reminderData)
        val administerPendingIntent = createPendingIntentForAction(context, reminderData)
        notificationBuilder.addAction(
            R.drawable.ic_notification_icon_24,
            context.getString(R.string.done_a_cup),
            administerPendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, groupBuilder.build()) //group 1 = water
        notificationManager.notify(reminderData.notificationId, notificationBuilder.build())
    }

    private fun buildGroupNotification(context: Context, reminderData: ReminderData): NotificationCompat.Builder {
        val channelId = "${context.packageName}-${context.getString(R.string.app_name)}"
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.ic_notification_icon_24)
            setContentTitle("Drink Reminders")
            setContentText("It's time to get a cup of water!")
            setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's time to get a cup of water!"))
            setAutoCancel(true)
            setGroupSummary(true)
            setGroup("WATER_REMINDER")
        }
    }

    private fun buildNotificationForWater(context: Context, reminderData: ReminderData): NotificationCompat.Builder {
        // 1
        val channelId = "${context.packageName}-${context.getString(R.string.app_name)}"
        return NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.ic_notification_icon_24)
            setContentTitle("It's $reminderData")
            setAutoCancel(true)
            setLargeIcon(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_notification_icon_24, null)
                ?.toBitmap())
            setContentText("It's time to get a cup of water!")
            // 4
            setGroup("WATER_REMINDER")
            setStyle(NotificationCompat.BigTextStyle()
                .bigText("Click \"Done A Cup\" to record a cup of water, or click elsewhere for others."))
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("Id", reminderData.id)
            }
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            setContentIntent(pendingIntent)

            setDefaults(Notification.DEFAULT_SOUND)
            setDefaults(Notification.DEFAULT_VIBRATE)
        }
    }

    private fun createPendingIntentForAction(context: Context, reminderData: ReminderData): PendingIntent? {
        // 1
        val administerIntent = Intent(context, DoneACupReceiver::class.java).apply {
            action = context.getString(R.string.action_water_drinked)
            type = "${reminderData.hour}-${reminderData.minute}-drunk"
            putExtra(DoneACupReceiver.NOTIFICATION_ID, reminderData.notificationId)
            putExtra("Id", reminderData.id)
            putExtra("drinked", true)
        }
// 2
        return PendingIntent.getBroadcast(context, ADMINISTER_REQUEST_CODE, administerIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    }

}
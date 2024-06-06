package com.jiuntian.hydratedme

import android.app.Application
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat
import com.jiuntian.hydratedme.controller.ScheduleController

class App: Application() {
    companion object {
        lateinit var instance: App private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        ScheduleController.createNotificationChannel(this,
            NotificationManagerCompat.IMPORTANCE_HIGH,
            false,
            getString(R.string.app_name),
            "App notification channel")
    }
}

object Strings {
    fun get(@StringRes stringRes: Int, vararg formatArgs: Any = emptyArray()): String {
        return App.instance.getString(stringRes, *formatArgs)
    }
}
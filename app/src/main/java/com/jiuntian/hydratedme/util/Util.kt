package com.jiuntian.hydratedme.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.jiuntian.hydratedme.R
import com.jiuntian.hydratedme.model.ReminderData

class Util {
    companion object {
        fun getPreference(context: Context, key: String, default: String = "200"): Int {
            val preference = PreferenceManager.getDefaultSharedPreferences(context)
            val value: String = preference.getString(key, default)!!
            return Integer.valueOf(value)
        }

        fun getTimePreference(context: Context, key: String): IntArray {
            val preference = PreferenceManager.getDefaultSharedPreferences(context)
            val wakeUpTimeString: String = preference.getString(key, getDefaultTimeByKey(context, key))!!
            val hourMinuteString = wakeUpTimeString.split(":")
            assert(hourMinuteString.size == 2)
            val hour = hourMinuteString[0].toInt()
            val minute = hourMinuteString[1].toInt()
            return intArrayOf(hour, minute)
        }

        private fun getDefaultTimeByKey(context: Context, key: String): String {
            return when (key) {
                context.getString(R.string.wake_up_time) -> "08:00"
                context.getString(R.string.sleep_time) -> "00:00"
                else -> "00:00"
            }
        }

        fun getReminderById(int: Int): ReminderData {
            val hour = int / 60
            val minute = int % 60
            return ReminderData(hour, minute)
        }

        fun getRecommendedIntake(kg: Double): Int {
            return (kg * 33.0).toInt()
        }

        fun updateIntakeGoal(context: Context, kg: Double, key: String): String {
            val newIntake = getRecommendedIntake(kg)
            val preference = PreferenceManager.getDefaultSharedPreferences(context)
            preference.edit().putString(key, newIntake.toString()).apply()
            return newIntake.toString()
        }

        fun getBatteryOptimizationSettingActivity(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent()
                val packageName = context.packageName
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Already fixed. Great job!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
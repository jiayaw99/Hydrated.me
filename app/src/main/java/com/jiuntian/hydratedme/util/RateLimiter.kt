package com.jiuntian.hydratedme.util

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import android.util.ArrayMap
import androidx.preference.PreferenceManager

class RateLimiter(private val timeout: Long = 1*60*1000) {

    companion object {
        private const val SHARED_PREFERENCE_ID = "rate_limiter"
    }

    fun getTime(key : String, context: Context) : Long? {
        val pref = context.getSharedPreferences(SHARED_PREFERENCE_ID, Context.MODE_PRIVATE)
        return (pref.all[key] as Long?)
    }

    fun setTime(key: String, value: Long, context: Context) {
        val pref = context.getSharedPreferences(SHARED_PREFERENCE_ID, Context.MODE_PRIVATE)
        return pref.edit().putLong(key, value).apply()
    }

    @Synchronized
    fun shouldRun(key: String, context: Context): Boolean {
        val lastRun = getTime(key, context)
        val now = now()
        if (lastRun == null) {
            setTime(key, now, context)
            return true
        }
        if (now - lastRun > timeout) {
            setTime(key, now, context)
            return true
        }
        return false
    }

    private fun now(): Long {
        return System.currentTimeMillis()
    }

    @Synchronized
    fun reset(key: String, context: Context) {
        val pref = context.getSharedPreferences(SHARED_PREFERENCE_ID, Context.MODE_PRIVATE)
        return pref.edit().remove(key).apply()
    }
}
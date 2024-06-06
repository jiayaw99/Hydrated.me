package com.jiuntian.hydratedme.model

class ReminderData(val hour: Int, val minute: Int) {
    var id = hour*60+minute
    var notificationId = id + 10000

    override fun toString(): String {
        return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }

    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReminderData

        if (hour != other.hour) return false
        if (minute != other.minute) return false

        return true
    }
}
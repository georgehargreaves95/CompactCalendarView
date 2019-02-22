package com.github.sundeepk.RotaCalendar

import com.github.sundeepk.RotaCalendar.domain.Event

internal class Events(val timeInMillis: Long, val events: ArrayList<Event>?) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val event = other as Events?

        if (timeInMillis != event!!.timeInMillis) return false
        return !if (events != null) events != event.events else event.events != null

    }

    override fun hashCode(): Int {
        var result = events?.hashCode() ?: 0
        result = 31 * result + (timeInMillis xor timeInMillis.ushr(32)).toInt()
        return result
    }

    override fun toString(): String {
        return "Events{" +
                "events=" + events +
                ", timeInMillis=" + timeInMillis +
                '}'.toString()
    }
}

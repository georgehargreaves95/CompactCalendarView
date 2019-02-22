package com.github.sundeepk.RotaCalendar.domain

class Event {

    var color: Int = 0
        private set
    var timeInMillis: Long = 0
        private set
    lateinit var data: Any

    constructor(color: Int, timeInMillis: Long) {
        this.color = color
        this.timeInMillis = timeInMillis
    }

    constructor(color: Int, timeInMillis: Long, data: Any) {
        this.color = color
        this.timeInMillis = timeInMillis
        this.data = data
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val event = other as Event?

        if (color != event!!.color) return false
        if (timeInMillis != event.timeInMillis) return false
        return data != event.data

    }

    override fun hashCode(): Int {
        var result = color
        result = 31 * result + (timeInMillis xor timeInMillis.ushr(32)).toInt()
        result = 31 * result + (data.hashCode())
        return result
    }

    override fun toString(): String {
        return "Event{" +
                "color=" + color +
                ", timeInMillis=" + timeInMillis +
                ", data=" + data +
                '}'.toString()
    }
}

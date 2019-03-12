package com.github.sundeepk.rotacalendar.comparators

import com.github.sundeepk.rotacalendar.events.Event

import java.util.Comparator

class EventComparator : Comparator<Event> {

    override fun compare(lhs: Event, rhs: Event): Int {
        return if (lhs.timeInMillis < rhs.timeInMillis) -1 else if (lhs.timeInMillis == rhs.timeInMillis) 0 else 1
    }
}

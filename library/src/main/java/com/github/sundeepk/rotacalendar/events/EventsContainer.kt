package com.github.sundeepk.rotacalendar.events

import com.github.sundeepk.rotacalendar.comparators.EventComparator

import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.HashMap

class EventsContainer(private val eventsCalendar: Calendar) {

    private val eventsByMonthAndYearMap = HashMap<String, MutableList<Events>>()
    private val eventsComparator = EventComparator()

    internal fun addEvent(event: Event) {
        eventsCalendar.timeInMillis = event.timeInMillis
        val key = getKeyForCalendarEvent(eventsCalendar)
        var eventsForMonth: MutableList<Events>? = eventsByMonthAndYearMap[key]
        if (eventsForMonth == null) {
            eventsForMonth = ArrayList()
        }
        val eventsForTargetDay = getEventDayEvent(event.timeInMillis)
        if (eventsForTargetDay == null) {
            val events = ArrayList<Event>()
            events.add(event)
            eventsForMonth.add(
                Events(
                    event.timeInMillis,
                    events
                )
            )
        } else {
            eventsForTargetDay.events!!.add(event)
        }
        eventsByMonthAndYearMap[key] = eventsForMonth
    }

    internal fun removeAllEvents() {
        eventsByMonthAndYearMap.clear()
    }

    internal fun addEvents(events: List<Event>) {
        val count = events.size
        for (i in 0 until count) {
            addEvent(events[i])
        }
    }

    internal fun getEventsFor(epochMillis: Long): List<Event>? {
        val events = getEventDayEvent(epochMillis)
        return if (events == null) {
            ArrayList()
        } else {
            events.events
        }
    }

    internal fun getEventsForMonthAndYear(month: Int, year: Int): List<Events> {
        return eventsByMonthAndYearMap[year.toString() + "_" + month]?.toList().orEmpty()
    }

    internal fun getEventsForMonth(eventTimeInMillis: Long): List<Event> {
        eventsCalendar.timeInMillis = eventTimeInMillis
        val keyForCalendarEvent = getKeyForCalendarEvent(eventsCalendar)
        val events = eventsByMonthAndYearMap[keyForCalendarEvent]
        val allEventsForMonth = ArrayList<Event>()
        if (events != null) {
            for (eve in events) {
                allEventsForMonth.addAll(eve.events.orEmpty())
            }
        }
        Collections.sort(allEventsForMonth, eventsComparator)
        return allEventsForMonth
    }

    private fun getEventDayEvent(eventTimeInMillis: Long): Events? {
        eventsCalendar.timeInMillis = eventTimeInMillis
        val dayInMonth = eventsCalendar.get(Calendar.DAY_OF_MONTH)
        val keyForCalendarEvent = getKeyForCalendarEvent(eventsCalendar)
        val eventsForMonthsAndYear = eventsByMonthAndYearMap[keyForCalendarEvent]
        if (eventsForMonthsAndYear != null) {
            for (events in eventsForMonthsAndYear) {
                eventsCalendar.timeInMillis = events.timeInMillis
                val dayInMonthFromCache = eventsCalendar.get(Calendar.DAY_OF_MONTH)
                if (dayInMonthFromCache == dayInMonth) {
                    return events
                }
            }
        }
        return null
    }

    internal fun removeEventByEpochMillis(epochMillis: Long) {
        eventsCalendar.timeInMillis = epochMillis
        val dayInMonth = eventsCalendar.get(Calendar.DAY_OF_MONTH)
        val key = getKeyForCalendarEvent(eventsCalendar)
        val eventsForMonthAndYear = eventsByMonthAndYearMap[key]
        if (eventsForMonthAndYear != null) {
            val calendarDayEventIterator = eventsForMonthAndYear.iterator()
            while (calendarDayEventIterator.hasNext()) {
                val next = calendarDayEventIterator.next()
                eventsCalendar.timeInMillis = next.timeInMillis
                val dayInMonthFromCache = eventsCalendar.get(Calendar.DAY_OF_MONTH)
                if (dayInMonthFromCache == dayInMonth) {
                    calendarDayEventIterator.remove()
                    break
                }
            }
            if (eventsForMonthAndYear.isEmpty()) {
                eventsByMonthAndYearMap.remove(key)
            }
        }
    }

    internal fun removeEvent(event: Event) {
        eventsCalendar.timeInMillis = event.timeInMillis
        val key = getKeyForCalendarEvent(eventsCalendar)
        val eventsForMonthAndYear = eventsByMonthAndYearMap[key]
        if (eventsForMonthAndYear != null) {
            val eventsForMonthYrItr = eventsForMonthAndYear.iterator()
            while (eventsForMonthYrItr.hasNext()) {
                val events = eventsForMonthYrItr.next()
                val indexOfEvent = events.events!!.indexOf(event)
                if (indexOfEvent >= 0) {
                    if (events.events!!.size == 1) {
                        eventsForMonthYrItr.remove()
                    } else {
                        events.events!!.removeAt(indexOfEvent)
                    }
                    break
                }
            }
            if (eventsForMonthAndYear.isEmpty()) {
                eventsByMonthAndYearMap.remove(key)
            }
        }
    }

    internal fun removeEvents(events: List<Event>) {
        val count = events.size
        for (i in 0 until count) {
            removeEvent(events[i])
        }
    }

    //E.g. 4 2016 becomes 2016_4
    private fun getKeyForCalendarEvent(cal: Calendar): String {
        return cal.get(Calendar.YEAR).toString() + "_" + cal.get(Calendar.MONTH)
    }

}

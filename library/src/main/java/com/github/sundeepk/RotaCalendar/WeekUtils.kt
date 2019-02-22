package com.github.sundeepk.RotaCalendar

import java.text.DateFormatSymbols
import java.util.Arrays
import java.util.Locale

object WeekUtils {

    internal fun getWeekdayNames(
        locale: Locale,
        day: Int,
        useThreeLetterAbbreviation: Boolean
    ): Array<String?> {
        val dateFormatSymbols = DateFormatSymbols(locale)
        val dayNames = dateFormatSymbols.shortWeekdays
            ?: throw IllegalStateException("Unable to determine weekday names from default locale")
        if (dayNames.size != 8) {
            throw IllegalStateException(
                "Expected weekday names from default locale to be of size 7 but: "
                        + Arrays.toString(dayNames) + " with size " + dayNames.size + " was returned."
            )
        }

        val weekDayNames = arrayOfNulls<String>(7)
        val weekDaysFromSunday = arrayOf(
            dayNames[1],
            dayNames[2],
            dayNames[3],
            dayNames[4],
            dayNames[5],
            dayNames[6],
            dayNames[7]
        )
        run {
            var currentDay = day - 1
            var i = 0
            while (i <= 6) {
                currentDay = if (currentDay >= 7) 0 else currentDay
                weekDayNames[i] = weekDaysFromSunday[currentDay]
                i++
                currentDay++
            }
        }

        if (!useThreeLetterAbbreviation) {
            for (i in weekDayNames.indices) {
                weekDayNames[i] = weekDayNames[i].orEmpty().substring(0, 1)
            }
        }

        return weekDayNames
    }


}

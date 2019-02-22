package com.github.sundeepk.RotaCalendar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.VelocityTracker
import android.widget.OverScroller

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import com.github.sundeepk.RotaCalendar.CompactCalendarHelper.getDayEventWith2EventsPerDay
import com.github.sundeepk.RotaCalendar.CompactCalendarHelper.getDayEventWithMultipleEventsPerDay
import com.github.sundeepk.RotaCalendar.CompactCalendarHelper.getEvents
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`

@RunWith(MockitoJUnitRunner::class)
class CompactCalendarControllerTest {

    @Mock
    private val paint: Paint? = null
    @Mock
    private val overScroller: OverScroller? = null
    @Mock
    private val canvas: Canvas? = null
    @Mock
    private val rect: Rect? = null
    @Mock
    private val calendar: Calendar? = null
    @Mock
    private val motionEvent: MotionEvent? = null
    @Mock
    private val velocityTracker: VelocityTracker? = null
    @Mock
    private val eventsContainer: EventsContainer? = null

    internal var underTest: CompactCalendarController

    @Before
    fun setUp() {
        Locale.setDefault(Locale.ENGLISH)
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"))

        `when`(velocityTracker!!.xVelocity).thenReturn(-200f)
        underTest = CompactCalendarController(
            paint,
            overScroller,
            rect,
            null,
            null,
            0,
            0,
            0,
            velocityTracker,
            0,
            eventsContainer,
            Locale.getDefault(),
            TimeZone.getDefault()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testItThrowsWhenDayColumnsIsNotLengthSeven() {
        val dayNames: Array<String?> = arrayOf("Mon", "Tue", "Wed", "Thur", "Fri")
        underTest.setDayColumnNames(dayNames)
    }

    @Test
    fun testManualScrollAndGestureScrollPlayNicelyTogether() {
        //Set width of view so that scrolling will return a correct value
        underTest.onMeasure(720, 1080, 0, 0)

        val cal = Calendar.getInstance()

        //Sun, 08 Feb 2015 00:00:00 GMT
        underTest.setCurrentDate(Date(setTimeToMidnightAndGet(cal, 1423353600000L)))

        underTest.scrollRight()

        //Sun, 01 Mar 2015 00:00:00 GMT - expected
        assertEquals(
            Date(setTimeToMidnightAndGet(cal, 1425168000000L)),
            underTest.firstDayOfCurrentMonth
        )

        `when`(motionEvent!!.action).thenReturn(MotionEvent.ACTION_UP)

        //Scroll enough to push calender to next month
        underTest.onScroll(motionEvent, motionEvent, 600f, 0f)
        underTest.onDraw(canvas)
        underTest.onTouch(motionEvent)

        //Wed, 01 Apr 2015 00:00:00 GMT
        assertEquals(
            Date(setTimeToMidnightAndGet(cal, 1427846400000L)),
            underTest.firstDayOfCurrentMonth
        )
    }

    @Test
    fun testItScrollsToNextMonth() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        underTest.setCurrentDate(Date(1423353600000L))

        underTest.scrollRight()
        val actualDate = underTest.firstDayOfCurrentMonth

        //Sun, 01 Mar 2015 00:00:00 GMT - expected
        assertEquals(Date(1425168000000L), actualDate)
    }

    @Test
    fun testItScrollsToPreviousMonth() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        underTest.setCurrentDate(Date(1423353600000L))

        underTest.scrollLeft()
        val actualDate = underTest.firstDayOfCurrentMonth

        // Thu, 01 Jan 2015 00:00:00 GMT - expected
        assertEquals(Date(1420070400000L), actualDate)
    }

    @Test
    fun testItScrollsToNextMonthWhenRtl() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        underTest.setCurrentDate(Date(1423353600000L))
        underTest.setIsRtl(true)

        underTest.scrollRight()
        val actualDate = underTest.firstDayOfCurrentMonth

        // Thu, 01 Jan 2015 00:00:00 GMT - expected
        assertEquals(Date(1420070400000L), actualDate)
    }

    @Test
    fun testItScrollsToPreviousMonthWhenRtl() {
        //Sun, 08 Feb 2015 00:00:00 GMT
        underTest.setCurrentDate(Date(1423353600000L))
        underTest.setIsRtl(true)

        underTest.scrollLeft()
        val actualDate = underTest.firstDayOfCurrentMonth

        //Sun, 01 Mar 2015 00:00:00 GMT - expected
        assertEquals(Date(1425168000000L), actualDate)
    }

    @Test
    fun testItSetsDayColumns() {
        //simulate Feb month
        `when`(calendar!!.get(Calendar.DAY_OF_WEEK)).thenReturn(1)
        `when`(calendar.get(Calendar.MONTH)).thenReturn(1)
        `when`(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)).thenReturn(28)

        val dayNames = arrayOf("Mon", "Tue", "Wed", "Thur", "Fri", "Sat", "Sun")
        underTest.setGrowProgress(1000f) //set grow progress so that it simulates the calendar being open
        underTest.setDayColumnNames(dayNames)
        underTest.drawMonth(canvas, calendar, 0)

        val inOrder = inOrder(canvas)
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("Mon"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("Tue"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("Wed"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("Thur"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("Fri"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("Sat"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("Sun"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
    }

    @Test
    fun testListenerIsCalledOnMonthScroll() {
        //Sun, 01 Mar 2015 00:00:00 GMT
        val expectedDateOnScroll = Date(1425168000000L)

        `when`(motionEvent!!.action).thenReturn(MotionEvent.ACTION_UP)

        //Set width of view so that scrolling will return a correct value
        underTest.onMeasure(720, 1080, 0, 0)

        //Sun, 08 Feb 2015 00:00:00 GMT
        underTest.setCurrentDate(Date(1423353600000L))

        //Scroll enough to push calender to next month
        underTest.onScroll(motionEvent, motionEvent, 600f, 0f)
        underTest.onDraw(canvas)
        underTest.onTouch(motionEvent)
        assertEquals(expectedDateOnScroll, underTest.firstDayOfCurrentMonth)
    }

    @Test
    fun testItAbbreviatesDayNames() {
        //simulate Feb month
        `when`(calendar!!.get(Calendar.DAY_OF_WEEK)).thenReturn(1)
        `when`(calendar.get(Calendar.MONTH)).thenReturn(1)
        `when`(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)).thenReturn(28)

        underTest.setGrowProgress(1000f) //set grow progress so that it simulates the calendar being open
        underTest.setLocale(TimeZone.getTimeZone("Europe/Paris"), Locale.FRANCE)
        reset<Canvas>(canvas) //reset because invalidate is called
        underTest.setUseWeekDayAbbreviation(true)
        reset<Canvas>(canvas) //reset because invalidate is called
        underTest.drawMonth(canvas, calendar, 0)

        val dateFormatSymbols = DateFormatSymbols(Locale.FRANCE)
        val dayNames = dateFormatSymbols.shortWeekdays

        val inOrder = inOrder(canvas)
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq(dayNames[2]),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq(dayNames[3]),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq(dayNames[4]),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq(dayNames[5]),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq(dayNames[6]),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq(dayNames[7]),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq(dayNames[1]),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
    }

    @Test
    fun testItReturnsFirstDayOfMonthAfterDateHasBeenSet() {
        //Sun, 01 Feb 2015 00:00:00 GMT
        val expectedDate = Date(1422748800000L)

        //Sun, 08 Feb 2015 00:00:00 GMT
        underTest.setCurrentDate(Date(1423353600000L))

        val actualDate = underTest.firstDayOfCurrentMonth
        assertEquals(expectedDate, actualDate)
    }

    @Test
    fun testItReturnsFirstDayOfMonth() {
        val currentCalender = Calendar.getInstance()
        currentCalender.set(Calendar.DAY_OF_MONTH, 1)
        currentCalender.set(Calendar.HOUR_OF_DAY, 0)
        currentCalender.set(Calendar.MINUTE, 0)
        currentCalender.set(Calendar.SECOND, 0)
        currentCalender.set(Calendar.MILLISECOND, 0)
        val expectFirstDayOfMonth = currentCalender.time

        val actualDate = underTest.firstDayOfCurrentMonth

        assertEquals(expectFirstDayOfMonth, actualDate)
    }

    @Test
    fun testItDrawsSundayAsFirstDay() {
        //simulate Feb month
        `when`(calendar!!.get(Calendar.DAY_OF_WEEK)).thenReturn(1)
        `when`(calendar.get(Calendar.MONTH)).thenReturn(1)
        `when`(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)).thenReturn(28)

        underTest.setGrowProgress(1000f) //set grow progress so that it simulates the calendar being open
        underTest.setUseWeekDayAbbreviation(true)
        underTest.setFirstDayOfWeek(Calendar.SUNDAY)
        underTest.drawMonth(canvas, calendar, 0)

        val inOrder = inOrder(canvas)
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("Sun"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("Mon"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("Tue"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("Wed"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("Thu"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("Fri"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("Sat"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
    }

    @Test
    fun testItDrawsFirstLetterOfEachDay() {
        //simulate Feb month
        `when`(calendar!!.get(Calendar.DAY_OF_WEEK)).thenReturn(1)
        `when`(calendar.get(Calendar.MONTH)).thenReturn(1)
        `when`(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)).thenReturn(28)

        underTest.setGrowProgress(1000f) //set grow progress so that it simulates the calendar being open
        underTest.drawMonth(canvas, calendar, 0)

        val inOrder = inOrder(canvas)
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("M"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("T"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("W"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("T"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("F"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("S"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
        inOrder.verify<Canvas>(canvas).drawText(
            ArgumentMatchers.eq("S"),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
    }

    @Test
    fun testItDrawsDaysOnCalender() {
        //simulate Feb month
        underTest.setGrowProgress(1000f) //set grow progress so that it simulates the calendar being open
        `when`(calendar!!.get(Calendar.DAY_OF_WEEK)).thenReturn(1)
        `when`(calendar.get(Calendar.MONTH)).thenReturn(1)
        `when`(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)).thenReturn(28)

        underTest.drawMonth(canvas, calendar, 0)

        var dayColumn = 0
        var dayRow = 0
        while (dayColumn <= 6) {
            if (dayRow == 7) {
                dayRow = 0
                if (dayColumn <= 6) {
                    dayColumn++
                }
            }
            if (dayColumn == dayColumnNames.size) {
                break
            }
            if (dayColumn == 0) {
                verify<Canvas>(canvas).drawText(
                    ArgumentMatchers.eq(dayColumnNames[dayColumn]),
                    ArgumentMatchers.anyFloat(),
                    ArgumentMatchers.anyFloat(),
                    ArgumentMatchers.eq(paint)
                )
            } else {
                val day = (dayRow - 1) * 7 + dayColumn + 1 - 6
                if (day > 0 && day <= 28) {
                    verify<Canvas>(canvas).drawText(
                        ArgumentMatchers.eq(day.toString()),
                        ArgumentMatchers.anyFloat(),
                        ArgumentMatchers.anyFloat(),
                        ArgumentMatchers.eq(paint)
                    )
                }
            }
            dayRow++
        }
    }

    @Test
    fun testItDrawsEventDaysOnCalendar() {
        //Sun, 07 Jun 2015 18:20:51 GMT
        //get 30 events in total
        val numberOfDaysWithEvents = 30
        val events = getEvents(0, numberOfDaysWithEvents, 1433701251000L)
        `when`(eventsContainer!!.getEventsForMonthAndYear(5, 2015)).thenReturn(events)
        `when`(calendar!!.get(Calendar.MONTH)).thenReturn(5)
        `when`(calendar.get(Calendar.YEAR)).thenReturn(2015)

        underTest.shouldDrawIndicatorsBelowSelectedDays(true) // always draw events, even on current day
        underTest.setGrowProgress(1000f) //set grow progress so that it simulates the calendar being open
        underTest.drawEvents(canvas, calendar, 0)

        //draw events for every day with an event
        verify<Canvas>(
            canvas,
            times(numberOfDaysWithEvents)
        ).drawCircle(
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
    }

    @Test
    fun testItDrawsMultipleEventDaysOnCalendar() {
        //Sun, 07 Jun 2015 18:20:51 GMT
        //get 60 events in total
        val numberOfDaysWithEvents = 30
        val events = getDayEventWith2EventsPerDay(0, numberOfDaysWithEvents, 1433701251000L)
        `when`(eventsContainer!!.getEventsForMonthAndYear(5, 2015)).thenReturn(events)
        `when`(calendar!!.get(Calendar.MONTH)).thenReturn(5)
        `when`(calendar.get(Calendar.YEAR)).thenReturn(2015)

        underTest.shouldDrawIndicatorsBelowSelectedDays(true) // always draw events, even on current day
        underTest.setGrowProgress(1000f) //set grow progress so that it simulates the calendar being open
        underTest.drawEvents(canvas, calendar, 0)

        //draw 2 events per day
        verify<Canvas>(
            canvas,
            times(numberOfDaysWithEvents * 2)
        ).drawCircle(
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
    }

    @Test
    fun testItDrawsMultipleEventDaysOnCalendarWithPlusIndicator() {
        //Sun, 07 Jun 2015 18:20:51 GMT
        //get 120 events in total but only draw 3 event indicators per a day
        val numberOfDaysWithEvents = 30
        val events = getDayEventWithMultipleEventsPerDay(0, numberOfDaysWithEvents, 1433701251000L)
        `when`(eventsContainer!!.getEventsForMonthAndYear(5, 2015)).thenReturn(events)
        `when`(calendar!!.get(Calendar.MONTH)).thenReturn(5)
        `when`(calendar.get(Calendar.YEAR)).thenReturn(2015)

        underTest.shouldDrawIndicatorsBelowSelectedDays(true) // always draw events, even on current day
        underTest.setGrowProgress(1000f) //set grow progress so that it simulates the calendar being open
        underTest.drawEvents(canvas, calendar, 0)

        //draw 2 events per day because we don't draw more than 3 indicators
        verify<Canvas>(
            canvas,
            times(numberOfDaysWithEvents * 2)
        ).drawCircle(
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )

        //draw event indicator with lines
        // 2 calls for each plus event indicator since it takes 2 draw calls to make a plus sign
        verify<Canvas>(
            canvas,
            times(numberOfDaysWithEvents * 2)
        ).drawLine(
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
    }

    @Test
    fun testItDrawsEventDaysOnCalendarForCurrentMonth() {
        val todayCalendar = Calendar.getInstance()
        val numberOfDaysInMonth = todayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val todayMonth = todayCalendar.get(Calendar.MONTH)
        val todayYear = todayCalendar.get(Calendar.YEAR)

        //get events for every day in the month
        val events = getEvents(0, numberOfDaysInMonth, todayCalendar.timeInMillis)
        `when`(eventsContainer!!.getEventsForMonthAndYear(todayMonth, todayYear)).thenReturn(events)
        `when`(calendar!!.get(Calendar.MONTH)).thenReturn(todayMonth)
        `when`(calendar.get(Calendar.YEAR)).thenReturn(todayYear)

        underTest.setGrowProgress(1000f) //set grow progress so that it simulates the calendar being open
        underTest.drawEvents(canvas, calendar, 0)

        //draw events for every day except the current day -- selected day is also the current day
        verify<Canvas>(
            canvas,
            times(numberOfDaysInMonth - 1)
        ).drawCircle(
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
    }

    @Test
    fun testItDrawsEventDaysOnCalendarWithSelectedDay() {
        //Sun, 07 Jun 2015 18:20:51 GMT
        val selectedDayTimestamp = 1433701251000L
        //get 30 events in total
        val numberOfDaysWithEvents = 30
        val events = getEvents(0, numberOfDaysWithEvents, selectedDayTimestamp)
        `when`(eventsContainer!!.getEventsForMonthAndYear(5, 2015)).thenReturn(events)
        `when`(calendar!!.get(Calendar.MONTH)).thenReturn(5)
        `when`(calendar.get(Calendar.YEAR)).thenReturn(2015)

        underTest.setGrowProgress(1000f) //set grow progress so that it simulates the calendar being open
        // Selects first day of the month
        underTest.setCurrentDate(Date(selectedDayTimestamp))
        underTest.drawEvents(canvas, calendar, 0)

        //draw events for every day except the selected day
        verify<Canvas>(
            canvas,
            times(numberOfDaysWithEvents - 1)
        ).drawCircle(
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
    }

    @Test
    fun testItDrawsEventDaysOnCalendarForCurrentMonthWithSelectedDay() {
        val todayCalendar = Calendar.getInstance()
        val numberOfDaysInMonth = todayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val todayMonth = todayCalendar.get(Calendar.MONTH)
        val todayYear = todayCalendar.get(Calendar.YEAR)

        //get events for every day in the month
        val events = getEvents(0, numberOfDaysInMonth, todayCalendar.timeInMillis)
        `when`(eventsContainer!!.getEventsForMonthAndYear(todayMonth, todayYear)).thenReturn(events)
        `when`(calendar!!.get(Calendar.MONTH)).thenReturn(todayMonth)
        `when`(calendar.get(Calendar.YEAR)).thenReturn(todayYear)

        // sets either 1st day or 2nd day so that there are always 2 days selected
        val dayOfMonth = todayCalendar.get(Calendar.DAY_OF_MONTH)
        if (dayOfMonth == 1) {
            todayCalendar.set(Calendar.DAY_OF_MONTH, 2)
        } else {
            todayCalendar.set(Calendar.DAY_OF_MONTH, 1)
        }
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
        todayCalendar.set(Calendar.MINUTE, 0)
        todayCalendar.set(Calendar.SECOND, 0)
        todayCalendar.set(Calendar.MILLISECOND, 0)
        underTest.setCurrentDate(todayCalendar.time)

        underTest.setGrowProgress(1000f) //set grow progress so that it simulates the calendar being open
        underTest.drawEvents(canvas, calendar, 0)

        //draw events for every day except the current day and the selected day
        verify<Canvas>(
            canvas,
            times(numberOfDaysInMonth - 2)
        ).drawCircle(
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.anyFloat(),
            ArgumentMatchers.eq(paint)
        )
    }

    @Test
    fun testItAddsEvent() {
        val event = CompactCalendarHelper.getOneEventPerDayForMonth(0, 30, 1433701251000L)[0]
        underTest.addEvent(event)
        verify<EventsContainer>(eventsContainer).addEvent(event)
        verifyNoMoreInteractions(eventsContainer)
    }

    @Test
    fun testItAddsEvents() {
        val events = CompactCalendarHelper.getOneEventPerDayForMonth(0, 30, 1433701251000L)
        underTest.addEvents(events)
        verify<EventsContainer>(eventsContainer).addEvents(events)
        verifyNoMoreInteractions(eventsContainer)
    }

    @Test
    fun testItRemovesEvent() {
        val event = CompactCalendarHelper.getOneEventPerDayForMonth(0, 30, 1433701251000L)[0]
        underTest.removeEvent(event)
        verify<EventsContainer>(eventsContainer).removeEvent(event)
        verifyNoMoreInteractions(eventsContainer)
    }

    @Test
    fun testItRemovesEvents() {
        val events = CompactCalendarHelper.getOneEventPerDayForMonth(0, 30, 1433701251000L)
        underTest.removeEvents(events)
        verify<EventsContainer>(eventsContainer).removeEvents(events)
        verifyNoMoreInteractions(eventsContainer)
    }

    @Test
    fun testItGetCalendarEventsForADate() {
        underTest.getCalendarEventsFor(1433701251000L)
        verify<EventsContainer>(eventsContainer).getEventsFor(1433701251000L)
        verifyNoMoreInteractions(eventsContainer)
    }

    @Test
    fun testItRemovesCalendarEventsForADate() {
        underTest.removeEventsFor(1433701251000L)
        verify<EventsContainer>(eventsContainer).removeEventByEpochMillis(1433701251000L)
        verifyNoMoreInteractions(eventsContainer)
    }

    @Test
    fun testItRemovesAllEvents() {
        underTest.removeAllEvents()
        verify<EventsContainer>(eventsContainer).removeAllEvents()
        verifyNoMoreInteractions(eventsContainer)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testItThrowsWhenZeroIsUsedAsFirstDayOfWeek() {
        underTest.setFirstDayOfWeek(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testItThrowsWhenValuesGreaterThanSevenIsUsedAsFirstDayOfWeek() {
        underTest.setFirstDayOfWeek(8)
    }

    @Test
    fun testItGetsDayOfWeekWhenSundayIsFirstDayOfWeek() {
        // zero based indexes used internally so instead of returning range of 1-7 it returns 0-6
        // Sunday as first day means Saturday is last day of week
        // first index corresponds to Sunday and last is Saturday
        val expectedDaysOfWeekOrder = intArrayOf(0, 1, 2, 3, 4, 5, 6)
        val actualDaysOfWeekOrder = IntArray(7)
        val calendar = Calendar.getInstance()
        underTest.setFirstDayOfWeek(Calendar.SUNDAY)
        for (day in 1..7) {
            calendar.set(Calendar.DAY_OF_WEEK, day)
            actualDaysOfWeekOrder[day - 1] = underTest.getDayOfWeek(calendar)
        }
        assertArrayEquals(expectedDaysOfWeekOrder, actualDaysOfWeekOrder)
    }

    @Test
    fun testItGetsDayOfWeekWhenMondayIsFirstDayOfWeek() {
        // zero based indexes used internally so instead of returning range of 1-7 it returns 0-6
        // Monday as first day means Sunday is last day of week
        // first index corresponds to Sunday and last is Saturday
        val expectedDaysOfWeekOrder = intArrayOf(6, 0, 1, 2, 3, 4, 5)
        val actualDaysOfWeekOrder = IntArray(7)
        val calendar = Calendar.getInstance()
        underTest.setFirstDayOfWeek(Calendar.MONDAY)
        for (day in 1..7) {
            calendar.set(Calendar.DAY_OF_WEEK, day)
            actualDaysOfWeekOrder[day - 1] = underTest.getDayOfWeek(calendar)
        }
        assertArrayEquals(expectedDaysOfWeekOrder, actualDaysOfWeekOrder)
    }

    @Test
    fun testItGetsDayOfWeekWhenTuesdayIsFirstDayOfWeek() {
        // zero based indexes used internally so instead of returning range of 1-7 it returns 0-6
        // Tuesday as first day means Monday is last day of week
        // first index corresponds to Sunday and last is Saturday
        val expectedDaysOfWeekOrder = intArrayOf(5, 6, 0, 1, 2, 3, 4)
        val actualDaysOfWeekOrder = IntArray(7)
        val calendar = Calendar.getInstance()
        underTest.setFirstDayOfWeek(Calendar.TUESDAY)
        for (day in 1..7) {
            calendar.set(Calendar.DAY_OF_WEEK, day)
            actualDaysOfWeekOrder[day - 1] = underTest.getDayOfWeek(calendar)
        }
        assertArrayEquals(expectedDaysOfWeekOrder, actualDaysOfWeekOrder)
    }

    @Test
    fun testItGetsDayOfWeekWhenWednesdayIsFirstDayOfWeek() {
        // zero based indexes used internally so instead of returning range of 1-7 it returns 0-6
        // Wednesday as first day means Tuesday is last day of week
        // first index corresponds to Sunday and last is Saturday
        val expectedDaysOfWeekOrder = intArrayOf(4, 5, 6, 0, 1, 2, 3)
        val actualDaysOfWeekOrder = IntArray(7)
        val calendar = Calendar.getInstance()
        underTest.setFirstDayOfWeek(Calendar.WEDNESDAY)
        for (day in 1..7) {
            calendar.set(Calendar.DAY_OF_WEEK, day)
            actualDaysOfWeekOrder[day - 1] = underTest.getDayOfWeek(calendar)
        }
        assertArrayEquals(expectedDaysOfWeekOrder, actualDaysOfWeekOrder)
    }

    @Test
    fun testItGetsDayOfWeekWhenThursdayIsFirstDayOfWeek() {
        // zero based indexes used internally so instead of returning range of 1-7 it returns 0-6
        // Thursday as first day means Wednesday is last day of week
        // first index corresponds to Sunday and last is Saturday
        val expectedDaysOfWeekOrder = intArrayOf(3, 4, 5, 6, 0, 1, 2)
        val actualDaysOfWeekOrder = IntArray(7)
        val calendar = Calendar.getInstance()
        underTest.setFirstDayOfWeek(Calendar.THURSDAY)
        for (day in 1..7) {
            calendar.set(Calendar.DAY_OF_WEEK, day)
            actualDaysOfWeekOrder[day - 1] = underTest.getDayOfWeek(calendar)
        }
        assertArrayEquals(expectedDaysOfWeekOrder, actualDaysOfWeekOrder)
    }

    @Test
    fun testItGetsDayOfWeekWhenFridayIsFirstDayOfWeek() {
        // zero based indexes used internally so instead of returning range of 1-7 it returns 0-6
        // Friday as first day means Wednesday is last day of week
        // first index corresponds to Sunday and last is Saturday
        val expectedDaysOfWeekOrder = intArrayOf(2, 3, 4, 5, 6, 0, 1)
        val actualDaysOfWeekOrder = IntArray(7)
        val calendar = Calendar.getInstance()
        underTest.setFirstDayOfWeek(Calendar.FRIDAY)
        for (day in 1..7) {
            calendar.set(Calendar.DAY_OF_WEEK, day)
            actualDaysOfWeekOrder[day - 1] = underTest.getDayOfWeek(calendar)
        }
        assertArrayEquals(expectedDaysOfWeekOrder, actualDaysOfWeekOrder)
    }

    @Test
    fun testItGetsDayOfWeekWhenSaturdayIsFirstDayOfWeek() {
        // zero based indexes used internally so instead of returning range of 1-7 it returns 0-6
        // Saturday as first day means Friday is last day of week
        // first index corresponds to Sunday and last is Saturday
        val expectedDaysOfWeekOrder = intArrayOf(1, 2, 3, 4, 5, 6, 0)
        val actualDaysOfWeekOrder = IntArray(7)
        val calendar = Calendar.getInstance()
        underTest.setFirstDayOfWeek(Calendar.SATURDAY)
        for (day in 1..7) {
            calendar.set(Calendar.DAY_OF_WEEK, day)
            actualDaysOfWeekOrder[day - 1] = underTest.getDayOfWeek(calendar)
        }
        assertArrayEquals(expectedDaysOfWeekOrder, actualDaysOfWeekOrder)
    }

    private fun setTimeToMidnightAndGet(cal: Calendar, epoch: Long): Long {
        cal.time = Date(epoch)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    companion object {

        private val dayColumnNames = arrayOf("M", "T", "W", "T", "F", "S", "S")
    }
}

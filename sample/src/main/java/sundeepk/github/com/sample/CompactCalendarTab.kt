package sundeepk.github.com.sample

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.RelativeLayout
import com.github.sundeepk.rotacalendar.calendar.CompactCalendarView
import com.github.sundeepk.rotacalendar.events.Event
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

class CompactCalendarTab : Fragment() {
    private val currentCalender = Calendar.getInstance(Locale.getDefault())
    private var dateFormatForDisplaying =
        SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.getDefault())
    private val dateFormatForMonth = SimpleDateFormat("MMM - yyyy", Locale.getDefault())
    private var shouldShow = false
    private var compactCalendarView: CompactCalendarView? = null
    private var toolbar: ActionBar? = null

    private val calendarShowLis: View.OnClickListener
        get() = View.OnClickListener {
            if (!compactCalendarView!!.isAnimating) {
                if (shouldShow) {
                    compactCalendarView!!.showCalendar()
                } else {
                    compactCalendarView!!.hideCalendar()
                }
                shouldShow = !shouldShow
            }
        }

    private val calendarExposeLis: View.OnClickListener
        get() = View.OnClickListener {
            if (!compactCalendarView!!.isAnimating) {
                if (shouldShow) {
                    compactCalendarView!!.showCalendarWithAnimation()
                } else {
                    compactCalendarView!!.hideCalendarWithAnimation()
                }
                shouldShow = !shouldShow
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainTabView = inflater.inflate(R.layout.main_tab, container, false)

        val mutableBookings = ArrayList<String>()

        val bookingsListView = mainTabView.findViewById<ListView>(R.id.bookings_listview)
        val showPreviousMonthBut = mainTabView.findViewById<Button>(R.id.prev_button)
        val showNextMonthBut = mainTabView.findViewById<Button>(R.id.next_button)
        val slideCalendarBut = mainTabView.findViewById<Button>(R.id.slide_calendar)
        val showCalendarWithAnimationBut =
            mainTabView.findViewById<Button>(R.id.show_with_animation_calendar)
        val setLocaleBut = mainTabView.findViewById<Button>(R.id.set_locale)
        val removeAllEventsBut = mainTabView.findViewById<Button>(R.id.remove_all_events)

        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, mutableBookings)
        bookingsListView.adapter = adapter
        compactCalendarView = mainTabView.findViewById(R.id.compactCalendar_view)

        // below allows you to configure color for the current day in the month
        // compactCalendarView.setCurrentDayBackgroundColor(getResources().getColor(R.color.black));
        // below allows you to configure colors for the current day the user has selected
        // compactCalendarView.setCurrentSelectedDayBackgroundColor(getResources().getColor(R.color.dark_red));
        compactCalendarView!!.setUseThreeLetterAbbreviation(false)
        compactCalendarView!!.setFirstDayOfWeek(Calendar.MONDAY)
        compactCalendarView!!.setIsRtl(false)
        compactCalendarView!!.displayOtherMonthDays(false)
        //compactCalendarView.setIsRtl(true);
        loadEvents()
        loadEventsForYear(2017)
        compactCalendarView!!.invalidate()

        logEventsByMonth(compactCalendarView!!)

        // below line will display Sunday as the first day of the week
        // compactCalendarView.setShouldShowMondayAsFirstDay(false);

        // disable scrolling calendar
        // compactCalendarView.shouldScrollMonth(false);

        // show days from other months as greyed out days
        // compactCalendarView.displayOtherMonthDays(true);

        // show Sunday as first day of month
        // compactCalendarView.setShouldShowMondayAsFirstDay(false);

        //set initial title
        toolbar = (activity as AppCompatActivity).supportActionBar
        toolbar!!.title = dateFormatForMonth.format(compactCalendarView!!.firstDayOfCurrentMonth)

        //set title on calendar scroll
        compactCalendarView!!.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                toolbar!!.title = dateFormatForMonth.format(dateClicked)
                val bookingsFromMap = compactCalendarView!!.getEvents(dateClicked)
                if (bookingsFromMap != null) {
                    Log.d(TAG, bookingsFromMap.toString())
                    mutableBookings.clear()
                    for (booking in bookingsFromMap) {
                        mutableBookings.add(booking.data as String)
                    }
                    adapter.notifyDataSetChanged()
                }

            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                toolbar!!.title = dateFormatForMonth.format(firstDayOfNewMonth)
            }
        })

        showPreviousMonthBut.setOnClickListener { compactCalendarView!!.scrollLeft() }

        showNextMonthBut.setOnClickListener { compactCalendarView!!.scrollRight() }

        val showCalendarOnClickLis = calendarShowLis
        slideCalendarBut.setOnClickListener(showCalendarOnClickLis)

        val exposeCalendarListener = calendarExposeLis
        showCalendarWithAnimationBut.setOnClickListener(exposeCalendarListener)

        compactCalendarView!!.setAnimationListener(object :
            CompactCalendarView.CompactCalendarAnimationListener {
            override fun onOpened() {}

            override fun onClosed() {}
        })

        setLocaleBut.setOnClickListener {
            val locale = Locale.FRANCE
            dateFormatForDisplaying = SimpleDateFormat("dd-M-yyyy hh:mm:ss a", locale)
            val timeZone = TimeZone.getTimeZone("Europe/Paris")
            dateFormatForDisplaying.timeZone = timeZone
            dateFormatForMonth.timeZone = timeZone
            compactCalendarView!!.setLocale(timeZone, locale)
            compactCalendarView!!.setUseThreeLetterAbbreviation(false)
            loadEvents()
            loadEventsForYear(2017)
            logEventsByMonth(compactCalendarView!!)
        }

        removeAllEventsBut.setOnClickListener { compactCalendarView!!.removeAllEvents() }


        // uncomment below to show indicators above small indicator events
        // compactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);

        // uncomment below to open onCreate
        //openCalendarOnCreate(v);

        return mainTabView
    }

    private fun openCalendarOnCreate(v: View) {
        val layout = v.findViewById<RelativeLayout>(R.id.main_content)
        val vto = layout.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                compactCalendarView!!.showCalendarWithAnimation()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        toolbar!!.title = dateFormatForMonth.format(compactCalendarView!!.firstDayOfCurrentMonth)
        // Set to current day on resume to set calendar to latest day
        // toolbar.setTitle(dateFormatForMonth.format(new Date()));
    }

    private fun loadEvents() {
        addEvents(-1, -1)
        addEvents(Calendar.DECEMBER, -1)
        addEvents(Calendar.AUGUST, -1)
    }

    private fun loadEventsForYear(year: Int) {
        addEvents(Calendar.DECEMBER, year)
        addEvents(Calendar.AUGUST, year)
    }

    private fun logEventsByMonth(compactCalendarView: CompactCalendarView) {
        currentCalender.time = Date()
        currentCalender.set(Calendar.DAY_OF_MONTH, 1)
        currentCalender.set(Calendar.MONTH, Calendar.AUGUST)
        val dates = ArrayList<String>()
        for (e in compactCalendarView.getEventsForMonth(Date())) {
            dates.add(dateFormatForDisplaying.format(e.timeInMillis))
        }
        Log.d(TAG, "Events for Aug with simple date formatter: $dates")
        Log.d(
            TAG,
            "Events for Aug month using default local and timezone: " + compactCalendarView.getEventsForMonth(
                currentCalender.time
            )
        )
    }

    private fun addEvents(month: Int, year: Int) {
        currentCalender.time = Date()
        currentCalender.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = currentCalender.time
        for (i in 0..5) {
            currentCalender.time = firstDayOfMonth
            if (month > -1) {
                currentCalender.set(Calendar.MONTH, month)
            }
            if (year > -1) {
                currentCalender.set(Calendar.ERA, GregorianCalendar.AD)
                currentCalender.set(Calendar.YEAR, year)
            }
            currentCalender.add(Calendar.DATE, i)
            setToMidnight(currentCalender)
            val timeInMillis = currentCalender.timeInMillis

            val events = getEvents(timeInMillis, i)

            compactCalendarView!!.addEvents(events)
        }
    }

    private fun getEvents(timeInMillis: Long, day: Int): List<Event> {
        return if (day < 2) {
            Arrays.asList(
                Event(
                    Color.argb(255, 169, 68, 65),
                    timeInMillis,
                    "Event at " + Date(timeInMillis)
                )
            )
        } else if (day in 3..4) {
            Arrays.asList(
                Event(
                    Color.argb(255, 169, 68, 65),
                    timeInMillis,
                    "Event at " + Date(timeInMillis)
                ),
                Event(
                    Color.argb(255, 100, 68, 65),
                    timeInMillis,
                    "Event 2 at " + Date(timeInMillis)
                )
            )
        } else {
            Arrays.asList(
                Event(
                    Color.argb(255, 169, 68, 65),
                    timeInMillis,
                    "Event at " + Date(timeInMillis)
                ),
                Event(
                    Color.argb(255, 100, 68, 65),
                    timeInMillis,
                    "Event 2 at " + Date(timeInMillis)
                ),
                Event(
                    Color.argb(255, 70, 68, 65),
                    timeInMillis,
                    "Event 3 at " + Date(timeInMillis)
                )
            )
        }
    }

    private fun setToMidnight(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    companion object {

        private val TAG = "MainActivity"
    }
}
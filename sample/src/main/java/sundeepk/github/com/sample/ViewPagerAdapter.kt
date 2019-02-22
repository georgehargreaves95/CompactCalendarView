package sundeepk.github.com.sample

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class ViewPagerAdapter(
    fm: FragmentManager,
    internal var titles: Array<CharSequence>,
    internal var numbOfTabs: Int
) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        if (position == 0) {
            val compactCalendarTab = CompactCalendarTab()
            return compactCalendarTab
        } else {
            val tab2 = Tab2()
            return tab2
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }

    override fun getCount(): Int {
        return numbOfTabs
    }
}
package kr.co.lion.modigm.ui.join.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class JoinViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    private lateinit var fragmentList : ArrayList<Fragment>

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]

    fun addFragments(fragments: ArrayList<Fragment>) {
        fragmentList = fragments
        notifyDataSetChanged()
    }

}
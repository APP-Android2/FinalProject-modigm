package kr.co.lion.modigm.ui.detail.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import kr.co.lion.modigm.ui.detail.DetailApplyMemberFragment
import kr.co.lion.modigm.ui.detail.DetailJoinMemberFragment

class DetailViewPagerAdapter (fragment: Fragment, private val studyIdx: Int) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> DetailJoinMemberFragment()
            1 -> DetailApplyMemberFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
        fragment.arguments = Bundle().apply {
            putInt("studyIdx", studyIdx)
        }
        return fragment
    }
}
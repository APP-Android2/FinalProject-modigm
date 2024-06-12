package kr.co.lion.modigm.ui.write.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import kr.co.lion.modigm.ui.write.WriteFieldFragment
import kr.co.lion.modigm.ui.write.WriteIntroFragment
import kr.co.lion.modigm.ui.write.WritePeriodFragment
import kr.co.lion.modigm.ui.write.WriteProceedFragment
import kr.co.lion.modigm.ui.write.WriteSkillFragment

class WritePagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 5 // 탭의 개수

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WriteFieldFragment()
            1 -> WritePeriodFragment()
            2 -> WriteProceedFragment()
            3 -> WriteSkillFragment()
            4 -> WriteIntroFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
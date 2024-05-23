package kr.co.lion.modigm.ui.write

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kr.co.lion.modigm.databinding.FragmentWriteBinding
import kr.co.lion.modigm.ui.MainActivity


class WriteFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    lateinit var fragmentWriteBinding: FragmentWriteBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentWriteBinding = FragmentWriteBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        viewPagerActivation()

        return fragmentWriteBinding.root
    }


    // ViewPager 설정0
     fun viewPagerActivation(){
        fragmentWriteBinding.apply {

            // 1. 페이지 데이터를 로드
            val fragmentList = listOf(
                WriteFieldFragment(),
                WritePeriodFragment(),
                WriteProceedFragment(),
                WriteSkillFragment(),
                WriteIntroFragment()
            )

            // 2. Adapter 생성
            val pagerAdapter = FragmentPagerAdapter(fragmentList, mainActivity)

            // 3. Adapter와 ViewPager 연결
            viewPagerWriteFragment.adapter = pagerAdapter

            // 4. Tab Layout과 ViewPager 연결
            TabLayoutMediator(tabLayoutWriteFragment, viewPagerWriteFragment){tab, position ->
                tab.text = when(position){
                    0 -> "분야"
                    1 -> "기간"
                    2 -> "진행방식"
                    3 -> "기술"
                    4 -> "소개"
                    else -> throw IllegalArgumentException("Invalid postion : $position")
                }
            }.attach()
        }
    }
    private inner class FragmentPagerAdapter(val fragmentList: List<Fragment>,fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity){
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            val fragment = fragmentList[position]


            // 각 fragment에 전달할 데이터를 설정해준다
            val bundle = Bundle().apply {

            }
            fragment.arguments = bundle

            return fragment
        }

    }
}

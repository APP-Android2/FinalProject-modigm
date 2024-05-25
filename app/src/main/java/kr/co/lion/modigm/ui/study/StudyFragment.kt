package kr.co.lion.modigm.ui.study

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudyBinding
import kr.co.lion.modigm.ui.join.JoinStep1Fragment
import kr.co.lion.modigm.ui.join.JoinStep2Fragment
import kr.co.lion.modigm.ui.join.JoinStep3Fragment
import kr.co.lion.modigm.ui.join.adapter.JoinViewPagerAdapter
import kr.co.lion.modigm.ui.study.adapter.StudyViewPagerAdapter

class StudyFragment : Fragment() {

    private lateinit var binding : FragmentStudyBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // 바인딩
        binding = FragmentStudyBinding.inflate(inflater,container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 프래그먼트 설정
        if (savedInstanceState == null) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerStudy, StudyAllFragment())
                .commit()
        }

        // 초기 뷰 세팅
        initView()
        
    }

    // 초기 뷰 세팅
    fun initView(){

        // 바인딩
        with(binding){
            val tabLayout: TabLayout = tabLayoutStudy

            // 탭 선택 리스너 설정
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val fragment = when (tab.position) {
                        0 -> StudyAllFragment()
                        1 -> StudyMyFragment()
                        else -> StudyAllFragment()
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerStudy, fragment)
                        .commit()
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    // 필요시 구현
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    // 필요시 구현
                }
            })



        }
    }
}
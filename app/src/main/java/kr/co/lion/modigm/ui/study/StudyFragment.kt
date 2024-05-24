package kr.co.lion.modigm.ui.study

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
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

        // 초기 뷰 세팅
        initView()
        // 탭레이아웃에 뷰페이저 적용
        setViewPager()
    }

    // 초기 뷰 세팅
    fun initView(){

        // 바인딩
        with(binding){

            // 툴바



        }
    }

    // 뷰페이저 어댑터
    fun setViewPager(){

        val fragments = listOf(StudyAllFragment(), StudyMyFragment())
        val titles = listOf("전체 스터디", "내 스터디")

        val adapter = StudyViewPagerAdapter(fragments, childFragmentManager, viewLifecycleOwner.lifecycle)
        with(binding){
            viewPagerStudy.adapter = adapter
            // TabLayout과 ViewPager2 연동
            TabLayoutMediator(tabLayoutStudy, viewPagerStudy) { tab, position ->
                tab.text = titles[position]
            }.attach()
        }
    }
}
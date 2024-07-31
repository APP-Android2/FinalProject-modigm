package kr.co.lion.modigm.ui.study

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.tabs.TabLayout
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudyBinding

class StudyFragment : Fragment(R.layout.fragment_study) {

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바인딩
        val binding = FragmentStudyBinding.bind(view)

        // 초기 뷰 세팅
        initView(binding)

        // 초기 프래그먼트 설정
        if (savedInstanceState == null) {
            childFragmentManager.commit {
                replace<StudyAllFragment>(R.id.fragmentContainerStudy)
            }
        }
    }

    // --------------------------------- LC END ---------------------------------

    private fun initView(binding: FragmentStudyBinding) {
        // 바인딩
        with(binding) {
            // 탭 레이아웃 설정
            val tabLayout: TabLayout = tabLayoutStudy

            // 탭 선택 리스너 설정
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val fragment = when (tab.position) {
                        0 -> StudyAllFragment()
                        1 -> StudyMyFragment()
                        else -> StudyAllFragment()
                    }
                    childFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace(R.id.fragmentContainerStudy, fragment)
                    }
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

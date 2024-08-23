package kr.co.lion.modigm.ui.study

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.tabs.TabLayout
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentStudyBinding
import kr.co.lion.modigm.ui.VBBaseFragment

class StudyFragment : VBBaseFragment<FragmentStudyBinding>(FragmentStudyBinding::inflate), OnRecyclerViewScrollListener {

    private var scrollListener: OnRecyclerViewScrollListener? = null

    // --------------------------------- LC START ---------------------------------

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 부모 프래그먼트(BottomNaviFragment)와 인터페이스 연결
        if (parentFragment is OnRecyclerViewScrollListener) {
            scrollListener = parentFragment as OnRecyclerViewScrollListener
        } else if (context is OnRecyclerViewScrollListener) {
            scrollListener = context
        } else {
            throw RuntimeException("$context or parentFragment must implement OnRecyclerViewScrollListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 뷰 세팅
        initView()

        // 초기 프래그먼트 설정
        if (savedInstanceState == null) {
            childFragmentManager.commit {
                replace<StudyAllFragment>(R.id.fragmentContainerStudy)
            }
        }
    }

    // --------------------------------- LC END ---------------------------------

    override fun onRecyclerViewScrolled(dy: Int) {
        scrollListener?.onRecyclerViewScrolled(dy)
    }

    override fun onRecyclerViewScrollStateChanged(newState: Int) {
        scrollListener?.onRecyclerViewScrollStateChanged(newState)
    }

    private fun initView() {
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

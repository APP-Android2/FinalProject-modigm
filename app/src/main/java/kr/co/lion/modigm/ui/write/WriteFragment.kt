package kr.co.lion.modigm.ui.write

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.tabs.TabLayout
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.login.CustomCancelDialog
import kr.co.lion.modigm.ui.write.vm.WriteViewModel
import kr.co.lion.modigm.util.FragmentName

class WriteFragment : VBBaseFragment<FragmentWriteBinding>(FragmentWriteBinding::inflate) {
    // 뷰모델
    private val viewModel: WriteViewModel by activityViewModels()

    private var previousTabPosition: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 뷰
        initView()

        // 초기 프래그먼트 설정
        if (savedInstanceState == null) {
            childFragmentManager.commit {
                replace<WriteFieldFragment>(R.id.containerWrite)
            }
        }
        // 뷰모델 관찰
        observeViewModel()
        // 뒤로가기 버튼 처리
        backButton()

    }

    private fun initView(){
        with(binding) {

            // 탭 레이아웃 설정
            with(tabLayoutWrite){
                addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        // 선택된 탭에 따라 프래그먼트 교체
                        val fragment = when (tab.position) {
                            0 -> WriteFieldFragment()
                            1 -> WritePeriodFragment()
                            2 -> WriteProceedFragment()
                            3 -> WriteSkillFragment()
                            4 -> WriteIntroFragment()
                            else -> WriteFieldFragment()
                        }

                        viewModel.updateSelectedTab(tab.position)

                        // 프래그먼트 교체
                        childFragmentManager.commit {
                            setReorderingAllowed(true)
                            replace(R.id.containerWrite, fragment)
                        }

                        // 이전 탭 위치를 업데이트
                        previousTabPosition = tab.position
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {}

                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                })
            }

            // 툴바 설정
            with(toolbarWriteFragment){
                // 뒤로가기 버튼
                setNavigationOnClickListener {
                    showCancelDialog()
                }
            }
        }
    }

    // 프로그래스바 애니메이션 함수
    private fun animateProgressBar(progressBar: ProgressBar, from: Int, to: Int) {
        ObjectAnimator.ofInt(progressBar, "progress", from, to).apply {
            duration = 200 // 애니메이션 지속 시간 (500ms)
            interpolator = DecelerateInterpolator() // 자연스러운 감속 애니메이션
            start()
        }
    }

    private fun observeViewModel() {
        // 탭 선택 상태 관찰
        viewModel.selectedTabPosition.observe(viewLifecycleOwner) { position ->
            // 해당 포지션의 탭을 선택
            val tab = binding.tabLayoutWrite.getTabAt(position)
            tab?.select()

            // 탭에 따른 프래그먼트 교체
            val fragment = when (position) {
                0 -> WriteFieldFragment()
                1 -> WritePeriodFragment()
                2 -> WriteProceedFragment()
                3 -> WriteSkillFragment()
                4 -> WriteIntroFragment()
                else -> WriteFieldFragment()
            }

            childFragmentManager.commit {
                replace(R.id.containerWrite, fragment)
            }
        }

        // 프로그래스바 상태 관찰
        viewModel.progressBarState.observe(viewLifecycleOwner) { progress ->
            animateProgressBar(binding.progressBarWrite, binding.progressBarWrite.progress, progress)
        }
    }

    // 프래그먼트 선택 메서드
    private fun selectFragment(fragment: Fragment) {
        childFragmentManager.commit {
            replace(R.id.containerWrite, fragment)
        }
    }

    // 뒤로가기 버튼 처리
    private fun backButton() {
        with(binding) {
            // 백버튼 처리
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                val currentTab = tabLayoutWrite.selectedTabPosition
                if (currentTab > 0) {
                    val previousTabPosition = currentTab - 1
                    val tab = tabLayoutWrite.getTabAt(previousTabPosition)
                    tab?.select()

                    // 프로그래스바 감소 애니메이션 적용
                    val progressPercentage = (previousTabPosition + 1) * 20
                    animateProgressBar(progressBarWrite, progressBarWrite.progress, progressPercentage)
                } else {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    // 뒤로가기 다이얼로그 표시
    private fun showCancelDialog() {
        val dialog = CustomCancelDialog(requireContext())
        with(dialog){
            setTitle("뒤로가기")
            setPositiveButton("확인") {
                parentFragmentManager.popBackStack(FragmentName.BOTTOM_NAVI.str,0)
            }
            setNegativeButton("취소") {
                dismiss()
            }
            show()
        }
    }
}

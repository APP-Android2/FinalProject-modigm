package kr.co.lion.modigm.ui.write

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import androidx.activity.addCallback
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 뷰
        initView()

        // 초기 프래그먼트 설정
        if (savedInstanceState == null) {
            childFragmentManager.commit {
                replace<WriteTypeFragment>(R.id.containerWrite)
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
                            0 -> WriteTypeFragment()
                            1 -> WritePeriodFragment()
                            2 -> WriteProceedFragment()
                            3 -> WriteTechStackFragment()
                            4 -> WriteIntroFragment()
                            else -> WriteTypeFragment()
                        }

                        viewModel.updateSelectedTab(tab.position)

                        // 프래그먼트 교체
                        childFragmentManager.commit {
                            setReorderingAllowed(true)
                            replace(R.id.containerWrite, fragment)
                        }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {}

                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                })
            }

            // 툴바 설정
            with(toolbarWriteFragment){
                // 뒤로가기 버튼
                setNavigationOnClickListener {
                    // 이전 탭 클릭 시 처리
                    moveToPreviousTab()
                }
            }
        }
    }

    // 프로그래스바 애니메이션 함수
    private fun animateProgressBar(progressBar: ProgressBar, from: Int, to: Int) {
        ObjectAnimator.ofInt(progressBar, "progress", from, to).apply {
            duration = 500 // 애니메이션 지속 시간 (500ms)
            interpolator = DecelerateInterpolator() // 자연스러운 감속 애니메이션
            start()
        }
    }

    private fun observeViewModel() {
        with(binding) {
            // 탭 선택 상태 관찰
            viewModel.selectedTabPosition.observe(viewLifecycleOwner) { position ->
                // 해당 포지션의 탭을 선택
                tabLayoutWrite.getTabAt(position)?.select()
            }

            // 프로그래스바 상태 관찰
            viewModel.progressBarState.observe(viewLifecycleOwner) { progress ->
                animateProgressBar(progressBarWrite, progressBarWrite.progress, progress)
            }

            // writeDataMap 변경 사항을 관찰
            viewModel.writeDataMap.observe(viewLifecycleOwner) { dataMap ->
                updateTabEnabledState(dataMap)
            }
        }

    }

    // 탭의 활성화/비활성화 상태를 업데이트하는 함수
    private fun updateTabEnabledState(dataMap: MutableMap<String, Any?>?) {
        with(binding.tabLayoutWrite) {
            // 첫 번째 탭은 항상 활성화
            getTabAt(0)?.view?.isEnabled = true

            // 두 번째 탭은 첫 번째 탭에 해당하는 데이터가 있을 때 활성화
            getTabAt(1)?.view?.isEnabled = dataMap?.get("studyType") != null

            // 세 번째 탭은 두 번째 탭에 해당하는 데이터가 있을 때 활성화
            getTabAt(2)?.view?.isEnabled = dataMap?.get("studyPeriod") != null

            // 네 번째 탭은 세 번째 탭에 해당하는 데이터가 있을 때 활성화
            getTabAt(3)?.view?.isEnabled = dataMap?.get("studyOnOffline") != null

            // 다섯 번째 탭은 네 번째 탭에 해당하는 데이터가 있을 때 활성화
            getTabAt(4)?.view?.isEnabled = dataMap?.get("studyTechStackList") != null
        }
    }

    // 뒤로가기 버튼 처리
    private fun backButton() {
        // 백버튼 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // 뒤로가기 처리
            moveToPreviousTab()
        }
    }

    // 뒤로가기 처리
    private fun moveToPreviousTab() {
        with(binding){
            // 현재 탭
            val currentTab = tabLayoutWrite.selectedTabPosition
            // 기간/진행방식/기술/소개 = 이전 탭
            if (currentTab > 0) {
                val previousTabPosition = currentTab - 1
                val tab = tabLayoutWrite.getTabAt(previousTabPosition)
                tab?.select()

                // 프로그래스바 감소 애니메이션 적용
                val progressPercentage = (previousTabPosition + 1) * 20
                animateProgressBar(progressBarWrite, progressBarWrite.progress, progressPercentage)
            // 분류 = 팝 백스택,
            } else {
                showCancelDialog()
            }
        }

    }

    // 뒤로가기 다이얼로그 표시
    private fun showCancelDialog() {
        val dialog = CustomCancelDialog(requireContext())
        with(dialog){
            setTitle("뒤로가기")
            setMessage("작성을 취소하시겠습니까?")
            setPositiveButton("확인") {
                viewModel.clearData()
                parentFragmentManager.popBackStack(FragmentName.BOTTOM_NAVI.str,0)
            }
            setNegativeButton("취소") {
                dismiss()
            }
            show()
        }
    }
}

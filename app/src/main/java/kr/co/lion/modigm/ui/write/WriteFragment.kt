package kr.co.lion.modigm.ui.write

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
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
import kr.co.lion.modigm.util.showLoginSnackBar

class WriteFragment : VBBaseFragment<FragmentWriteBinding>(FragmentWriteBinding::inflate) {

    // 뷰모델
    private val viewModel: WriteViewModel by activityViewModels()

    // 각 탭이 처음 접근된 여부를 저장하는 배열 (탭 개수만큼 초기화)
    private val isTabAccessed = BooleanArray(5) { false }

    // 이전 탭 위치 저장 변수
    private var previousTabPosition: Int = -1

    // 디버깅을 위한 태그
    private val logTag = "WriteFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(logTag, "onViewCreated 호출됨")

        // 초기 뷰
        initView()

        // 초기 프래그먼트 설정
        if (savedInstanceState == null) {
            Log.d(logTag, "savedInstanceState가 null이므로 초기 프래그먼트 설정")
            childFragmentManager.commit {
                replace<WriteTypeFragment>(R.id.containerWrite)
            }
            // 첫 번째 탭은 항상 처음 접근되었으므로 true로 설정
            isTabAccessed[0] = true
            Log.d(logTag, "첫 번째 탭이 처음 접근됨: isTabAccessed[0] = true")
        }
        // 뷰모델 관찰
        observeViewModel()
        // 뒤로가기 버튼 처리
        backButton()
    }

    private fun initView() {
        Log.d(logTag, "initView 호출됨")
        with(binding) {
            // 탭 레이아웃 설정
            with(tabLayoutWrite) {
                addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        val currentPosition = tab.position
                        Log.d(logTag, "탭 선택됨: currentPosition = $currentPosition")

                        // 탭이 처음 접근된 경우 유효성 검사 생략
                        if (!isTabAccessed[currentPosition]) {
                            Log.d(logTag, "탭이 처음 접근됨: currentPosition = $currentPosition, isTabAccessed[$currentPosition] = true")
                            isTabAccessed[currentPosition] = true // 탭 접근 상태 업데이트
                        } else if (!validateCurrentTab()) {
                            // 유효하지 않으면 이전 탭으로 되돌아감
                            Log.d(logTag, "유효성 검사 실패: 이전 탭으로 이동 - previousTabPosition = $previousTabPosition")
                            tabLayoutWrite.getTabAt(previousTabPosition)?.select()
                            return
                        }

                        // 선택된 탭에 따라 프래그먼트 교체
                        val fragment = when (currentPosition) {
                            0 -> WriteTypeFragment()
                            1 -> WritePeriodFragment()
                            2 -> WriteProceedFragment()
                            3 -> WriteTechStackFragment()
                            4 -> WriteIntroFragment()
                            else -> WriteTypeFragment()
                        }

                        viewModel.updateSelectedTab(currentPosition)

                        // 프래그먼트 교체
                        childFragmentManager.commit {
                            setReorderingAllowed(true)
                            replace(R.id.containerWrite, fragment)
                        }

                        // 탭 이동 후 이전 탭 위치 업데이트
                        previousTabPosition = currentPosition
                        Log.d(logTag, "탭 이동: previousTabPosition = $previousTabPosition")
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                        Log.d(logTag, "탭 선택 해제됨: tabPosition = ${tab?.position}")
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {
                        Log.d(logTag, "탭 다시 선택됨: tabPosition = ${tab?.position}")
                    }
                })
            }

            // 툴바 설정
            with(toolbarWriteFragment) {
                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {
                    Log.d(logTag, "툴바 뒤로가기 버튼 클릭됨")
                    // 글작성 취소 다이얼로그
                    showCancelDialog()
                }
            }
        }
    }

    private fun validateCurrentTab(): Boolean {

        // 데이터가 초기화된 직후에는 유효성 검사를 생략하기 위한 조건
        if (viewModel.isDataCleared) {
            viewModel.isDataCleared = false // 초기화 플래그 리셋
            Log.d(logTag, "validateCurrentTab: 데이터 초기화 직후이므로 유효성 검사를 생략합니다.")
            return true
        }
        Log.d(logTag, "validateCurrentTab 호출됨: previousTabPosition = $previousTabPosition")
        val currentTabPosition = previousTabPosition
        fun writeDataMap(item: String): Any? {
            return viewModel.writeDataMap.value?.get(item)
        }

        val isValid = when (currentTabPosition) {
            0 -> {
                if (writeDataMap("studyType") == null) {
                    requireActivity().showLoginSnackBar("타입을 선택해주세요.", null)
                    Log.d(logTag, "유효성 검사 실패: studyType이 null임")
                    false
                } else {
                    true
                }
            }
            1 -> {
                if (writeDataMap("studyPeriod") == null) {
                    requireActivity().showLoginSnackBar("기간을 선택해주세요.", null)
                    Log.d(logTag, "유효성 검사 실패: studyPeriod가 null임")
                    false
                } else {
                    true
                }
            }
            2 -> {
                if (writeDataMap("studyOnOffline") == null) {
                    requireActivity().showLoginSnackBar("진행방식을 선택해주세요.", null)
                    Log.d(logTag, "유효성 검사 실패: studyOnOffline이 null임")
                    false
                } else if ((writeDataMap("studyOnOffline") == "오프라인"
                            || writeDataMap("studyOnOffline") == "온오프혼합")
                    && writeDataMap("studyPlace") == null
                ) {
                    requireActivity().showLoginSnackBar("장소를 입력해주세요.", null)
                    Log.d(logTag, "유효성 검사 실패: studyPlace가 null임")
                    false
                } else if (writeDataMap("studyMaxMember") == null) {
                    requireActivity().showLoginSnackBar("최대 인원을 입력해주세요.", null)
                    Log.d(logTag, "유효성 검사 실패: studyMaxMember가 null임")
                    false
                } else if (writeDataMap("studyMaxMember").toString().toInt() < 2) {
                    requireActivity().showLoginSnackBar("최소 2명 이상의 인원을 입력해주세요.", null)
                    Log.d(logTag, "유효성 검사 실패: studyMaxMember가 2명 이하임")
                    false
                } else {
                    true
                }
            }
            3 -> {
                if (writeDataMap("studyTechStackList") == null) {
                    requireActivity().showLoginSnackBar("기술스택을 선택해주세요.", null)
                    Log.d(logTag, "유효성 검사 실패: studyTechStackList가 null임")
                    false
                } else {
                    true
                }
            }
            4 -> {
                if (writeDataMap("studyTitle") == null) {
                    requireActivity().showLoginSnackBar("제목을 입력해주세요.", null)
                    Log.d(logTag, "유효성 검사 실패: studyTitle이 null임")
                    false
                } else if (writeDataMap("studyContent") == null) {
                    requireActivity().showLoginSnackBar("소개글을 입력해주세요.", null)
                    Log.d(logTag, "유효성 검사 실패: studyContent가 null임")
                    false
                } else if (writeDataMap("studyChatLink") == null) {
                    requireActivity().showLoginSnackBar("오픈채팅 링크를 입력해주세요.", null)
                    Log.d(logTag, "유효성 검사 실패: studyChatLink가 null임")
                    false
                } else {
                    true
                }
            }
            else -> true
        }

        Log.d(logTag, "유효성 검사 결과: isValid = $isValid")
        return isValid
    }


    // 프로그래스바 애니메이션 함수
    private fun animateProgressBar(progressBar: ProgressBar, from: Int, to: Int) {
        Log.d(logTag, "animateProgressBar 호출됨: from = $from, to = $to")
        ObjectAnimator.ofInt(progressBar, "progress", from, to).apply {
            duration = 500 // 애니메이션 지속 시간 (500ms)
            interpolator = DecelerateInterpolator() // 자연스러운 감속 애니메이션
            start()
        }
    }

    private fun observeViewModel() {
        Log.d(logTag, "observeViewModel 호출됨")
        with(binding) {
            // 탭 선택 상태 관찰
            viewModel.selectedTabPosition.observe(viewLifecycleOwner) { position ->
                Log.d(logTag, "selectedTabPosition 변경됨: position = $position")
                // 해당 포지션의 탭을 선택
                tabLayoutWrite.getTabAt(position)?.select()
            }

            // 프로그래스바 상태 관찰
            viewModel.progressBarState.observe(viewLifecycleOwner) { progress ->
                Log.d(logTag, "progressBarState 변경됨: progress = $progress")
                animateProgressBar(progressBarWrite, progressBarWrite.progress, progress)
            }

            // writeDataMap 변경 사항을 관찰
            viewModel.writeDataMap.observe(viewLifecycleOwner) { dataMap ->
                Log.d(logTag, "writeDataMap 변경됨: dataMap = $dataMap")
                updateTabEnabledState(dataMap)
            }
        }
    }

    // 탭의 활성화/비활성화 상태를 업데이트하는 함수
    private fun updateTabEnabledState(dataMap: MutableMap<String, Any?>?) {
        Log.d(logTag, "updateTabEnabledState 호출됨: dataMap = $dataMap")
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
        Log.d(logTag, "backButton 호출됨")
        // 백버튼 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // 뒤로가기 처리
            moveToPreviousTab()
        }
    }

    // 뒤로가기 처리
    private fun moveToPreviousTab() {
        Log.d(logTag, "moveToPreviousTab 호출됨")
        with(binding) {
            // 현재 탭
            val currentTab = tabLayoutWrite.selectedTabPosition
            Log.d(logTag, "현재 탭 위치: currentTab = $currentTab")

            // 이전 탭으로 이동
            if (currentTab > 0) {
                val previousTabPosition = currentTab - 1
                val tab = tabLayoutWrite.getTabAt(previousTabPosition)
                tab?.select()
                Log.d(logTag, "이전 탭으로 이동: previousTabPosition = $previousTabPosition")

                // 프로그래스바 감소 애니메이션 적용
                val progressPercentage = (previousTabPosition + 1) * 20
                animateProgressBar(progressBarWrite, progressBarWrite.progress, progressPercentage)
                // 이전 탭 위치 업데이트
                this@WriteFragment.previousTabPosition = previousTabPosition
            } else {
                showCancelDialog()
            }
        }
    }

    // 뒤로가기 다이얼로그 표시
    private fun showCancelDialog() {
        Log.d(logTag, "showCancelDialog 호출됨")
        val dialog = CustomCancelDialog(requireContext())
        with(dialog) {
            setTitle("뒤로가기")
            setMessage("작성을 취소하시겠습니까?")
            setPositiveButton("계속 작성") {
                Log.d(logTag, "다이얼로그 계속 작성 버튼 클릭됨: 다이얼로그 닫기")
                dismiss()
            }
            setNegativeButton("취소하기") {
                Log.d(logTag, "다이얼로그 취소하기 버튼 클릭됨: 데이터 초기화 및 화면 이동")
                viewModel.clearData()
                parentFragmentManager.popBackStack(FragmentName.BOTTOM_NAVI.str, 0)

            }
            show()
        }
    }
}

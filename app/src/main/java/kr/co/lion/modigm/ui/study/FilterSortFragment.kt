package kr.co.lion.modigm.ui.study

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFilterSortBinding
import kr.co.lion.modigm.model.FilterStudyData
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.study.vm.StudyViewModel
import kr.co.lion.modigm.util.FilterSort
import kr.co.lion.modigm.util.FilterSort.Category
import kr.co.lion.modigm.util.FragmentName

class FilterSortFragment : VBBaseFragment<FragmentFilterSortBinding>(FragmentFilterSortBinding::inflate) {

    private val viewModel: StudyViewModel by activityViewModels()

    // 태그
    private val logTag by lazy { FilterSortFragment::class.simpleName }

    private val filterWhere by lazy {
        arguments?.getString("filterWhere") ?: ""
    }

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 뷰 세팅
        initView()

        backButton()
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView() {

        // 바인딩
        with(binding) {

            // 툴바
            with(toolbarFilter) {
                // 뒤로가기 내비게이션 아이콘 클릭 시
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack(FragmentName.BOTTOM_NAVI.str, 0)
                }
            }

            // 분류 칩그룹 초기화
            setupChipGroup(chipGroupBunryu, getChipsByCategory(Category.TYPE))

            // 기간 칩그룹 초기화 및 비가시성 설정
            layoutFilterGigan.visibility = View.GONE
            setupChipGroup(chipGroupGigan, getChipsByCategory(Category.PERIOD))

            // 분류 칩 클릭 이벤트
            setChipClickListener(chipGroupBunryu, layoutFilterGigan)

            // 장소 칩그룹 초기화 및 비가시성 설정
            layoutFilterJangso.visibility = View.GONE
            setupChipGroup(chipGroupJangso, getChipsByCategory(Category.ONOFFLINE))

            // 기간 칩 클릭 이벤트
            setChipClickListener(chipGroupGigan, layoutFilterJangso)

            // 인원수 칩그룹 초기화 및 비가시성 설정
            layoutFilterInwon.visibility = View.GONE
            setupChipGroup(chipGroupInwon, getChipsByCategory(Category.PEOPLE))

            // 장소 칩 클릭 이벤트
            setChipClickListener(chipGroupJangso, layoutFilterInwon)

            // 신청 방식 칩그룹 초기화 및 비가시성 설정
            layoutFilterSinchung.visibility = View.GONE
            setupChipGroup(chipGroupSinchung, getChipsByCategory(Category.APPLY_METHOD))

            // 인원수 칩 클릭 이벤트
            setChipClickListener(chipGroupInwon, layoutFilterSinchung)

            // 기술 스택 칩그룹 초기화 및 비가시성 설정
            layoutFilterGisul.visibility = View.GONE
            setupChipGroup(chipGroupGisul, getChipsByCategory(Category.TECH_STACK))

            // 신청 방식 칩 클릭 이벤트
            setChipClickListener(chipGroupSinchung, layoutFilterGisul)

            // 기술 스택 칩 클릭 이벤트
            setTechStackChipClickListener(chipGroupGisul, layoutFilterPrograming)

            // 프로그래밍 언어 칩그룹 초기화 및 비가시성 설정
            layoutFilterPrograming.visibility = View.GONE
            setupChipGroup(chipGroupPrograming, getChipsByCategory(Category.PROGRAMMING))

            setChipClickListener(chipGroupPrograming, layoutFilterPrograming)

            // 필터 적용 버튼 클릭 시
            buttonApplyFilter.setOnClickListener {
                // 필터 데이터 수집
                val studyTypeValue = getSelectedChipText(chipGroupBunryu)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                val studyPeriodValue = getSelectedChipText(binding.chipGroupGigan)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                val studyOnOfflineValue = getSelectedChipText(chipGroupJangso)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                val studyMaxMemberValue = getSelectedChipText(chipGroupInwon)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                val studyApplyMethodValue = getSelectedChipText(chipGroupSinchung)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                val studySkillListValue = getSelectedChipText(chipGroupGisul)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                val programmingLanguageValue = getSelectedChipText(chipGroupPrograming)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                // 필터 데이터 담기
                val filterStudyData = FilterStudyData(
                    studyType = studyTypeValue,
                    studyPeriod = studyPeriodValue,
                    studyOnOffline = studyOnOfflineValue,
                    studyMaxMember = studyMaxMemberValue.toIntOrNull() ?: 0,  // 기본값으로 0 설정
                    studyApplyMethod = studyApplyMethodValue,
                    studySkillList = studySkillListValue,
                    programmingLanguage = programmingLanguageValue
                )

                Log.d("FilterSortFragment", "적용된 필터 데이터: $filterStudyData")


                when(filterWhere){
                    "StudyAllFragment" -> viewModel.getFilteredAllStudyList(filterStudyData)
                    "StudyMyFragment" -> viewModel.getFilteredMyStudyList(filterStudyData)
                }

                parentFragmentManager.popBackStack()
            }
        }
    }

    // 카테고리별 칩 리스트 생성
    private fun getChipsByCategory(category: Category): List<String> {
        return FilterSort.entries.filter { it.category == category }.map { it.displayName }
    }

    // ChipGroup 초기화 함수
    private fun setupChipGroup(chipGroup: ChipGroup, chipNames: List<String>) {
        chipGroup.isSingleSelection = true
        chipGroup.removeAllViews()

        chipNames.forEach { name ->
            val chip = Chip(chipGroup.context).apply {
                text = name
                isClickable = true
                isCheckable = true
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTextAppearance(R.style.ChipTextStyle)
                id = View.generateViewId()
            }
            chipGroup.addView(chip)
        }
        Log.d("FilterSortFragment", "ChipGroup 초기화: ${chipNames.joinToString()}")
    }

    // 기술 스택 칩 클릭 리스너 설정 함수
    private fun setTechStackChipClickListener(chipGroup: ChipGroup, targetLayout: View) {
        with(binding) {
            // ChipGroup의 상태가 변경될 때 호출되는 리스너 설정
            chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                if (checkedIds.isNotEmpty()) {
                    // 선택된 칩이 있는 경우
                    val checkedId = checkedIds[0] // 선택된 칩의 ID 가져오기
                    val chip = group.findViewById<Chip>(checkedId) // ID로 칩 찾기
                    updateVisibility(targetLayout, chip.isChecked) // 선택된 칩에 따라 레이아웃 가시성 업데이트
                    updateChipStyles(chipGroup, checkedId) // 선택된 칩의 스타일을 업데이트
                    Log.d("FilterSortFragment", "Chip 선택됨: ${chip.text}") // 로그로 선택된 칩의 텍스트 출력

                    // 선택된 칩의 텍스트를 TextView에 업데이트
                    textViewFilterPrograming.text = "${chip.text}"

                    // 선택된 칩의 텍스트에 따라 다른 카테고리의 칩 그룹을 설정하고 스크롤 이동
                    when (chip.text) {
                        "프로그래밍 언어" -> {
                            setupChipGroup(chipGroupPrograming, getChipsByCategory(Category.PROGRAMMING))
                            scrollToView(layoutFilterPrograming)
                        }
                        "프론트엔드" -> {
                            setupChipGroup(chipGroupPrograming, getChipsByCategory(Category.FRONT_END))
                            scrollToView(layoutFilterPrograming)
                        }
                        "백엔드" -> {
                            setupChipGroup(chipGroupPrograming, getChipsByCategory(Category.BACK_END))
                            scrollToView(layoutFilterPrograming)
                        }
                        "모바일개발" -> {
                            setupChipGroup(chipGroupPrograming, getChipsByCategory(Category.MOBILE))
                            scrollToView(layoutFilterPrograming)
                        }
                        "데이터과학" -> {
                            setupChipGroup(chipGroupPrograming, getChipsByCategory(Category.DATA_SCIENCE))
                            scrollToView(layoutFilterPrograming)
                        }
                        "데브옵스 및 시스템관리" -> {
                            setupChipGroup(chipGroupPrograming, getChipsByCategory(Category.DEVOPS))
                            scrollToView(layoutFilterPrograming)
                        }
                        "클라우드 및 인프라" -> {
                            setupChipGroup(chipGroupPrograming, getChipsByCategory(Category.CLOUD))
                            scrollToView(layoutFilterPrograming)
                        }
                        "게임개발" -> {
                            setupChipGroup(chipGroupPrograming, getChipsByCategory(Category.GAME_DEVELOPMENT))
                            scrollToView(layoutFilterPrograming)
                        }
                        "보안" -> {
                            setupChipGroup(chipGroupPrograming, getChipsByCategory(Category.SECURITY))
                            scrollToView(layoutFilterPrograming)
                        }
                        "인공지능" -> {
                            setupChipGroup(chipGroupPrograming, getChipsByCategory(Category.AI))
                            scrollToView(layoutFilterPrograming)
                        }
                        "UI/UX 디자인" -> {
                            setupChipGroup(chipGroupPrograming, getChipsByCategory(Category.UI_UX))
                            scrollToView(layoutFilterPrograming)
                        }
                        "빅데이터" -> {
                            setupChipGroup(chipGroupPrograming, getChipsByCategory(Category.BIG_DATA))
                            scrollToView(layoutFilterPrograming)
                        }
                        // 다른 카테고리 추가 가능...
                        else -> updateVisibility(layoutFilterPrograming, false) // 기본적으로 레이아웃 가시성 비활성화
                    }

                    // '전체'가 아닌 선택된 칩이 체크된 경우 레이아웃을 표시
                    updateVisibility(layoutFilterPrograming, chip.text != "전체" && chip.isChecked)
                } else {
                    // 선택된 칩이 없는 경우
                    updateVisibility(targetLayout, false) // 타겟 레이아웃을 숨기기
                    updateVisibility(layoutFilterPrograming, false) // 필터 레이아웃도 숨기기
                    updateChipStyles(chipGroup, -1) // 모든 칩 스타일 초기화
                    Log.d("FilterSortFragment", "Chip 선택 해제됨") // 로그로 칩 해제 출력
                }
                updateApplyButtonState() // 칩 상태 변경에 따라 적용 버튼 상태 업데이트
            }
        }
    }


    private fun setChipClickListener(chipGroup: ChipGroup, targetLayout: View) {
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedId = checkedIds[0]
                val chip = group.findViewById<Chip>(checkedId)
                updateVisibility(targetLayout, chip.isChecked)
                updateChipStyles(chipGroup, checkedId)
                Log.d("FilterSortFragment", "Chip 선택됨: ${chip.text}")
            } else {
                updateVisibility(targetLayout, false)
                updateChipStyles(chipGroup, -1) // Reset all chip styles
                Log.d("FilterSortFragment", "Chip 선택 해제됨")
            }
            updateApplyButtonState()
        }
    }

    // 칩 스타일 업데이트
    private fun updateChipStyles(group: ChipGroup, checkedId: Int) {
        group.children.forEach { view ->
            if (view is Chip) {
                updateChipStyle(view, view.id == checkedId)
            }
        }
    }

    // 개별 칩 스타일 업데이트
    private fun updateChipStyle(chip: Chip, isSelected: Boolean) {
        val context = chip.context
        val backgroundColor = if (isSelected) ContextCompat.getColor(context, R.color.pointColor) else ContextCompat.getColor(context, R.color.white)
        val textColor = if (isSelected) ContextCompat.getColor(context, R.color.white) else ContextCompat.getColor(context, R.color.black)

        chip.chipBackgroundColor = ColorStateList.valueOf(backgroundColor)
        chip.setTextColor(textColor)
    }

    // 가시성 업데이트
    private fun updateVisibility(view: View, isVisible: Boolean) {
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    // 선택된 칩의 텍스트 반환
    private fun getSelectedChipText(chipGroup: ChipGroup): String? {
        val selectedChipText = chipGroup.checkedChipId.takeIf { it != View.NO_ID }
            ?.let { chipGroup.findViewById<Chip>(it).text.toString() }
        Log.d("FilterSortFragment", "getSelectedChipText: $selectedChipText")
        return selectedChipText
    }

    // 스크롤 이동 함수
    private fun scrollToView(targetView: View) {
        with(binding){
            root.post {
                scrollViewFilterSort.smoothScrollTo(0, targetView.top)
            }
        }

    }

    // 추가된 부분: 적용 버튼 상태 업데이트 함수
    private fun updateApplyButtonState() {
        with(binding){
            val hasActiveChip = listOf(
                chipGroupBunryu,
                chipGroupGigan,
                chipGroupJangso,
                chipGroupInwon,
                chipGroupSinchung,
                chipGroupGisul,
                chipGroupPrograming
            ).any { it.checkedChipId != View.NO_ID }

            buttonApplyFilter.apply {
                isEnabled = hasActiveChip
                backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        context,
                        if (hasActiveChip) R.color.pointColor else R.color.buttonGray
                    )
                )
            }
        }

    }
    private fun backButton(){
        // 백버튼 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            parentFragmentManager.popBackStack()
        }
    }
}

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

    // 뷰모델
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
            // 분류 칩 클릭 이벤트
            setChipClickListener(chipGroupBunryu)

            // 기간 칩그룹 초기화
            setupChipGroup(chipGroupGigan, getChipsByCategory(Category.PERIOD))
            // 기간 칩 클릭 이벤트
            setChipClickListener(chipGroupGigan)

            // 장소 칩그룹 초기화
            setupChipGroup(chipGroupOnOffline, getChipsByCategory(Category.ONOFFLINE))
            // 장소 칩 클릭 이벤트
            setChipClickListener(chipGroupOnOffline)

            // 인원수 칩그룹 초기화
            setupChipGroup(chipGroupMaxMember, getChipsByCategory(Category.PEOPLE))
            // 인원수 칩 클릭 이벤트
            setChipClickListener(chipGroupMaxMember)




            // 필터 적용 버튼 클릭 시
            buttonApplyFilter.setOnClickListener {
                // 필터 데이터 수집
                val studyTypeValue = getSelectedChipText(chipGroupBunryu)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                val studyPeriodValue = getSelectedChipText(binding.chipGroupGigan)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                val studyOnOfflineValue = getSelectedChipText(chipGroupOnOffline)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                val studyMaxMemberValue = getSelectedChipText(chipGroupMaxMember)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                // 필터 데이터 담기
                val filterStudyData = FilterStudyData(
                    studyType = studyTypeValue,
                    studyPeriod = studyPeriodValue,
                    studyOnOffline = studyOnOfflineValue,
                    studyMaxMember = studyMaxMemberValue
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


    private fun setChipClickListener(chipGroup: ChipGroup) {
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedId = checkedIds[0]
                val chip = group.findViewById<Chip>(checkedId)
                updateChipStyles(chipGroup, checkedId)
                Log.d(logTag, "Chip 선택됨: ${chip.text}")
            } else {
                updateChipStyles(chipGroup, -1) // Reset all chip styles
                Log.d(logTag, "Chip 선택 해제됨")
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

    // 선택된 칩의 텍스트 반환
    private fun getSelectedChipText(chipGroup: ChipGroup): String? {
        val selectedChipText = chipGroup.checkedChipId.takeIf { it != View.NO_ID }
            ?.let { chipGroup.findViewById<Chip>(it).text.toString() }
        Log.d("FilterSortFragment", "getSelectedChipText: $selectedChipText")
        return selectedChipText
    }

    // 추가된 부분: 적용 버튼 상태 업데이트 함수
    private fun updateApplyButtonState() {
        with(binding){
            val hasActiveChip = listOf(
                chipGroupBunryu,
                chipGroupGigan,
                chipGroupOnOffline,
                chipGroupMaxMember
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

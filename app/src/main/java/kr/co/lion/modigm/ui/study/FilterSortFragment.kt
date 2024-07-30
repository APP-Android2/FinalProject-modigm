package kr.co.lion.modigm.ui.study

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFilterSortBinding
import kr.co.lion.modigm.ui.study.vm.StudyViewModel
import kr.co.lion.modigm.util.FilterSort
import kr.co.lion.modigm.util.FilterSort.Category
import kr.co.lion.modigm.util.FragmentName

class FilterSortFragment : Fragment(R.layout.fragment_filter_sort) {

    private val viewModel: StudyViewModel by activityViewModels()

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바인딩
        val binding = FragmentFilterSortBinding.bind(view)

        // 초기 뷰 세팅
        initView(binding)
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView(binding: FragmentFilterSortBinding) {

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
            setupChipGroup(binding.chipGroupBunryu, getChipsByCategory(Category.TYPE))

            // 기간 칩그룹 초기화 및 비가시성 설정
            binding.layoutFilterGigan.visibility = View.GONE
            setupChipGroup(binding.chipGroupGigan, getChipsByCategory(Category.PERIOD))

            // 분류 칩 클릭 이벤트
            setChipClickListener(binding, binding.chipGroupBunryu, binding.layoutFilterGigan)

            // 장소 칩그룹 초기화 및 비가시성 설정
            binding.layoutFilterJangso.visibility = View.GONE
            setupChipGroup(binding.chipGroupJangso, getChipsByCategory(Category.ONOFFLINE))

            // 기간 칩 클릭 이벤트
            setChipClickListener(binding, binding.chipGroupGigan, binding.layoutFilterJangso)

            // 인원수 칩그룹 초기화 및 비가시성 설정
            binding.layoutFilterInwon.visibility = View.GONE
            setupChipGroup(binding.chipGroupInwon, getChipsByCategory(Category.PEOPLE))

            // 장소 칩 클릭 이벤트
            setChipClickListener(binding, binding.chipGroupJangso, binding.layoutFilterInwon)

            // 신청 방식 칩그룹 초기화 및 비가시성 설정
            binding.layoutFilterSinchung.visibility = View.GONE
            setupChipGroup(binding.chipGroupSinchung, getChipsByCategory(Category.APPLY_METHOD))

            // 인원수 칩 클릭 이벤트
            setChipClickListener(binding, binding.chipGroupInwon, binding.layoutFilterSinchung)

            // 기술 스택 칩그룹 초기화 및 비가시성 설정
            binding.layoutFilterGisul.visibility = View.GONE
            setupChipGroup(binding.chipGroupGisul, getChipsByCategory(Category.TECH_STACK))

            // 신청 방식 칩 클릭 이벤트
            setChipClickListener(binding, binding.chipGroupSinchung, binding.layoutFilterGisul)

            // 기술 스택 칩 클릭 이벤트
            setTechStackChipClickListener(binding, binding.chipGroupGisul, binding.layoutFilterPrograming)

            // 프로그래밍 언어 칩그룹 초기화 및 비가시성 설정
            binding.layoutFilterPrograming.visibility = View.GONE
            setupChipGroup(binding.chipGroupPrograming, getChipsByCategory(Category.PROGRAMMING))

            setChipClickListener(binding, binding.chipGroupPrograming, binding.layoutFilterPrograming)

            // 필터 적용 버튼 클릭 시
            buttonApplyFilter.setOnClickListener {
                val filterData = mutableMapOf<String, String>()

                // studyType 필터 데이터 설정
                val selectedStudyTypeText = getSelectedChipText(binding.chipGroupBunryu)
                Log.d("FilterSortFragment", "선택된 분류 칩 텍스트: $selectedStudyTypeText")
                val studyTypeValue = selectedStudyTypeText?.let { displayName ->
                    FilterSort.fromDisplayName(displayName)?.value.toString()
                } ?: ""
                Log.d("FilterSortFragment", "변환된 studyType 값: $studyTypeValue")

                // studyPeriod 필터 데이터 설정
                val selectedStudyPeriodText = getSelectedChipText(binding.chipGroupGigan)
                Log.d("FilterSortFragment", "선택된 기간 칩 텍스트: $selectedStudyPeriodText")
                val studyPeriodValue = selectedStudyPeriodText?.let { displayName ->
                    FilterSort.fromDisplayName(displayName)?.value.toString()
                } ?: ""
                Log.d("FilterSortFragment", "변환된 studyPeriod 값: $studyPeriodValue")

                // studyOnOffline 필터 데이터 설정
                val selectedStudyOnOfflineText = getSelectedChipText(binding.chipGroupJangso)
                Log.d("FilterSortFragment", "선택된 장소 칩 텍스트: $selectedStudyOnOfflineText")
                val studyOnOfflineValue = selectedStudyOnOfflineText?.let { displayName ->
                    FilterSort.fromDisplayName(displayName)?.value.toString()
                } ?: ""
                Log.d("FilterSortFragment", "변환된 studyOnOffline 값: $studyOnOfflineValue")

                // studyMaxMember 필터 데이터 설정
                val selectedStudyMaxMemberText = getSelectedChipText(binding.chipGroupInwon)
                Log.d("FilterSortFragment", "선택된 최대 인원수 칩 텍스트: $selectedStudyMaxMemberText")
                val studyMaxMemberValue = selectedStudyMaxMemberText?.let { displayName ->
                    FilterSort.fromDisplayName(displayName)?.value.toString()
                } ?: ""
                Log.d("FilterSortFragment", "변환된 studyMaxMember 값: $studyMaxMemberValue")

                // studyApplyMethod 필터 데이터 설정
                val selectedStudyApplyMethodText = getSelectedChipText(binding.chipGroupSinchung)
                Log.d("FilterSortFragment", "선택된 신청 방식 칩 텍스트: $selectedStudyApplyMethodText")
                val studyApplyMethodValue = selectedStudyApplyMethodText?.let { displayName ->
                    FilterSort.fromDisplayName(displayName)?.value.toString()
                } ?: ""
                Log.d("FilterSortFragment", "변환된 studyApplyMethod 값: $studyApplyMethodValue")

                // studySkillList 필터 데이터 설정
                val selectedStudySkillListText = getSelectedChipText(binding.chipGroupGisul)
                Log.d("FilterSortFragment", "선택된 기술 스택 칩 텍스트: $selectedStudySkillListText")
                val studySkillListValue = selectedStudySkillListText?.let { displayName ->
                    FilterSort.fromDisplayName(displayName)?.value.toString()
                } ?: ""
                Log.d("FilterSortFragment", "변환된 studySkillList 값: $studySkillListValue")

                // programmingLanguage 필터 데이터 설정
                val selectedProgrammingLanguageText = getSelectedChipText(binding.chipGroupPrograming)
                Log.d("FilterSortFragment", "선택된 프로그래밍 언어 칩 텍스트: $selectedProgrammingLanguageText")
                val programmingLanguageValue = selectedProgrammingLanguageText?.let { displayName ->
                    FilterSort.fromDisplayName(displayName)?.value.toString()
                } ?: ""
                Log.d("FilterSortFragment", "변환된 programmingLanguage 값: $programmingLanguageValue")

                // 필터 데이터 담기
                filterData["studyType"] = studyTypeValue
                filterData["studyPeriod"] = studyPeriodValue
                filterData["studyOnOffline"] = studyOnOfflineValue
                filterData["studyMaxMember"] = studyMaxMemberValue
                filterData["studyApplyMethod"] = studyApplyMethodValue
                filterData["studySkillList"] = studySkillListValue
                filterData["programmingLanguage"] = programmingLanguageValue

                Log.d("FilterSortFragment", "적용된 필터 데이터: $filterData")
                viewModel.updateFilterData(filterData)
                parentFragmentManager.popBackStack()
            }
        }
    }

    // 카테고리별 칩 리스트 생성
    private fun getChipsByCategory(category: Category): List<String> {
        return FilterSort.values().filter { it.category == category }.map { it.displayName }
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
    private fun setTechStackChipClickListener(binding:FragmentFilterSortBinding, chipGroup: ChipGroup, targetLayout: View) {
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedId = checkedIds[0]
                val chip = group.findViewById<Chip>(checkedId)
                updateVisibility(targetLayout, chip.isChecked)
                updateChipStyles(chipGroup, checkedId)
                Log.d("FilterSortFragment", "Chip 선택됨: ${chip.text}")

                binding.textViewFilterPrograming.text = "${chip.text}" // 여기서 TextView 업데이트

                when (chip.text) {
                    "프로그래밍 언어" -> {
                        setupChipGroup(binding.chipGroupPrograming, getChipsByCategory(Category.PROGRAMMING))
                        scrollToView(binding, binding.layoutFilterPrograming)
                    }
                    "프론트엔드" -> {
                        setupChipGroup(binding.chipGroupPrograming, getChipsByCategory(Category.FRONT_END))
                        scrollToView(binding, binding.layoutFilterPrograming)
                    }
                    "백엔드" -> {
                        setupChipGroup(binding.chipGroupPrograming, getChipsByCategory(Category.BACK_END))
                        scrollToView(binding, binding.layoutFilterPrograming)
                    }
                    "모바일개발" -> {
                        setupChipGroup(binding.chipGroupPrograming, getChipsByCategory(Category.MOBILE))
                        scrollToView(binding, binding.layoutFilterPrograming)
                    }
                    "데이터과학" -> {
                        setupChipGroup(binding.chipGroupPrograming, getChipsByCategory(Category.DATA_SCIENCE))
                        scrollToView(binding, binding.layoutFilterPrograming)
                    }
                    "데브옵스 및 시스템관리" -> {
                        setupChipGroup(binding.chipGroupPrograming, getChipsByCategory(Category.DEVOPS))
                        scrollToView(binding, binding.layoutFilterPrograming)
                    }
                    "클라우드 및 인프라" -> {
                        setupChipGroup(binding.chipGroupPrograming, getChipsByCategory(Category.CLOUD))
                        scrollToView(binding, binding.layoutFilterPrograming)
                    }
                    "게임개발" -> {
                        setupChipGroup(binding.chipGroupPrograming, getChipsByCategory(Category.GAME_DEVELOPMENT))
                        scrollToView(binding, binding.layoutFilterPrograming)
                    }
                    "보안" -> {
                        setupChipGroup(binding.chipGroupPrograming, getChipsByCategory(Category.SECURITY))
                        scrollToView(binding, binding.layoutFilterPrograming)
                    }
                    "인공지능" -> {
                        setupChipGroup(binding.chipGroupPrograming, getChipsByCategory(Category.AI))
                        scrollToView(binding, binding.layoutFilterPrograming)
                    }
                    "UI/UX 디자인" -> {
                        setupChipGroup(binding.chipGroupPrograming, getChipsByCategory(Category.UI_UX))
                        scrollToView(binding, binding.layoutFilterPrograming)
                    }
                    "빅데이터" -> {
                        setupChipGroup(binding.chipGroupPrograming, getChipsByCategory(Category.BIG_DATA))
                        scrollToView(binding, binding.layoutFilterPrograming)
                    }

                    // 다른 카테고리 추가...
                    else -> updateVisibility(binding.layoutFilterPrograming, false)
                }

                updateVisibility(binding.layoutFilterPrograming, chip.text != "전체" && chip.isChecked)
            } else {
                updateVisibility(targetLayout, false)
                updateVisibility(binding.layoutFilterPrograming, false)
                updateChipStyles(chipGroup, -1) // Reset all chip styles
                Log.d("FilterSortFragment", "Chip 선택 해제됨")
            }
            updateApplyButtonState(binding)
        }
    }

    private fun setChipClickListener(binding: FragmentFilterSortBinding,chipGroup: ChipGroup, targetLayout: View) {
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
            updateApplyButtonState(binding)
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
    private fun scrollToView(binding:FragmentFilterSortBinding, targetView: View) {
        binding.root.post {
            binding.scrollViewFilterSort.smoothScrollTo(0, targetView.top)
        }
    }

    // 추가된 부분: 적용 버튼 상태 업데이트 함수
    private fun updateApplyButtonState(binding:FragmentFilterSortBinding) {
        val hasActiveChip = listOf(
            binding.chipGroupBunryu,
            binding.chipGroupGigan,
            binding.chipGroupJangso,
            binding.chipGroupInwon,
            binding.chipGroupSinchung,
            binding.chipGroupGisul,
            binding.chipGroupPrograming
        ).any { it.checkedChipId != View.NO_ID }

        binding.buttonApplyFilter.apply {
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

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

    // 기술 스택 선택 데이터를 담을 리스트
    private val selectedTechIdxs = mutableListOf<Int>()
    private lateinit var techStackData: List<Triple<Int, String, String>>

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
            viewModel.getTechStackData()

            // 기술 스택 데이터 초기화 (추가된 부분)
            viewModel.techStackData.observe(viewLifecycleOwner) { data ->
                techStackData = data
                val categories = techStackData.map { it.third }.distinct()
                setupChipGroupWithListener(chipGroupTechCategory, categories) { selectedCategory ->
                    val techNames = techStackData.filter { it.third == selectedCategory }.map { it.second }
                    setupMultiSelectableChipGroup(chipGroupTechName, techNames)
                }
            }

            // 일반 필터 칩 초기화
            setupChipGroupWithListener(chipGroupType, getChips(Category.TYPE))
            setupChipGroupWithListener(chipGroupPeriod, getChips(Category.PERIOD))
            setupChipGroupWithListener(chipGroupOnOffline, getChips(Category.ONOFFLINE))
            setupChipGroupWithListener(chipGroupMaxMember, getChips(Category.PEOPLE))

            // 필터 적용 버튼 클릭 시
            buttonApplyFilter.setOnClickListener {
                // 필터 데이터 수집
                val studyTypeValue = getSelectedChip(chipGroupType)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                val studyPeriodValue = getSelectedChip(chipGroupPeriod)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                val studyOnOfflineValue = getSelectedChip(chipGroupOnOffline)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                val studyMaxMemberValue = getSelectedChip(chipGroupMaxMember)?.let { displayName ->
                    if (displayName == "전체") "" else FilterSort.fromDisplayName(displayName)?.displayName.toString()
                } ?: ""

                // 선택된 TechName에 해당하는 TechIdx를 가져오기
                selectedTechIdxs.clear()
                chipGroupTechName.checkedChipIds.forEach { id ->
                    val chip = chipGroupTechName.findViewById<Chip>(id)
                    val techName = chip.text.toString()
                    techStackData.find { it.second == techName }?.first?.let { techIdx ->
                        selectedTechIdxs.add(techIdx)
                    }
                }

                // 필터 데이터 담기
                val filterStudyData = FilterStudyData(
                    studyType = studyTypeValue,
                    studyPeriod = studyPeriodValue,
                    studyOnOffline = studyOnOfflineValue,
                    studyMaxMember = studyMaxMemberValue,
                    studyTechStack = selectedTechIdxs // 선택된 TechIdx 목록 추가
                )

                Log.d(logTag, "적용된 필터 데이터: $filterStudyData")

                when(filterWhere){
                    FragmentName.STUDY_ALL.str -> viewModel.getFilteredAllStudyList(filterStudyData)
                    FragmentName.STUDY_MY.str -> viewModel.getFilteredMyStudyList(filterStudyData)
                }

                parentFragmentManager.popBackStack()
            }
        }
    }

    // 카테고리별 칩 리스트 생성
    private fun getChips(category: Category): List<String> {
        return FilterSort.entries.filter { it.category == category }.map { it.displayName }
    }

    // 다중 선택 가능한 ChipGroup 초기화 및 클릭 리스너 설정 함수
    private fun setupMultiSelectableChipGroup(chipGroup: ChipGroup, chipNames: List<String>) {
        chipGroup.isSingleSelection = false // 다중 선택 가능하도록 설정
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

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val context = group.context

            // 모든 칩 스타일 업데이트
            group.children.forEach { view ->
                if (view is Chip) {
                    val isSelected = view.id in checkedIds
                    val backgroundColor = if (isSelected) ContextCompat.getColor(context, R.color.pointColor) else ContextCompat.getColor(context, R.color.white)
                    val textColor = if (isSelected) ContextCompat.getColor(context, R.color.white) else ContextCompat.getColor(context, R.color.black)

                    view.chipBackgroundColor = ColorStateList.valueOf(backgroundColor)
                    view.setTextColor(textColor)
                }
            }

            // 로그 출력
            val selectedTechNames = checkedIds.map { id ->
                group.findViewById<Chip>(id).text.toString()
            }
            Log.d(logTag, "선택된 기술 스택: ${selectedTechNames.joinToString()}")
            updateApplyButtonState()
        }
    }

    // 기존 ChipGroup 초기화 및 클릭 리스너 설정 함수 (단일 선택용)
    private fun setupChipGroupWithListener(chipGroup: ChipGroup, chipNames: List<String>, onChipSelected: ((String) -> Unit)? = null) {
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

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val context = group.context
            val hasActiveChip = checkedIds.isNotEmpty()

            // 모든 칩 스타일 업데이트
            group.children.forEach { view ->
                if (view is Chip) {
                    val isSelected = view.id == checkedIds.firstOrNull()
                    val backgroundColor = if (isSelected) ContextCompat.getColor(context, R.color.pointColor) else ContextCompat.getColor(context, R.color.white)
                    val textColor = if (isSelected) ContextCompat.getColor(context, R.color.white) else ContextCompat.getColor(context, R.color.black)

                    view.chipBackgroundColor = ColorStateList.valueOf(backgroundColor)
                    view.setTextColor(textColor)
                }
            }

            // 로그 출력 및 칩 선택 처리
            if (hasActiveChip) {
                val checkedId = checkedIds[0]
                val chip = group.findViewById<Chip>(checkedId)
                Log.d(logTag, "Chip 선택됨: ${chip.text}")

                // 칩 선택 시 추가 작업 수행
                onChipSelected?.invoke(chip.text.toString())
            } else {
                Log.d(logTag, "Chip 선택 해제됨")
            }

            // 적용 버튼 상태 업데이트 (선택된 칩이 하나라도 있으면 버튼 활성화)
            updateApplyButtonState()
        }

        Log.d(logTag, "ChipGroup 초기화 및 리스너 설정: ${chipNames.joinToString()}")
    }

    // 적용 버튼 상태 업데이트 함수
    private fun updateApplyButtonState() {
        with(binding) {
            val hasActiveChip = listOf(
                chipGroupType,
                chipGroupPeriod,
                chipGroupOnOffline,
                chipGroupMaxMember,
                chipGroupTechCategory,
                chipGroupTechName
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

    // 선택된 칩의 텍스트 반환
    private fun getSelectedChip(chipGroup: ChipGroup): String? {
        val selectedChipText = chipGroup.checkedChipId.takeIf { it != View.NO_ID }
            ?.let { chipGroup.findViewById<Chip>(it).text.toString() }
        Log.d(logTag, "getSelectedChipText: $selectedChipText")
        return selectedChipText
    }

    private fun backButton(){
        // 백버튼 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            parentFragmentManager.popBackStack()
        }
    }
}


package kr.co.lion.modigm.ui.detail

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentSkillBottomSheetBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.VBBaseBottomSheetFragment
import kr.co.lion.modigm.util.Skill

class SkillBottomSheetFragment : VBBaseBottomSheetFragment<FragmentSkillBottomSheetBinding>(FragmentSkillBottomSheetBinding::inflate) {

    private var selectedSkills: MutableSet<Skill> = mutableSetOf()  // 선택된 스킬 관리
    private var skillSelectedListener: OnSkillSelectedListener? = null

    private var initialSkills: List<Skill> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.style_bottom_sheet_background)
        initializeCategoryChips()
        setupCompleteButton()
        setupScrollView()
        setupSkillSelectScrollView()
        ScrollViewSkillSelectVisibility()
        binding.imageViewSkillBottomSheetClose.setOnClickListener { dismiss() }

        // 초기 선택된 스킬을 바인딩이 완료된 후에 처리
        applySelectedSkills()
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        dialog?.let {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBehavior.isDraggable = false
            bottomSheet.layoutParams.height = (getScreenHeight(requireContext()) * 0.8).toInt()
        }
    }

    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            metrics.bounds.height() - insets.top - insets.bottom
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }

    fun setOnSkillSelectedListener(listener: OnSkillSelectedListener) {
        skillSelectedListener = listener
    }

    fun setSelectedSkills(skills: List<Skill>) {
        initialSkills = skills
        // 초기 스킬 설정을 여기에 저장만 하고, 실제 UI 업데이트는 onViewCreated에서 처리
    }

    private fun applySelectedSkills() {
        // 실제로 UI에 초기 선택된 스킬을 반영
        selectedSkills.clear()
        selectedSkills.addAll(initialSkills)
        updateSelectedChipsUI()

        initialSkills.forEach { skill ->
            val category = skill.category
            if (category != null) {
                updateCategoryChipState(skill, true)
                displaySubCategories(category)
//                selectSubCategoryChip(skill)

                // 모든 카테고리의 "기타"가 선택된 경우, 각 카테고리별로 선택 상태 유지
                if (skill.displayName == "기타") {
                    selectSubCategoryChip(skill)
                }
            }
        }
    }

    fun selectSubCategoryChip(skill: Skill) {
        binding.subCategoryChipGroupSkill.children.forEach { view ->
            val chip = view as Chip
            if (chip.text == skill.displayName) {
                // 카테고리별로 동일한 "기타"가 선택될 수 있으므로, 카테고리를 기반으로 체크
                if (skill.category != null && chip.tag == skill.category) {
                    chip.isChecked = true
                    updateChipStyle(chip, true)
                }
            }
        }
    }

    fun setupScrollView() {
        val scrollView = binding.ScrollViewSkill
        scrollView.isVerticalScrollBarEnabled = true
        scrollView.isScrollbarFadingEnabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val thumbDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(requireContext(), R.color.buttonGray))
                setSize(dpToPx(requireContext(), 4f).toInt(), -1)
                cornerRadius = dpToPx(requireContext(), 4f)
            }
            scrollView.verticalScrollbarThumbDrawable = thumbDrawable
        }
    }

    fun setupSkillSelectScrollView() {
        val scrollViewSkillSelect = binding.ScrollViewSkillSelect
        scrollViewSkillSelect.isVerticalScrollBarEnabled = true
        scrollViewSkillSelect.isScrollbarFadingEnabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val thumbDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(requireContext(), R.color.buttonGray))
                setSize(dpToPx(requireContext(), 4f).toInt(), -1)
                cornerRadius = dpToPx(requireContext(), 2f)
            }
            scrollViewSkillSelect.verticalScrollbarThumbDrawable = thumbDrawable
        }
    }

    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun ScrollViewSkillSelectVisibility() {
        val scrollViewSkillSelect = binding.ScrollViewSkillSelect
        scrollViewSkillSelect.visibility = if (selectedSkills.isNotEmpty()) View.VISIBLE else View.GONE
        if (scrollViewSkillSelect.visibility == View.VISIBLE) {
            scrollToBottom()
        }
    }

    fun initializeCategoryChips() {
        Skill.Category.values().forEach { category ->
            val categoryName = getCategoryName(category)
            val chip = Chip(context).apply {
                text = categoryName
                isClickable = true
                isCheckable = true
                setTextAppearance(R.style.ChipTextStyle)
                updateChipStyle(this, false)
            }
            chip.setOnCheckedChangeListener { _, isChecked ->
                updateChipStyle(chip, isChecked)
                if (isChecked) {
                    if (category == Skill.Category.OTHER) {
                        selectedSkills.clear()
                        selectedSkills.add(Skill.OTHER)
                        updateSelectedChipsUI()
                    } else {
                        selectedSkills.remove(Skill.OTHER)
                        updateCategoryChipState(Skill.OTHER, false)
                        displaySubCategories(category)
                    }

                }else {
                    // 카테고리 선택 해제 시 서브 카테고리 닫기
                    collapseSubCategories(category) // 서브 카테고리 닫기
                }
            }
            binding.chipGroupSkill.addView(chip)
        }
    }

    fun getCategoryName(category: Skill.Category): String {
        return when (category) {
            Skill.Category.PROGRAMMING -> "프로그래밍 언어"
            Skill.Category.FRONT_END -> "프론트 엔드"
            Skill.Category.BACK_END -> "백엔드"
            Skill.Category.MOBILE -> "모바일 개발"
            Skill.Category.DATA_SCIENCE -> "데이터 사이언스"
            Skill.Category.DEVOPS -> "데브옵스 및 시스템 관리"
            Skill.Category.CLOUD -> "클라우드 및 인프라"
            Skill.Category.GAME_DEVELOPMENT -> "게임 개발"
            Skill.Category.SECURITY -> "보안"
            Skill.Category.AI -> "인공지능"
            Skill.Category.UI_UX -> "UI/UX 디자인"
            Skill.Category.BIG_DATA -> "빅데이터"
            Skill.Category.OTHER -> "기타"
        }
    }

    fun displaySubCategories(category: Skill.Category) {
        binding.subCategoryChipGroupSkill.removeAllViews()
        binding.subCategoryChipGroupSkill.visibility = View.VISIBLE // 서브 카테고리를 다시 표시

        binding.subCategoryTextView.visibility = View.VISIBLE
        // 선택한 카테고리의 이름을 subCategoryTextView에 설정
        binding.subCategoryTextView.text = getCategoryName(category)

        if (category == Skill.Category.OTHER) {
            selectedSkills.clear()
            updateSelectedChipsUI()
            return
        }
        Skill.values().filter { it.category == category }.forEach { skill ->
            val chip = Chip(context).apply {
                text = skill.displayName
                isClickable = true
                isCheckable = true
                setTextAppearance(R.style.ChipTextStyle)
                updateChipStyle(this, selectedSkills.contains(skill)) // 선택 상태 반영
                isChecked = selectedSkills.contains(skill)  // 이미 선택된 스킬인 경우 선택된 상태로 설정
            }
            chip.setOnCheckedChangeListener { _, isChecked ->
                updateChipStyle(chip, isChecked)
                if (isChecked) {
                    selectedSkills.add(skill)
                } else {
                    selectedSkills.remove(skill)
                }
                updateSelectedChipsUI()
            }
            binding.subCategoryChipGroupSkill.addView(chip)
        }
        ScrollViewSkillSelectVisibility()
    }

    fun updateSelectedChipsUI() {
        binding.chipGroupSelectedItems.removeAllViews()

        // 기타 스킬을 필터링하여 "기타"는 하나만 남김
        val skillsToDisplay = selectedSkills.groupBy { it.displayName }
            .mapValues { entry ->
                if (entry.key == "기타") {
                    listOf(entry.value.first())  // "기타"인 항목이 여러 개 있으면 첫 번째만 남김
                } else {
                    entry.value
                }
            }.flatMap { it.value }

        skillsToDisplay.forEach { skill ->
            val chip = Chip(context).apply {
                text = skill.displayName
                isCloseIconVisible = true
                setTextAppearance(R.style.ChipTextStyle)
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dividerView))
                setOnCloseIconClickListener {
//                    selectedSkills.remove(skill)
//                    updateCategoryChipState(skill, false)
//                    updateSelectedChipsUI()

                    // "기타" 칩일 경우 모든 카테고리의 "기타" 칩을 선택 해제
                    if (skill.displayName == "기타") {
                        removeAllGitaChips() // "기타" 칩들을 모두 삭제하는 함수 호출
                    } else {
                        selectedSkills.remove(skill)  // 일반 스킬 제거
                        updateCategoryChipState(skill, false)
                    }
                    // UI 업데이트 및 서브 카테고리 칩 새로고침
                    updateSelectedChipsUI()
                    refreshSubCategoryChips(skill.category)  // 선택된 스킬에 맞춰 서브 카테고리 칩 새로고침
                }
            }
            binding.chipGroupSelectedItems.addView(chip)
        }
        if (selectedSkills.isEmpty() ) {
            val chip = Chip(context).apply {
                text = "기타"
                isCloseIconVisible = false
                setTextAppearance(R.style.ChipTextStyle)
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dividerView))
            }
            binding.chipGroupSelectedItems.addView(chip)
        }
        ScrollViewSkillSelectVisibility()
    }

    // 서브 카테고리 칩을 새로고침하는 함수
    fun refreshSubCategoryChips(category: Skill.Category?) {
        if (category == null) return

        // 선택된 카테고리에 맞는 서브 카테고리 칩을 다시 그립니다.
        binding.subCategoryChipGroupSkill.removeAllViews()

        Skill.values().filter { it.category == category }.forEach { skill ->
            val chip = Chip(context).apply {
                text = skill.displayName
                isClickable = true
                isCheckable = true
                setTextAppearance(R.style.ChipTextStyle)
                updateChipStyle(this, selectedSkills.contains(skill)) // 선택 상태 반영
                isChecked = selectedSkills.contains(skill)  // 이미 선택된 스킬인 경우 선택된 상태로 설정
            }
            chip.setOnCheckedChangeListener { _, isChecked ->
                updateChipStyle(chip, isChecked)
                if (isChecked) {
                    selectedSkills.add(skill)
                } else {
                    selectedSkills.remove(skill)
                }
                updateSelectedChipsUI()
            }
            binding.subCategoryChipGroupSkill.addView(chip)
        }

        ScrollViewSkillSelectVisibility()
    }

    fun removeAllGitaChips() {
        // "기타"가 포함된 모든 스킬을 선택 해제
        val toRemove = selectedSkills.filter { it.displayName == "기타" }
        selectedSkills.removeAll(toRemove)  // 선택된 "기타" 스킬 제거

        // 모든 카테고리에서 "기타" 칩의 선택 상태를 해제
        Skill.Category.values().forEach { category ->
            val gitaSkill = Skill.values().find { it.displayName == "기타" && it.category == category }
            gitaSkill?.let {
                updateCategoryChipState(it, false) // "기타" 선택 해제
                collapseSubCategories(category)     // 서브 카테고리 접기
            }
        }
        // 선택 상태를 적용하고 화면을 새로고침
        updateSelectedChipsUI()  // 칩 삭제 후 UI를 새로고침
        refreshSubCategoryChips(null)  // 모든 서브 카테고리 칩 새로고침
    }

    // 카테고리를 접는 함수
    fun collapseSubCategories(category: Skill.Category) {
        // 서브 카테고리 칩을 모두 제거하여 접기
        if (category != Skill.Category.OTHER) {
            // 서브 카테고리 전체 삭제
            binding.subCategoryChipGroupSkill.removeAllViews()
            binding.subCategoryChipGroupSkill.visibility = View.GONE
            binding.subCategoryTextView.visibility = View.GONE
        }
    }



    fun updateCategoryChipState(skill: Skill, isSelected: Boolean) {
        // category가 null이 아닌지 확인
        val category = skill.category
        if (category != null) {
            binding.chipGroupSkill.children.forEach { view ->
                val chip = view as Chip
                if (chip.text.toString() == getCategoryName(category)) {
                    chip.isChecked = isSelected
                    updateChipStyle(chip, isSelected)

                    if (isSelected) {
                        // 해당 카테고리의 서브 카테고리 칩들을 생성
                        displaySubCategories(category)
                    } else {
                        // 메인 카테고리 선택이 해제되면 서브 카테고리도 접음
                        collapseSubCategories(category)  // 카테고리가 해제되었을 때 서브 카테고리 접기
                    }
                }
            }
        } else {
            println("Category is null for skill: ${skill.displayName}")
        }
    }


    fun setupCompleteButton() {
        binding.buttonComplete.setOnClickListener {
            skillSelectedListener?.onSkillSelected(selectedSkills.toList())
            dismiss()
        }
    }

    fun updateChipStyle(chip: Chip, isSelected: Boolean) {
        if (isSelected) {
            chip.setChipBackgroundColorResource(R.color.pointColor) // 선택된 상태의 배경색
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // 선택된 상태의 텍스트 색상
        } else {
            chip.setChipBackgroundColorResource(R.color.white) // 비선택 상태의 배경색
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.black)) // 비선택 상태의 텍스트 색상
        }
    }

    fun scrollToBottom() {
        binding.ScrollViewSkill.post {
            val scrollView = binding.ScrollViewSkill
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

}

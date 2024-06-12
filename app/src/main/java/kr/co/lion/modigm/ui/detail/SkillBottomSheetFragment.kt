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
import kr.co.lion.modigm.util.Skill

class SkillBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentSkillBottomSheetBinding
    private var selectedSkills: MutableSet<Skill> = mutableSetOf()  // 선택된 스킬 관리
    private var skillSelectedListener: OnSkillSelectedListener? = null

    private var initialSkills: List<Skill> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSkillBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.style_bottom_sheet_background)
        initializeCategoryChips()
        setupCompleteButton()
        setupScrollView()
        setupSkillSelectScrollView()
        ScrollViewSkillSelectVisibility()
        binding.imageViewSkillBottomSheetClose.setOnClickListener { dismiss() }

        // 초기 선택된 스킬 설정
        setSelectedSkills(initialSkills)
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
        if (::binding.isInitialized) {
            selectedSkills.clear()
            selectedSkills.addAll(skills)
            updateSelectedChipsUI()
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
                    if (category == Skill.Category.OTHER) {
                        binding.subCategoryChipGroupSkill.removeAllViews()
                    }
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
                updateChipStyle(this, false)
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
        selectedSkills.forEach { skill ->
            val chip = Chip(context).apply {
                text = skill.displayName
                isCloseIconVisible = true
                setTextAppearance(R.style.ChipTextStyle)
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dividerView))
                setOnCloseIconClickListener {
                    selectedSkills.remove(skill)
                    updateCategoryChipState(skill, false)
                    updateSelectedChipsUI()
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

    fun updateCategoryChipState(skill: Skill, isSelected: Boolean) {
        binding.chipGroupSkill.children.forEach {
            val chip = it as Chip
            if (chip.text.toString() == getCategoryName(skill.category ?: Skill.Category.OTHER)) {
                chip.isChecked = isSelected
                updateChipStyle(chip, isSelected)
            }
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
            chip.setChipBackgroundColorResource(R.color.pointColor)
            chip.setTextColor(resources.getColor(R.color.white, null))
        } else {
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setTextColor(resources.getColor(R.color.black, null))
        }
    }

    fun scrollToBottom() {
        binding.ScrollViewSkill.post {
            val scrollView = binding.ScrollViewSkill
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

}

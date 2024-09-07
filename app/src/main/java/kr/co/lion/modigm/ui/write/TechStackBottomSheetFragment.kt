package kr.co.lion.modigm.ui.write

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentTechStackBottomSheetBinding
import kr.co.lion.modigm.model.TechStackData
import kr.co.lion.modigm.ui.VBBaseBottomSheetFragment
import kr.co.lion.modigm.ui.write.vm.WriteViewModel

class TechStackBottomSheetFragment : VBBaseBottomSheetFragment<FragmentTechStackBottomSheetBinding>(FragmentTechStackBottomSheetBinding::inflate) {

    private val viewModel: WriteViewModel by activityViewModels()  // 뷰모델 연결
    private var selectedTechStacks: MutableSet<TechStackData> = mutableSetOf()  // 선택된 스킬 관리
    private var techStackSelectedListener: OnTechStackSelectedListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.style_bottom_sheet_background)

        // ViewModel 데이터 옵저빙
        observeTechStackData()
        setupCompleteButton()
        setupTechStackSelectScrollView()
        scrollViewTechStackSelectVisibility()
        binding.imageViewTechStackBottomSheetClose.setOnClickListener { dismiss() }
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

    /**
     * 기술 스택 데이터 옵저빙
     */
    private fun observeTechStackData() {
        viewModel.techStackData.observe(viewLifecycleOwner) { techStackList ->
            initializeCategoryChips(techStackList)
        }

        // 기술 스택 데이터 로드
        viewModel.getTechStackData()
    }

    /**
     * 카테고리 칩을 초기화하고 기술 스택을 추가하는 함수
     */
    private fun initializeCategoryChips(techStackList: List<TechStackData>) {
        // 기존 칩들을 먼저 제거하여 중복 추가를 방지
        binding.chipGroupTechStack.removeAllViews()
        // 카테고리별로 그룹화
        val categoryMap = techStackList.groupBy { it.techCategory }

        // 카테고리 칩 추가
        categoryMap.keys.forEach { category ->
            val chip = Chip(context).apply {
                text = category
                isClickable = true
                isCheckable = true
                setTextAppearance(R.style.ChipTextStyle)
                updateChipStyle(this, false)
            }

            // 카테고리 칩 선택 시 서브 카테고리 칩들 생성
            chip.setOnCheckedChangeListener { _, isChecked ->
                updateChipStyle(chip, isChecked)
                if (isChecked) {
                    displaySubCategories(categoryMap[category] ?: emptyList())
                } else {
                    binding.subCategoryChipGroupTechStack.removeAllViews()
                }
            }

            // 카테고리 칩을 ChipGroup에 추가
            binding.chipGroupTechStack.addView(chip)
        }
    }

    /**
     * 선택한 카테고리의 기술 스택 칩들을 생성하는 함수
     */
    private fun displaySubCategories(techStackList: List<TechStackData>) {
        binding.subCategoryChipGroupTechStack.removeAllViews()

        techStackList.forEach { techStack ->
            val chip = Chip(context).apply {
                text = techStack.techName
                isClickable = true
                isCheckable = true
                setTextAppearance(R.style.ChipTextStyle)
                updateChipStyle(this, selectedTechStacks.contains(techStack))
                isChecked = selectedTechStacks.contains(techStack)  // 이미 선택된 기술 스택일 경우 선택된 상태로 설정
            }

            // 기술 스택 칩 선택 시 처리
            chip.setOnCheckedChangeListener { _, isChecked ->
                updateChipStyle(chip, isChecked)
                if (isChecked) {
                    selectedTechStacks.add(techStack)
                } else {
                    selectedTechStacks.remove(techStack)
                }
                updateSelectedChipsUI()
            }

            // 서브 카테고리 ChipGroup에 추가
            binding.subCategoryChipGroupTechStack.addView(chip)
        }
        scrollViewTechStackSelectVisibility()
    }

    /**
     * 선택된 기술 스택을 표시하는 UI 업데이트
     */
    private fun updateSelectedChipsUI() {
        binding.chipGroupSelectedItems.removeAllViews()

        selectedTechStacks.forEach { techStack ->
            val chip = Chip(context).apply {
                text = techStack.techName
                isCloseIconVisible = true
                setTextAppearance(R.style.ChipTextStyle)
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dividerView))
                setOnCloseIconClickListener {
                    selectedTechStacks.remove(techStack)
                    updateSelectedChipsUI()
                }
            }
            binding.chipGroupSelectedItems.addView(chip)
        }
        if (selectedTechStacks.isEmpty() ) {
            val chip = Chip(context).apply {
                text = "기타"
                isCloseIconVisible = false
                setTextAppearance(R.style.ChipTextStyle)
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dividerView))
            }
            binding.chipGroupSelectedItems.addView(chip)
        }

        scrollViewTechStackSelectVisibility()
    }

    /**
     * 버튼 클릭 시 선택한 스킬을 리스너에 전달
     */
    private fun setupCompleteButton() {
        binding.buttonComplete.setOnClickListener {
            techStackSelectedListener?.onTechStackSelected(selectedTechStacks.toList())
            dismiss()
        }
    }

    /**
     * 칩 스타일 업데이트 함수
     */
    private fun updateChipStyle(chip: Chip, isSelected: Boolean) {
        if (isSelected) {
            chip.setChipBackgroundColorResource(R.color.pointColor)  // 선택된 상태의 배경색
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))  // 선택된 상태의 텍스트 색상
        } else {
            chip.setChipBackgroundColorResource(R.color.white)  // 비선택 상태의 배경색
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))  // 비선택 상태의 텍스트 색상
        }
    }

    /**
     * 스크롤뷰 가시성 처리
     */
    private fun scrollViewTechStackSelectVisibility() {
        val scrollViewTechStackSelect = binding.ScrollViewTechStackSelect
        scrollViewTechStackSelect.visibility = if (selectedTechStacks.isNotEmpty()) View.VISIBLE else View.GONE
        if (scrollViewTechStackSelect.visibility == View.VISIBLE) {
            scrollToBottom()
        }
    }

    /**
     * 스크롤뷰를 맨 아래로 이동
     */
    private fun scrollToBottom() {
        binding.ScrollViewTechStack.post {
            val scrollView = binding.ScrollViewTechStack
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    fun setOnTechStackSelectedListener(listener: OnTechStackSelectedListener) {
        techStackSelectedListener = listener
    }


    private fun setupTechStackSelectScrollView() {
        val scrollViewTechStackSelect = binding.ScrollViewTechStackSelect
        scrollViewTechStackSelect.isVerticalScrollBarEnabled = true
        scrollViewTechStackSelect.isScrollbarFadingEnabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val thumbDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(requireContext(), R.color.buttonGray))
                setSize(dpToPx(requireContext(), 4f).toInt(), -1)
                cornerRadius = dpToPx(requireContext(), 2f)
            }
            scrollViewTechStackSelect.verticalScrollbarThumbDrawable = thumbDrawable
        }
    }

    private fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    private fun getScreenHeight(context: Context): Int {
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
}


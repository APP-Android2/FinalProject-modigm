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

/**
 * 기술 스택을 선택하는 BottomSheetDialogFragment.
 * 사용자는 기술 스택을 선택하고 이를 상위 Fragment/Activity에 전달 가능.
 */
class TechStackBottomSheetFragment : VBBaseBottomSheetFragment<FragmentTechStackBottomSheetBinding>(FragmentTechStackBottomSheetBinding::inflate) {

    // 액티비티와 공유하는 뷰모델
    private val viewModel: WriteViewModel by activityViewModels()

    // 사용자가 선택한 기술 스택 목록
    private var selectedTechStacks: MutableSet<TechStackData> = mutableSetOf()

    // 기술 스택이 선택되었을 때 호출되는 리스너
    private var techStackSelectedListener: OnTechStackSelectedListener? = null

    /**
     * Fragment가 생성되었을 때 호출되며, 뷰 초기화 및 ViewModel 옵저버 설정을 담당.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.style_bottom_sheet_background)

        // 뷰 초기화 및 뷰모델 데이터 관찰
        initView()
        observeViewModel()
    }

    /**
     * BottomSheetDialog가 화면에 나타날 때 호출되며,
     * Dialog의 설정 및 화면 크기 조정을 처리.
     */
    override fun onStart() {
        super.onStart()
        setupBottomSheetDialog()
    }

    /**
     * 뷰 초기화 함수. UI 요소들의 클릭 이벤트 및 스크롤 설정 등을 처리.
     */
    private fun initView() {
        // 스크롤뷰 설정
        setupTechStackSelectScrollView()
        scrollViewTechStackSelectVisibility()

        // 완료 버튼 및 닫기 버튼 설정
        with(binding) {
            // 완료 버튼 클릭 시 선택된 기술 스택 목록을 리스너에 전달하고 다이얼로그 닫기
            buttonComplete.apply {
                setOnClickListener {
                    techStackSelectedListener?.onTechStackSelected(selectedTechStacks.toList())
                    viewModel.updateSelectedTechStacks(selectedTechStacks) // ViewModel에 저장
                    dismiss()
                }
            }

            // 닫기 버튼 클릭 시 다이얼로그 닫기
            imageViewTechStackBottomSheetClose.apply {
                setOnClickListener { dismiss() }
            }
        }
    }

    /**
     * BottomSheetDialog의 동작 및 크기를 설정하는 함수.
     * 화면의 80% 높이로 설정하며, 드래그로 닫히지 않도록 설정.
     */
    private fun setupBottomSheetDialog() {
        (dialog as? BottomSheetDialog)?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let { bottomSheet ->
            BottomSheetBehavior.from(bottomSheet).apply {
                state = BottomSheetBehavior.STATE_EXPANDED  // 다이얼로그 시작 시 확장 상태로 설정
                isDraggable = false  // 다이얼로그를 드래그로 닫을 수 없게 설정
            }
            bottomSheet.layoutParams.height = (getScreenHeight(requireContext()) * 0.8).toInt()  // 화면의 80% 높이로 설정
        }
    }

    /**
     * ViewModel에 있는 기술 스택 데이터를 관찰하는 함수.
     * ViewModel의 데이터를 받아서 기술 스택을 화면에 표시.
     */
    private fun observeViewModel() {
        viewModel.techStackData.observe(viewLifecycleOwner) { techStackList ->
            showMainCategory(techStackList)  // 기술 스택 목록을 화면에 표시
        }

        // ViewModel의 선택된 데이터로 초기화
        viewModel.selectedTechStacks.observe(viewLifecycleOwner) { techStacks ->
            selectedTechStacks.clear()
            selectedTechStacks.addAll(techStacks)
            updateSelectedChipsUI()
        }

        viewModel.getTechStackData()  // ViewModel에서 데이터 요청
    }

    /**
     * 카테고리별로 그룹화된 기술 스택을 화면에 칩 형태로 추가하는 함수.
     * 각 카테고리 칩을 선택하면 해당 카테고리의 하위 기술 스택이 표시됨.
     */
    private fun showMainCategory(techStackList: List<TechStackData>) {
        with(binding) {
            chipGroupTechStack.removeAllViews()  // 기존 칩들을 먼저 제거

            // 기술 스택을 카테고리별로 그룹화하여 칩 추가
            techStackList
                .groupBy { it.techCategory }
                .forEach { (category, techStacks) ->
                    Chip(context).apply {
                        text = category  // 카테고리 이름 설정
                        isClickable = true
                        isCheckable = true  // 선택 가능하도록 설정
                        setTextAppearance(R.style.ChipTextStyle)
                        updateChipStyle(this, false)  // 초기 스타일 설정

                        // 카테고리 칩 선택 시 하위 기술 스택 표시
                        setOnCheckedChangeListener { _, isChecked ->
                            updateChipStyle(this, isChecked)
                            if (isChecked) {
                                if(category == "기타"){
                                    selectedTechStacks.add(TechStackData(104, "기타", "기타"))
                                    updateSelectedChipsUI()
                                }
                                showSubCategory(techStacks)
                            } else {
                                subCategoryChipGroupTechStack.removeAllViews()  // 선택 해제 시 하위 칩 제거
                            }
                        }
                    }.also { chipGroupTechStack.addView(it) }  // 카테고리 칩을 그룹에 추가
                }
        }
        // 선택된 기술 스택을 UI에 반영
        updateSelectedChipsUI()
    }

    /**
     * 선택된 카테고리의 하위 기술 스택을 칩 형태로 화면에 표시하는 함수.
     */
    private fun showSubCategory(techStackList: List<TechStackData>) {
        with(binding.subCategoryChipGroupTechStack) {
            removeAllViews()  // 기존 하위 칩들을 제거

            // 각 기술 스택에 대해 칩을 생성하여 추가
            techStackList.forEach { techStack ->
                Chip(context).apply {
                    text = techStack.techName  // 기술 스택 이름 설정
                    isClickable = true
                    isCheckable = true
                    setTextAppearance(R.style.ChipTextStyle)
                    updateChipStyle(this, selectedTechStacks.contains(techStack))  // 선택된 상태에 맞춰 스타일 적용
                    isChecked = selectedTechStacks.contains(techStack)  // 이미 선택된 기술 스택은 선택된 상태로 표시

                    // 기술 스택 칩 선택/해제 시 처리
                    setOnCheckedChangeListener { _, isChecked ->
                        updateChipStyle(this, isChecked)
                        if (isChecked) selectedTechStacks.add(techStack)
                        else selectedTechStacks.remove(techStack)
                        updateSelectedChipsUI()  // 선택된 기술 스택 UI 업데이트
                    }
                }.also { addView(it) }  // 기술 스택 칩을 그룹에 추가
            }
            scrollViewTechStackSelectVisibility()  // 스크롤뷰 가시성 업데이트
        }
    }

    /**
     * 선택된 기술 스택을 화면에 표시하는 함수.
     * 사용자가 선택한 기술 스택이 상단에 표시됨.
     */
    private fun updateSelectedChipsUI() {
        with(binding.chipGroupSelectedItems) {
            removeAllViews()  // 기존 선택된 칩들을 제거
            selectedTechStacks
                .distinctBy { it.techName } // "기타" 칩 중복 제거
                .forEach { techStack ->
                    Chip(context).apply {
                        text = techStack.techName
                        isCloseIconVisible = true  // 선택된 기술 스택 칩에 삭제 버튼 표시
                        setTextAppearance(R.style.ChipTextStyle)
                        chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dividerView))
                        setOnCloseIconClickListener {
                            selectedTechStacks.remove(techStack)  // 삭제 버튼 클릭 시 선택 목록에서 제거
                            viewModel.updateSelectedTechStacks(selectedTechStacks)  // ViewModel 업데이트
                            updateSelectedChipsUI()  // UI 업데이트
                        }
                    }.also { addView(it) }
                }

            scrollViewTechStackSelectVisibility()  // 스크롤뷰 가시성 업데이트
        }
    }

    /**
     * 칩의 스타일을 업데이트하는 함수.
     * 선택된 상태와 미선택 상태에 따라 칩의 배경색과 텍스트 색상이 변경됨.
     */
    private fun updateChipStyle(chip: Chip, isSelected: Boolean) {
        // chip의 배경색과 텍스트 색상을 업데이트
        if (isSelected) {
            // 선택된 경우
            with(chip) {
                setChipBackgroundColorResource(R.color.pointColor)  // 배경색 설정
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))  // 텍스트 색상 설정
            }
        } else {
            // 선택되지 않은 경우
            with(chip) {
                setChipBackgroundColorResource(R.color.white)  // 배경색 설정
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))  // 텍스트 색상 설정
            }
        }
    }

    /**
     * 선택된 기술 스택이 있을 경우 스크롤뷰를 표시하는 함수.
     */
    private fun scrollViewTechStackSelectVisibility() {
        val scrollViewTechStackSelect = binding.ScrollViewTechStackSelect
        scrollViewTechStackSelect.visibility = if (selectedTechStacks.isNotEmpty()) View.VISIBLE else View.GONE  // 선택된 항목에 따라 가시성 설정
        if (scrollViewTechStackSelect.visibility == View.VISIBLE) {
            scrollToBottom()  // 선택된 항목이 있으면 스크롤뷰를 맨 아래로 이동
        }
    }

    /**
     * 스크롤뷰를 맨 아래로 스크롤하는 함수.
     */
    private fun scrollToBottom() {
        binding.ScrollViewTechStack.post {
            val scrollView = binding.ScrollViewTechStack
            scrollView.fullScroll(View.FOCUS_DOWN)  // 스크롤뷰를 아래로 스크롤
        }
    }

    /**
     * 기술 스택이 선택되었을 때 호출되는 리스너 설정 함수.
     */
    fun setOnTechStackSelectedListener(listener: OnTechStackSelectedListener) {
        techStackSelectedListener = listener
    }

    /**
     * 스크롤뷰의 설정을 처리하는 함수. API 29 이상에서는 스크롤바 스타일을 설정함.
     */
    private fun setupTechStackSelectScrollView() {
        with(binding.ScrollViewTechStackSelect) {
            isVerticalScrollBarEnabled = true
            isScrollbarFadingEnabled = false

            // API 29 (Q) 이상에서는 스크롤바 스타일을 설정
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                verticalScrollbarThumbDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(ContextCompat.getColor(requireContext(), R.color.buttonGray))  // 스크롤바 색상 설정
                    setSize(dpToPx(requireContext(), 4f).toInt(), -1)  // 스크롤바 크기 설정
                    cornerRadius = dpToPx(requireContext(), 2f)  // 스크롤바 모서리 둥글게 설정
                }
            }
        }
    }

    /**
     * dp 값을 px 값으로 변환하는 유틸리티 함수.
     */
    private fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    /**
     * 화면 높이를 반환하는 함수. API 30 이상과 이하에 따라 다르게 처리됨.
     */
    private fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30 이상일 때
            val metrics = windowManager.currentWindowMetrics
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            metrics.bounds.height() - insets.top - insets.bottom
        } else {
            // API 30 이하일 때 (Deprecated 코드 처리)
            @Suppress("DEPRECATION")
            DisplayMetrics().also { metrics ->
                windowManager.defaultDisplay.getMetrics(metrics)
            }.heightPixels
        }
    }
}

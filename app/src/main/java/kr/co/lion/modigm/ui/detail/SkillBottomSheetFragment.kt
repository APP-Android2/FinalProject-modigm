package kr.co.lion.modigm.ui.detail

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
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

class SkillBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentSkillBottomSheetBinding

    private lateinit var selectedChips: MutableSet<String>  // 선택된 카테고리 관리

    private var skillSelectedListener: OnSkillSelectedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSkillBottomSheetBinding.inflate(inflater)

        return binding.root
    }

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // bottomSheet 배경 설정
        view.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.style_bottom_sheet_background)
        initializeCategoryChips()

        // 선택된 칩 초기화
        selectedChips = mutableSetOf<String>()

        setupCompleteButton()
        setupScrollView()

        // ImageView 클릭 시 BottomSheet 닫기
        binding.imageViewSkillBottomSheetClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog

        dialog?.let {
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout

            // 바텀 시트의 행동을 제어하는 BottomSheetBehavior 객체를 가져옴
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

            // 바텀 시트를 확장된 상태로 설정
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            // 바텀 시트의 드래그를 비활성화 (x버튼을 눌러야만 닫히고 손으로 잡아끌 수 없음)
            bottomSheetBehavior.isDraggable = false

            // 바텀시트의 높이를 화면 높이의 80%로 설정
            bottomSheet.layoutParams.height = (getScreenHeight(requireContext()) * 0.8).toInt()
        }
    }

    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Android 11 (API 레벨 30) 이상
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 현재 창의 전체 화면 메트릭스를 가져옴
            val metrics = windowManager.currentWindowMetrics
            // 화면의 인셋을 가져와서 보이지 않는 시스템 바 영역을 계산
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            // 화면의 전체 높이에서 상단 및 하단 인셋을 제외한 순수 사용 가능한 높이를 반환
            metrics.bounds.height() - insets.top - insets.bottom
        } else {
            // Android 11 미만 버전
            val displayMetrics = DisplayMetrics()

            //deprecated 경고 억제
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            // 화면의 전체 높이를 반환
            displayMetrics.heightPixels
        }
    }

    fun setOnSkillSelectedListener(listener: OnSkillSelectedListener) {
        skillSelectedListener = listener
    }


    fun setupScrollView() {
        val scrollView = binding.ScrollViewSkill

        // ScrollView의 스크롤바를 항상 보이게 설정
        scrollView.isVerticalScrollBarEnabled = true
        scrollView.isScrollbarFadingEnabled = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (API level 29)
            // 스크롤바 커스텀 Drawable 생성 및 적용
            val thumbDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(requireContext(), R.color.pointColor)) // 스크롤바 색상
                setSize(dpToPx(requireContext(), 4f).toInt(), -1) // 스크롤바 너비
                cornerRadius = dpToPx(requireContext(), 4f) // 스크롤바 모서리 둥글기
            }
            scrollView.verticalScrollbarThumbDrawable = thumbDrawable
        } else {
        }
    }

    // 스낵바 글시 크기 설정을 위해 dp를 px로 변환
    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun initializeCategoryChips() {
        val categories = listOf("전체", "프로그래밍 언어", "백엔드", "프론트 엔드","모바일 개발","데이터 사이언스","데브옵스 및 시스템 관리" ,"클라우드 및 인프라","게임 개발","보안","인공지능","UI/UX 디자인","빅데이터","기타")
        categories.forEach { category ->
            val chip = Chip(context).apply {
                text = category
                isClickable = true
                isCheckable = true
                setTextAppearance(R.style.ChipTextStyle)
                // 초기 스타일 설정
                updateChipStyle(this, false)
            }
            chip.setOnCheckedChangeListener { compoundButton, isChecked ->
                updateChipStyle(compoundButton as Chip, isChecked)  // 칩 스타일 업데이트
                handleChipSelection(category, isChecked)  // 칩 선택 처리
                displaySelectedCategory(category)  // 선택된 카테고리 표시
                displaySubCategories(category)  // 선택된 카테고리의 서브 카테고리 표시
            }

            // 칩 추가
            binding.chipGroupSkill.addView(chip)
        }
    }

    fun handleChipSelection(category: String, isChecked: Boolean) {

        if (isChecked) {
            // 모든 칩의 선택 상태를 업데이트
            binding.chipGroupSkill.children.forEach {
                (it as Chip).isChecked = it.text.toString() == category
            }

            if (category == "전체" || category == "기타") {
                selectedChips.clear() // 선택된 칩 집합을 초기화
                selectedChips.add(category) // "전체" 또는 "기타"를 선택된 칩 집합에 추가
                showSubCategories(false) // 서브 카테고리를 표시하지 않음
            } else {
                selectedChips.remove("전체") // "전체" 카테고리를 선택된 칩 집합에서 제거
                binding.chipGroupSkill.findViewWithTag<Chip>("전체")?.isChecked = false
                showSubCategories(true, category) // 선택된 카테고리에 해당하는 서브 카테고리를 표시
            }
        } else {
            selectedChips.remove(category) // 해당 카테고리를 선택된 칩 집합에서 제거
            showSubCategories(false) // 서브 카테고리 표시하지 않음
        }
        updateSelectedChipsUI() // 선택된 칩 UI 업데이트

    }

    fun showSubCategories(show: Boolean, category: String = "") {
        if (show && category.isNotEmpty() && category != "전체" && category != "기타") {
            // 해당 카테고리에 맞는 서브 카테고리를 표시
            displaySubCategories(category)
        } else {
            // ChipGroup의 모든 뷰를 제거
            binding.subCategoryChipGroupSkill.removeAllViews()
        }
    }


    fun updateSelectedChipsUI() {
        //ChipGroup의 모든 뷰를 제거
        binding.chipGroupSelectedItems.removeAllViews()

        //각 카테고리에 대한 칩 생성
        selectedChips.forEach { category ->
            val chip = Chip(context).apply {
                text = category
                isCloseIconVisible = true
                setTextAppearance(R.style.ChipTextStyle) // 칩의 텍스트 스타일 설정
                // 칩의 배경색 설정
                chipBackgroundColor =ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dividerView))

                // 닫기 아이콘 클릭 리스너 설정
                setOnCloseIconClickListener {
                    // 선택된 칩 목록에서 현재 칩 제거
                    selectedChips.remove(category)
                    // 칩 그룹에서 해당 카테고리의 선택 상태를 false로 설정
                    updateCategoryChipState(category, false)
                    // 메인 카테고리 내의 서브 카테고리 칩의 선택 상태 업데이트
                    updateSubCategoryChipState(category, false)

                    // UI를 다시 업데이트
                    updateSelectedChipsUI()
                }
            }
            // 칩 추가
            binding.chipGroupSelectedItems.addView(chip)
        }
    }

    fun updateCategoryChipState(category: String, isSelected: Boolean) {
        // 카테고리 칩 그룹에서 모든 칩을 순회하며 해당 카테고리의 칩 찾기
        binding.chipGroupSkill.children.forEach {
            val chip = it as Chip
            if (chip.text.toString() == category) {
                // 해당 카테고리의 칩의 선택 상태를 업데이트
                chip.isChecked = isSelected
            }
        }
    }

    fun updateSubCategoryChipState(subCategory: String, isSelected: Boolean) {
        binding.subCategoryChipGroupSkill.children.forEach {
            val chip = it as Chip
            if (chip.text.toString() == subCategory) {
                chip.isChecked = isSelected
            }
        }
    }

    // 바텀시트 하단 완료 버튼
    fun setupCompleteButton() {
        binding.buttonComplete.setOnClickListener {

            val selectedSkills = selectedChips.toList()
            // 인터페이스를 사용하여 부모 프래그먼트에 데이터 전달
            skillSelectedListener?.onSkillSelected(selectedSkills)

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

    fun displaySelectedCategory(category: String) {
        if (category == "전체" || category == "기타") {
            // '전체' 카테고리 선택 시 텍스트 뷰를 숨김
            binding.selectedCategoryTextView.visibility = View.GONE
        } else {
            // 다른 카테고리 선택 시 텍스트 뷰를 보이게 하고 카테고리 이름을 표시
            binding.selectedCategoryTextView.visibility = View.VISIBLE
            binding.selectedCategoryTextView.text = category
        }
    }

    fun displaySubCategories(category: String) {
        binding.subCategoryChipGroupSkill.removeAllViews()
        val subCategories = getSubCategoriesFor(category)
        subCategories.forEach { subCategory ->
            val chip = Chip(context).apply {
                text = subCategory
                isClickable = true
                isCheckable = true  // 칩이 선택 가능하도록 설정
                setTextAppearance(R.style.ChipTextStyle)
                // 초기 스타일 설정
                updateChipStyle(this, false)
            }
            chip.setOnCheckedChangeListener { compoundButton, isChecked ->
                updateChipStyle(compoundButton as Chip, isChecked)

                val chipText = (compoundButton as Chip).text.toString()
                if (isChecked) {
                    selectedChips.add(chipText)  // 선택된 칩 목록에 추가
                    updateChipStyle(chip, true)  // 칩 스타일을 '선택됨'으로 업데이트
                } else {
                    selectedChips.remove(chipText)  // 선택된 칩 목록에서 제거
                    updateChipStyle(chip, false)  // 칩 스타일을 '선택 해제됨'으로 업데이트
                }
                updateSelectedChipsUI()  // 선택된 칩 목록 UI 업데이트

            }
            // 칩추가
            binding.subCategoryChipGroupSkill.addView(chip)
        }
    }

    fun getSubCategoriesFor(category: String): List<String> {
        return when (category) {
            "프로그래밍 언어" -> listOf("Python", "Java", "C#", "JavaScript", "Ruby", "Go", "Swift", "Kotlin", "R", "C++", "PHP", "Rust", "TypeScript", "기타")
            "프론트 엔드" -> listOf("HTML", "CSS", "JavaScript", "React", "Angular", "Vue.js", "NPM", "Webpack", "Babel", "기타")
            "백엔드" -> listOf("Java", "Python", "Ruby", "Node.js", "PHP", "C#", "Express", "Django", "Flask", "Spring", ".NET", "MySQL", "PostgreSQL", "MongoDB", "Redis", "기타")
            "모바일 개발" -> listOf("Swift(iOS)", "Kotlin(Android)", "React Native", "Flutter", "Xamarin", "기타")
            "데이터 사이언스" -> listOf("Python", "R", "Pandas", "NumPy", "SciPy", "scikit-learn", "TensorFlow", "PyTorch", "Jupyter Notebook", "Anaconda", "기타")
            "데브옵스 및 시스템 관리" -> listOf("Jenkins", "Ansible", "Terraform", "Docker", "Kubernetes", "Prometheus", "Grafana", "ELK Stack", "기타")
            "클라우드 및 인프라" -> listOf("AWS", "Google Cloud", "Azure", "AWS Lambda", "Azure Functions", "Terraform", "CloudFormation", "기타")
            "게임 개발" -> listOf("Unity", "Unreal Engine", "C#", "C++", "Blender", "Maya","기타")
            "보안" -> listOf("Wireshark", "Metasploit", "암호화", "인증", "네트워크 보안", "Kali Linux", "OWASP","기타")
            "인공지능" -> listOf("Python", "R", "TensorFlow", "PyTorch", "Keras", "자연어 처리", "컴퓨터 비전", "머신러닝","기타")
            "UI/UX 디자인" -> listOf("Sketch", "Adobe XD", "Figma", "사용자 중심 디자인", "인터랙션 디자인","기타")
            "빅데이터" -> listOf("Hadoop", "Spark", "HDFS", "Cassandra", "Apache Kafka", "Apache Flink","기타")
            "전체" -> listOf()  // "전체"에 대한 서브 카테고리는 없음
            "기타" -> listOf()  // "기타"에 대한 서브 카테고리는 없음
            else -> listOf("No sub-categories")
        }
    }

}
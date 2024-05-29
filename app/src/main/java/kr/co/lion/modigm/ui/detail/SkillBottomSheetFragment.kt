package kr.co.lion.modigm.ui.detail

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        // Initialize the mutable set of selected chips
        selectedChips = mutableSetOf<String>()

        setupCompleteButton()
        setupScrollView()

        // ImageView 클릭 시 BottomSheet 닫기
        binding.imageViewSkillBottomSheetClose.setOnClickListener {
            dismiss()  // BottomSheetDialogFragment의 dismiss() 메서드를 호출하여 바텀 시트를 닫음
        }
    }


    fun setupScrollView() {
        // ScrollView를 찾고
        val scrollView = binding.ScrollViewSkill

        // ScrollView의 스크롤바를 항상 보이게 설정
        scrollView.isVerticalScrollBarEnabled = true
        scrollView.isScrollbarFadingEnabled = false

        // 스크롤바 커스텀 Drawable 생성 및 적용
        val thumbDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(ContextCompat.getColor(requireContext(), R.color.pointColor)) // 스크롤바 색상
            setSize(dpToPx(requireContext(), 4f).toInt(), -1) // 스크롤바 너비
            cornerRadius = dpToPx(requireContext(), 4f) // 스크롤바 모서리 둥글기
        }
        scrollView.verticalScrollbarThumbDrawable = thumbDrawable
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
                isCheckedIconVisible = false
//                tag = category  // 칩에 태그 설정
                setTextAppearance(R.style.ChipTextStyle)
                // 초기 스타일 설정
                updateChipStyle(this, false)
            }
            chip.setOnCheckedChangeListener { compoundButton, isChecked ->
                updateChipStyle(compoundButton as Chip, isChecked)
                handleChipSelection(category, isChecked)
                displaySelectedCategory(category)
                displaySubCategories(category)
            }
            binding.chipGroup.addView(chip)
        }
    }

    fun handleChipSelection(category: String, isChecked: Boolean) {

        if (isChecked) {
            // 모든 칩의 선택 상태를 업데이트
            binding.chipGroup.children.forEach {
                (it as Chip).isChecked = it.text.toString() == category
            }

            if (category == "전체" || category == "기타") {
                selectedChips.clear() // 기존 선택 초기화
                selectedChips.add(category)
                showSubCategories(false) // 서브 카테고리 숨기기
            } else {
                selectedChips.remove("전체") // "전체" 칩 제거
                binding.chipGroup.findViewWithTag<Chip>("전체")?.isChecked =
                    false
                showSubCategories(true, category) // 선택한 카테고리의 서브 카테고리 표시
            }
        } else {
            selectedChips.remove(category)
            showSubCategories(false)
        }
        updateSelectedChipsUI() // 선택된 칩 UI 업데이트

    }

    fun showSubCategories(show: Boolean, category: String = "") {
        if (show && category.isNotEmpty() && category != "전체" && category !="기타") {
            displaySubCategories(category)
        } else {
            // 서브 카테고리가 숨겨지거나 없는 경우
            binding.subCategoryChipGroup.removeAllViews()
        }
    }


    fun updateSelectedChipsUI() {
        binding.chipGroupSelectedItems.removeAllViews()
        selectedChips.forEach { category ->
            val chip = Chip(context).apply {
                text = category
                isCloseIconVisible = true
                setTextAppearance(R.style.ChipTextStyle)
                chipBackgroundColor =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dividerView))
                setOnCloseIconClickListener {
                    // 선택된 칩 목록에서 현재 칩 제거
                    selectedChips.remove(category)
                    // 칩 그룹에서 해당 카테고리의 선택 상태를 false로 설정
                    updateCategoryChipState(category, false)
                    // 메인 카테고리 내의 서브 카테고리 칩의 선택 상태 업데이트
                    updateSubCategoryChipState(category, false)

                    updateSelectedChipsUI()
                }
            }
            binding.chipGroupSelectedItems.addView(chip)
        }
    }

    fun updateCategoryChipState(category: String, isSelected: Boolean) {
        // 카테고리 칩 그룹에서 모든 칩을 순회하며 해당 카테고리의 칩 찾기
        binding.chipGroup.children.forEach {
            val chip = it as Chip
            if (chip.text.toString() == category) {
                // 해당 카테고리의 칩의 선택 상태를 업데이트
                chip.isChecked = isSelected
            }
        }
    }

    fun updateSubCategoryChipState(subCategory: String, isSelected: Boolean) {
        binding.subCategoryChipGroup.children.forEach {
            val chip = it as Chip
            if (chip.text.toString() == subCategory) {
                chip.isChecked = isSelected
            }
        }
    }

    fun setupCompleteButton() {
        binding.buttonComplete.setOnClickListener {
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
        binding.subCategoryChipGroup.removeAllViews()
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
                    selectedChips.add(chipText)
                    updateChipStyle(chip, true)
                } else {
                    selectedChips.remove(chipText)
                    updateChipStyle(chip, false)
                }
                updateSelectedChipsUI()  // 선택된 칩 목록 UI 업데이트

            }
            binding.subCategoryChipGroup.addView(chip)
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
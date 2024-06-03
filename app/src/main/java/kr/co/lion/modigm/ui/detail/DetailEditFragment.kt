package kr.co.lion.modigm.ui.detail

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentDetailEditBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.util.FragmentName


class DetailEditFragment : Fragment(), OnSkillSelectedListener, OnPlaceSelectedListener {

    lateinit var fragmentDetailEditBinding: FragmentDetailEditBinding

    lateinit var mainActivity: MainActivity


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentDetailEditBinding = FragmentDetailEditBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity

        return fragmentDetailEditBinding.root
    }

    // 뷰가 생성된 직후 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingToolbar()
        setupBottomSheet()
        setupButton()
        setupChipGroups()
        preselectChips()
    }

    // 인터페이스 구현
    // bottomSheet에서 선택한 항목의 제목
    override fun onPlaceSelected(placeName: String) {
        fragmentDetailEditBinding.editTextDetailEditTitleLocation.setText(placeName)
    }

    // 툴바 설정
    fun settingToolbar() {
        fragmentDetailEditBinding.apply {
            toolBarDetailEdit.apply {
                title="게시글 수정"
                //네비게이션
                setNavigationIcon(R.drawable.icon_arrow_back_24px)
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    // 칩 그룹 설정
    fun setupChipGroups() {
        setupChipGroup(
            fragmentDetailEditBinding.chipGroupDetailEditType,
            listOf("스터디", "공모전", "프로젝트")
        )

        setupChipGroup(
            fragmentDetailEditBinding.chipGroupDetailEditPlace,
            listOf("오프라인", "온라인", "온·오프 혼합")
        )

        setupChipGroup(
            fragmentDetailEditBinding.chipGroupDetailEditApply,
            listOf("신청제", "선착순")
        )

        // 장소선택 가시성 설정
        setPlaceSelectionListener()
    }

    // 장소 선택에 따른 UI 가시성 조절
    fun setPlaceSelectionListener() {
        // 반복하며 칩에 클릭 리스너 설정
        fragmentDetailEditBinding.chipGroupDetailEditPlace.children.forEach { view ->
            (view as Chip).setOnClickListener {
                // 칩 클릭시 가시성 업데이트 함수 호출
                updatePlaceVisibility(view)
                // 칩 스타일 업데이트
                updateChipStyles(fragmentDetailEditBinding.chipGroupDetailEditPlace, view.id)
            }
        }
    }

    // 가시성 업데이트
    fun updatePlaceVisibility(chip: Chip) {
        when (chip.text.toString()) {
            // '온라인'이 선택된 경우 장소 선택 입력 필드 숨김
            "온라인" -> fragmentDetailEditBinding.textInputLayoutDetailEditPlace.visibility = View.GONE
            else -> fragmentDetailEditBinding.textInputLayoutDetailEditPlace.visibility = View.VISIBLE
        }
    }

    // 칩 스타일 업데이트
    fun updateChipStyles(group: ChipGroup, checkedId: Int) {
        group.children.forEach { view ->
            if (view is Chip) {
                // 현재 칩이 선택된 상태인지 확인
                val isSelected = view.id == checkedId
                // 선택된 칩의 스타일 업데이트
                updateChipStyle(view, isSelected)
            }
        }
    }

    // 각 그룹에 칩 추가
    fun setupChipGroup(chipGroup: ChipGroup, chipNames: List<String>) {
        chipGroup.isSingleSelection = true  // Single selection 모드 활성화

        chipNames.forEach { name ->
            val chip = Chip(context).apply {
                text = name
                id = View.generateViewId()  // 동적으로 ID 생성
                isClickable = true
                isCheckable = true
                chipBackgroundColor =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTextAppearance(R.style.ChipTextStyle)
            }
            // 칩을 칩그룹에 추가
            chipGroup.addView(chip)
            setupChipListener(chip, chipGroup)
        }
    }

    // 개별 칩에 클릭리스너 설정
    fun setupChipListener(chip: Chip, chipGroup: ChipGroup) {
        chip.setOnClickListener {
            // 클릭된 칩이 현재 선택되지 않았다면, 선택 처리
            if (!chip.isChecked) {
                // 모든 칩의 선택 상태를 해제하고, 클릭된 칩만 선택
                chipGroup.children.forEach { view ->
                    (view as? Chip)?.isChecked = false
                }
                // 클릭된 칩을 선택 상태로 설정
                chip.isChecked = true
            }
            // 칩 스타일 업데이트
            chipGroup.children.forEach { view ->
                if (view is Chip) {
                    updateChipStyle(view, view.isChecked)
                }
            }
        }
    }

    // 칩 스타일 업데이트
    fun updateChipStyle(chip: Chip, isSelected: Boolean) {
        val context = chip.context
        val backgroundColor = if (isSelected) ContextCompat.getColor(
            context,
            R.color.pointColor
        ) else ContextCompat.getColor(context, R.color.white)
        val textColor = if (isSelected) ContextCompat.getColor(
            context,
            R.color.white
        ) else ContextCompat.getColor(context, R.color.black)

        chip.chipBackgroundColor = ColorStateList.valueOf(backgroundColor)
        chip.setTextColor(textColor)
    }

    override fun onSkillSelected(selectedSkills: List<String>) {
        // ChipGroup에 칩 추가
        addChipsToGroup(fragmentDetailEditBinding.ChipGroupDetailEdit, selectedSkills)
    }

    fun addChipsToGroup(chipGroup: ChipGroup, skills: List<String>) {
        // 기존의 칩들을 삭제
        chipGroup.removeAllViews()

        // 전달받은 스킬 리스트를 이용하여 칩을 생성 및 추가
        for (skill in skills) {
            val chip = Chip(context).apply {
                text = skill
                isClickable = true
                isCheckable = true
                isCloseIconVisible=true
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dividerView))
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTextAppearance(R.style.ChipTextStyle)
                id = View.generateViewId()

                // 'X' 아이콘 클릭시 해당 칩을 ChipGroup에서 제거
                setOnCloseIconClickListener {
                    chipGroup.removeView(this)  // 'this'는 현재 클릭된 Chip 인스턴스를 참조
                }
            }
            chipGroup.addView(chip)
        }
    }

    fun setupBottomSheet() {
        fragmentDetailEditBinding.textInputLayoutDetailEditSkill.editText?.setOnClickListener {
            // bottom sheet
            val bottomSheet = SkillBottomSheetFragment().apply {
                setOnSkillSelectedListener(this@DetailEditFragment)
            }
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        // 프래그 먼트간 연결 설정
        fragmentDetailEditBinding.textInputLayoutDetailEditPlace.editText?.setOnClickListener {
            val bottomSheet = PlaceBottomSheetFragment().apply {
                setOnPlaceSelectedListener(this@DetailEditFragment)
            }
            bottomSheet.show(childFragmentManager,bottomSheet.tag)
        }

    }

    fun setupButton() {
        fragmentDetailEditBinding.buttonDetailEditDone.setOnClickListener {
            if (validateInputs()) {
                // 모든 입력이 유효한 경우 데이터 저장 또는 처리
                saveData()
            }
        }

        // 작성 예시보기 text클릭
        fragmentDetailEditBinding.textviewDetailIntroEx.setOnClickListener {
            val dialog = CustomIntroDialog(requireContext())
            dialog.show()
        }
    }

    fun saveData() {

        val snackbar =
            Snackbar.make(fragmentDetailEditBinding.root, "수정되었습니다", Snackbar.LENGTH_LONG)

        // 스낵바의 뷰를 가져옵니다.
        val snackbarView = snackbar.view

        // 스낵바 텍스트 뷰 찾기
        val textView =
            snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

        // 텍스트 크기를 dp 단위로 설정
        val textSizeInPx = dpToPx(requireContext(), 16f)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx)

        snackbar.show()
    }

    // 스낵바 글시 크기 설정을 위해 dp를 px로 변환
    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    // 입력 유효성 검사
    fun validateInputs(): Boolean {
        val title = fragmentDetailEditBinding.editTextDetailEditTitle.text.toString()
        val description = fragmentDetailEditBinding.editTextDetailEditContext.text.toString()

        // 제목이 비어있거나 너무 짧은 경우 검사
        if (title.isEmpty() || title.length < 8) {
            fragmentDetailEditBinding.textInputLayoutDetailEditTitle.error = "제목은 최소 8자 이상이어야 합니다."
            return false
        } else {
            fragmentDetailEditBinding.textInputLayoutDetailEditTitle.error = null
        }

        // 소개글이 비어있거나 너무 짧은 경우 검사
        if (description.isEmpty() || description.length < 10) {
            fragmentDetailEditBinding.textInputLayoutDetailEditContext.error =
                "소개글은 최소 10자 이상이어야 합니다."
            return false
        } else {
            fragmentDetailEditBinding.textInputLayoutDetailEditContext.error = null
        }

        return true
    }

    // 사용자가 이전에 선택한 내용 선택(나중에 DB에서가져올 예정)
    fun preselectChips() {
        // '스터디' 칩을 선택하고 스타일을 업데이트합니다.
        findChipByText(fragmentDetailEditBinding.chipGroupDetailEditType, "스터디")?.let {
            it.isChecked = true
            updateChipStyle(it, true)
        }

        // '온라인' 칩을 선택하고 스타일을 업데이트하며, 관련 UI 가시성도 설정합니다.
        findChipByText(fragmentDetailEditBinding.chipGroupDetailEditPlace, "온라인")?.let {
            it.isChecked = true
            updateChipStyle(it, true)
            updatePlaceVisibility(it)  // 이 함수를 호출하여 '온라인' 선택 시 관련 UI를 숨깁니다.
        }

        // '신청제' 칩을 선택하고 스타일을 업데이트합니다.
        findChipByText(fragmentDetailEditBinding.chipGroupDetailEditApply, "신청제")?.let {
            it.isChecked = true
            updateChipStyle(it, true)
        }
    }

    // 특정 텍스트를 가진 칩을 찾는 함수
    fun findChipByText(chipGroup: ChipGroup, text: String): Chip? {
        // 해당 텍스트를 가진 첫 번째 칩 반환, 없으면 null 반환
        return chipGroup.children.firstOrNull { (it as Chip).text == text } as? Chip
    }

}
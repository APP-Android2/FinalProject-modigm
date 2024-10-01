package kr.co.lion.modigm.ui.write

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteProceedBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.OnPlaceSelectedListener
import kr.co.lion.modigm.ui.detail.PlaceBottomSheetFragment
import kr.co.lion.modigm.ui.write.vm.WriteViewModel
import kr.co.lion.modigm.util.showLoginSnackBar

class WriteProceedFragment : VBBaseFragment<FragmentWriteProceedBinding>(FragmentWriteProceedBinding::inflate),
    OnPlaceSelectedListener {

    private val viewModel: WriteViewModel by activityViewModels()

    // bottomSheet에서 선택한 항목의 제목
    override fun onPlaceSelected(placeName: String, detailPlaceName:String) {
        with(binding) {
            // 선택된 장소 이름
            val locationText  = "$placeName\n$detailPlaceName"
            textInputEditWriteProceedPlace.setText(locationText)
            viewModel.updateWriteData("studyPlace", placeName)
            viewModel.updateWriteData("studyDetailPlace", detailPlaceName)
            updateButtonColor()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

    }

    private fun initView() {

        // 칩 및 초기 데이터 세팅
        initChipGroupAndData()

        // 바텀 시트
        setupBottomSheet()

        // 인원수 입력
        setupMemberInputWatcher()

        // 다음 버튼
        nextButton()


    }

    // 칩 및 초기 데이터 세팅
    private fun initChipGroupAndData(){
        with(binding) {
            with(chipGroupWriteType) {
                removeAllViews()
                // 처음 접근 시 값이 없을 경우 오프라인으로 설정
                if(viewModel.getUpdateData("studyOnOffline") == null || viewModel.getUpdateData("studyOnOffline").toString() == ""){
                    viewModel.updateWriteData("studyOnOffline", "오프라인")
                }


                // 이전에 저장한 값
                val studyOnOffline = viewModel.getUpdateData("studyOnOffline")
                val studyPlace = viewModel.getUpdateData("studyPlace")
                val studyDetailPlace = viewModel.getUpdateData("studyDetailPlace")
                val studyMaxMember = viewModel.getUpdateData("studyMaxMember")

                setupChipGroup(
                    chipGroupWriteType,
                    listOf("오프라인", "온라인", "온오프혼합"),
                    mapOf("온라인" to 1, "오프라인" to 2, "온오프혼합" to 3)
                )

                // 장소선택 가시성 설정
                setPlaceSelectionListener()

                // studyOnOffline 값에 따라 칩 선택.
                studyOnOffline.let { selectedValue ->
                    children.forEach { view ->
                        if (view is Chip) {
                            if (view.text == selectedValue) {
                                view.isChecked = true
                                updateChipStyle(view, true)

                                when (selectedValue) {
                                    "온라인" -> {
                                        updatePlaceVisibility(view)
                                        if(studyMaxMember != null) {
                                            textInputEditProceedMaxMember.setText(studyMaxMember.toString())
                                        }
                                    }
                                    "오프라인", "온오프혼합" -> {
                                        if(studyMaxMember != null) {
                                            textInputEditProceedMaxMember.setText(studyMaxMember.toString())
                                        }
                                        if(studyPlace != null) {
                                            textInputEditWriteProceedPlace.setText("$studyPlace\n$studyDetailPlace")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                updateButtonColor()
            }
        }
    }

    // 바텀 시트
    private fun setupBottomSheet() {
        // 바텀 시트
        with(binding) {
            with(textInputLayoutProceedPlace) {
                editText?.setOnClickListener {
                    val bottomSheet = PlaceBottomSheetFragment().apply {
                        setOnPlaceSelectedListener(this@WriteProceedFragment)
                    }
                    bottomSheet.show(childFragmentManager, bottomSheet.tag)
                }
            }
        }
    }

    // 인원수 입력
    private fun setupMemberInputWatcher() {

        with(binding) {
            with(textInputEditProceedMaxMember) {
                addTextChangedListener(object :
                    TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        // 텍스트가 변경되는 동안 호출됩니다.
                        if (s != null && s.startsWith("0") && s.length > 1) {
                            // 입력된 값이 "0"으로 시작하고 길이가 1 초과인 경우, "0"을 제거합니다.
                            val correctString = s.toString().substring(1)
                            setText(correctString)
                            setSelection(correctString.length) // 커서를 수정된 텍스트의 끝으로 이동
                        }
                    }
                    override fun afterTextChanged(s: Editable?) {
                        // 텍스트 변경 후 호출됩니다.
                        s?.toString()?.let {
                            if(it.isEmpty()){
                                // 빈 값일 경우 최소값으로 지정
                                setText("0")
                                setSelection(text.toString().length) // 커서를 텍스트 끝으로 이동
                            }else{
                                // 사용자가 입력한 수의 최대 값을 30으로 제한
                                val num = it.toIntOrNull()
                                num?.let { value ->
                                    if (value > 30) {
                                        setText("30")
                                    }
                                }
                            }
                        }
                        setSelection(text.toString().length) // 커서를 끝으로 이동
                        viewModel.updateWriteData("studyMaxMember", text.toString().toInt())

                        updateButtonColor()
                    }
                })
                setOnEditorActionListener { v, actionId, event ->
                    if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                        // 포커스 해제
                        v.clearFocus()
                        // 키보드 숨기기
                        val imm = context?.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.hideSoftInputFromWindow(v.windowToken, 0)
                        true // 이벤트 처리 완료
                    } else {
                        false // 다른 액션 ID의 경우 이벤트 처리 안함
                    }
                }
            }
        }
    }

    // 다음 버튼
    private fun nextButton() {
        with(binding) {
            with(buttonWriteProceedNext) {

                setOnClickListener {

                    // 버튼 비활성화
                    isEnabled = false

                    // 온오프라인 방식으로 분기
                    // 선택된 칩을 찾고, 그 텍스트를 studyOnOffline에 저장
                    val selectedChip = chipGroupWriteType.children
                        .filterIsInstance<Chip>()
                        .firstOrNull { it.isChecked }  // 선택된 칩 찾기
                    val studyOnOffline = selectedChip?.text.toString()  // 선택된 칩의 텍스트

                    val studyPlace = viewModel.getUpdateData("studyPlace")
                    val studyDetailPlace = viewModel.getUpdateData("studyDetailPlace")

                    when (studyOnOffline) {
                        "온라인" -> {
                            // 버튼 활성화
                            isEnabled = true

                            if(!checkMaxMember()){
                                requireActivity().showLoginSnackBar("입력되지 않은 항목이 있습니다.", null)
                                return@setOnClickListener
                            }
                            viewModel.updateWriteData("studyOnOffline", studyOnOffline)
                            viewModel.updateWriteData("studyPlace", "")
                            viewModel.updateSelectedTab(3)
                        }
                        "오프라인", "온오프혼합" -> {
                            // 버튼 활성화
                            isEnabled = true

                            if (!checkAllInput()) {
                                requireActivity().showLoginSnackBar("입력되지 않은 항목이 있습니다.", null)
                                return@setOnClickListener
                            }
                            viewModel.updateWriteData("studyOnOffline", studyOnOffline)
                            viewModel.updateWriteData("studyPlace", studyPlace)
                            viewModel.updateWriteData("studyDetailPlace", studyDetailPlace)
                            viewModel.updateSelectedTab(3)
                        }
                    }
                }
            }
        }
    }

    // 장소 선택 리스너
    private fun setPlaceSelectionListener() {
        with(binding){
            // 반복하며 칩에 클릭 리스너 설정
            chipGroupWriteType.children.forEach { view ->
                (view as Chip).setOnClickListener {
                    // 칩 클릭시 가시성 업데이트 함수 호출
                    updatePlaceVisibility(view)
                    // 칩 스타일 업데이트
                    updateChipStyles(chipGroupWriteType, view.id)

                    val studyOnOffline = view.text.toString()
                    viewModel.updateWriteData("studyOnOffline", studyOnOffline)

                    if (studyOnOffline == "온라인") { // "온라인"이 선택된 경우
                        viewModel.updateWriteData("studyPlace", null)
                        viewModel.updateWriteData("studyDetailPlace", null)
                    }
                    updateButtonColor()
                }
            }
        }
    }
    // 칩 스타일 업데이트
    private fun updateChipStyles(group: ChipGroup, checkedId: Int) {
        group.children.forEach { view ->
            if (view is Chip) {
                // 현재 칩이 선택된 상태인지 확인
                val isSelected = view.id == checkedId
                // 선택된 칩의 스타일 업데이트
                updateChipStyle(view, isSelected)
            }
        }
    }

    // 칩 그룹 설정
    private fun setupChipGroup(chipGroup: ChipGroup, chipNames: List<String>, chipTags: Map<String, Int>? = null) {
        chipGroup.isSingleSelection = true  // Single selection 모드 활성화
        chipGroup.removeAllViews()  // 중복 생성을 방지하기 위해 기존 뷰를 제거

        chipNames.forEachIndexed { index, name ->
            val chip = Chip(context).apply {
                text = name
                id = View.generateViewId()  // 동적으로 ID 생성
                isClickable = true
                isCheckable = true
                chipBackgroundColor =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTextAppearance(R.style.ChipTextStyle)
                tag = chipTags?.get(name) ?: (index + 1)
            }
            // 칩을 칩그룹에 추가
            chipGroup.addView(chip)
            setupChipListener(chip, chipGroup)
        }
    }

    // 개별 칩에 클릭리스너 설정
    private fun setupChipListener(chip: Chip, chipGroup: ChipGroup) {
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
    private fun updateChipStyle(chip: Chip, isSelected: Boolean) {
        with(chip) {
            // 칩 배경색 설정
            val backgroundColor = if (isSelected) {
                ContextCompat.getColor(context, R.color.pointColor)
            } else {
                ContextCompat.getColor(context, R.color.white)
            }
            // 칩 텍스트 색상 설정
            val textColor = if (isSelected) {
                ContextCompat.getColor(context, R.color.white)
            } else {
                ContextCompat.getColor(context, R.color.black)
            }
            chipBackgroundColor = ColorStateList.valueOf(backgroundColor)
            setTextColor(textColor)
        }
    }

    // 가시성 업데이트
    private fun updatePlaceVisibility(chip: Chip) {
        with(binding) {
            when (chip.text.toString()) {
                // '온라인'이 선택된 경우 장소 선택 입력 필드 숨김
                "온라인" -> textInputLayoutProceedPlace.visibility = View.GONE
                else -> {
                    textInputLayoutProceedPlace.visibility = View.VISIBLE

                }
            }
        }

    }

    // 버튼의 색상을 업데이트하는 함수
    private fun updateButtonColor() {
        with(binding) {
            // 진행방식 가져오기
            val studyOnOffline = viewModel.getUpdateData("studyOnOffline")

            // 각 진행방식에 따른 입력 조건 확인
            val isMaxMemberFilled = !textInputEditProceedMaxMember.text.isNullOrEmpty()
            val isPlaceFilled = !textInputEditWriteProceedPlace.text.isNullOrEmpty()

            val isButtonEnabled = when (studyOnOffline) {
                "온라인" -> isMaxMemberFilled  // 온라인일 경우 인원 수만 확인
                "오프라인", "온오프혼합" -> isMaxMemberFilled && isPlaceFilled  // 오프라인, 온오프혼합일 경우 인원 수와 장소 모두 확인
                else -> false
            }

            // 버튼 활성화 여부 설정
            val colorResId = if (isButtonEnabled) {
                R.color.pointColor  // 모든 조건이 충족되었을 때 버튼을 활성화
            } else {
                R.color.buttonGray  // 조건이 충족되지 않으면 버튼을 비활성화
            }

            with(buttonWriteProceedNext) {
                setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                isEnabled = isButtonEnabled  // 버튼 활성화/비활성화 설정
            }
        }
    }


    // 전체 유효성 검사
    private fun checkAllInput(): Boolean {
        return checkPlace() && checkMaxMember()
    }

    // 장소 입력 유효성 검사
    private fun checkPlace(): Boolean {
        with(binding) {
            // 에러 메시지를 설정하고 포커스와 흔들기 동작을 수행하는 함수
            fun showError(message: String) {
                textInputLayoutProceedPlace.error = message
                textInputEditWriteProceedPlace.requestFocus()
            }
            return when {
                textInputEditWriteProceedPlace.text.toString().isEmpty() -> {
                    showError("장소를 입력해주세요.")
                    false
                }
                else -> {
                    textInputLayoutProceedPlace.error = null
                    true
                }
            }
        }
    }

    // 인원수 입력 유효성 검사
    private fun checkMaxMember():Boolean {
        with(binding) {

            // 에러 메시지를 설정하고 포커스와 흔들기 동작을 수행하는 함수
            fun showError(message: String) {
                textInputLayoutProceedMaxMember.error = message
                textInputEditProceedMaxMember.requestFocus()
            }
            return when {
                textInputEditProceedMaxMember.text.toString().isEmpty() -> {
                    showError("인원 수를 입력해주세요.")
                    false
                }
                else -> {
                    textInputLayoutProceedMaxMember.error = null
                    true
                }
            }
        }
    }
}

package kr.co.lion.modigm.ui.write

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteBinding
import kr.co.lion.modigm.databinding.FragmentWriteProceedBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.OnPlaceSelectedListener
import kr.co.lion.modigm.ui.detail.PlaceBottomSheetFragment
import kr.co.lion.modigm.ui.write.more.BottomSheetWriteProceedFragment
import kr.co.lion.modigm.ui.write.vm.WriteViewModel

class WriteProceedFragment : VBBaseFragment<FragmentWriteProceedBinding>(FragmentWriteProceedBinding::inflate), OnPlaceSelectedListener {

    private val viewModel: WriteViewModel by activityViewModels()
    val tabName = "proceed"

    var onOffline: Int = 0

    // 선택된 장소 이름
    private var selectedPlaceName: String = ""
    // 선택된 상세 장소 이름
    private var selectedDetailPlaceName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // 칩 세팅
        setupChipGroups()

        //bottom Sheet
        setupBottomSheet()

        // 인원수 입력
        setupMemberInputWatcher()

        // 유효성 검사
        validateAnswer()

        // ViewModel에 저장된 선택 상태 복원
        restoreState()
    }

    private fun restoreState() {
        // 칩 그룹의 선택 상태 복원
        viewModel.studyOnOffline.value?.let { proceedType ->
            binding.chipGroupWriteType.children.forEach { view ->
                if (view is Chip && view.tag == proceedType) {
                    view.isChecked = true
                    updateChipStyle(view, true)
                    updatePlaceVisibility(view)
                }
            }
        }

        // 장소 선택 상태 복원
        viewModel.studyPlace.value?.let { placeName ->
            binding.textFieldWriteProceedLocation.setText(placeName)
        }

        viewModel.studyDetailPlace.value?.let { detailPlaceName ->
            binding.textFieldWriteProceedLocation.setText(detailPlaceName)
        }
    }

    fun setupChipGroups(){
        binding.chipGroupWriteType.removeAllViews()

        setupChipGroup(
            binding.chipGroupWriteType,
            listOf("오프라인", "온라인", "온·오프 혼합"),
            mapOf("온라인" to 1, "오프라인" to 2, "온·오프 혼합" to 3) // tag 값을 지정
        )

        // 장소선택 가시성 설정
        setPlaceSelectionListener()

        // "오프라인" 칩을 선택 상태로 설정
        binding.chipGroupWriteType.children.forEach { view ->
            if ((view as Chip).text == "오프라인") {
                view.isChecked = true
                // 선택된 칩의 스타일 업데이트
                updateChipStyle(view, true)
            }
        }
        // 초기 유효성 검사 실행
        viewModel.studyOnOffline.value = "오프라인" // 오프라인으로 설정
        viewModel.validateProceedInput()
    }

    fun setPlaceSelectionListener() {
        // 반복하며 칩에 클릭 리스너 설정
        binding.chipGroupWriteType.children.forEach { view ->
            (view as Chip).setOnClickListener {
                Log.d("SelectedChip", "Selected chip: ${view.text}")
                // 칩 클릭시 가시성 업데이트 함수 호출
                updatePlaceVisibility(view)
                // 칩 스타일 업데이트
                updateChipStyles(binding.chipGroupWriteType, view.id)

                onOffline = view.tag as Int
//                viewModel.studyOnOffline.value = onOffline.toString()
                viewModel.studyOnOffline.value = if (onOffline == 1) "온라인" else "오프라인" // "온라인"이나 "오프라인"으로 설정

                if (onOffline == 1) { // "온라인"이 선택된 경우
                    val locationText = ""
                    binding.textFieldWriteProceedLocation.setText(locationText)
                    viewModel.studyPlace.value = locationText
                    viewModel.studyDetailPlace.value = ""
                }

                Log.d("SelectedChip", "onOffline: ${onOffline}")

                viewModel.validateProceedInput()
            }
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


    fun setupChipGroup(chipGroup: ChipGroup, chipNames: List<String>, chipTags: Map<String, Int>? = null) {
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
                tag = chipTags?.get(name) ?: index + 1
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
            // 선택된 칩의 tag 출력
            Log.d("DetailEditFragment", "Selected Chip Tag: ${chip.tag}")
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

    // 가시성 업데이트
    fun updatePlaceVisibility(chip: Chip) {
        when (chip.text.toString()) {
            // '온라인'이 선택된 경우 장소 선택 입력 필드 숨김
            "온라인" -> binding.textInputLayoutWriteProceedOfflineClicked.visibility = View.GONE
            else -> {
                binding.textInputLayoutWriteProceedOfflineClicked.visibility = View.VISIBLE

            }
        }
    }

    // 인터페이스 구현
    // bottomSheet에서 선택한 항목의 제목
    override fun onPlaceSelected(placeName: String, detailPlaceName:String) {
        selectedPlaceName = placeName
        selectedDetailPlaceName = detailPlaceName
        val locationText  = "$selectedPlaceName\n$selectedDetailPlaceName"
        binding.textFieldWriteProceedLocation.setText(locationText)

        viewModel.studyPlace.value = selectedPlaceName
        viewModel.studyDetailPlace.value = selectedDetailPlaceName
        viewModel.validateProceedInput()
    }

    fun setupBottomSheet() {
        binding.textInputLayoutWriteProceedOfflineClicked.editText?.setOnClickListener {
            val bottomSheet = PlaceBottomSheetFragment().apply {
                setOnPlaceSelectedListener(this@WriteProceedFragment)
            }
            bottomSheet.show(childFragmentManager, bottomSheet.tag)

        }
    }

    // 인원수 입력 설정
    fun setupMemberInputWatcher() {
        binding.textFieldWriteProceedNumOfMember.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 변경 전에 호출됩니다.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 변경되는 동안 호출됩니다.
                if (s != null && s.startsWith("0") && s.length > 1) {
                    // 입력된 값이 "0"으로 시작하고 길이가 1 초과인 경우, "0"을 제거합니다.
                    val correctString = s.toString().substring(1)
                    binding.textFieldWriteProceedNumOfMember.setText(correctString)
                    binding.textFieldWriteProceedNumOfMember.setSelection(correctString.length) // 커서를 수정된 텍스트의 끝으로 이동
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // 텍스트 변경 후 호출됩니다.
                s?.toString()?.let {
                    if(it.isEmpty()){
                        // 사용자가 모든 텍스트를 지웠을 경우 "0"을 자동으로 입력
                        binding.textFieldWriteProceedNumOfMember.setText("0")
                        binding.textFieldWriteProceedNumOfMember.setSelection(binding.textFieldWriteProceedNumOfMember.text.toString().length) // 커서를 텍스트 끝으로 이동
                    }else{
                        val num = it.toIntOrNull()
                        num?.let { value ->
                            if (value > 30) {
                                binding.textFieldWriteProceedNumOfMember.setText("30")
                            }
                        }
                    }
                }
                binding.textFieldWriteProceedNumOfMember.setSelection(binding.textFieldWriteProceedNumOfMember.text.toString().length) // 커서를 끝으로 이동
                viewModel.studyMaxMember.value = binding.textFieldWriteProceedNumOfMember.text.toString().toInt()
                viewModel.validateProceedInput()
            }
        })

        binding.textFieldWriteProceedNumOfMember.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 포커스 해제
                v.clearFocus()

                // 키보드 숨기기
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)

                true // 이벤트 처리 완료
            } else {
                false // 다른 액션 ID의 경우 이벤트 처리 안함
            }
        }

    }

    fun validateAnswer() {
        viewModel.studyOnOffline.observe(viewLifecycleOwner) { onOffline ->
            if (onOffline == "온라인") {
                binding.textFieldWriteProceedLocation.error = null // "온라인"일 경우 장소 오류를 초기화
                return@observe
            }

            viewModel.studyPlace.observe(viewLifecycleOwner) { place ->
                if (onOffline != "온라인" && place.isEmpty()) {
                    binding.textFieldWriteProceedLocation.error = "스터디 할 장소를 입력해주세요"
                } else {
                    binding.textFieldWriteProceedLocation.error = null
                }
            }
        }

        viewModel.studyMaxMember.observe(viewLifecycleOwner) { max ->
            if (max > 30) {
                binding.textFieldWriteProceedNumOfMember.error = "최대 정원은 30명입니다"
            } else {
                binding.textFieldWriteProceedNumOfMember.error = null
            }
        }
    }

}

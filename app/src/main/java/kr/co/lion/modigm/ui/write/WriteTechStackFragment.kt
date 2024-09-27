package kr.co.lion.modigm.ui.write

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteTechStackBinding
import kr.co.lion.modigm.model.TechStackData
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.write.vm.WriteViewModel
import kr.co.lion.modigm.util.showLoginSnackBar

class WriteTechStackFragment : VBBaseFragment<FragmentWriteTechStackBinding>(FragmentWriteTechStackBinding::inflate), OnTechStackSelectedListener {

    private val viewModel: WriteViewModel by activityViewModels()

    // 선택된 스킬 목록
    private var selectedTechStacks: List<Int> = listOf()

    private var selectedTechStackList: MutableList<Int> = mutableListOf()

    // TechStackBottomSheetFragment에서 호출되는 콜백 메서드, 선택된 스킬 데이터를 처리
    override fun onTechStackSelected(selectedTechStacks: List<TechStackData>) {
        // 선택된 스킬들의 번호를 추출
        val selectedTechStackIdx = selectedTechStacks.map { it.techIdx }  // TechStackData에서 techIdx 값을 추출
        val selectedTechStackObjects = selectedTechStacks  // 원본 TechStackData 리스트를 저장

        // 선택된 스킬을 ViewModel에 저장
        this.selectedTechStacks = selectedTechStackIdx
        viewModel.updateWriteData("studyTechStackList", selectedTechStackIdx)  // ViewModel에 선택된 스킬 번호 리스트 저장

        // 디버깅을 위한 로그 출력
        selectedTechStackIdx.forEach { techIdx ->
            Log.d("WriteTechStackFragment", "Selected techStack number: $techIdx")
        }

        // 선택된 스킬 리스트를 ChipGroup에 추가
        addChipsToGroup(binding.chipGroupWriteTechStack, selectedTechStackObjects)
        // 버튼 색상 업데이트
        updateButtonColor()
    }

    //-------------------------------LC START---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        restoreSelectedTechStacks()  // 저장된 스킬 목록 복원
    }

    //-------------------------------LC END---------------------------------

    private fun initView() {
        with(binding) {
            // 필요한 기술 레이아웃 클릭 시
            textInputLayoutWriteTechStack.apply {
                editText?.setOnClickListener {
                    bottomSheetTechStack()
                }
                setOnClickListener{
                    bottomSheetTechStack()
                }
                setEndIconOnClickListener {
                    bottomSheetTechStack()
                }
            }

            buttonWriteTechStackNext.apply {
                setOnClickListener {
                    // 유효성 검사
                    if (selectedTechStackList.isEmpty()) {
                        requireActivity().showLoginSnackBar("필요한 기술을 선택해주세요",null)
                        return@setOnClickListener
                    }
                    viewModel.updateSelectedTab(4)
                }
            }
        }
    }

    // ViewModel에서 저장된 스킬 데이터를 복원하는 함수 (LiveData observe 방식)
    private fun restoreSelectedTechStacks() {
        // techStackData와 studyTechStackList를 모두 옵저빙
        viewModel.techStackData.observe(viewLifecycleOwner) { techStackDataList ->
            viewModel.getUpdateData("studyTechStackList")?.let { studyTechStackList ->
                val techStackIdxs = studyTechStackList as? List<Int> ?: return@observe

                // techStackData에서 techIdx와 일치하는 스킬 필터링
                val techStacks = techStackDataList.filter { techStackData ->
                    techStackIdxs.contains(techStackData.techIdx)
                }

                // 필터링된 스킬 데이터를 ChipGroup에 추가
                addChipsToGroup(binding.chipGroupWriteTechStack, techStacks)

                // 버튼 색상 업데이트
                updateButtonColor()
            }
        }
    }

    // ChipGroup에 칩을 추가하는 함수
    private fun addChipsToGroup(chipGroup: ChipGroup, techStacks: List<TechStackData>) {
        // 기존의 칩들을 삭제
        chipGroup.removeAllViews()
        selectedTechStackList.clear()

        // "기타" 칩이 추가되었는지 여부를 확인하기 위한 플래그
        var isOtherChipAdded = false

        // 전달받은 스킬 리스트를 이용하여 칩을 생성하고 추가
        for (techStack in techStacks) {
            // "기타" 칩일 경우, 이미 추가된 경우는 건너뛴다.
            if (techStack.techName == "기타") {
                if (isOtherChipAdded) continue  // "기타" 칩이 이미 추가된 경우 추가하지 않음
                isOtherChipAdded = true  // "기타" 칩을 추가했다고 표시
            }

            // 선택된 스킬 번호를 저장
            selectedTechStackList.add(techStack.techIdx)

            // 칩을 생성하여 ChipGroup에 추가
            val chip = Chip(context).apply {
                text = techStack.techName  // 스킬의 이름을 칩 텍스트로 설정
                isClickable = true
                isCheckable = false
                isCloseIconVisible = true
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dividerView))
                setTextColor(ContextCompat.getColor(context, R.color.black))
                setTextAppearance(R.style.ChipTextStyle)
                id = View.generateViewId()

                // 'X' 아이콘 클릭 시 해당 칩을 ChipGroup에서 제거
                setOnCloseIconClickListener {
                    chipGroup.removeView(this)  // 현재 클릭된 Chip 인스턴스를 ChipGroup에서 제거
                    selectedTechStackList.remove(techStack.techIdx)  // 선택된 스킬 목록에서 해당 스킬 제거

                    // 선택된 스킬 목록이 비어 있으면 ViewModel 데이터를 null로 업데이트
                    if (selectedTechStackList.isEmpty()) {
                        viewModel.updateWriteData("studyTechStackList", null)  // ViewModel에 데이터를 null로 설정
                    } else {
                        viewModel.updateWriteData("studyTechStackList", selectedTechStackList)  // ViewModel에 선택된 스킬 목록 업데이트
                    }

                    // 버튼 색상 업데이트
                    updateButtonColor()
                }
            }
            chipGroup.addView(chip)  // ChipGroup에 칩을 추가
        }
        // 버튼 색상 초기 업데이트
        updateButtonColor()
    }

    private fun bottomSheetTechStack() {
        // 바텀 시트 띄우기
        val bottomSheet = TechStackBottomSheetFragment().apply {
            setOnTechStackSelectedListener(this@WriteTechStackFragment)
        }
        bottomSheet.show(childFragmentManager, bottomSheet.tag)
    }
    // 버튼의 색상을 업데이트하는 함수
    private fun updateButtonColor() {
        with(binding){
            val colorResId = if (selectedTechStackList.isNotEmpty()) R.color.pointColor else R.color.buttonGray
            with(buttonWriteTechStackNext) {
                setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
    }
}
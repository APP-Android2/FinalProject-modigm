package kr.co.lion.modigm.ui.write

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteSkillBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.detail.OnSkillSelectedListener
import kr.co.lion.modigm.ui.detail.SkillBottomSheetFragment
import kr.co.lion.modigm.ui.write.vm.WriteViewModel
import kr.co.lion.modigm.util.Skill

class WriteSkillFragment : VBBaseFragment<FragmentWriteSkillBinding>(FragmentWriteSkillBinding::inflate), OnSkillSelectedListener {

    private val viewModel: WriteViewModel by activityViewModels()

    private var selectedCardView: MaterialCardView? = null // 선택된 카드뷰를 기억하기 위한 변수

    // 선택된 스킬 목록
    private var selectedSkills: List<Int> = listOf()

    private var selectedSkillList: MutableList<Int> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingView()
        settingBottomSheeet()

        restoreSelectedSkills() // 저장된 스킬 목록 복원

        validateCardSelection() // 유효성 검사 추가

    }

    private fun settingBottomSheeet(){
        // textLaout 클릭 시 리스너
        binding.textInputLayoutWriteSkill.editText?.setOnClickListener {
            // bottom sheet
            val bottomSheet = SkillBottomSheetFragment().apply {
                setOnSkillSelectedListener(this@WriteSkillFragment)
            }
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }
    }

    private fun settingView(){
        // cardView 클릭 시 Stroke 색상 변경
        with(binding){
            cardviewWriteSkillApplicationSystem.tag = "신청제"
            cardviewWriteSkillFirstCome.tag = "선착순"
            // 신청제
            cardviewWriteSkillApplicationSystem.setOnClickListener {
                onCardClicked(it as MaterialCardView)
            }

            // 선착순
            cardviewWriteSkillFirstCome.setOnClickListener {
                onCardClicked(it as MaterialCardView)
            }
        }

        // ViewModel에 저장된 선택 상태를 확인하고 복원
        viewModel.selectedApplyTag.value?.let { selectedTag ->
            when (selectedTag) {
                "신청제" -> binding.cardviewWriteSkillApplicationSystem.performClick()
                "선착순" -> binding.cardviewWriteSkillFirstCome.performClick()
                else -> { /* 저장된 데이터 없음 */ }
            }
        }
    }
    // 클릭된 카드뷰의 스트로크 색상 변경 함수
    private fun changeStrokeColor(cardView: MaterialCardView, isSelected: Boolean) {
        val colorResId = if (isSelected) R.color.pointColor else R.color.textGray
        cardView.strokeColor = ContextCompat.getColor(requireContext(), colorResId)
    }

    // 카드뷰 클릭 이벤트 처리 함수
    private fun onCardClicked(clickedCardView: MaterialCardView) {
        // 이미 선택된 카드뷰인 경우, 선택을 취소하지 않음
        if (clickedCardView == selectedCardView) {
            return
        }
        val wasSelected = clickedCardView == selectedCardView
        // 선택된 카드뷰가 이미 있다면 선택 해제
        selectedCardView?.apply {
            changeStrokeColor(this, false) // 선택 해제 시 스트로크 색상을 변경
        }
        // 새로 클릭된 카드뷰를 선택하고 스트로크 색상 변경
        selectedCardView = if (!wasSelected) clickedCardView else null
        changeStrokeColor(clickedCardView, !wasSelected)

        validateCardSelection() // 유효성 검사 추가
        // 선택된 카드뷰의 태그를 뷰모델에 저장
        viewModel.selectedApplyTag.value = clickedCardView.tag as String
    }

    override fun onSkillSelected(selectedSkills: List<Skill>) {

        val selectedSkillNums = selectedSkills.map { it.num }  // Skill 객체에서 num 값 추출
        val selectedSkillObjects = selectedSkills  // 원본 Skill 리스트를 저장

        this.selectedSkills = selectedSkillNums  // selectedSkills를 num 리스트로 설정

        viewModel.studySkillList.value = selectedSkillNums // 뷰모델에 저장

        selectedSkillNums.forEach { num ->
            Log.d("WriteSkillFragment", "Selected skill number: $selectedSkillNums")
        }

        addChipsToGroup(binding.chipGroupWriteSkill, selectedSkillObjects)
    }


    private fun addChipsToGroup(chipGroup: ChipGroup, skills: List<Skill>) {
        // 기존의 칩들을 삭제
        chipGroup.removeAllViews()
        selectedSkillList.clear()


        // 전달받은 스킬 리스트를 이용하여 칩을 생성 및 추가
        for (skill in skills) {
            selectedSkillList.add(skill.num) // 초기 스킬 목록을 selectedSkillList에 추가
            val chip = Chip(context).apply {
                text = skill.displayName
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
                    // skill List에서 목록 제거
                    selectedSkillList.remove(skill.num)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun restoreSelectedSkills() {
        // ViewModel에 저장된 스킬 목록을 가져와서 칩을 추가
        viewModel.studySkillList.value?.let { skillNums ->
            val skills = skillNums.mapNotNull { num ->
                Skill.entries.find { it.num == num }
            }
            addChipsToGroup(binding.chipGroupWriteSkill, skills)
        }
    }

    private fun validateCardSelection() {
        val isCardSelected = selectedCardView != null
        viewModel.validateSkill(isCardSelected)
    }

}
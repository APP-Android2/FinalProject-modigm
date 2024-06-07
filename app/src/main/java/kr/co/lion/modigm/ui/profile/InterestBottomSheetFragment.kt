package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentInterestBottomSheetBinding
import kr.co.lion.modigm.databinding.FragmentSkillBottomSheetBinding
import kr.co.lion.modigm.ui.detail.OnSkillSelectedListener
import kr.co.lion.modigm.ui.profile.vm.EditProfileViewModel
import kr.co.lion.modigm.util.Interest

class InterestBottomSheetFragment: BottomSheetDialogFragment() {
    private lateinit var fragmentInterestBottomSheetBinding: FragmentInterestBottomSheetBinding
    private val editProfileViewModel: EditProfileViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentInterestBottomSheetBinding = FragmentInterestBottomSheetBinding.inflate(inflater)

        return fragmentInterestBottomSheetBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {
        // bottomSheet 배경 설정
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.style_bottom_sheet_background)

        // 칩 구성
        editProfileViewModel.editProfileInterestList.observe(viewLifecycleOwner) { checkedInterests ->
            setupChips(checkedInterests)
        }

        // 오른쪽 위 아이콘 클릭 시 BottomSheet 닫기
        fragmentInterestBottomSheetBinding.iconInterestClose.setOnClickListener {
            dismiss()
        }
    }

    fun setupChips(checkedInterests: List<Int>) {
        // 기존 칩들 제거
        fragmentInterestBottomSheetBinding.chipGroupInterest.removeAllViews()

        // Enum 클래스의 모든 값을 가져와 Chip을 생성
        Interest.entries.forEach { interest ->
            val chip = Chip(requireContext()).apply {
                // chip 텍스트 설정: 관심분야
                text = interest.str
                // 자동 padding 없애기
                setEnsureMinTouchTargetSize(false)
                // 선택 가능
                isCheckable = true
                // 리스트에 들어 있다면 선택된 것으로 표시
                isChecked = checkedInterests.contains(interest.num)
                // 선택된 칩은 파란색, 선택되지 않은 칩은 흰색
                updateChipState(this, isChecked)
                // 칩의 선택 상태 변경
                setOnCheckedChangeListener { buttonView, isChecked ->
                    // 선택된 칩은 파란색, 선택되지 않은 칩은 흰색
                    updateChipState(buttonView as Chip, isChecked)
                    // 칩 선택 여부 변경에 따라 리스트 수정
                    updateChipList(interest.num, isChecked)
                }
            }
            fragmentInterestBottomSheetBinding.chipGroupInterest.addView(chip)
        }
    }

    fun updateChipState(chip: Chip, isChecked: Boolean) {
        if (isChecked) {
            chip.setChipBackgroundColorResource(R.color.pointColor)
            chip.setTextColor(resources.getColor(R.color.white, null))
        } else {
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setTextColor(resources.getColor(R.color.black, null))
        }
    }

    private fun updateChipList(interestNum: Int, isChecked: Boolean) {
        val currentList = editProfileViewModel.editProfileInterestList.value ?: listOf()
        val updatedList = if (isChecked) {
            currentList + interestNum
        } else {
            currentList - interestNum
        }
        editProfileViewModel.editProfileInterestList.value = updatedList
    }
}
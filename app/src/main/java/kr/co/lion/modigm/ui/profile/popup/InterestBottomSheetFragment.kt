package kr.co.lion.modigm.ui.profile.popup

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentInterestBottomSheetBinding
import kr.co.lion.modigm.ui.VBBaseBottomSheetFragment
import kr.co.lion.modigm.ui.profile.vm.EditProfileViewModel
import kr.co.lion.modigm.util.Interest

class InterestBottomSheetFragment: VBBaseBottomSheetFragment<FragmentInterestBottomSheetBinding>(FragmentInterestBottomSheetBinding::inflate) {
    private val editProfileViewModel: EditProfileViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {
        // bottomSheet 배경 설정
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.style_bottom_sheet_background)

        // 오른쪽 위 아이콘 클릭 시 BottomSheet 닫기
        binding.iconInterestClose.setOnClickListener {
            dismiss()
        }

        // 칩 구성
        lifecycleScope.launch {
            editProfileViewModel.editProfileInterests.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { interests ->
                setupChips(interests)
            }
        }
    }

    fun setupChips(interests: String?) {
        // 기존 칩들 제거
        binding.chipGroupInterest.removeAllViews()

        val interestList = interests?.split(",")?.map { it.trim() }

        // Enum 클래스의 모든 값을 가져와 Chip을 생성
        Interest.entries.forEach { interest ->
            val chip = Chip(requireContext()).apply {
                // chip 텍스트 설정: 관심분야
                text = interest.str

                setTextAppearance(R.style.ChipTextStyle)

                // 자동 padding 없애기
                setEnsureMinTouchTargetSize(false)
                // 선택 가능
                isCheckable = true
                // 리스트에 들어 있다면 선택된 것으로 표시
                if (interestList != null) {
                    isChecked = interestList.contains(interest.str)
                }
                // 선택된 칩은 파란색, 선택되지 않은 칩은 흰색
                updateChipState(this, isChecked)
                // 칩의 선택 상태 변경
                setOnCheckedChangeListener { buttonView, isChecked ->
                    // 선택된 칩은 파란색, 선택되지 않은 칩은 흰색
                    updateChipState(buttonView as Chip, isChecked)
                    // 칩 선택 여부 변경에 따라 리스트 수정
                    updateChipList()
                }
            }
            binding.chipGroupInterest.addView(chip)
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

    private fun updateChipList() {
        val selectedChips = binding.chipGroupInterest.children
            .filterIsInstance<Chip>()
            .filter { it.isChecked }
            .map { it.text.toString() }
            .toList()

        // 선택된 칩들을 콤마로 연결한 문자열로 변환
        val selectedChipsString = selectedChips.joinToString(", ")

        // ViewModel의 문자열 필드 업데이트
        editProfileViewModel.editProfileInterests.value = selectedChipsString
    }
}
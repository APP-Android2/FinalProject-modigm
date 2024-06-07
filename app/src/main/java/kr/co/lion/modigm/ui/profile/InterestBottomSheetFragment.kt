package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentInterestBottomSheetBinding
import kr.co.lion.modigm.databinding.FragmentSkillBottomSheetBinding
import kr.co.lion.modigm.ui.detail.OnSkillSelectedListener
import kr.co.lion.modigm.util.Interest

class InterestBottomSheetFragment: BottomSheetDialogFragment() {
    private lateinit var fragmentInterestBottomSheetBinding: FragmentInterestBottomSheetBinding
    private lateinit var selectedChips: MutableSet<String>  // 선택된 칩 관리
    private var interestSelectedListener: OnSkillSelectedListener? = null

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
        setupChips()

        // 선택된 칩 초기화
        selectedChips = mutableSetOf<String>()

//        setupCompleteButton()
//        setupScrollView()
//
//        // ScrollViewSkillSelect에 대한 스크롤바 설정
//        setupSkillSelectScrollView()
//        ScrollViewSkillSelectVisibility()
//
//        // ImageView 클릭 시 BottomSheet 닫기
//        binding.imageViewSkillBottomSheetClose.setOnClickListener {
//            dismiss()
//        }
    }

    fun setupChips() {
        // 특정 리스트 (Int 값으로 이루어짐)
        val checkedInterests = listOf(2, 3, 8) // 예: 서버, 프론트엔드, 파이썬의 num 값

        // Enum 클래스의 모든 값을 가져와 Chip을 생성
        Interest.entries.forEach { interest ->
            val chip = Chip(requireContext()).apply {
                text = interest.str
                isCheckable = true
                isChecked = checkedInterests.contains(interest.num)
            }
            fragmentInterestBottomSheetBinding.chipGroupInterest.addView(chip)
        }
    }
}
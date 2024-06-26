package kr.co.lion.modigm.ui.write

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.card.MaterialCardView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteFieldBinding
import kr.co.lion.modigm.ui.write.vm.WriteViewModel

class WriteFieldFragment : Fragment() {

    lateinit var binding: FragmentWriteFieldBinding
    private val viewModel: WriteViewModel by activityViewModels()
    private var selectedCardView: MaterialCardView? = null // 선택된 카드뷰를 기억하기 위한 변수

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentWriteFieldBinding.inflate(layoutInflater)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding){
            cardviewWriteFieldStudy.setTag(1)
            cardviewWriteFieldContest.setTag(3)
            cardviewWriteFieldProject.setTag(2)

            cardviewWriteFieldStudy.setOnClickListener {
                onCardClicked(it as MaterialCardView)
            }
            cardviewWriteFieldContest.setOnClickListener {
                onCardClicked(it as MaterialCardView)
            }
            cardviewWriteFieldProject.setOnClickListener {
                onCardClicked(it as MaterialCardView)
            }
        }

        // ViewModel에 저장된 선택 상태를 확인하고 복원
        viewModel.selectedFieldTag.value?.let { selectedTag ->
            when (selectedTag) {
                1 -> binding.cardviewWriteFieldStudy.performClick()
                2 -> binding.cardviewWriteFieldProject.performClick()
                3 -> binding.cardviewWriteFieldContest.performClick()
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
        viewModel.validateField(true)

        val wasSelected = clickedCardView == selectedCardView
        // 선택된 카드뷰가 이미 있다면 선택 해제
        selectedCardView?.apply {
            changeStrokeColor(this, false) // 선택 해제 시 스트로크 색상을 변경
        }
        // 새로 클릭된 카드뷰를 선택하고 스트로크 색상 변경
        selectedCardView = if (!wasSelected) clickedCardView else null
        changeStrokeColor(clickedCardView, !wasSelected)

        // 선택된 카드뷰의 태그를 뷰모델에 저장
        viewModel.selectedFieldTag.value = clickedCardView.tag as Int
    }

}
package kr.co.lion.modigm.ui.write

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.google.android.material.card.MaterialCardView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteTypeBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.write.vm.WriteViewModel

class WriteTypeFragment : VBBaseFragment<FragmentWriteTypeBinding>(FragmentWriteTypeBinding::inflate) {

    private val viewModel: WriteViewModel by activityViewModels()

    private var selectedCardView: MaterialCardView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        with(binding) {
            var selectedStudyType: String? = null // 이 변수를 함수 내부에서만 사용

            // ViewModel에 저장된 선택 상태를 확인하고 복원
            when(viewModel.getUpdateData("studyType")) {
                "스터디" -> {
                    selectedStudyType = "스터디"
                    selectCardView(cardViewWriteTypeStudy)
                }
                "프로젝트" -> {
                    selectedStudyType = "프로젝트"
                    selectCardView(cardViewWriteTypeProject)
                }
                "공모전" -> {
                    selectedStudyType = "공모전"
                    selectCardView(cardViewWriteTypeContest)
                }
            }

            // 다음 버튼
            with(buttonWriteTypeNext) {
                setOnClickListener {
                    // 선택된 타입이 없으면 기본값 설정
                    val studyType = selectedStudyType ?: "스터디"

                    // 글작성 데이터의 스터디 타입을 선택된 카드뷰로 업데이트
                    viewModel.updateWriteData("studyType", studyType)

                    // 탭과 프로그래스바 상태를 뷰모델을 통해 업데이트
                    viewModel.updateSelectedTab(1) // 탭을 두 번째로 이동

                    parentFragmentManager.commit {
                        replace(R.id.containerWrite, WritePeriodFragment())
                    }
                }
            }

            cardViewWriteTypeStudy.setOnClickListener {
                selectedStudyType = "스터디"
                selectCardView(cardViewWriteTypeStudy)
            }
            cardViewWriteTypeContest.setOnClickListener {
                selectedStudyType = "공모전"
                selectCardView(cardViewWriteTypeContest)
            }
            cardViewWriteTypeProject.setOnClickListener {
                selectedStudyType = "프로젝트"
                selectCardView(cardViewWriteTypeProject)
            }
        }
    }

    private fun selectCardView(cardView: MaterialCardView) {
        with(binding) {
            selectedCardView?.let {
                changeStrokeColor(it, false) // 기존 선택 해제
            }
            selectedCardView = cardView
            changeStrokeColor(cardView, true) // 새로 선택

            // ViewModel 업데이트 (카드뷰가 선택될 때 바로 업데이트)
            val studyType = when (cardView.id) {
                cardViewWriteTypeStudy.id -> "스터디"
                cardViewWriteTypeContest.id -> "공모전"
                cardViewWriteTypeProject.id -> "프로젝트"
                else -> null
            }
            studyType?.let {
                viewModel.updateWriteData("studyType", it)
            }
            updateButtonColor()
        }
    }

    // 클릭된 카드뷰의 스트로크 색상 변경 함수
    private fun changeStrokeColor(cardView: MaterialCardView, isSelected: Boolean) {
        val colorResId = if (isSelected) R.color.pointColor else R.color.textGray
        cardView.strokeColor = ContextCompat.getColor(requireContext(), colorResId)
    }

    // 버튼의 색상을 업데이트하는 함수
    private fun updateButtonColor() {
        with(binding){
            val colorResId = if (selectedCardView != null) R.color.pointColor else R.color.buttonGray
            buttonWriteTypeNext.setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId))
            buttonWriteTypeNext.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }
}
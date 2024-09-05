package kr.co.lion.modigm.ui.write

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.google.android.material.card.MaterialCardView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteFieldBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.write.vm.WriteViewModel

class WriteFieldFragment : VBBaseFragment<FragmentWriteFieldBinding>(FragmentWriteFieldBinding::inflate) {

    private val viewModel: WriteViewModel by activityViewModels()

    private var selectedCardView: MaterialCardView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {
        with(binding) {
            // ViewModel에 저장된 선택 상태를 확인하고 복원
            when(viewModel.getUpdateData("studyType")) {
                "스터디" -> {
                    selectCardView(cardviewWriteFieldStudy)
                }
                "프로젝트" -> {
                    selectCardView(cardviewWriteFieldProject)
                }
                "공모전" -> {
                    selectCardView(cardviewWriteFieldContest)
                }
            }

            // 카드뷰 태그 설정
            cardviewWriteFieldStudy.tag = "스터디"
            cardviewWriteFieldContest.tag = "공모전"
            cardviewWriteFieldProject.tag = "프로젝트"

            // 다음 버튼
            with(buttonWriteFieldNext) {
                setOnClickListener {
                    viewModel.updateData("StudyType", selectedCardView?.tag.toString())

                    // 탭과 프로그래스바 상태를 뷰모델을 통해 업데이트
                    viewModel.updateSelectedTab(1) // 탭을 두 번째로 이동

                    parentFragmentManager.commit {
                        replace(R.id.containerWrite, WritePeriodFragment())
                    }
                }
            }


            cardviewWriteFieldStudy.setOnClickListener {
                selectCardView(cardviewWriteFieldStudy)
            }
            cardviewWriteFieldContest.setOnClickListener {
                selectCardView(cardviewWriteFieldContest)
            }
            cardviewWriteFieldProject.setOnClickListener {
                selectCardView(cardviewWriteFieldProject)
            }
        }
    }

    private fun selectCardView(cardView: MaterialCardView) {
        selectedCardView?.let {
            changeStrokeColor(it, false) // 기존 선택 해제
        }
        selectedCardView = cardView
        changeStrokeColor(cardView, true) // 새로 선택
    }

    // 클릭된 카드뷰의 스트로크 색상 변경 함수
    private fun changeStrokeColor(cardView: MaterialCardView, isSelected: Boolean) {
        val colorResId = if (isSelected) R.color.pointColor else R.color.textGray
        cardView.strokeColor = ContextCompat.getColor(requireContext(), colorResId)
    }

}
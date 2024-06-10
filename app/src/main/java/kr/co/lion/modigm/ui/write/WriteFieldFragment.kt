package kr.co.lion.modigm.ui.write

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
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteFieldBinding
import kr.co.lion.modigm.ui.write.vm.WriteViewModel

class WriteFieldFragment : Fragment() {

    lateinit var fragmentWriteFieldBinding: FragmentWriteFieldBinding
    private val viewModel: WriteViewModel by activityViewModels()

    val tabName = "field"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        fragmentWriteFieldBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_write_field, container, false)
        fragmentWriteFieldBinding.writeViewModel = viewModel
        fragmentWriteFieldBinding.lifecycleOwner = this

        return fragmentWriteFieldBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        settingEvent()
        // viewModel에서 값의 변화를 감지
        getAnswer()
    }
    fun settingEvent(){

        // 카드 뷰 클릭시 이벤트
        cardViewEffect()

    }

    // 카드뷰 클릭시 효과 설정
    fun cardViewEffect(){
        val context = requireContext()
        val clickedStrokeColor = ContextCompat.getColor(context, R.color.pointColor)
        val unclickedStrokeColor = ContextCompat.getColor(context, R.color.textGray)

        // 스터디 선택 시 클릭 리스너
        fragmentWriteFieldBinding.apply {
            cardviewWriteFieldStudy.setOnClickListener {
                cardviewWriteFieldStudy.apply {
                    if (cardElevation == 20F && strokeColor == clickedStrokeColor){
                        // 애니메이션 효과 제거
                        cardElevation = 0F
                        strokeColor = unclickedStrokeColor

                        viewModel.gettingStudyField(0)
                    } else {

                        // Stroke 색상 변경
                        strokeColor = clickedStrokeColor
                        cardviewWriteFieldContest.strokeColor = unclickedStrokeColor
                        cardviewWriteFieldProject.strokeColor = unclickedStrokeColor

                        // Elevation 추가
                        cardElevation = 20F
                        cardviewWriteFieldContest.cardElevation = 0F
                        cardviewWriteFieldProject.cardElevation = 0F

                        viewModel.gettingStudyField(1)
                    }
                }
            }

            // 공모전 선택 시 클릭 리스너
            cardviewWriteFieldContest.setOnClickListener {
                cardviewWriteFieldContest.apply {
                    if (cardElevation == 20F && strokeColor == clickedStrokeColor){
                        // 애니메이션 효과 제거
                        cardElevation = 0F
                        strokeColor = unclickedStrokeColor

                        viewModel.gettingStudyField(0)
                    } else {
                        // Stroke 색상 변경
                        strokeColor = clickedStrokeColor
                        cardviewWriteFieldStudy.strokeColor = unclickedStrokeColor
                        cardviewWriteFieldProject.strokeColor = unclickedStrokeColor

                        // Elevation 추가
                        cardElevation = 20F
                        cardviewWriteFieldStudy.cardElevation = 0F
                        cardviewWriteFieldProject.cardElevation = 0F

                        viewModel.gettingStudyField(2)
                    }
                }
            }

            // 프로젝트 선택 시 클릭 리스너
            cardviewWriteFieldProject.setOnClickListener {
                cardviewWriteFieldProject.apply {
                    if (cardElevation == 20F && strokeColor == clickedStrokeColor){
                        // 애니메이션 효과 제거
                        cardElevation = 0F
                        strokeColor = unclickedStrokeColor

                        viewModel.gettingStudyField(0)
                    } else {

                        // Stroke 색상 변경
                        strokeColor = clickedStrokeColor
                        cardviewWriteFieldContest.strokeColor = unclickedStrokeColor
                        cardviewWriteFieldStudy.strokeColor = unclickedStrokeColor

                        // Elevation 추가
                        cardElevation = 20F
                        cardviewWriteFieldContest.cardElevation = 0F
                        cardviewWriteFieldStudy.cardElevation = 0F

                        viewModel.gettingStudyField(3)
                    }
                }
            }

        }
    }

    // 입력처리 함수
    fun getAnswer(){

        // studyType Observing!!
        viewModel.studyType.observe(viewLifecycleOwner){ type ->
            when (type){
                // 입력 해제
                0 -> {
                    // fieldClicked.value = false
                    viewModel.userDidNotAnswer(tabName)

                    val context = requireContext()
                    val toast = Toast.makeText(context, "스터디, 공모전, 프로젝트 중에 어떤 분야를 모집할 지 선택해주세요", Toast.LENGTH_SHORT)
                    toast.show()
                }
                // 스터디 클릭
                1, 2, 3 -> {
                    // fieldClicked.value = true
                    viewModel.userDidAnswer(tabName)
                }
                else -> {
                    Log.d("WriteFieldFragment", "studyType 입력 오류")
                }
            }
        }
    }
}
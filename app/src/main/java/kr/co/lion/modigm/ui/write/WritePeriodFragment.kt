package kr.co.lion.modigm.ui.write

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.DialogWritePeriodFragmentBinding
import kr.co.lion.modigm.databinding.FragmentWriteBinding
import kr.co.lion.modigm.databinding.FragmentWritePeriodBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.write.vm.WriteViewModel


class WritePeriodFragment : Fragment() {

    lateinit var fragmentWritePeriodBinding: FragmentWritePeriodBinding
    private val viewModel: WriteViewModel by activityViewModels()
    val tabName = "period"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentWritePeriodBinding = FragmentWritePeriodBinding.inflate(inflater, container, false)

        return fragmentWritePeriodBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        settingEvent()
    }

    fun initData(){
        // 입력 초기화
        viewModel.userDidNotAnswer(tabName)

        // 전에 받은 입력이 있다면~
        if (viewModel.periodClicked.value == true){
            // 버튼을 활성화
            viewModel.activateButton()
        } else {
            // 버튼을 비활성화
            viewModel.deactivateButton()
        }

    }


    fun settingEvent(){
        // 기간선택 클릭시
        fragmentWritePeriodBinding.apply {
            // 다이얼로그를 띄운다
            textinputWritePeriod.setOnClickListener {
                val context = requireContext() // 이 Fragment의 context가져오기
                val builder = MaterialAlertDialogBuilder(context).apply {

                    // 뷰를 설정한다
                     val dialogWritePeriodFragmentBinding = DialogWritePeriodFragmentBinding.inflate(layoutInflater)
                    setView(dialogWritePeriodFragmentBinding.root)

                    // 버튼 클릭 이벤트 처리
                    dialogWritePeriodFragmentBinding.apply {
                        textViewDialogWritePeriod01.setOnClickListener {

                            clickAnimation(it)
                            textinputWritePeriod.setText("1개월 이하")
                            viewModel.userDidAnswer(tabName)


                        }

                        textViewDialogWritePeriod02.setOnClickListener {
                            clickAnimation(it)
                            textinputWritePeriod.setText("1개월 이상")
                            viewModel.userDidAnswer(tabName)
                        }

                        textViewDialogWritePeriod03.setOnClickListener {
                            clickAnimation(it)
                            textinputWritePeriod.setText("3개월 이상")
                            viewModel.userDidAnswer(tabName)
                        }

                        textViewDialogWritePeriod04.setOnClickListener {
                            clickAnimation(it)
                            textinputWritePeriod.setText("6개월 이상")
                            viewModel.userDidAnswer(tabName)
                        }
                    }
                }
                builder.show()
            }
        }
    }

    // textView 클릭 시 애니메이션 처리
    private fun clickAnimation(view: View){
        view.animate().scaleX(-1.2f).scaleY(1.2f).setDuration(300).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(300)
        }
    }
}
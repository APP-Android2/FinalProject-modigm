package kr.co.lion.modigm.ui.write

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.DialogWritePeriodFragmentBinding
import kr.co.lion.modigm.databinding.FragmentWriteBinding
import kr.co.lion.modigm.databinding.FragmentWritePeriodBinding
import kr.co.lion.modigm.ui.MainActivity


class WritePeriodFragment : Fragment() {

    lateinit var fragmentWritePeriodBinding: FragmentWritePeriodBinding
    lateinit var fragmentWriteBinding: FragmentWriteBinding
    lateinit var mainActity: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mainActity = activity as MainActivity
        fragmentWritePeriodBinding = FragmentWritePeriodBinding.inflate(inflater, container, false)
        fragmentWriteBinding = FragmentWriteBinding.inflate(inflater)

        return fragmentWritePeriodBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingProgressBar()
        settingEvent()
    }

    fun settingProgressBar(){
        fragmentWriteBinding.progressBarWriteFragment.apply {
            setProgress(40, true)
        }
    }

    fun settingEvent(){
        // 기간선택 클릭시
        fragmentWritePeriodBinding.apply {
            // 다이얼로그를 띄운다
            textinputWritePeriod.setOnClickListener {
                val builder = MaterialAlertDialogBuilder(mainActity).apply {

                    // 뷰를 설정한다
                     val dialogWritePeriodFragmentBinding = DialogWritePeriodFragmentBinding.inflate(layoutInflater)
                    setView(dialogWritePeriodFragmentBinding.root)

                    // 버튼 클릭 이벤트 처리
                    dialogWritePeriodFragmentBinding.apply {
                        textViewDialogWritePeriod01.setOnClickListener {

                            clickAnimation(it)
                            textinputWritePeriod.setText("1개월 이하")

                        }

                        textViewDialogWritePeriod02.setOnClickListener {
                            clickAnimation(it)
                            textinputWritePeriod.setText("1개월 이상")
                        }

                        textViewDialogWritePeriod03.setOnClickListener {
                            clickAnimation(it)
                            textinputWritePeriod.setText("3개월 이상")

                        }

                        textViewDialogWritePeriod04.setOnClickListener {
                            clickAnimation(it)
                            textinputWritePeriod.setText("6개월 이상")
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
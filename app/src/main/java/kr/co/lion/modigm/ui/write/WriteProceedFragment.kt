package kr.co.lion.modigm.ui.write

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteProceedBinding
import kr.co.lion.modigm.ui.MainActivity

class WriteProceedFragment : Fragment() {

    lateinit var fragmentWriteProceedBinding: FragmentWriteProceedBinding
    lateinit var mainActivity: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        fragmentWriteProceedBinding = FragmentWriteProceedBinding.inflate(inflater)
        mainActivity = activity as MainActivity
        return fragmentWriteProceedBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        settingView()
        settingEvent()
    }


    fun settingEvent(){
        fragmentWriteProceedBinding.apply {
            // 키보드 엔터시 키보드 제거
            textFieldWriteProceedNumOfMember.setOnEditorActionListener { v, actionId, event ->

                // 엔터키를 누르면 다음 View로 포커스 이동
                false
            }

            // 선택된 chip 강조 효과


        }
    }

    fun settingView(){
        // 칩 클릭 시 효과 설정
        chipViewEffect()
    }

    // 칩 클릭시 효과 설정
    fun chipViewEffect(){
        // 칩 클릭시 효과 설정
        fragmentWriteProceedBinding.apply {
            chipWriteProceedOffline.apply {
                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        val color = ContextCompat.getColorStateList(mainActivity,R.color.pointColor)
                        this.chipBackgroundColor = color
                        this.invalidate()  // 강제로 UI 갱신
                    } else {
                        val defaultColor = context.getColor(R.color.textGray)
                        this.chipStrokeColor = ColorStateList.valueOf(defaultColor)
                        this.invalidate()  // 강제로 UI 갱신
                    }
                }
            }
        }
    }
}

package kr.co.lion.modigm.ui.write

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentWriteSkillBinding
import kr.co.lion.modigm.ui.MainActivity

class WriteSkillFragment : Fragment() {

    private lateinit var binding: FragmentWriteSkillBinding
    lateinit var mainActivity: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentWriteSkillBinding.inflate(inflater)
        mainActivity = activity as MainActivity

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingView()
        settingEvent()

    }


    fun settingEvent(){
        binding.apply {
            textFieldWriteSkill.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus){
                    // 포커스 On
                    // 바텀시트를 띄운다

                }
                else {

                }
            }
        }


    }

    fun settingView(){
        // cardView 클릭 시 Stroke 색상 변경
        binding.apply {
            val clickedStrokeColor = ContextCompat.getColor(mainActivity, R.color.pointColor)
            val unclickedStrokeColor = ContextCompat.getColor(mainActivity, R.color.textGray)

            // 신청제 Card
            cardviewWriteSkillApplicationSystem.setOnClickListener {
                cardviewWriteSkillApplicationSystem.apply {

                    setOnCheckedChangeListener { cardView, isChecked ->
                        if (isChecked){
                            Log.d("TedMoon", "Card Clicked!")
                        }
                        else {
                            Log.d("TedMoon", "Card unClicked!")
                        }
                    }
                }

            }

            // 선착순 Card
            cardviewWriteSkillFirstCome.setOnClickListener {

            }
        }

    }
}
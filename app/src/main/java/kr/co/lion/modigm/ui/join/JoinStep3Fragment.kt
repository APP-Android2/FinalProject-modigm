package kr.co.lion.modigm.ui.join

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.chip.Chip
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinStep3Binding

class JoinStep3Fragment : Fragment() {

    val binding: FragmentJoinStep3Binding by lazy {
        FragmentJoinStep3Binding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        settingChips()
        return binding.root
    }

    private fun settingChips(){
        // 데이터는 추후 수정
        val interestList = arrayListOf<String>(
            "웹", "서버", "임베디드", "프론트 엔드", "백 엔드",
            "iOS", "안드로이드", "C, C++", "파이썬", "하드웨어",
            "머신러닝", "빅데이터", "Node.js", ".NET", "블록체인",
            "크로스플랫폼", "그래픽스", "VR"
        )
        for(chipName in interestList){
            binding.chipGroupJoinInterest.addView(
                Chip(requireContext()).apply {
                    text = chipName
                    isCheckable = true
                    setTextColor(resources.getColor(R.color.textGray, null))
                    setChipBackgroundColorResource(R.color.white)
                    setChipStrokeColorResource(R.color.buttonGray)
                    setOnCheckedChangeListener { _, isChecked ->
                        if(isChecked){
                            setTextColor(resources.getColor(R.color.white, null))
                            setChipBackgroundColorResource(R.color.pointColor)
                        }else{
                            setTextColor(resources.getColor(R.color.textGray, null))
                            setChipBackgroundColorResource(R.color.white)
                        }
                    }
                }
            )
        }
    }

    // 입력한 내용 유효성 검사
    fun validate(): Boolean {
        binding.textViewJoinAlert.visibility = View.GONE
        return if(binding.chipGroupJoinInterest.checkedChipIds.isEmpty()){
            binding.textViewJoinAlert.visibility = View.VISIBLE
            false
        }else{
            true
        }
    }

}
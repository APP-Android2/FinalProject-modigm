package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinStep3Binding
import kr.co.lion.modigm.ui.join.vm.JoinStep3ViewModel

class JoinStep3Fragment : Fragment() {

    val binding: FragmentJoinStep3Binding by lazy {
        FragmentJoinStep3Binding.inflate(layoutInflater)
    }

    val joinStep3ViewModel: JoinStep3ViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        settingChips()
        return binding.root
    }

    private fun settingChips(){
        joinStep3ViewModel.interestList.observe(viewLifecycleOwner){
            for(chipName in it){
                binding.chipGroupJoinInterest.addView(
                    Chip(requireContext()).apply {
                        text = chipName
                        isCheckable = true
                        textSize = 18f
                        // setEnsureMinTouchTargetSize(false)

                        setTextColor(resources.getColor(R.color.textGray, null))
                        setChipBackgroundColorResource(R.color.white)
                        setChipStrokeColorResource(R.color.buttonGray)
                        setOnCheckedChangeListener { it, isChecked ->
                            if(isChecked){
                                setTextColor(resources.getColor(R.color.white, null))
                                setChipBackgroundColorResource(R.color.pointColor)
                                Log.d("test1234",it.text.toString())
                                joinStep3ViewModel.addInterest(it.text.toString())
                            }else{
                                setTextColor(resources.getColor(R.color.textGray, null))
                                setChipBackgroundColorResource(R.color.white)
                                joinStep3ViewModel.removeInterest(it.text.toString())
                            }
                        }
                    }
                )
            }
        }
    }

    // 입력한 내용 유효성 검사
    fun validate(): Boolean {
        binding.textViewJoinAlert.visibility = View.GONE
        return if(joinStep3ViewModel.validate()){
            binding.textViewJoinAlert.visibility = View.VISIBLE
            false
        }else{
            true
        }
    }

}
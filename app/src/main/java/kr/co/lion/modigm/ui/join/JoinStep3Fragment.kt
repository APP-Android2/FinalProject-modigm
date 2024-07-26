package kr.co.lion.modigm.ui.join

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinStep3Binding
import kr.co.lion.modigm.ui.join.vm.JoinStep3ViewModel

class JoinStep3Fragment : Fragment() {

    val binding: FragmentJoinStep3Binding by lazy {
        FragmentJoinStep3Binding.inflate(layoutInflater)
    }

    private val joinStep3ViewModel: JoinStep3ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        settingCollector()
        return binding.root
    }

    private fun settingCollector(){
        //Chip 셋팅
        lifecycleScope.launch {
            joinStep3ViewModel.interestList.collect {
                if(it.isNotEmpty()){
                    binding.textViewJoinAlert.visibility = View.GONE
                }
                for(chipName in it){
                    binding.chipGroupJoinInterest.addView(
                        Chip(requireContext()).apply {
                            text = chipName.str
                            isCheckable = true
                            setTextAppearance(R.style.ChipTextStyle)

                            setTextColor(resources.getColor(R.color.textGray, null))
                            setChipBackgroundColorResource(R.color.white)
                            setChipStrokeColorResource(R.color.buttonGray)
                            setOnCheckedChangeListener { _, isChecked ->
                                if(isChecked){
                                    setTextColor(resources.getColor(R.color.white, null))
                                    setChipBackgroundColorResource(R.color.pointColor)
                                    joinStep3ViewModel.addInterest(chipName.str)

                                }else{
                                    setTextColor(resources.getColor(R.color.textGray, null))
                                    setChipBackgroundColorResource(R.color.white)
                                    joinStep3ViewModel.removeInterest(chipName.str)
                                }
                            }
                        }
                    )
                }
            }
        }

        // 에러메시지 셋팅
        lifecycleScope.launch {
            joinStep3ViewModel.isValidate.collect {
                if(it == true){
                    binding.textViewJoinAlert.visibility = View.GONE
                }else if(it == false){
                    binding.textViewJoinAlert.visibility = View.VISIBLE
                }
            }
        }
    }

}
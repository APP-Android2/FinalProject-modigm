package kr.co.lion.modigm.ui.join

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
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
        settingChips()
        settingObserver()
        return binding.root
    }

    private fun settingChips(){
        joinStep3ViewModel.interestList.observe(viewLifecycleOwner){
            for(chipName in it){
                binding.chipGroupJoinInterest.addView(
                    Chip(requireContext()).apply {
                        text = chipName.str
                        isCheckable = true
                        setTextAppearance(R.style.ChipTextStyle)

                        setTextColor(resources.getColor(R.color.textGray, null))
                        setChipBackgroundColorResource(R.color.white)
                        setChipStrokeColorResource(R.color.buttonGray)
                        setOnCheckedChangeListener { it, isChecked ->
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

    private fun settingObserver(){
        joinStep3ViewModel.isValidate.observe(viewLifecycleOwner){
            if(it){
                binding.textViewJoinAlert.visibility = View.GONE
            }else{
                binding.textViewJoinAlert.visibility = View.VISIBLE
            }
        }
    }

}
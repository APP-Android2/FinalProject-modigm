package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinStep3InterestBinding
import kr.co.lion.modigm.ui.DBBaseFragment
import kr.co.lion.modigm.ui.join.vm.JoinStep3InterestViewModel
import kr.co.lion.modigm.util.Interest
import kr.co.lion.modigm.util.collectWhenStarted

class JoinStep3InterestFragment : DBBaseFragment<FragmentJoinStep3InterestBinding>(R.layout.fragment_join_step3_interest) {

    private val joinStep3InterestViewModel: JoinStep3InterestViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        binding.viewModel = joinStep3InterestViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingChips()
        settingCollector()
    }

    private fun settingChips(){
        //Chip 셋팅
        Interest.entries.toTypedArray().also {
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
                                joinStep3InterestViewModel.addToInterestList(chipName.str)

                            }else{
                                setTextColor(resources.getColor(R.color.textGray, null))
                                setChipBackgroundColorResource(R.color.white)
                                joinStep3InterestViewModel.removeFromInterestList(chipName.str)
                            }
                        }
                    }
                )
            }
        }
    }
    private fun settingCollector(){
        // 에러메시지 셋팅
        collectWhenStarted(joinStep3InterestViewModel.isInterestListValidated) {
            if(it == true){
                binding.textViewJoinAlert.visibility = View.GONE
            }else if(it == false){
                binding.textViewJoinAlert.visibility = View.VISIBLE
            }
        }
    }

}
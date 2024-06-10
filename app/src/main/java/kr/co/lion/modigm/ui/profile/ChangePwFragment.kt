package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChangePwBinding
import kr.co.lion.modigm.ui.profile.vm.ChangePwViewModel

class ChangePwFragment : Fragment() {

    lateinit var binding: FragmentChangePwBinding
    private val viewModel: ChangePwViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_change_pw, container, false)
        binding.changePwViewModel = viewModel
        binding.lifecycleOwner = this

        settingErrorText()

        return binding.root
    }

    private fun settingErrorText(){
        binding.changePWTextFieldPW.error = viewModel.oldPwError.value
        binding.changePWTextFieldNewPW.error = viewModel.newPwError.value
        binding.changePWTextFieldCheck.error = viewModel.newPwCheckError.value
    }


}
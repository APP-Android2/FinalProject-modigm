package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinStep1Binding
import kr.co.lion.modigm.ui.DBBaseFragment
import kr.co.lion.modigm.ui.join.vm.JoinStep1ViewModel

class JoinStep1Fragment : DBBaseFragment<FragmentJoinStep1Binding>(R.layout.fragment_join_step1) {

    private val joinStep1ViewModel: JoinStep1ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        binding.viewModel = joinStep1ViewModel

        settingTextInputLayoutError()

        return binding.root
    }

    // 에러 메시지 설정
    private fun settingTextInputLayoutError(){
        lifecycleScope.launch {
            joinStep1ViewModel.emailValidation.collect{
                if(it.isNotEmpty()){
                    binding.textInputLayoutJoinUserEmail.error = it
                }
            }
        }

        lifecycleScope.launch {
            joinStep1ViewModel.pwValidation.collect{
                if(it.isNotEmpty()){
                    binding.textInputLayoutJoinUserPassword.error = it
                }
            }
        }

        lifecycleScope.launch {
            joinStep1ViewModel.pwValidation.collect{
                if(it.isNotEmpty()){
                    binding.textInputLayoutJoinUserPassword.error = it
                }
            }
        }

        lifecycleScope.launch {
            joinStep1ViewModel.pwCheckValidation.collect{
                binding.textInputLayoutJoinUserPasswordCheck.error = it
            }
        }
    }

}
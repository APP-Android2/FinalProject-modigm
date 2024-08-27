package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinStep1Binding
import kr.co.lion.modigm.ui.DBBaseFragment
import kr.co.lion.modigm.ui.join.vm.JoinStep1ViewModel
import kr.co.lion.modigm.util.collectWhenStarted

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

        // 이메일 에러
        collectWhenStarted(joinStep1ViewModel.emailValidation) {
            binding.textInputLayoutJoinUserEmail.error = it
        }

        // 비밀번호 에러
        collectWhenStarted(joinStep1ViewModel.pwValidation) {
            binding.textInputLayoutJoinUserPassword.error = it
        }

        // 비밀번호 확인 에러
        collectWhenStarted(joinStep1ViewModel.pwCheckValidation) {
            binding.textInputLayoutJoinUserPasswordCheck.error = it
        }
    }

}
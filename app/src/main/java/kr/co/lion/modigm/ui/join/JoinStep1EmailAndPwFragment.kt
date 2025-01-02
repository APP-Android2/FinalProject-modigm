package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinStep1EmailAndPwBinding
import kr.co.lion.modigm.ui.DBBaseFragment
import kr.co.lion.modigm.ui.join.vm.JoinStep1EmailAndPwViewModel
import kr.co.lion.modigm.util.collectWhenStarted

class JoinStep1EmailAndPwFragment : DBBaseFragment<FragmentJoinStep1EmailAndPwBinding>(R.layout.fragment_join_step1_email_and_pw) {

    private val joinStep1EmailAndPwViewModel: JoinStep1EmailAndPwViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        binding.viewModel = joinStep1EmailAndPwViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingTextInputLayoutError()
    }

    // 에러 메시지 설정
    private fun settingTextInputLayoutError(){

        // 이메일 에러
        collectWhenStarted(joinStep1EmailAndPwViewModel.userInputEmailValidation) {
            binding.textInputLayoutJoinUserEmail.error = it
        }

        // 비밀번호 에러
        collectWhenStarted(joinStep1EmailAndPwViewModel.userInputPwValidation) {
            binding.textInputLayoutJoinUserPassword.error = it
        }

        // 비밀번호 확인 에러
        collectWhenStarted(joinStep1EmailAndPwViewModel.userInputPwCheckValidation) {
            binding.textInputLayoutJoinUserPasswordCheck.error = it
        }
    }

}
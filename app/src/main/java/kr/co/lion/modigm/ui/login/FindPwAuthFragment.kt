package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindPwAuthBinding
import kr.co.lion.modigm.ui.login.vm.FindPwAuthViewModel
import kr.co.lion.modigm.util.FragmentName

class FindPwAuthFragment : Fragment(R.layout.fragment_find_pw_auth) {

    private val viewModel: FindPwAuthViewModel by viewModels()

    private val verificationId by lazy {
        arguments?.getString("verificationId")
    }

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바인딩
        val binding = FragmentFindPwAuthBinding.bind(view)

        viewModel.setVerificationId(verificationId?:"")

        initView(binding)
        settingObserver(binding)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 뷰모델 클리어 함수 구현요망
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView(binding: FragmentFindPwAuthBinding) {
        with(binding) {
            // 툴바
            with(toolbarFindPwAuth) {
                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

            // 인증 버튼
            with(buttonFindPwAuthOK) {
                setOnClickListener {
                    isClickable = false
                    // 유효성 검사
                    val validate = viewModel!!.validateInput()
                    if(!validate){
                        isClickable = true
                        return@setOnClickListener
                    }

                    // 인증 번호 확인
                    viewModel!!.checkCodeAndFindEmail()
                    isClickable = true
                }
            }
        }
    }

    private fun settingObserver(binding: FragmentFindPwAuthBinding){
        // 유효성 검사
        viewModel.inputCodeError.observe(viewLifecycleOwner) {
            binding.textInputEditFindPwPassCode.error = it
            binding.textInputEditFindPwPassCode.requestFocus()
        }
        // 인증번호 확인이 완료되면 다음으로 이동
        viewModel.isComplete.observe(viewLifecycleOwner){
            if(it){
                // 완료 여부는 초기화해서 popStackBack으로 돌아와도 문제 없게
                viewModel.resetComplete()

                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, ResetPwFragment())
                    .addToBackStack(FragmentName.RESET_PW.str)
                    .commit()
            }
            binding.buttonFindPwAuthOK.isClickable = true
        }
    }
}
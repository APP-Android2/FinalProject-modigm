package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindPwAuthBinding
import kr.co.lion.modigm.ui.BaseFragment
import kr.co.lion.modigm.ui.login.vm.FindPwAuthViewModel
import kr.co.lion.modigm.util.FragmentName

class FindPwAuthFragment : BaseFragment<FragmentFindPwAuthBinding>(FragmentFindPwAuthBinding::inflate) {

    private val viewModel: FindPwAuthViewModel by viewModels()

    private val verificationId by lazy {
        arguments?.getString("verificationId")
    }

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setVerificationId(verificationId?:"")

        initView()
        settingObserver()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 뷰모델 클리어 함수 구현요망
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView() {
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

    private fun settingObserver(){

        with(binding){
            // 유효성 검사
            viewModel.inputCodeError.observe(viewLifecycleOwner) {
                textInputEditFindPwPassCode.error = it
                textInputEditFindPwPassCode.requestFocus()
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
                buttonFindPwAuthOK.isClickable = true
            }
        }

    }
}
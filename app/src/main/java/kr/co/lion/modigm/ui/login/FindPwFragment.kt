package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindPwBinding
import kr.co.lion.modigm.ui.BaseFragment
import kr.co.lion.modigm.ui.login.vm.FindPwViewModel
import kr.co.lion.modigm.util.FragmentName

class FindPwFragment : BaseFragment<FragmentFindPwBinding>(FragmentFindPwBinding::inflate) {

    private val viewModel: FindPwViewModel by viewModels()

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        settingObserver()
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView() {
        with(binding) {
            // 툴바
            with(toolbarFindPw) {
                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

            // 번호 입력 시 자동으로 하이픈을 넣어줌
            textInputEditFindPwPhone.addTextChangedListener(
                PhoneNumberFormattingTextWatcher()
            )

            // 다음 버튼
            with(binding.buttonFindPwNext) {
                setOnClickListener {
                    isClickable = false
                    // 유효성 검사
                    val validate = viewModel!!.validateInput()
                    if(!validate){
                        isClickable = true
                        return@setOnClickListener
                    }

                    // 이메일과 전화번호 확인 후 인증번호 발송
                    viewModel!!.checkEmailAndPhone(requireActivity())
                    isClickable = true
                }
            }
        }
    }

    private fun settingObserver(){
        with(binding){
            // 유효성 검사
            viewModel.emailError.observe(viewLifecycleOwner) {
                textInputEditFindPwEmail.error = it
                textInputEditFindPwEmail.requestFocus()
            }
            viewModel.phoneError.observe(viewLifecycleOwner) {
                textInputEditFindPwPhone.error = it
                textInputEditFindPwPhone.requestFocus()
            }

            // 다음으로 이동
            viewModel.isComplete.observe(viewLifecycleOwner) {
                if(it){
                    moveToNext()
                }
            }
        }
    }

    private fun moveToNext(){
        // 완료 여부는 초기화해서 popStackBack으로 돌아와도 문제 없게
        viewModel.resetComplete()
        // 다음 화면에 verficationId를 전달하여 번호 인증에 사용
        val fragment = FindPwAuthFragment().apply {
            arguments = Bundle().apply {
                putString("verificationId", viewModel.verificationId.value)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.containerMain, fragment)
            .addToBackStack(FragmentName.FIND_PW_AUTH.str)
            .commit()
    }
}
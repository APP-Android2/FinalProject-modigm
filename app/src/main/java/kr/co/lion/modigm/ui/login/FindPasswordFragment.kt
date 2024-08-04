package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindPasswordBinding
import kr.co.lion.modigm.ui.ViewBindingFragment
import kr.co.lion.modigm.ui.login.vm.UpdatePasswordViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.shake

class FindPasswordFragment : ViewBindingFragment<FragmentFindPasswordBinding>(FragmentFindPasswordBinding::inflate) {

    private val viewModel: UpdatePasswordViewModel by viewModels()

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        observeViewModel()
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView() {
        with(binding) {

            // 실시간 텍스트 변경 감지 설정
            textInputEditFindPwEmail.addTextChangedListener(inputWatcher)
            textInputEditFindPwPhone.addTextChangedListener(inputWatcher)

            // 툴바
            with(toolbarFindPw) {
                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack(FragmentName.OTHER_LOGIN.str,0)
                }
            }

            // 번호 입력 시 자동으로 하이픈을 넣어줌
            textInputEditFindPwPhone.addTextChangedListener(
                PhoneNumberFormattingTextWatcher()
            )

            // 다음 버튼
            with(buttonFindPwNext) {
                isEnabled = false  // 버튼을 처음에 비활성화
                setOnClickListener {

                    // 유효성 검사
                    if (!checkAllInputs()) {
                        return@setOnClickListener
                    }
                    // 입력한 이름이 계정 정보와 일치하는지 확인하고 인증 문자 발송
                    val email = textInputEditFindPwEmail.text.toString()
                    val phone = textInputEditFindPwPhone.text.toString()
                    viewModel.checkEmailAndPhone(requireActivity(), email, phone)
                }
            }
        }
    }

    // 유효성 검사 및 버튼 활성화/비활성화 업데이트
    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            with(binding){
                buttonFindPwNext.isEnabled =
                    !textInputEditFindPwEmail.text.isNullOrEmpty() && !textInputEditFindPwPhone.text.isNullOrEmpty()
            }

        }

        override fun afterTextChanged(p0: Editable?) { }
    }

    private fun observeViewModel(){
        with(binding) {
            viewModel.isComplete.observe(viewLifecycleOwner) { isComplete ->
                if (isComplete) {
                    moveToNext()
                }
            }
            // 유효성 검사
            viewModel.emailError.observe(viewLifecycleOwner) { error ->
                textInputLayoutFindPwEmail.error = error.message
                textInputEditFindPwEmail.requestFocus()
                textInputLayoutFindPwEmail.shake()
            }
            viewModel.phoneError.observe(viewLifecycleOwner) { error ->
                textInputLayoutFindPwPhone.error = error.message
                textInputEditFindPwPhone.requestFocus()
                textInputLayoutFindPwPhone.shake()
            }
        }
    }

    // 유효성 검사
    private fun checkAllInputs(): Boolean {
        return checkEmail() && checkPhone()
    }

    private fun checkEmail(): Boolean {
        with(binding) {
            return if (textInputEditFindPwEmail.text.isNullOrEmpty()) {
                textInputLayoutFindPwEmail.error = "이메일을 입력해주세요."
                textInputEditFindPwEmail.requestFocus()
                textInputLayoutFindPwEmail.shake()
                false
            } else if (!isValidEmail(textInputEditFindPwEmail.text.toString())) {
                textInputLayoutFindPwEmail.error = "올바른 이메일을 입력해주세요."
                textInputEditFindPwEmail.requestFocus()
                textInputLayoutFindPwEmail.shake()
                false
            } else {
                textInputLayoutFindPwEmail.error = null
                true
            }
        }
    }

    // 연락처 유효성 검사
    private fun checkPhone(): Boolean {
        with(binding) {
            return if (textInputEditFindPwPhone.text.isNullOrEmpty()) {
                textInputLayoutFindPwPhone.error = "연락처를 입력해주세요."
                textInputEditFindPwPhone.requestFocus()
                textInputLayoutFindPwPhone.shake()
                false
            } else if (textInputEditFindPwPhone.text.toString().length <= 11) {
                textInputLayoutFindPwPhone.error = "올바른 연락처를 입력해주세요."
                textInputEditFindPwPhone.requestFocus()
                textInputLayoutFindPwPhone.shake()
                false
            } else {
                textInputLayoutFindPwPhone.error = null
                true
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(Regex(emailPattern))
    }

    private fun moveToNext(){
        // 완료 여부는 초기화해서 popStackBack으로 돌아와도 문제 없게
        viewModel.isCompleteTo(false)
        // 다음 화면에 verficationId를 전달하여 번호 인증에 사용
        val fragment = FindPasswordAuthFragment().apply {
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
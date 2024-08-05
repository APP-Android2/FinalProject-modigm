package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.databinding.FragmentUpdatePasswordBinding
import kr.co.lion.modigm.ui.ViewBindingFragment
import kr.co.lion.modigm.ui.login.vm.UpdatePasswordViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.shake
import java.util.regex.Pattern

class UpdatePasswordFragment : ViewBindingFragment<FragmentUpdatePasswordBinding>(FragmentUpdatePasswordBinding::inflate) {

    private val viewModel: UpdatePasswordViewModel by viewModels()

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.clearData() // 뷰모델 클리어 함수
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView() {
        with(binding) {

            // 실시간 텍스트 변경 감지 설정
            textInputEditUpdatePassword.addTextChangedListener(inputWatcher)
            textInputEditUpdatePasswordConfirm.addTextChangedListener(inputWatcher)

            // 툴바
            with(toolbarResetPw) {
                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {

                    // 취소 다이얼로그
                    showCancelDialog()
                }
            }

            // 다음 버튼
            with(buttonFindPwResetOK) {
                isEnabled = false // 버튼을 처음에 비활성화
                setOnClickListener {
                    // 유효성 검사

                    if(!validateInput()){
                        return@setOnClickListener
                    }
                    val newPassword = textInputEditUpdatePasswordConfirm.text.toString()
                    // 비밀번호 변경
                    viewModel.updatePassword(newPassword)
                }
            }
        }
    }

    // 유효성 검사 및 버튼 활성화/비활성화 업데이트
    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            with(binding) {
                buttonFindPwResetOK.isEnabled =
                    !textInputEditUpdatePassword.text.isNullOrEmpty() && !textInputEditUpdatePasswordConfirm.text.isNullOrEmpty()
            }

        }

        override fun afterTextChanged(p0: Editable?) {}
    }

    // 유효성 검사
    private fun validateInput(): Boolean {
        with(binding) {
            if (textInputEditUpdatePassword.text.isNullOrEmpty()) {
                textInputLayoutUpdatePassword.error = "비밀번호를 입력해주세요."
                textInputEditUpdatePassword.requestFocus()
                textInputLayoutUpdatePassword.shake()
                return false

            } else {
                if (!Pattern.matches(
                        "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]{8,20}$",
                        textInputEditUpdatePassword.text.toString()
                    )
                ) {
                    textInputLayoutUpdatePassword.error = "영문, 숫자, 특수문자가 포함된 비밀번호를 8~20자로 입력해주세요."
                    return false
                }
            }
            if (textInputEditUpdatePassword.text.toString() != textInputEditUpdatePasswordConfirm.text.toString()) {
                textInputLayoutUpdatePasswordConfirm.error = "비밀번호가 일치하지 않습니다."
                return false
            }
            return true
        }

    }

    private fun observeViewModel(){
        with(binding){
            // 유효성 검사
            viewModel.newPasswordError.observe(viewLifecycleOwner) { error ->
                textInputLayoutUpdatePassword.error = error.message
                textInputEditUpdatePassword.requestFocus()
                textInputLayoutUpdatePassword.shake()
            }
            viewModel.newPasswordConfirmError.observe(viewLifecycleOwner) { error ->
                textInputLayoutUpdatePasswordConfirm.error = error.message
                textInputEditUpdatePasswordConfirm.requestFocus()
                textInputLayoutUpdatePasswordConfirm.shake()
            }

            // 비밀번호 변경 완료 여부
            viewModel.isComplete.observe(viewLifecycleOwner){
                if(it){
                    showFindEmailDialog()
                }
            }
        }
    }

    // 비밀번호 변경 다이얼로그 표시
    private fun showFindEmailDialog() {
        viewModel.isCompleteTo(false)
        val dialog = CustomUpdatePasswordDialog(requireContext())
        dialog.setTitle("비밀번호 변경")
        dialog.setMessage("비밀번호 변경이 완료되었습니다.")
        dialog.setPositiveButton("확인") {
            // 비밀번호 변경 완료 후 로그인 화면으로 이동
            parentFragmentManager.popBackStack(FragmentName.OTHER_LOGIN.str,0)
        }
        dialog.show()
    }

    // 뒤로가기 다이얼로그 표시
    private fun showCancelDialog() {

        val dialog = CustomCancelDialog(requireContext())
        dialog.setTitle("뒤로가기")
        dialog.setPositiveButton("예") {
            lifecycleScope.launch {
                viewModel.authLogout()
                parentFragmentManager.popBackStack(FragmentName.OTHER_LOGIN.str,0)
            }

        }
        dialog.setNegativeButton("아니오") {

            dialog.dismiss()
        }
        dialog.show()
    }
}
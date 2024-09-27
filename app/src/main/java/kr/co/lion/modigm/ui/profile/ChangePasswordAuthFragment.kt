package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChangePasswordAuthBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.login.CustomCancelDialog
import kr.co.lion.modigm.ui.login.CustomUpdatePasswordDialog
import kr.co.lion.modigm.ui.profile.vm.ChangePasswordViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.shake

class ChangePasswordAuthFragment : VBBaseFragment<FragmentChangePasswordAuthBinding>(FragmentChangePasswordAuthBinding::inflate) {

    // 뷰모델
    private val viewModel: ChangePasswordViewModel by viewModels()

    // 태그
    private val logTag by lazy { ChangePasswordAuthFragment::class.simpleName }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        observeViewModel()
        backButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearData()
    }

    private fun initView(){
        with(binding){
            // 실시간 텍스트 변경 감지 설정
            textInputEditChangePasswordAuthNewPassword.addTextChangedListener(inputWatcher)
            textInputEditChangePasswordAuthNewPasswordConfirm.addTextChangedListener(inputWatcher)

            // 툴바 설정
            with(toolbarChangePasswordAuth){
                title = "비밀번호 변경"
                setNavigationIcon(R.drawable.icon_arrow_back_24px)
                setNavigationOnClickListener {
                    showCancelDialog()
                }
            }

            with(buttonChangePasswordAuthDone){
                isEnabled = false // 버튼을 처음에 비활성화
                setOnClickListener {
                    // 유효성 검사
                    if(!checkPasswordInput()){
                        return@setOnClickListener
                    }
                    val newPasswordConfirm = textInputEditChangePasswordAuthNewPasswordConfirm.text.toString()
                    viewModel.updatePassword(newPasswordConfirm)
                }
            }
        }
    }

    private fun checkPasswordInput(): Boolean {
        with(binding){
            // 비밀번호 유효성 검사
            val newPassword = textInputEditChangePasswordAuthNewPassword
            val newPasswordText = newPassword.text.toString()
            val newPasswordConfirm = textInputEditChangePasswordAuthNewPasswordConfirm
            val newPasswordConfirmText = newPasswordConfirm.text.toString()
            val newPasswordLayout = textInputLayoutChangePasswordAuthNewPassword
            val newPasswordConfirmLayout = textInputLayoutChangePasswordAuthNewPasswordConfirm

            // 에러 메시지를 설정하고 포커스와 흔들기 동작을 수행하는 함수
            fun showError(message: String) {
                newPasswordLayout.error = message
                newPassword.requestFocus()
                newPassword.shake()
            }
            fun showConfirmError(message: String) {
                newPasswordConfirmLayout.error = message
                newPasswordConfirm.requestFocus()
                newPasswordConfirm.shake()
            }

            return when{
                newPasswordText.isEmpty() -> {
                    showError("새로운 비밀번호를 입력해주세요.")
                    false
                }
                newPasswordText.length < 8 -> {
                    showError("새로운 비밀번호는 8자 이상이어야 합니다.")
                    false
                }

                newPasswordConfirmText.isEmpty() -> {
                    showConfirmError("새로운 비밀번호 확인을 입력해주세요.")
                    false
                }
                newPasswordText != newPasswordConfirmText -> {
                    showConfirmError("비밀번호가 서로 일치하지 않습니다.")
                    false
                }
                else -> {
                    newPasswordLayout.error = null
                    newPasswordConfirmLayout.error = null
                    true
                }
            }
        }
    }

    // 뷰모델 관찰 설정
    private fun observeViewModel() {
        with(binding) {
            // 새로운 비밀번호 유효성 검사
            viewModel.newPasswordInputError.observe(viewLifecycleOwner) { error ->
                if (error != null) {
                    textInputLayoutChangePasswordAuthNewPassword.error = error.message
                    textInputEditChangePasswordAuthNewPassword.requestFocus()
                    textInputLayoutChangePasswordAuthNewPassword.shake()
                }
            }
            // 새로운 비밀번호 확인 유효성 검사
            viewModel.newPasswordConfirmInputError.observe(viewLifecycleOwner) { error ->
                if (error != null) {
                    textInputLayoutChangePasswordAuthNewPasswordConfirm.error = error.message
                    textInputEditChangePasswordAuthNewPasswordConfirm.requestFocus()
                    textInputLayoutChangePasswordAuthNewPasswordConfirm.shake()
                }
            }
            // 비밀번호 변경 완료
            viewModel.isComplete.observe(viewLifecycleOwner) { isComplete ->
                if (isComplete) {
                    changePasswordCompleteDialog()
                }
            }
        }
    }

    // 유효성 검사 및 버튼 활성화/비활성화 업데이트
    private val inputWatcher = object : TextWatcher {
        // 텍스트가 변경되기 전
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

        // 텍스트가 변경될 때
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            with(binding){
                // 인증하기 버튼 활성화
                buttonChangePasswordAuthDone.isEnabled =
                        // 비밀번호가 비어있지 않을 때
                    !textInputEditChangePasswordAuthNewPassword.text.isNullOrEmpty() && !textInputEditChangePasswordAuthNewPasswordConfirm.text.isNullOrEmpty()
            }
        }
        // 텍스트가 변경된 후
        override fun afterTextChanged(p0: Editable?) { }

    }

    // 비밀번호 변경 완료 다이얼로그
    private fun changePasswordCompleteDialog() {
        viewModel.isCompleteTo(false)
        val dialog = CustomUpdatePasswordDialog(requireContext())
        with(dialog) {
            setTitle("비밀번호 변경")
            setMessage("비밀번호 변경을 완료했습니다")
            setPositiveButton("확인") {
                parentFragmentManager.popBackStack(FragmentName.SETTINGS.str,0)
            }
            show()
        }
    }

    // 뒤로가기 다이얼로그 표시
    private fun showCancelDialog() {
        val dialog = CustomCancelDialog(requireContext())
        with(dialog){
            setTitle("뒤로가기")
            setMessage("변경을 취소하시겠습니까?")
            setPositiveButton("확인") {
                lifecycleScope.launch {
                    parentFragmentManager.popBackStack(FragmentName.SETTINGS.str,0)
                }
            }
            setNegativeButton("취소") {
                dismiss()
            }
            show()
        }
    }

    // 뒤로가기 버튼 처리
    private fun backButton() {
        // 백버튼 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // 뒤로가기 처리
            showCancelDialog()
        }
    }
}
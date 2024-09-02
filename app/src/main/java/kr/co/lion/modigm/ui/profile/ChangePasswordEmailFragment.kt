package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChangePasswordEmailBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.profile.vm.ChangePasswordViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.shake


class ChangePasswordEmailFragment : VBBaseFragment<FragmentChangePasswordEmailBinding>(FragmentChangePasswordEmailBinding::inflate) {

    // 뷰모델
    private val viewModel: ChangePasswordViewModel by viewModels()

    // 태그
    private val logTag by lazy { ChangePasswordEmailFragment::class.simpleName }

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
            textInputEditChangePasswordPassword.addTextChangedListener(inputWatcher)

            // 툴바 설정
            with(toolbarChangePasswordEmail) {
                title = "비밀번호 변경"
                setNavigationIcon(R.drawable.icon_arrow_back_24px)
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

            // 다음 버튼
            with(buttonChangePasswordNext) {
                isEnabled = false // 버튼을 처음에 비활성화
                setOnClickListener {

                    // 유효성 검사
                    if (!checkPasswordInput()) {
                        return@setOnClickListener
                    }
                    val userPassword = textInputEditChangePasswordPassword.text.toString()
                    viewModel.checkPassword(userPassword)
                }
            }
        }
    }

    // 뷰모델 옵저버 설정
    private fun observeViewModel() {
        with(binding){
            // 완료 여부
            viewModel.isCurrentPasswordComplete.observe(viewLifecycleOwner) { isCurrentPasswordComplete ->
                if (isCurrentPasswordComplete) {
                    moveToNext()
                }
            }

            // 비밀번호 에러 메시지
            viewModel.passwordInputError.observe(viewLifecycleOwner) {
                textInputLayoutChangePasswordPassword.error = it?.message
                textInputEditChangePasswordPassword.requestFocus()
                textInputLayoutChangePasswordPassword.shake()
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
                buttonChangePasswordNext.isEnabled =
                        // 비밀번호가 비어있지 않을 때
                    !textInputEditChangePasswordPassword.text.isNullOrEmpty()
            }
        }
        // 텍스트가 변경된 후
        override fun afterTextChanged(p0: Editable?) { }

    }

    // 전화번호 유효성 검사
    private fun checkPasswordInput(): Boolean {
        with(binding) {
            if (textInputEditChangePasswordPassword.text.isNullOrEmpty()) {
                textInputLayoutChangePasswordPassword.error = "전화번호를 입력해주세요."
                textInputEditChangePasswordPassword.requestFocus()
                textInputLayoutChangePasswordPassword.shake()
                return false
            }
            return true
        }
    }

    private fun moveToNext() {
        // 완료 여부는 초기화해서 popStackBack으로 돌아와도 문제 없게
        viewModel.isCurrentPasswordCompleteTo(false)
        val currentUserPhone = viewModel.currentUserPhone.value ?: ""
        val changePasswordAuthFragment = ChangePasswordAuthFragment().apply {
            arguments = Bundle().apply {
                putString("currentUserPhone", currentUserPhone)
            }
        }

        parentFragmentManager.commit {
            replace(R.id.containerMain, changePasswordAuthFragment)
            addToBackStack(FragmentName.CHANGE_PASSWORD_AUTH.str)
        }
    }
}
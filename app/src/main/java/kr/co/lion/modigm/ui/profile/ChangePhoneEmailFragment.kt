package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChangePhoneEmailBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.profile.vm.ChangePhoneViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.shake

class ChangePhoneEmailFragment : VBBaseFragment<FragmentChangePhoneEmailBinding>(FragmentChangePhoneEmailBinding::inflate) {

    private val viewModel: ChangePhoneViewModel by viewModels()

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
//        viewModel.cancelTimer()
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView() {
        with(binding) {

            // 실시간 텍스트 변경 감지 설정
            textInputEditChangePhonePassword.addTextChangedListener(inputWatcher)

            // 툴바 설정
            with(toolbarChangePhone) {
                title = "전화번호 변경"
                setNavigationIcon(R.drawable.icon_arrow_back_24px)
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

            // 다음 버튼
            with(buttonChangePhoneNext) {
                isEnabled = false // 버튼을 처음에 비활성화
                setOnClickListener {

                    // 유효성 검사
                    if (!checkPasswordInput()) {
                        return@setOnClickListener
                    }
                    val userPassword = textInputEditChangePhonePassword.text.toString()
                    viewModel.checkPassword(userPassword)
                }
            }
        }
    }

    // 뷰모델 옵저버 설정
    private fun observeViewModel() {
        with(binding){
            // 완료 여부
            viewModel.isPasswordComplete.observe(viewLifecycleOwner) { isComplete ->
                if (isComplete) {
                    moveToNext()
                }
            }

            // 비밀번호 에러 메시지
            viewModel.passwordInputError.observe(viewLifecycleOwner) {
                textInputLayoutChangePhonePassword.error = it?.message
                textInputEditChangePhonePassword.requestFocus()
                textInputLayoutChangePhonePassword.shake()
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
                buttonChangePhoneNext.isEnabled =
                    // 비밀번호가 비어있지 않을 때
                    !textInputEditChangePhonePassword.text.isNullOrEmpty()
            }
        }
        // 텍스트가 변경된 후
        override fun afterTextChanged(p0: Editable?) { }

    }

    // 전화번호 유효성 검사
    private fun checkPasswordInput(): Boolean {
        with(binding) {
            if (textInputEditChangePhonePassword.text.isNullOrEmpty()) {
                textInputLayoutChangePhonePassword.error = "전화번호를 입력해주세요."
                textInputEditChangePhonePassword.requestFocus()
                textInputLayoutChangePhonePassword.shake()
                return false
            }
            return true
        }
    }

    private fun moveToNext() {
        // 완료 여부는 초기화해서 popStackBack으로 돌아와도 문제 없게
        viewModel.isPasswordCompleteTo(false)
        val currentUserPhone = viewModel.currentUserPhone.value ?: ""
        val changePhoneAuthFragment = ChangePhoneAuthFragment().apply {
            arguments = Bundle().apply {
                putString("currentUserPhone", currentUserPhone)
            }
        }

        parentFragmentManager.commit {
            replace(R.id.containerMain, changePhoneAuthFragment)
            addToBackStack(FragmentName.CHANGE_PHONE_AUTH.str)
        }
    }
}

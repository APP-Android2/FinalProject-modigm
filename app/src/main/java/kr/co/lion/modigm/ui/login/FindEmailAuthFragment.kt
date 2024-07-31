package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindEmailAuthBinding
import kr.co.lion.modigm.ui.ViewBindingFragment
import kr.co.lion.modigm.ui.login.vm.FindEmailViewModel
import kr.co.lion.modigm.util.hideSoftInput
import kr.co.lion.modigm.util.shake

class FindEmailAuthFragment :
    ViewBindingFragment<FragmentFindEmailAuthBinding>(FragmentFindEmailAuthBinding::inflate) {

    private val viewModel: FindEmailViewModel by viewModels()

    private val verificationId by lazy {
        arguments?.getString("verificationId") ?:""
    }

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 뷰모델 클리어 함수 구현 요망
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView() {
        with(binding) {

            // 실시간 텍스트 변경 감지 설정
            binding.textInputEditFindPassCode.addTextChangedListener(inputWatcher)

            // 툴바
            with(toolbarFindEmailAuth) {
                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

            // 완료 버튼
            with(buttonFindEmailAuthOK) {
                isEnabled = false // 버튼을 처음에 비활성화
                setOnClickListener {
                    requireActivity().hideSoftInput()

                    // 유효성 검사
                    if (!validateInput()) {
                        return@setOnClickListener
                    }
                    // 인증번호 확인
                    val inputCode = textInputEditFindPassCode.text.toString()
                    Log.d("FindEmailAuthFragment", "완료 버튼 클릭됨. inputCode: $inputCode")
                    viewModel.checkCodeAndFindEmail(verificationId, inputCode)
                }
            }
        }
    }

    // 유효성 검사
    private fun validateInput(): Boolean {
        with(binding) {
            if (textInputEditFindPassCode.text.isNullOrEmpty()) {
                textInputLayoutFindPassCode.error = "인증번호를 입력해주세요."
                textInputEditFindPassCode.requestFocus()
                textInputLayoutFindPassCode.shake()
                return false
            }
            return true
        }
    }

    private fun observeViewModel() {

        with(binding){
            // 유효성 검사
            viewModel.inputCodeError.observe(viewLifecycleOwner) { error ->
                textInputLayoutFindPassCode.error = error.message
                textInputEditFindPassCode.requestFocus()
                textInputLayoutFindPassCode.shake()
            }
            // 인증번호 확인해서 메일 찾았는지 여부
            viewModel.email.observe(viewLifecycleOwner) {
                if (!it.isNullOrEmpty()) {
                    // 다이얼로그
                    showFindEmailDialog(viewModel.email.value?:"")
                }
                buttonFindEmailAuthOK.isEnabled = true
            }
        }

    }

    // 이메일 다이얼로그 표시
    private fun showFindEmailDialog(email: String) {
        val dialog = CustomFindEmailDialog(requireContext())
        dialog.setTitle("이메일 찾기")
        dialog.setEmail("$email 입니다.")
        dialog.setPositiveButton("확인") {
            // Handle button click
            parentFragmentManager.commit {
                replace(R.id.containerMain, OtherLoginFragment())
            }
        }
        dialog.show()
    }

    // 유효성 검사 및 버튼 활성화/비활성화 업데이트
    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            with(binding){
                buttonFindEmailAuthOK.isEnabled =
                    !textInputEditFindPassCode.text.isNullOrEmpty()
            }

        }

        override fun afterTextChanged(p0: Editable?) { }
    }

}

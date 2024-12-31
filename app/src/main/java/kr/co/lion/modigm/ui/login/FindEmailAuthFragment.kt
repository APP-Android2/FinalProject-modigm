package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.databinding.FragmentFindEmailAuthBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.login.vm.FindEmailViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.hideSoftInput
import kr.co.lion.modigm.util.shake

class FindEmailAuthFragment : VBBaseFragment<FragmentFindEmailAuthBinding>(FragmentFindEmailAuthBinding::inflate) {

    // 뷰모델
    private val viewModel: FindEmailViewModel by viewModels()

    // 태그
    private val logTag by lazy { FindEmailAuthFragment::class.simpleName }

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

        viewModel.clearData() // 뷰모델 클리어 함수
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView() {
        with(binding) {
            // 실시간 텍스트 변경 감지 설정
            textInputEditFindAuthCode.addTextChangedListener(inputWatcher)

            // 툴바
            with(toolbarFindEmailAuth) {
                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {

                    // 취소 다이얼로그
                    showCancelDialog()
                }
            }
            // 완료 버튼
            with(buttonFindEmailAuthOK) {
                isEnabled = false // 버튼을 처음에 비활성화
                setOnClickListener {
                    requireActivity().hideSoftInput()

                    // 유효성 검사
                    if (!checkInput()) {
                        return@setOnClickListener
                    }
                    // 인증번호 확인
                    val authCode = textInputEditFindAuthCode.text.toString()
                    viewModel.checkCodeAndFindEmail(verificationId, authCode)
                }
            }
        }
    }

    // 유효성 검사
    private fun checkInput(): Boolean {
        with(binding) {
            if (textInputEditFindAuthCode.text.isNullOrEmpty()) {
                textInputLayoutFindAuthCode.error = "인증번호를 입력해주세요."
                textInputEditFindAuthCode.requestFocus()
                textInputLayoutFindAuthCode.shake()
                return false
            }
            return true
        }
    }

    private fun observeViewModel() {
        with(binding){
            // 유효성 검사
            viewModel.authCodeInputError.observe(viewLifecycleOwner) { error ->
                if (error != null) {
                    textInputLayoutFindAuthCode.error = error.message
                    textInputEditFindAuthCode.requestFocus()
                    textInputLayoutFindAuthCode.shake()
                }
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

    // 유효성 검사 및 버튼 활성화/비활성화 업데이트
    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            with(binding){
                buttonFindEmailAuthOK.isEnabled =
                    !textInputEditFindAuthCode.text.isNullOrEmpty()
            }
        }
        override fun afterTextChanged(p0: Editable?) { }
    }

    // 이메일 다이얼로그 표시
    private fun showFindEmailDialog(email: String) {
        val dialog = CustomFindEmailDialog(requireContext())
        with(dialog){
            setTitle("이메일 찾기")
            setEmail("$email 입니다.")
            setPositiveButton("확인") {
                parentFragmentManager.popBackStack(FragmentName.EMAIL_LOGIN.str,0)
            }
            show()
        }
    }

    // 뒤로가기 다이얼로그 표시
    private fun showCancelDialog() {
        val dialog = CustomCancelDialog(requireContext())
        with(dialog){
            setTitle("뒤로가기")
            setPositiveButton("확인") {
                lifecycleScope.launch {
                    viewModel.authLogout()
                    parentFragmentManager.popBackStack(FragmentName.EMAIL_LOGIN.str,0)
                }
            }
            setNegativeButton("취소") {
                dismiss()
            }
            show()
        }
    }
}

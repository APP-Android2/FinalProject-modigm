package kr.co.lion.modigm.ui.login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentOtherLoginBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.hideSoftInput
import kr.co.lion.modigm.util.shake

class OtherLoginFragment : VBBaseFragment<FragmentOtherLoginBinding>(FragmentOtherLoginBinding::inflate) {

    private val viewModel: LoginViewModel by viewModels()  // LoginViewModel 인스턴스 생성

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView() // 초기 UI 설정
        observeViewModel() // ViewModel의 데이터 변경 관찰
    }

    override fun onResume() {
        super.onResume()

        // 이메일 텍스트 필드 포커싱 및 소프트키보드 보여주기
        with(binding.textInputEditOtherEmail) {
            requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.clearData() // ViewModel 데이터 초기화
    }

    // --------------------------------- LC END ---------------------------------

    private fun initView() {
        with(binding){

            // 실시간 텍스트 변경 감지 설정
            textInputEditOtherEmail.addTextChangedListener(inputWatcher)
            textInputEditOtherPassword.addTextChangedListener(inputWatcher)

            // 로그인 버튼 초기값을 비활성화 상태로 설정
            buttonOtherLogin.isEnabled = false

            // 로그인 버튼 클릭 시 로그인 시도
            buttonOtherLogin.setOnClickListener {
                if(!checkAllInput()) {
                    return@setOnClickListener
                }
                requireActivity().hideSoftInput()
                val email = textInputEditOtherEmail.text.toString()
                val password = textInputEditOtherPassword.text.toString()
                val autoLogin = checkBoxOtherAutoLogin.isChecked
                viewModel.emailLogin(email, password, autoLogin)
            }

            // 회원가입 버튼 클릭 시 회원가입 화면으로 이동
            buttonOtherJoin.setOnClickListener {
                val joinType = JoinType.EMAIL
                navigateToJoinFragment(joinType)
            }

            // 이메일 찾기 버튼 클릭 시
            buttonOtherFindEmail.setOnClickListener {
                parentFragmentManager.commit {
                    replace<FindEmailFragment>(R.id.containerMain)
                    addToBackStack(FragmentName.FIND_EMAIL.str)
                }
            }

            // 비밀번호 찾기 버튼 클릭 시
            buttonOtherFindPassword.setOnClickListener {
                parentFragmentManager.commit {
                    replace<FindPasswordFragment>(R.id.containerMain)
                    addToBackStack(FragmentName.FIND_PW.str)
                }
            }
            // 돌아가기 버튼 클릭 시
            buttonOtherBack.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    // ViewModel의 데이터 변경을 관찰하여 UI 업데이트
    private fun observeViewModel() {
        // 이메일 로그인 데이터 관찰
        viewModel.emailLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Log.i("LoginFragment", "이메일 로그인 성공")
                val joinType = JoinType.EMAIL
                navigateToBottomNaviFragment(joinType)
            }
        }
        // 이메일 로그인 실패 시 에러 처리
        viewModel.emailDialogError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showErrorDialog(error)
            }
        }
        viewModel.emailInputError.observe(viewLifecycleOwner) { error ->
            with(binding) {
                if (error != null) {
                    textInputLayoutOtherEmail.error = error.message
                    textInputEditOtherEmail.requestFocus()
                    textInputLayoutOtherEmail.shake()
                }
            }
        }
    }

    /**
     * 로그인 오류 처리 메서드
     * @param e 발생한 오류
     */
    private fun showErrorDialog(e: Throwable) {
        val message = if (e.message != null) {
            e.message.toString()
        } else {
            "알 수 없는 오류!\n코드번호: 9999"
        }
        showLoginErrorDialog(message)
    }

    // 회원가입 화면으로 이동하는 메소드
    private fun navigateToJoinFragment(joinType: JoinType) {
        Log.d("LoginFragment", "navigateToJoinFragment - joinType: ${joinType.provider}")

        val bundle = Bundle().apply {
            putString("joinType", joinType.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, JoinFragment().apply { arguments = bundle })
            addToBackStack(FragmentName.JOIN.str)
        }
    }

    private fun navigateToBottomNaviFragment(joinType: JoinType) {

        val bundle = Bundle().apply {
            putString("joinType", joinType.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, BottomNaviFragment().apply { arguments = bundle })
            addToBackStack(FragmentName.BOTTOM_NAVI.str)
        }
    }

    // 오류 다이얼로그 표시
    private fun showLoginErrorDialog(message: String) {
        val dialog = CustomLoginErrorDialog(requireContext())
        dialog.setTitle("오류")
        dialog.setMessage(message)
        dialog.setPositiveButton("확인") {
            dialog.dismiss()
        }
        dialog.show()
    }

    // 유효성 검사
    private fun checkAllInput(): Boolean {
        return checkEmail() && checkPassword()
    }

    private fun checkEmail(): Boolean {
        with(binding){
            val emailEditText = textInputEditOtherEmail
            val emailInputLayout = textInputLayoutOtherEmail
            val emailText = emailEditText.text.toString()

            // 에러 메시지를 설정하고 포커스와 흔들기 동작을 수행하는 함수
            fun showError(message: String) {
                emailInputLayout.error = message
                emailEditText.requestFocus()
                emailEditText.shake()
            }
            return when {
                emailText.isEmpty() -> {
                    showError("이메일을 입력해주세요.")
                    false
                }
                !isEmailValid(emailText) -> {
                    showError("올바른 이메일을 입력해주세요.")
                    false
                }
                else -> {
                    emailInputLayout.error = null
                    true
                }
            }
        }
    }

    private fun checkPassword(): Boolean {
        with(binding) {
            val passwordEditText = textInputEditOtherPassword
            val passwordInputLayout = textInputLayoutOtherPassword
            val passwordText = passwordEditText.text.toString()

            // 에러 메시지를 설정하고 포커스와 흔들기 동작을 수행하는 함수
            fun showError(message: String) {
                passwordInputLayout.error = message
                passwordEditText.requestFocus()
                passwordEditText.shake()
            }
            return when {
                passwordText.isEmpty() -> {
                    showError("비밀번호를 입력해주세요.")
                    false
                }
                !isPasswordValid(passwordText) -> {
                    showError("올바른 비밀번호를 입력해주세요.")
                    false
                }
                else -> {
                    passwordInputLayout.error = null
                    true
                }
            }
        }
    }

    // 이메일 유효성을 검사하는 함수
    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    // 비밀번호 유효성을 검사하는 함수
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    // 유효성 검사 및 버튼 활성화/비활성화 업데이트
    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            with(binding){
                buttonOtherLogin.isEnabled =
                    !textInputEditOtherEmail.text.isNullOrEmpty() && !textInputEditOtherPassword.text.isNullOrEmpty()
            }
        }
        override fun afterTextChanged(p0: Editable?) { }
    }
}

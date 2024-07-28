package kr.co.lion.modigm.ui.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentOtherLoginBinding
import kr.co.lion.modigm.ui.BaseFragment
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.hideSoftInput
import kr.co.lion.modigm.util.showLoginSnackBar

class OtherLoginFragment : BaseFragment<FragmentOtherLoginBinding>(FragmentOtherLoginBinding::inflate) {

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
            // 로그인 버튼 초기값을 비활성화 상태로 설정
            buttonOtherLogin.isEnabled = false

            // 이메일 필드 포커스 변경 시 유효성 검사
            textInputEditOtherEmail.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    validateEmail() // 포커스가 떠날 때 이메일 유효성 검사
                }
            }

            // 비밀번호 필드 포커스 변경 시 유효성 검사
            textInputEditOtherPassword.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    validatePassword() // 포커스가 떠날 때 비밀번호 유효성 검사
                }
            }

            // 이메일 입력 중 에러 메시지 제거 및 유효성 검사
            textInputEditOtherEmail.addTextChangedListener {
                clearEmailError()
                validateLoginForm()
            }

            // 비밀번호 입력 중 에러 메시지 제거 및 유효성 검사
            textInputEditOtherPassword.addTextChangedListener {
                clearPasswordError()
                validateLoginForm()
            }

            // 로그인 버튼 클릭 시 로그인 시도
            buttonOtherLogin.setOnClickListener {
                requireActivity().hideSoftInput()
                val email = textInputEditOtherEmail.text.toString()
                val password = textInputEditOtherPassword.text.toString()
                val autoLogin = checkBoxOtherAutoLogin.isChecked
                Log.d("OtherLoginFragment", "autoLogin : $autoLogin")
                viewModel.emailLogin(email, password, autoLogin)
            }

            // 회원가입 버튼 클릭 시 회원가입 화면으로 이동
            buttonOtherJoin.setOnClickListener {
                handleJoinClick()
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
                    replace<FindPwFragment>(R.id.containerMain)
                    addToBackStack(FragmentName.FIND_PW.str)
                }
            }
            // 돌아가기 버튼 클릭 시
            buttonOtherBack.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    // 이메일 유효성 검사
    private fun validateEmail() {
        with(binding){
            val email = textInputEditOtherEmail.text.toString()
            if (email.isEmpty()) {
                textInputLayoutOtherEmail.error = "이메일을 입력해주세요"
            } else if (!viewModel.isEmailValid(email)) {
                textInputLayoutOtherEmail.error = "형식에 맞는 이메일을 입력해주세요"
            } else {
                textInputLayoutOtherEmail.error = null
            }
        }
        
    }

    // 비밀번호 유효성 검사
    private fun validatePassword() {
        with(binding){
            val password = textInputEditOtherPassword.text.toString()
            if (password.isEmpty()) {
                textInputLayoutOtherPassword.error = "비밀번호를 입력해주세요"
            } else if (!viewModel.isPasswordValid(password)) {
                textInputLayoutOtherPassword.error = "비밀번호는 6자리 이상 입력해주세요"
            } else {
                textInputLayoutOtherPassword.error = null
            }
        }
    }

    // 로그인 폼 유효성 검사 및 로그인 버튼 활성화
    private fun validateLoginForm() {
        with(binding){
            val email = textInputEditOtherEmail.text.toString()
            val password = textInputEditOtherPassword.text.toString()
            val isEmailValid = viewModel.isEmailValid(email)
            val isPasswordValid = viewModel.isPasswordValid(password)
            buttonOtherLogin.isEnabled = isEmailValid && isPasswordValid
        }

    }

    // 이메일 에러 메시지 제거
    private fun clearEmailError() {
        with(binding){
            textInputLayoutOtherEmail.error = null
        }

    }

    // 비밀번호 에러 메시지 제거
    private fun clearPasswordError() {
        with(binding){
            textInputLayoutOtherPassword.error = null
        }

    }

    // ViewModel의 데이터 변경을 관찰하여 UI 업데이트
    private fun observeViewModel() {
        // 이메일 로그인 데이터 관찰
        viewModel.emailLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Log.i("LoginFragment", "이메일 로그인 성공")
                requireActivity().showLoginSnackBar("이메일 로그인 성공", R.drawable.email_login_logo)
                navigateToBottomNaviFragment()
            }
        }
        // 이메일 로그인 실패 시 에러 처리
        viewModel.emailLoginError.observe(viewLifecycleOwner) { e ->
            handleLoginError(e)
        }
    }

    // 로그인 오류 처리 메서드
    private fun handleLoginError(e: Throwable) {
        val message = if (e is LoginError) {
            e.getFullMessage()
        } else {
            "알 수 없는 오류!\n코드번호: 9999"
        }
        requireActivity().showLoginSnackBar(message, R.drawable.icon_error_24px)
    }

    // 회원가입 버튼 클릭 시 호출되는 메소드
    private fun handleJoinClick() {
        val joinType = JoinType.EMAIL
        navigateToJoinFragment(joinType)
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

    private fun navigateToBottomNaviFragment() {
        parentFragmentManager.commit {
            replace(R.id.containerMain, BottomNaviFragment())
            addToBackStack(FragmentName.BOTTOM_NAVI.str)
        }
    }
}

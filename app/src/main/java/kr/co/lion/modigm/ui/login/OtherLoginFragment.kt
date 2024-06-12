package kr.co.lion.modigm.ui.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentOtherLoginBinding
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.vm.LoginResult
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.hideSoftInput
import kr.co.lion.modigm.util.showCustomSnackbar

class OtherLoginFragment : Fragment(R.layout.fragment_other_login) { // 이 줄을 추가하여 올바른 레이아웃을 지정합니다.

    private val viewModel: LoginViewModel by viewModels() // LoginViewModel 인스턴스 생성
    private lateinit var binding: FragmentOtherLoginBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentOtherLoginBinding.bind(view) // view에서 binding을 초기화합니다.
        initView(binding) // 초기 UI 설정
        observeViewModel(binding) // ViewModel의 데이터 변경 관찰
    }

    override fun onResume() {
        super.onResume()

        // 이메일 텍스트 필드 포커싱 및 소프트키보드 보여주기
        binding.textInputEditOtherEmail.requestFocus()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.textInputEditOtherEmail, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun initView(binding: FragmentOtherLoginBinding) {
        // 로그인 버튼 초기값을 비활성화 상태로 설정
        binding.buttonOtherLogin.isEnabled = false

        // 이메일 필드 포커스 변경 시 유효성 검사
        binding.textInputEditOtherEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateEmail(binding) // 포커스가 떠날 때 이메일 유효성 검사
            }
        }

        // 비밀번호 필드 포커스 변경 시 유효성 검사
        binding.textInputEditOtherPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validatePassword(binding) // 포커스가 떠날 때 비밀번호 유효성 검사
            }
        }

        // 이메일 입력 중 에러 메시지 제거
        binding.textInputEditOtherEmail.addTextChangedListener {
            clearEmailError(binding)
            viewModel.loginDataChanged(
                binding.textInputEditOtherEmail.text.toString(),
                binding.textInputEditOtherPassword.text.toString()
            )
        }

        // 비밀번호 입력 중 에러 메시지 제거
        binding.textInputEditOtherPassword.addTextChangedListener {
            clearPasswordError(binding)
            viewModel.loginDataChanged(
                binding.textInputEditOtherEmail.text.toString(),
                binding.textInputEditOtherPassword.text.toString()
            )
        }

        // 로그인 버튼 클릭 시 로그인 시도
        binding.buttonOtherLogin.setOnClickListener {
            requireActivity().hideSoftInput()
            val email = binding.textInputEditOtherEmail.text.toString()
            val password = binding.textInputEditOtherPassword.text.toString()
            val autoLogin = binding.checkBoxOtherAutoLogin.isChecked
            Log.d("OtherLoginFragment", "autoLogin : $autoLogin")
            viewModel.login(email, password, autoLogin)
        }

        // 회원가입 버튼 클릭 시 회원가입 화면으로 이동
        binding.buttonOtherJoin.setOnClickListener {
            handleJoinClick()
        }

        // 이메일 찾기 버튼 클릭 시
        binding.buttonOtherFindEmail.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.containerMain, FindEmailFragment())
                addToBackStack(FragmentName.FIND_EMAIL.str)
            }
        }

        // 비밀번호 찾기 버튼 클릭 시
        binding.buttonOtherFindPassword.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.containerMain, FindPwFragment())
                addToBackStack(FragmentName.FIND_PW.str)
            }
        }
        // 돌아가기 버튼 클릭 시
        binding.buttonOtherBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    // 이메일 유효성 검사
    private fun validateEmail(binding: FragmentOtherLoginBinding) {
        val email = binding.textInputEditOtherEmail.text.toString()
        if (email.isEmpty()) {
            binding.textInputLayoutOtherEmail.error = "이메일을 입력해주세요"
        } else if (!viewModel.isEmailValid(email)) {
            binding.textInputLayoutOtherEmail.error = "형식에 맞는 이메일을 입력해주세요"
        } else {
            binding.textInputLayoutOtherEmail.error = null
        }
        viewModel.loginDataChanged(email, binding.textInputEditOtherPassword.text.toString())
    }

    // 비밀번호 유효성 검사
    private fun validatePassword(binding: FragmentOtherLoginBinding) {
        val password = binding.textInputEditOtherPassword.text.toString()
        if (password.isEmpty()) {
            binding.textInputLayoutOtherPassword.error = "비밀번호를 입력해주세요"
        } else if (!viewModel.isPasswordValid(password)) {
            binding.textInputLayoutOtherPassword.error = "비밀번호는 6자리 이상 입력해주세요"
        } else {
            binding.textInputLayoutOtherPassword.error = null
        }
        viewModel.loginDataChanged(binding.textInputEditOtherEmail.text.toString(), password)
    }

    // 이메일 에러 메시지 제거
    private fun clearEmailError(binding: FragmentOtherLoginBinding) {
        binding.textInputLayoutOtherEmail.error = null
    }

    // 비밀번호 에러 메시지 제거
    private fun clearPasswordError(binding: FragmentOtherLoginBinding) {
        binding.textInputLayoutOtherPassword.error = null
    }

    // ViewModel의 데이터 변경을 관찰하여 UI 업데이트
    private fun observeViewModel(binding: FragmentOtherLoginBinding) {
        viewModel.loginFormState.observe(viewLifecycleOwner, Observer { loginState ->
            if (loginState != null) {
                binding.buttonOtherLogin.isEnabled = loginState.isDataValid
                if (loginState.emailError != null && !binding.textInputEditOtherEmail.hasFocus()) {
                    binding.textInputLayoutOtherEmail.error = loginState.emailError
                }
                if (loginState.passwordError != null && !binding.textInputEditOtherPassword.hasFocus()) {
                    binding.textInputLayoutOtherPassword.error = loginState.passwordError
                }
            }
        })

        // 이메일 로그인 데이터 관찰
        viewModel.emailLoginResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is LoginResult.Loading -> {
                    Log.i("LoginFragment", "이메일 로그인 진행 중...")
                }
                is LoginResult.Success -> {
                    Log.i("LoginFragment", "이메일 로그인 성공")
                    // 커스텀 스낵바 메시지 추가
                    requireActivity().showCustomSnackbar("이메일 로그인 성공", R.drawable.email_login_logo)
                    // 메인 화면으로 이동
                    navigateToBottomNaviFragment()
                }
                is LoginResult.NeedSignUp -> {
                    Log.i("LoginFragment", "이메일 로그인 성공, 회원가입 필요")
                    val joinType = viewModel.joinType.value ?: JoinType.EMAIL
                    navigateToJoinFragment(joinType) // 이메일 로그인은 별도의 토큰이 필요 없음
                }
                is LoginResult.Error -> {
                    Log.e("LoginFragment", "이메일 로그인 실패", result.exception)
                }
            }
        })
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

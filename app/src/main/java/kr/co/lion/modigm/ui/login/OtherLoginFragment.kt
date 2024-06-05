package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.util.Log
import android.view.View
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
import kr.co.lion.modigm.util.JoinType

class OtherLoginFragment : Fragment(R.layout.fragment_other_login) {

    private val viewModel: LoginViewModel by viewModels() // LoginViewModel 인스턴스 생성

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOtherLoginBinding.bind(view) // 뷰 바인딩 설정

        setupUI(binding) // 초기 UI 설정
        observeViewModel(binding) // ViewModel의 데이터 변경 관찰
    }

    // 초기 UI 설정 메소드
    private fun setupUI(binding: FragmentOtherLoginBinding) {
        // 이메일 필드 포커스 변경 시 유효성 검사
        binding.textInputEditOtherEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateInput(binding)
        }

        // 비밀번호 필드 포커스 변경 시 유효성 검사
        binding.textInputEditOtherPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateInput(binding)
        }

        // 로그인 버튼 클릭 리스너 설정
        binding.buttonOtherLogin.setOnClickListener {
            val email = binding.textInputEditOtherEmail.text.toString()
            val password = binding.textInputEditOtherPassword.text.toString()
            viewModel.login(email, password)
        }

        // 회원가입 버튼 클릭 리스너 설정
        binding.buttonOtherJoin.setOnClickListener {
            handleJoinClick()
        }
    }

    // ViewModel의 데이터 변경을 관찰하는 메소드
    private fun observeViewModel(binding: FragmentOtherLoginBinding) {
        viewModel.loginFormState.observe(viewLifecycleOwner, Observer { loginState ->
            if (loginState != null) {
                binding.buttonOtherLogin.isEnabled = loginState.isDataValid
                binding.textInputLayoutOtherEmail.error = loginState.emailError
                binding.textInputLayoutOtherPassword.error = loginState.passwordError
            }
        })

        viewModel.loginResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is LoginResult.Success -> {
                    navigateToMain()
                }
                is LoginResult.Error -> {
                    Log.e("OtherLoginFragment", "로그인 실패", result.exception)
                }
                is LoginResult.Loading -> {
                    // 로딩시 보여줄 인디케이터 등
                }
            }
        })
    }

    // 입력 데이터 유효성 검사를 수행하는 메소드
    private fun validateInput(binding: FragmentOtherLoginBinding) {
        val email = binding.textInputEditOtherEmail.text.toString()
        val password = binding.textInputEditOtherPassword.text.toString()
        viewModel.loginDataChanged(email, password)
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
            addToBackStack(null)
        }
    }

    // 메인 화면으로 이동하는 메소드
    private fun navigateToMain() {
        parentFragmentManager.commit {
            replace(R.id.containerMain, BottomNaviFragment())
            addToBackStack(null)
        }
    }
}

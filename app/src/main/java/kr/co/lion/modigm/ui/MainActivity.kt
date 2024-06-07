package kr.co.lion.modigm.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.login.LoginFragment
import kr.co.lion.modigm.ui.login.vm.LoginResult
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.util.FragmentName
import android.util.Log

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "onCreate: MainActivity 시작")

        // 앱이 시작될 때 자동 로그인 시도
        Log.d("MainActivity", "onCreate: 자동 로그인 시도 중")
        loginViewModel.attemptAutoLogin(this)

        // 자동 로그인에 성공 시 loginResult를 관찰하여 메인 화면으로 이동
        observeLoginResults()
    }

    private fun observeLoginResults() {
        loginViewModel.loginResult.observe(this) { result ->
            handleLoginResult(result)
        }

        loginViewModel.kakaoLoginResult.observe(this) { result ->
            handleLoginResult(result)
        }

        loginViewModel.githubLoginResult.observe(this) { result ->
            handleLoginResult(result)
        }
    }

    private fun handleLoginResult(result: LoginResult) {
        when (result) {
            is LoginResult.Success -> {
                // 로그인 성공 시
                Log.d("MainActivity", "loginResult: 성공 - BottomNaviFragment로 이동")
                navigateToBottomNaviFragment()
            }
            is LoginResult.Error -> {
                // 로그인 에러 시
                Log.e("MainActivity", "loginResult: 오류 - LoginFragment로 이동", result.exception)
                navigateToLoginFragment()
            }
            is LoginResult.Loading -> {
                // 로그인 로딩 시
                Log.d("MainActivity", "loginResult: 로딩 중")
            }
            LoginResult.NeedSignUp -> {
                // 회원가입이 필요할 경우
                Log.d("MainActivity", "loginResult: 회원가입 필요")
            }
        }
    }

    private fun navigateToBottomNaviFragment() {
        supportFragmentManager.commit {
            replace(R.id.containerMain, BottomNaviFragment())
            addToBackStack(FragmentName.BOTTOM_NAVI.str)
        }
    }

    private fun navigateToLoginFragment() {
        supportFragmentManager.commit {
            replace(R.id.containerMain, LoginFragment())
            addToBackStack(FragmentName.LOGIN.str)
        }
    }
}

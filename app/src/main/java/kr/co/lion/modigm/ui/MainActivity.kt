package kr.co.lion.modigm.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.login.LoginFragment
import kr.co.lion.modigm.ui.login.vm.LoginResult
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.ModigmApplication
import kr.co.lion.modigm.util.showCustomSnackbar
//import android.util.Log
import kr.co.lion.modigm.ui.write.WriteFragment


class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity1", "onCreate: MainActivity 시작")

        ModigmApplication.prefs.logAllPreferences()
        observeLoginResults()

        Log.d("MainActivity1", "onCreate: 자동 로그인 시도 중")
        loginViewModel.attemptAutoLogin(this)
    }

    private fun observeLoginResults() {
        loginViewModel.emailLoginResult.observe(this) { result ->
            emailHandleLoginResult(result)
        }

        loginViewModel.kakaoLoginResult.observe(this) { result ->
            kakaoHandleLoginResult(result)
        }

        loginViewModel.githubLoginResult.observe(this) { result ->
            githubHandleLoginResult(result)
        }

        loginViewModel.autoLoginResult.observe(this) { result ->
            autoHandleLoginResult(result)
        }
    }
    // 이메일 로그인 핸들
    private fun emailHandleLoginResult(result: LoginResult) {
        when (result) {
            is LoginResult.Success -> {
                // 로그인 성공 시
                Log.d("MainActivity1", "이메일 로그인 성공 - BottomNaviFragment로 이동")
                showCustomSnackbar("이메일 로그인 성공", R.drawable.email_login_logo)
                navigateToBottomNaviFragment()
            }
            is LoginResult.Error -> {
                // 로그인 에러 시
                Log.e("MainActivity1", "이메일 로그인 오류", result.exception)
                navigateToLoginFragment()
            }
            is LoginResult.Loading -> {
                // 로그인 로딩 시
                Log.d("MainActivity1", "이메일 로그인 로딩 중")
            }
            LoginResult.NeedSignUp -> {
                // 회원가입이 필요할 경우
                Log.d("MainActivity1", "이메일 로그인 회원가입 필요")
            }
        }
    }

    // 카카오 로그인 핸들
    private fun kakaoHandleLoginResult(result: LoginResult) {
        when (result) {
            is LoginResult.Success -> {
                // 로그인 성공 시
                Log.d("MainActivity1", "카카오 로그인 성공 - BottomNaviFragment로 이동")
                showCustomSnackbar("카카오 로그인 성공", R.drawable.kakaotalk_sharing_btn_small)
                navigateToBottomNaviFragment()
            }
            is LoginResult.Error -> {
                // 로그인 에러 시
                Log.e("MainActivity1", "loginResult: 오류", result.exception)
                navigateToLoginFragment()
            }
            is LoginResult.Loading -> {
                // 로그인 로딩 시
                Log.d("MainActivity1", "loginResult: 로딩 중")
            }
            LoginResult.NeedSignUp -> {
                // 회원가입이 필요할 경우
                Log.d("MainActivity1", "loginResult: 회원가입 필요")
            }
        }
    }

    // 깃허브 로그인 핸들
    private fun githubHandleLoginResult(result: LoginResult) {
        when (result) {
            is LoginResult.Success -> {
                // 로그인 성공 시
                Log.d("MainActivity1", "깃허브 로그인 성공 - BottomNaviFragment로 이동")
                showCustomSnackbar("깃허브 로그인 성공", R.drawable.icon_github_logo)
                navigateToBottomNaviFragment()
            }
            is LoginResult.Error -> {
                // 로그인 에러 시
                Log.e("MainActivity1", "깃허브 로그인 오류", result.exception)
                navigateToLoginFragment()
            }
            is LoginResult.Loading -> {
                // 로그인 로딩 시
                Log.d("MainActivity1", "깃허브 로그인 로딩 중")
            }
            LoginResult.NeedSignUp -> {
                // 회원가입이 필요할 경우
                Log.d("MainActivity1", "깃허브 로그인 회원가입 필요")
            }
        }
    }


    // 자동 로그인 핸들
    private fun autoHandleLoginResult(result: LoginResult) {
        when (result) {
            is LoginResult.Success -> {
                // 로그인 성공 시
                Log.d("MainActivity1", "자동로그인 성공 - BottomNaviFragment로 이동")
                showCustomSnackbar("자동 로그인 성공", R.drawable.icon_error_24px)
                navigateToBottomNaviFragment()
            }
            is LoginResult.Error -> {
                // 로그인 에러 시
                Log.e("MainActivity1", "자동 로그인 오류", result.exception)
                navigateToLoginFragment()
            }
            is LoginResult.Loading -> {
                // 로그인 로딩 시
                Log.d("MainActivity1", "자동 로그인 로딩 중")
            }
            LoginResult.NeedSignUp -> {
                // 회원가입이 필요할 경우
                Log.d("MainActivity1", "자동 로그인 회원가입 필요")
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

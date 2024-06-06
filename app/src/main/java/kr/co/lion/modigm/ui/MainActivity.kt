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

        Log.d("MainActivity1", "onCreate: Starting MainActivity")

        // 앱이 시작될 때 자동 로그인 시도
        Log.d("MainActivity1", "onCreate: Attempting auto login")
        loginViewModel.attemptAutoLogin(this)

        // 자동 로그인에 성공 시 loginResult를 관찰하여 메인 화면으로 이동
        loginViewModel.loginResult.observe(this) { result ->
            when (result) {
                is LoginResult.Success -> {
                    // 로그인 성공 시
                    Log.d("MainActivity1", "loginResult: Success - Navigating to BottomNaviFragment")
                    supportFragmentManager.commit {
                        replace(R.id.containerMain, BottomNaviFragment())
                        addToBackStack(FragmentName.BOTTOM_NAVI.str)
                    }
                }
                is LoginResult.Error -> {
                    // 로그인 에러 시
                    Log.e("MainActivity1", "loginResult: Error - Navigating to LoginFragment", result.exception)
                    supportFragmentManager.commit {
                        replace(R.id.containerMain, LoginFragment())
                        addToBackStack(FragmentName.LOGIN.str)
                    }
                }
                is LoginResult.Loading -> {
                    // 로그인 로딩 시
                    Log.d("MainActivity1", "loginResult: Loading")
                }
            }
        }
    }
}

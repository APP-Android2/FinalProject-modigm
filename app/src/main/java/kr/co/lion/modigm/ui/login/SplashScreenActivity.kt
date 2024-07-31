package kr.co.lion.modigm.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.lion.modigm.databinding.ActivitySplashScreenBinding
import kr.co.lion.modigm.ui.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private val binding: ActivitySplashScreenBinding by lazy {
        ActivitySplashScreenBinding.inflate(layoutInflater)
    }

    // --------------------------------- LC START ---------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 바인딩
        setContentView(binding.root)

        showSplashScreen()
    }

    // --------------------------------- LC END ---------------------------------

    private fun showSplashScreen() {
        lifecycleScope.launch {
            // 안드로이드 12 미만에서는 스플래시 스크린 보여주기
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
                delay(1200)
            }
            // 안드로이드 12 이상에서는 기본 스플래시가 있으므로 바로 화면 전환.
            val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

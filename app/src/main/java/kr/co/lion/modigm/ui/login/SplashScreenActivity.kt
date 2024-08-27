package kr.co.lion.modigm.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
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

        // 로그인 배경 이미지 프리(미리)로드 하기
        Glide.with(this)
            .load(R.drawable.background_login2)
            .transform(CenterCrop(), BlurTransformation(5, 3), ColorFilterTransformation(0x60000000))
            .preload()

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

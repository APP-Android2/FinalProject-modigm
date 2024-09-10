package kr.co.lion.modigm.ui

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.ActivityMainBinding
import kr.co.lion.modigm.db.HikariCPDataSource
import kr.co.lion.modigm.ui.login.LoginFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // 바인딩
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    // --------------------------------- LC START ---------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isChromeOS(this)) {
            // Chrome OS가 아닌 장치에서는 세로 모드로 고정
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // 바인딩
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("MainActivity1", "onCreate: MainActivity 시작")

        // 백스택 변경 감지 리스너 추가 (필터 입력: FragmentBackStackLog)
        supportFragmentManager.addOnBackStackChangedListener {
            logFragmentBackStack()
        }
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<LoginFragment>(R.id.containerMain)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        // 애플리케이션 종료 시 히카리CP 데이터 소스를 해제
        CoroutineScope(Dispatchers.IO).launch {
            HikariCPDataSource.closeDataSource()
        }
    }

    // --------------------------------- LC END ---------------------------------

    // 백스택 로그 출력 함수
    private fun logFragmentBackStack() {
        val fragmentManager = supportFragmentManager
        val count = fragmentManager.backStackEntryCount
        Log.d("FragmentBackStackLog", "현재 백스택에 있는 프래그먼트 수: $count")
        for (i in 0 until count) {
            val entry = fragmentManager.getBackStackEntryAt(i)
            Log.d("FragmentBackStackLog", "백스택 ${i}: ${entry.name}")
        }
    }

    private fun isChromeOS(context: Context): Boolean {
        return context.packageManager.hasSystemFeature("org.chromium.arc.device_management")
    }

    //화면 아무곳이나 터치하면 소프트키가 사라짐
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}

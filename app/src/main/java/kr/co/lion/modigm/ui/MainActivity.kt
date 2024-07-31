package kr.co.lion.modigm.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.ActivityMainBinding
import kr.co.lion.modigm.ui.login.LoginFragment

class MainActivity : AppCompatActivity() {

    // 바인딩
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    // --------------------------------- LC START ---------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 바인딩
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
}

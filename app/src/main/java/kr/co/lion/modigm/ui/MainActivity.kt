package kr.co.lion.modigm.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.ActivityMainBinding
import kr.co.lion.modigm.databinding.CustomDialogBinding
import kr.co.lion.modigm.databinding.CustomDialogNotificationPermissionBinding
import kr.co.lion.modigm.db.HikariCPDataSource
import kr.co.lion.modigm.ui.detail.DetailFragment
import kr.co.lion.modigm.ui.login.LoginFragment
import kr.co.lion.modigm.ui.notification.CustomNotificationPermissionDialog

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // 바인딩
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    // 권한 요청 결과 처리기
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "알림 권한이 허용되었습니다.")
            // 푸시 알림을 수신하고 배지를 표시하는 로직 추가 가능
        } else {
            Log.d("MainActivity", "알림 권한이 거부되었습니다.")
            // 알림 권한이 거부되었을 때의 처리 로직 추가 가능
        }
    }

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

        // 알림 권한 요청
        checkNotificationPermission()

        // 알림을 통해 진입한 경우 처리
        handleNotificationIntent()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // 인텐트를 업데이트하여 최신 데이터를 가져옴
        handleNotificationIntent() // 알림에서 전달된 데이터를 처리
    }

    private fun handleNotificationIntent() {
        if (intent.getBooleanExtra("fromNotification", false)) {
            val studyIdx = intent.getIntExtra("studyIdx", -1)
            Log.d("MainActivity", "handleNotificationIntent: Received studyIdx = $studyIdx")

            if (studyIdx != -1) {
                // 현재 활성화된 프래그먼트 확인
                val currentFragment = supportFragmentManager.findFragmentById(R.id.containerMain)

                // DetailFragment가 이미 표시되어 있는지 확인하고, 같은 studyIdx를 가리키는지 체크
                if (currentFragment is DetailFragment && currentFragment.arguments?.getInt("studyIdx") == studyIdx) {
                    Log.d("MainActivity", "DetailFragment already displayed with the same studyIdx.")
                    return // 이미 같은 프래그먼트가 표시 중이므로 중복 이동 방지
                }

                // DetailFragment로 이동
                val detailFragment = DetailFragment().apply {
                    arguments = Bundle().apply {
                        putInt("studyIdx", studyIdx)
                    }
                }

                // 프래그먼트 트랜잭션 실행
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(R.id.containerMain, detailFragment, "DetailFragment")
                    addToBackStack("DetailFragment")
                }
            }
        }
    }


    // 권한 요청 메서드
    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MainActivity", "Notification permission already granted.")
        } else {
            showCustomPermissionDialog()
        }
    }

    private fun showCustomPermissionDialog() {
        val dialog = CustomNotificationPermissionDialog(this)
        dialog.show(
            onAllowClicked = {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
            onDenyClicked = {
                Log.d("MainActivity", "User denied notification permission.")
            }
        )
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
}

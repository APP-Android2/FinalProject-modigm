package kr.co.lion.modigm.ui.profile.popup

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.CustomDialogLogoutAdBinding
import kr.co.lion.modigm.ui.VBBaseDialogFragment
import kr.co.lion.modigm.ui.login.SocialLoginFragment
import kr.co.lion.modigm.util.ModigmApplication
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class LogoutAdDialog: VBBaseDialogFragment<CustomDialogLogoutAdBinding>(CustomDialogLogoutAdBinding::inflate) {

    // 로그아웃 확인 리스너
    private var confirmLogoutListener: (() -> Unit)? = null

    // 외부에서 리스너를 설정하는 메서드
    fun setOnConfirmLogoutListener(listener: () -> Unit) {
        confirmLogoutListener = listener
    }

    override fun onStart() {
        super.onStart()

        // 다이얼로그의 크기 설정
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent) // 투명 배경 설정 (필요한 경우)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(requireContext())

        }

        setupAdMob()
        setupButtonLogout()
        setupButtonQuit()
    }


    fun setupAdMob() {
        // 사전 로드된 AdView를 설정
        val adView = ModigmApplication.preloadedAdView

        // 광고가 이미 다른 부모 뷰에 추가되어 있을 경우 제거
        adView.parent?.let { parent ->
            (parent as ViewGroup).removeView(adView)
        }

        // 광고가 로드되어 있으면 바로 다이얼로그에 표시
        binding.adViewLogoutDialog.addView(adView)
        adView.visibility = View.VISIBLE
    }

    fun setupButtonLogout() {
        binding.buttonAdDialogLogout.setOnClickListener {
            // 로그아웃 확인 리스너 호출
            confirmLogoutListener?.invoke()

            // SharedPreferences 초기화
            prefs.clearAllPrefs()
            prefs.setBoolean("autoLogin", false)

            // 로그아웃 처리
            Firebase.auth.signOut()

            // 로그인 화면으로 돌아간다
            parentFragmentManager.beginTransaction()
                .replace(R.id.containerMain, SocialLoginFragment())
                .addToBackStack(null)
                .commit()

            dismiss()
        }
    }

    fun setupButtonQuit() {
        binding.buttonAdDialogQuit.setOnClickListener {
            dismiss()
        }
    }
}

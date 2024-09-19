package kr.co.lion.modigm.ui.profile.popup

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.CustomDialogLogoutAdBinding
import kr.co.lion.modigm.ui.login.LoginFragment
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

open class LogoutAdDialog: DialogFragment() {
    private var _binding: CustomDialogLogoutAdBinding? = null
    val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = CustomDialogLogoutAdBinding.inflate(layoutInflater)

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(requireContext())
        }

        setupAdMob()
        setupButtonLogout()
        setupButtonQuit()

        return AlertDialog.Builder(requireContext()).setView(binding.root).create()
    }

    fun setupAdMob() {
        // Start loading the ad in the background.
        val adRequest = AdRequest.Builder().build()
        _binding?.adViewLogoutDialog?.loadAd(adRequest)
    }

    fun setupButtonLogout() {
        _binding?.buttonAdDialogLogout?.setOnClickListener {
            // SharedPreferences 초기화
            prefs.clearAllPrefs()
            prefs.setBoolean("autoLogin", false)

            // 로그아웃 처리
            Firebase.auth.signOut()

            // 로그인 화면으로 돌아간다
            parentFragmentManager.beginTransaction()
                .replace(R.id.containerMain, LoginFragment())
                .addToBackStack(null)
                .commit()

            dismiss()
        }
    }

    fun setupButtonQuit() {
        _binding?.buttonAdDialogQuit?.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}

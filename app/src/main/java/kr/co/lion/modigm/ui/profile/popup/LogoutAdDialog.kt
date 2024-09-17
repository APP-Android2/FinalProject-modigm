package kr.co.lion.modigm.ui.profile.popup

import android.R
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Process
import android.view.View
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.databinding.CustomDialogBinding
import kr.co.lion.modigm.databinding.CustomDialogLogoutAdBinding


class LogoutAdDialog(context: Context) : Dialog(context) {
    val binding = CustomDialogLogoutAdBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(context)
        }

        setupAdMob()
        setupButtonLogout()
        setupButtonQuit()
    }

    fun setupAdMob() {
        // Start loading the ad in the background.
        val adRequest = AdRequest.Builder().build()
        binding.adViewLogoutDialog.loadAd(adRequest)

    }

    fun setupButtonLogout() {
        val
    }

    fun setupButtonQuit() {
        val
    }
}

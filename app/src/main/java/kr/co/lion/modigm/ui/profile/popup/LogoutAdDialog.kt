package kr.co.lion.modigm.ui.profile.popup

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.databinding.CustomDialogLogoutAdBinding

class LogoutAdDialog: DialogFragment() {
    lateinit var binding: CustomDialogLogoutAdBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = CustomDialogLogoutAdBinding.inflate(layoutInflater)

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
        binding.adViewLogoutDialog.loadAd(adRequest)

    }

    fun setupButtonLogout() {

    }

    fun setupButtonQuit() {

    }
}

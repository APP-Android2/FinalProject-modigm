package kr.co.lion.modigm.ui.profile.popup

import android.R
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Process
import android.view.View
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import kr.co.lion.modigm.databinding.CustomDialogBinding


class LogoutAdDialog(context: Context) : Dialog(context) {
    val binding = CustomDialogBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

    }
}

package kr.co.lion.modigm.ui.login

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.co.lion.modigm.databinding.CustomDialogFindEmailBinding

class CustomFindEmailDialog(context: Context){
    private val binding: CustomDialogFindEmailBinding =
        CustomDialogFindEmailBinding.inflate(LayoutInflater.from(context))

//    init {
//        requestWindowFeature(Window.FEATURE_NO_TITLE)
//        setContentView(binding.root)
//        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//    }

    private val dialog = MaterialAlertDialogBuilder(context)
        .setView(binding.root)
        .setCancelable(false)
        .create()

    fun setTitle(title: String) {
        binding.textViewDialogTitle.text = title
    }

    fun setEmail(email: String) {
        binding.textViewDialogMessageEmail.text = email
    }

    fun setPositiveButton(buttonText: String, onClickListener: (View) -> Unit) {
        binding.buttonDialogPositive.apply {
            text = buttonText
            setOnClickListener {
                onClickListener(it)
                dialog.dismiss()
            }
        }
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}
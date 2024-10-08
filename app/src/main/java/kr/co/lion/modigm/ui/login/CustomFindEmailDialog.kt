package kr.co.lion.modigm.ui.login

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.co.lion.modigm.databinding.CustomDialogFindEmailBinding

class CustomFindEmailDialog(context: Context){
    private val binding: CustomDialogFindEmailBinding =
        CustomDialogFindEmailBinding.inflate(LayoutInflater.from(context))

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
package kr.co.lion.modigm.ui.login

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.co.lion.modigm.databinding.CustomDialogCancelBinding

class CustomCancelDialog(context: Context) {
    private val binding: CustomDialogCancelBinding =
        CustomDialogCancelBinding.inflate(LayoutInflater.from(context))

    private val dialog = MaterialAlertDialogBuilder(context)
        .setView(binding.root)
        .setCancelable(false)
        .create()

    fun setTitle(title: String) {
        binding.textViewDialogTitle.text = title
    }

    fun setMessage(message: String) {
        binding.textViewDialogMessage.text = message
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

    fun setNegativeButton(buttonText: String, onClickListener: (View) -> Unit) {
        binding.buttonDialogNegative.apply {
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
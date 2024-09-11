package kr.co.lion.modigm.ui.notification

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.CustomDialogNotificationPermissionBinding

class CustomNotificationPermissionDialog(private val context: Context) {

    fun show(onAllowClicked: () -> Unit, onDenyClicked: () -> Unit) {
        // XML 레이아웃 바인딩 객체 생성
        val dialogBinding = CustomDialogNotificationPermissionBinding.inflate(LayoutInflater.from(context))

        // 다이얼로그 빌더 생성 및 설정
        val dialog = MaterialAlertDialogBuilder(context, R.style.dialogColor)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton("허용") { _, _ ->
                onAllowClicked()
            }
            .setNegativeButton("거부") { _, _ ->
                onDenyClicked()
            }
            .create()

        dialog.show()
    }
}
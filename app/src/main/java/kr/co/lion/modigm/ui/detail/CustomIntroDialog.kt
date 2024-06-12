package kr.co.lion.modigm.ui.detail

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.co.lion.modigm.R

class CustomIntroDialog (private val context: Context) {
    fun show() {
        // XML 레이아웃 로딩
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.custom_dialog_intro_example, null)


        // 텍스트뷰들을 찾아서 텍스트 설정하기 전에 공백을 non-breaking space로 치환
        val textViewIds = listOf(R.id.example1, R.id.example2, R.id.example3, R.id.example4)
        textViewIds.forEach { id ->
            val textView = view.findViewById<TextView>(id)
            textView.text = textView.text.toString().replace(" ", "\u00A0")
        }

        // 필요하다면 여기에서 뷰를 추가적으로 수정할 수 있습니다.

        // 다이얼로그 빌더 생성
        val dialog = MaterialAlertDialogBuilder(context, R.style.dialogColor)
            .setView(view)
            .setPositiveButton("닫기") { dialog, which ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }
}
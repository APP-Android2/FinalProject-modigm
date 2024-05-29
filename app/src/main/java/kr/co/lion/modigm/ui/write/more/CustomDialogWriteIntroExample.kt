package kr.co.lion.modigm.ui.write.more

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.WindowManager
import kr.co.lion.modigm.databinding.CustomDialogWriteIntroExampleBinding

class CustomDialogWriteIntroExample(context: Context) : Dialog(context) {

    //    lateinit var itemClickListener: ItemClickListener
    lateinit var binding: CustomDialogWriteIntroExampleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CustomDialogWriteIntroExampleBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        settingView()
    }


    private fun settingView() {
        binding.apply {
            // 사이즈를 조절하고 싶을 때 사용
            resize(this@CustomDialogWriteIntroExample, 0.8f, 0.55f)

            // 배경을 투명하게
            // 다이얼로그를 둥글게 표현하기 위해 필요
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // 다이얼로그 바깥쪽 클릭 시 종료되도록
            setCanceledOnTouchOutside(true)

            // 취소 가능 여부
            setCancelable(true)
            btnClose.setOnClickListener {
                // 다이얼로그 닫기
                dismiss()
            }
            textViewWriteIntroDialogContent.apply {

                setText("1. 자기소개\n")
                append(" · 안녕하세요 kotlin 공부를 하고 있는 취준생입니다.\n")
                append("\n")
                append("2. 활동\n")
                append(" · 책을 한 권 정해서 문제를 풀고 서로 설명해며 공부해요.\n")
                append(" · 모르는 부분을 서로 물어보고 설명해주며 해결해 나아가요.\n")
                append("\n")
                append("3. 희망멤버\n")
                append(" · kotlin 언어 혹은 자바 언어를 다룰 줄 아는 사람이면 좋겠어요.\n")
            }

            // 구분점에 따라 들여쓰기 적용 --> 왜 안 되지? (2차 개발)
            textViewWriteIntroDialogContent.text = SpannableStringBuilder(textViewWriteIntroDialogContent.text).apply {
                setSpan(IndentLeadingMarginSpan(),0, length, 0)
            }
        }
    }

    // 사이즈를 조절하고 싶을 때 사용
    private fun resize(dialog: Dialog, width: Float, height: Float) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT < 30){
            val size = Point()
            windowManager.defaultDisplay.getSize(size)

            val x = (size.x * width).toInt()
            val y = (size.y * height).toInt()
            dialog.window?.setLayout(x, y)
        } else {
            val rect = windowManager.currentWindowMetrics.bounds

            val x = (rect.width() * width).toInt()
            val y = (rect.height() * height).toInt()
            dialog.window?.setLayout(x, y)
        }

    }
}


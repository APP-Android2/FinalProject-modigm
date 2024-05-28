package kr.co.lion.modigm.ui.write

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
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
            resize(this@CustomDialogWriteIntroExample, 0.8f, 0.4f)

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
//    interface ItemClickListener{
//        fun onClick(message: String)
//    }
//
//    fun setItemClickListener(itemClickListener: ItemClickListener){
//        this.itemClickListener = itemClickListener
//    }
}


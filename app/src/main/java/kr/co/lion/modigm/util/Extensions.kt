package kr.co.lion.modigm.util

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.CustomSnackbarWithIconBinding
import kr.co.lion.modigm.databinding.CustomSnackbarWithoutIconBinding

// dp값으로 변환하는 확장함수
// ex) Int 로 필요할 경우 - > 16.dp 로 작성
// ex) Float 로 필요한 경우 - > 16.toFloat().dp 로 작성
inline val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()

inline val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )

// String - > Editable 변환
// ex) "왕감자".toEditable()
fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)


// EditText 텍스트 변경 시 특정 동작 실행을 위한 확장 함수
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged.invoke(s.toString())
        }
    })
}

fun Activity.showLoginSnackBar(message: String, iconResId: Int?) {

    val rootView = this.findViewById<View>(android.R.id.content)
    val snackBar = Snackbar.make(rootView, "", Snackbar.LENGTH_INDEFINITE)
    val snackBarLayout = snackBar.view as ViewGroup
    val layoutInflater = this.layoutInflater

    // 텍스트 숨기기
    snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).visibility = View.INVISIBLE

    // 커스텀 레이아웃 설정 함수
    fun setCustomLayoutWithIcon(layoutInflater: LayoutInflater, layoutId: Int, message: String, iconResId: Int?) {
        when(layoutId) {
            R.layout.custom_snackbar_with_icon -> {
                val binding = CustomSnackbarWithIconBinding.inflate(layoutInflater)
                binding.snackbarText.text = message
                iconResId?.let {
                    binding.snackbarIcon.setImageResource(it)
                    binding.snackbarIcon.visibility = View.VISIBLE
                }
                snackBarLayout.addView(binding.root, 0)
            }
            R.layout.custom_snackbar_without_icon -> {
                val binding = CustomSnackbarWithoutIconBinding.inflate(layoutInflater)
                binding.snackbarText.text = message
                snackBarLayout.addView(binding.root, 0)
            }
        }
    }

    // 레이아웃 선택
    val layoutId = if (iconResId != null) R.layout.custom_snackbar_with_icon else R.layout.custom_snackbar_without_icon
    setCustomLayoutWithIcon(layoutInflater, layoutId, message, iconResId)

    // 레이아웃 파라미터 설정
    val layoutParams = snackBar.view.layoutParams as FrameLayout.LayoutParams
    layoutParams.apply {
        width = FrameLayout.LayoutParams.MATCH_PARENT
        height = FrameLayout.LayoutParams.WRAP_CONTENT
        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        bottomMargin = 80.dp
    }
    snackBar.view.layoutParams = layoutParams

    snackBar.show()

    // 설정된 시간 후에 Snackbar를 닫기
    snackBar.view.postDelayed({
        snackBar.dismiss()
    }, 2000)
}
// 흔들림 애니메이션 함수
fun View.shake() {
    val shake = ObjectAnimator.ofFloat(this, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
    shake.duration = 300
    shake.start()

    // 진동 추가
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(50)
    }
}


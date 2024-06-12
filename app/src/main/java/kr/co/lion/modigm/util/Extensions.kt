package kr.co.lion.modigm.util

import android.app.Activity
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
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

// Activity의 확장 함수로 showCustomSnackbar 작성
fun Activity.showCustomSnackbar(message: String, iconResId: Int?) {
    Log.d("CustomSnackbar", "showCustomSnackbar 호출됨")
    // Snackbar를 생성할 rootView를 참조합니다.
    val rootView = this.findViewById<View>(android.R.id.content)

    // 기본 Snackbar 생성
    val snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_INDEFINITE)

    // Snackbar의 기본 텍스트를 숨김
    val snackbarText = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    snackbarText.visibility = View.INVISIBLE

    // 커스텀 레이아웃에 메시지와 아이콘을 설정
    if (iconResId != null) {
        val snackbarBinding = CustomSnackbarWithIconBinding.inflate(layoutInflater)
        snackbarBinding.snackbarIcon.setImageResource(iconResId)
        snackbarBinding.snackbarIcon.visibility = View.VISIBLE
        snackbarBinding.snackbarText.text = message
        // Snackbar의 레이아웃을 가져와서 커스텀 레이아웃을 추가
        val snackbarLayout = snackbar.view as ViewGroup
        snackbarLayout.addView(snackbarBinding.root, 0)
    } else {
        val snackbarBinding = CustomSnackbarWithoutIconBinding.inflate(layoutInflater)
        snackbarBinding.snackbarText.text = message
        // Snackbar의 레이아웃을 가져와서 커스텀 레이아웃을 추가
        val snackbarLayout = snackbar.view as ViewGroup
        snackbarLayout.addView(snackbarBinding.root, 0)
    }

    // Snackbar의 너비와 높이 및 위치 조정
    val layoutParams = snackbar.view.layoutParams as FrameLayout.LayoutParams
    layoutParams.width = 250.dp
    layoutParams.height = 50.dp
    layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL // 아래로 배치하고 가로로 가운데 정렬
    layoutParams.bottomMargin = 80.dp // BottomNavigationView를 고려하여 마진 추가 (적절한 값으로 설정)
    snackbar.view.layoutParams = layoutParams

    // Snackbar 표시
    snackbar.show()

    // 설정된 시간 후에 Snackbar를 닫기
    val displayDuration = 1500
    snackbar.view.postDelayed({
        snackbar.dismiss()
    }, displayDuration.toLong())
}
package kr.co.lion.modigm.util

import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.widget.EditText

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
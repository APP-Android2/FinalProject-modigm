package kr.co.lion.modigm.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

// 키보드 내림 + 포커스 제거
fun Activity.hideSoftInput() {
    // 포커스 있는지 체크
    window.currentFocus?.let { view ->
        val inputMethodManager = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager // 키보드 관리 객체 가져옴
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0) // 키보드 내리기
        view.clearFocus() // 포커스 제거
    }
}

// 키보드 올림 + 포커스 설정
fun Activity.showKeyboard(view: View) {
    view.requestFocus() // 포커스 설정
    val inputMethodManager = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager // 키보드 관리 객체 가져옴
    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT) // 키보드 올리기
}

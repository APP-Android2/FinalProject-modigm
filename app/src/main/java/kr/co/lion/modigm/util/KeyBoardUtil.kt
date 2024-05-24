package kr.co.lion.modigm.util

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

// 키보드 내림 + 포커스 제거
fun Activity.hideSoftInput(){
    // 포커스 있는지 체크
    if(window.currentFocus != null) {
        val inputMethodManger = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager // 키보드 관리 객체 가져옴
        inputMethodManger.hideSoftInputFromWindow(window.currentFocus?.windowToken, 0) // 키보드 내리기
        window.currentFocus?.clearFocus() // 포커스 제거
    }
}
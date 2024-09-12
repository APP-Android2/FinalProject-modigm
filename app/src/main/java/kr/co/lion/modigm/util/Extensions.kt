package kr.co.lion.modigm.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
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
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.CustomSnackbarWithIconBinding
import kr.co.lion.modigm.databinding.CustomSnackbarWithoutIconBinding
import kr.co.lion.modigm.ui.profile.ProfileWebFragment
import java.util.Locale

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
    // 루트 뷰 가져오기 및 Snackbar 생성 (기본 텍스트는 숨기기)
    val snackBar = Snackbar.make(findViewById(android.R.id.content), "", Snackbar.LENGTH_INDEFINITE).apply {
        // 기본 Snackbar 텍스트 숨기기
        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).visibility = View.INVISIBLE
    }

    // 레이아웃 인플레이터 및 커스텀 레이아웃 설정
    val layoutInflater = LayoutInflater.from(this)

    // 아이콘 리소스 ID가 null이 아닌 경우와 null인 경우에 따라 다른 레이아웃 인플레이트
    val customView = if (iconResId != null) {
        CustomSnackbarWithIconBinding.inflate(layoutInflater).apply {
            snackbarText.text = message
            snackbarIcon.setImageResource(iconResId)
            snackbarIcon.visibility = View.VISIBLE
        }.root
    } else {
        CustomSnackbarWithoutIconBinding.inflate(layoutInflater).apply {
            snackbarText.text = message
        }.root
    }

    // 커스텀 레이아웃을 Snackbar의 뷰에 추가
    (snackBar.view as ViewGroup).addView(customView, 0)

    // Snackbar 레이아웃 파라미터 설정
    (snackBar.view.layoutParams as FrameLayout.LayoutParams).apply {
        width = FrameLayout.LayoutParams.MATCH_PARENT
        height = 48.dp
        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        bottomMargin = 80.dp
        snackBar.view.layoutParams = this
    }

    // Snackbar 표시
    snackBar.show()
    // 일정 시간 후 Snackbar 닫기
    snackBar.view.postDelayed({ snackBar.dismiss() }, 2500)
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

// StateFlow값 collect하는 확장함수
fun <T> LifecycleOwner.collectWhenStarted(flow: Flow<T>, action: suspend (value: T) -> Unit) {
    lifecycleScope.launch {
        flow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect(action)
    }
}

fun String.toNationalPhoneNumber(): String {
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    val locale = Locale.getDefault().country
    val toNationalNum = phoneNumberUtil.parse(this, locale)
    return phoneNumberUtil.format(toNationalNum, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
}


// 뷰페이저 페이지 이동 애니메이션 속도 지정 함수
fun ViewPager2.setCurrentItemWithDuration(
    item: Int,
    duration: Long,
    interpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    pagePxWidth: Int = width // Default value taken from getWidth() from ViewPager2 view
) {
    val pxToDrag: Int = pagePxWidth * (item - currentItem)
    val animator = ValueAnimator.ofInt(0, pxToDrag)
    var previousValue = 0
    animator.addUpdateListener { valueAnimator ->
        val currentValue = valueAnimator.animatedValue as Int
        val currentPxToDrag = (currentValue - previousValue).toFloat()
        fakeDragBy(-currentPxToDrag)
        previousValue = currentValue
    }
    animator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            beginFakeDrag()
        }

        override fun onAnimationEnd(animation: Animator) {
            endFakeDrag()
        }

        override fun onAnimationCancel(animation: Animator) {
        }

        override fun onAnimationRepeat(animation: Animator) {
        }
    })
    animator.interpolator = interpolator
    animator.duration = duration
    animator.start()
}

// 웹뷰 띄워주는 함수
fun openWebView(viewLifecycleOwner: LifecycleOwner, parentFragmentManager: FragmentManager, url: String){
    viewLifecycleOwner.lifecycleScope.launch {
        // bundle 에 필요한 정보를 담는다
        val bundle = Bundle()
        bundle.putString("link", url)

        // 이동할 프래그먼트로 bundle을 넘긴다
        val profileWebFragment = ProfileWebFragment()
        profileWebFragment.arguments = bundle

        // Fragment 교체
        parentFragmentManager.commit {
            add(R.id.containerMain, profileWebFragment)
            addToBackStack(FragmentName.PROFILE_WEB.str)
        }
    }
}

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
fun Activity.showSoftInput(view: View) {
    view.requestFocus() // 포커스 설정
    val inputMethodManager = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager // 키보드 관리 객체 가져옴
    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT) // 키보드 올리기
}
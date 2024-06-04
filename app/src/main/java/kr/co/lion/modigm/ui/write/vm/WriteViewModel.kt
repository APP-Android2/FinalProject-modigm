package kr.co.lion.modigm.ui.write.vm

import android.graphics.Color
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.databinding.adapters.SeekBarBindingAdapter.setProgress
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WriteViewModel : ViewModel() {
    // 임시로 연결
    private val _fieldClicked = MutableLiveData<Boolean>()
    private val _periodClicked = MutableLiveData<Boolean>()
    private val _proceedClicked = MutableLiveData<Boolean>()
    private val _skillClicked = MutableLiveData<Boolean>()
    private val _introClicked = MutableLiveData<Boolean>()
    // 버튼 활성화 / 비활성화 상태
    private val _buttonState = MutableLiveData<Boolean>()
    val buttonState: LiveData<Boolean> = _buttonState
    // 버튼 색상 -> BackgoroundTint에 직접 넣어줄 수가 없음 ( 2차 )
    private val _buttonColor = MutableLiveData<Int>()
    val buttonColor: LiveData<Int> = _buttonColor

    // 버튼 텍스트 색상
    private val _buttonTextColor = MutableLiveData<Int>()
    val buttonTextColor: LiveData<Int> = _buttonTextColor

    // 버튼 다음? or 완료?
    private val _buttonText = MutableLiveData<String>()
    // progress 게이지
    private val _progressCount = MutableLiveData<Int>()

    private val _writeProceedLocation = MutableLiveData<String>()

    init {
        // 버튼 text 초기 설정
        _buttonText.value = "다음"
        // 버튼 초기 색상 설정
        _buttonColor.value = Color.parseColor("#bbbbbb")
        // 버튼 Text 초기 색상 설정
        _buttonTextColor.value = Color.parseColor("#777777")

    }
    val fieldClicked: LiveData<Boolean> = _fieldClicked
    val periodClicked: LiveData<Boolean> = _periodClicked
    val proceedClicked: LiveData<Boolean> = _proceedClicked
    val skillClicked: LiveData<Boolean> = _skillClicked
    val introClicked: LiveData<Boolean> = _introClicked


    val writeProceedLocation: LiveData<String> = _writeProceedLocation

    // 버튼 다음? or 완료?
    val buttonText: LiveData<String> = _buttonText
    // progress 게이지
    val progressCount: LiveData<Int> = _progressCount

    fun userDidAnswer(tabName: String) {
        when (tabName) {
            "field" -> {
                _fieldClicked.value = true
            }

            "period" -> {
                _periodClicked.value = true
            }

            "proceed" -> {
                _proceedClicked.value = true
            }

            "skill" -> {
                _skillClicked.value = true
            }

            "intro" -> {
                _introClicked.value = true
            }
        }
    }

    fun userDidNotAnswer(tabName: String) {
        when (tabName) {
            "field" -> {
                _fieldClicked.value = false
            }

            "period" -> {
                _periodClicked.value = false
            }

            "proceed" -> {
                _proceedClicked.value = false
            }

            "skill" -> {
                _skillClicked.value = false
            }

            "intro" -> {
                _introClicked.value = false
            }
        }
    }

    fun activateButton() {
        _buttonState.value = true
    }

    fun deactivateButton() {
        _buttonState.value = false
    }

    // WriteProceedFragment BottomSheet에서 사용
    fun settingLocation(location: String) {
        _writeProceedLocation.value = location
    }


    // WriteFragment 버튼 설정
    fun settingButton(btn: Button, tabName: String, didAnswer: Boolean) {
        when (tabName) {
            "field" -> {

                // text 설정
                _buttonText.value = "다음"

                if (didAnswer) {
                    btn.setBackgroundColor(Color.parseColor("#1A51C5"))
                    _buttonTextColor.value = Color.parseColor("#FFFFFF")
                    activateButton() // 버튼 활성화
                } else {
                    btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                    _buttonTextColor.value = Color.parseColor("#777777")
                    deactivateButton() // 버튼 비활성화
                }
            }

            "period" -> {

                // text 설정
                _buttonText.value = "다음"

                if (didAnswer) {
                    btn.setBackgroundColor(Color.parseColor("#1A51C5"))
                    _buttonTextColor.value = Color.parseColor("#FFFFFF")
                    activateButton() // 버튼 활성화
                } else {
                    btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                    _buttonTextColor.value = Color.parseColor("#777777")
                    deactivateButton() // 버튼 비활성화
                }
            }

            "proceed" -> {

                // text 설정
                _buttonText.value = "다음"

                if (didAnswer) {
                    btn.setBackgroundColor(Color.parseColor("#1A51C5"))
                    _buttonTextColor.value = Color.parseColor("#FFFFFF")
                    activateButton() // 버튼 활성화
                } else {
                    btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                    _buttonTextColor.value = Color.parseColor("#777777")
                    deactivateButton() // 버튼 비활성화
                }
            }

            "skill" -> {

                // text 설정
                _buttonText.value = "다음"

                if (didAnswer) {
                    btn.setBackgroundColor(Color.parseColor("#1A51C5"))
                    _buttonTextColor.value = Color.parseColor("#FFFFFF")
                    activateButton() // 버튼 활성화
                } else {
                    btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                    _buttonTextColor.value = Color.parseColor("#777777")
                    deactivateButton() // 버튼 비활성화
                }
            }

            "intro" -> {

                // text 설정
                _buttonText.value = "완료"

                if (didAnswer) {
                    btn.setBackgroundColor(Color.parseColor("#1A51C5"))
                    _buttonTextColor.value = Color.parseColor("#FFFFFF")
                    activateButton() // 버튼 활성화
                } else {
                    btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                    _buttonTextColor.value = Color.parseColor("#777777")
                    deactivateButton() // 버튼 비활성화
                }
            }
        }
    }

    // WriteFragment Progress Bar 설정
    fun settingProgressBar(position: Int){
        when (position) {
            0 -> {
                _progressCount.value = 20
            }

            1 -> {
                _progressCount.value = 40
            }

            2 -> {
                _progressCount.value = 60
            }

            3 -> {
                _progressCount.value = 80
            }

            4 -> {
                _progressCount.value = 100
            }
        }
    }
}
package kr.co.lion.modigm.ui.write.vm

import android.graphics.Color
import android.widget.Button
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
    private val _buttonState = MutableLiveData<Boolean>()

    // 버튼 다음? or 완료?
    private val _buttonText = MutableLiveData<String>()

    private val _writeProceedLocation = MutableLiveData<String>()

    val fieldClicked: LiveData<Boolean> = _fieldClicked
    val periodClicked: LiveData<Boolean> = _periodClicked
    val proceedClicked: LiveData<Boolean> = _proceedClicked
    val skillClicked: LiveData<Boolean> = _skillClicked
    val introClicked: LiveData<Boolean> = _introClicked

    val buttonState: LiveData<Boolean> = _buttonState

    val writeProceedLocation: LiveData<String> = _writeProceedLocation

    // 버튼 다음? or 완료?
    val buttonText: LiveData<String> = _buttonText

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
                    btn.setTextColor(Color.parseColor("#FFFFFF"))
                    activateButton() // 버튼 활성화
                } else {
                    btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                    btn.setTextColor(Color.parseColor("#777777"))
                    deactivateButton() // 버튼 비활성화
                }
            }

            "period" -> {

                // text 설정
                _buttonText.value = "다음"

                if (didAnswer) {
                    btn.setBackgroundColor(Color.parseColor("#1A51C5"))
                    btn.setTextColor(Color.parseColor("#FFFFFF"))
                    activateButton() // 버튼 활성화
                } else {
                    btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                    btn.setTextColor(Color.parseColor("#777777"))
                    deactivateButton() // 버튼 비활성화
                }
            }

            "proceed" -> {

                // text 설정
                _buttonText.value = "다음"

                if (didAnswer) {
                    btn.setBackgroundColor(Color.parseColor("#1A51C5"))
                    btn.setTextColor(Color.parseColor("#FFFFFF"))
                    activateButton() // 버튼 활성화
                } else {
                    btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                    btn.setTextColor(Color.parseColor("#777777"))
                    deactivateButton() // 버튼 비활성화
                }
            }

            "skill" -> {

                // text 설정
                _buttonText.value = "다음"

                if (didAnswer) {
                    btn.setBackgroundColor(Color.parseColor("#1A51C5"))
                    btn.setTextColor(Color.parseColor("#FFFFFF"))
                    activateButton() // 버튼 활성화
                } else {
                    btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                    btn.setTextColor(Color.parseColor("#777777"))
                    deactivateButton() // 버튼 비활성화
                }
            }

            "intro" -> {

                // text 설정
                _buttonText.value = "완료"

                if (didAnswer) {
                    btn.setBackgroundColor(Color.parseColor("#1A51C5"))
                    btn.setTextColor(Color.parseColor("#FFFFFF"))
                    activateButton() // 버튼 활성화
                } else {
                    btn.setBackgroundColor(Color.parseColor("#bbbbbb"))
                    btn.setTextColor(Color.parseColor("#777777"))
                    deactivateButton() // 버튼 비활성화
                }
            }
        }
    }
}
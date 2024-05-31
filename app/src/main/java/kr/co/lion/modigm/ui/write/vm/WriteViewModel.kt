package kr.co.lion.modigm.ui.write.vm

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

    val fieldClicked: LiveData<Boolean> = _fieldClicked
    val periodClicked: LiveData<Boolean> = _periodClicked
    val proceedClicked: LiveData<Boolean> = _proceedClicked
    val skillClicked: LiveData<Boolean> = _skillClicked
    val introClicked: LiveData<Boolean> = _introClicked

    val buttonState: LiveData<Boolean> = _buttonState

    fun userDidAnswer(tabName: String) {
        when (tabName){
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

    fun userDidNotAnswer(tabName: String){
        when (tabName){
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

    fun activateButton(){
        _buttonState.value = true
    }

    fun deactivateButton(){
        _buttonState.value = false
    }
}
package kr.co.lion.modigm.ui.study.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BottomNaviViewModel : ViewModel() {
    private val _isSnackBarShown = MutableLiveData(false)
    val isSnackBarShown: LiveData<Boolean> = _isSnackBarShown

    fun setSnackBarShown(shown: Boolean) {
        _isSnackBarShown.postValue(shown)
    }
}
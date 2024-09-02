package kr.co.lion.modigm.ui.study.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BottomNaviViewModel : ViewModel() {

    // 태그
    private val logTag by lazy { BottomNaviViewModel::class.simpleName }

    private val _isSnackBarShown = MutableLiveData(false)
    val isSnackBarShown: LiveData<Boolean> = _isSnackBarShown

    fun setSnackBarShown(shown: Boolean) {
        _isSnackBarShown.postValue(shown)
    }
}
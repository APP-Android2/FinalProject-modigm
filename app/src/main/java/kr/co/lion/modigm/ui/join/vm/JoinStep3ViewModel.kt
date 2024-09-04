package kr.co.lion.modigm.ui.join.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class JoinStep3ViewModel: ViewModel() {

    // 유효성 검사 여부
    private var _isValidate = MutableStateFlow<Boolean?>(null)
    val isValidate: StateFlow<Boolean?> = _isValidate

    // 선택한 관심분야 리스트
    private val _selectedInterestList = MutableStateFlow<MutableList<String>>(mutableListOf())
    val selectedInterestList: StateFlow<MutableList<String>> = _selectedInterestList

    fun addInterest(interest: String) {
        _selectedInterestList.value.add(interest)
    }

    fun removeInterest(interest: String) {
        _selectedInterestList.value.remove(interest)
    }

    // 유효성 검사
    fun validate(): Boolean {
        _isValidate.value = selectedInterestList.value.isNotEmpty()
        return isValidate.value ?: false
    }

    fun reset(){
        _isValidate.value = null
        _selectedInterestList.value = mutableListOf()
    }
}
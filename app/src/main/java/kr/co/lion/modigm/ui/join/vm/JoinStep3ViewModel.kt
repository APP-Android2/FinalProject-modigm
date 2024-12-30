package kr.co.lion.modigm.ui.join.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class JoinStep3ViewModel: ViewModel() {

    // 유효성 검사 여부
    private var _isInterestListValidated = MutableStateFlow<Boolean?>(null)
    val isInterestListValidated: StateFlow<Boolean?> = _isInterestListValidated

    // 선택한 관심분야 리스트
    private val _selectedInterestList = MutableStateFlow<MutableList<String>>(mutableListOf())
    val selectedInterestList: StateFlow<MutableList<String>> = _selectedInterestList

    fun addToInterestList(interest: String) {
        _selectedInterestList.value.add(interest)
    }

    fun removeFromInterestList(interest: String) {
        _selectedInterestList.value.remove(interest)
    }

    // 유효성 검사
    fun validateStep3UserInput(): Boolean {
        _isInterestListValidated.value = selectedInterestList.value.isNotEmpty()
        return isInterestListValidated.value ?: false
    }

    fun resetStep3States(){
        _isInterestListValidated.value = null
        _selectedInterestList.value = mutableListOf()
    }
}
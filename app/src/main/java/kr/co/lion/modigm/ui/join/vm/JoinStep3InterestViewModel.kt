package kr.co.lion.modigm.ui.join.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class JoinStep3InterestViewModel: ViewModel() {

    // 유효성 검사 여부
    private val _isInterestListValidated = MutableStateFlow(true)
    val isInterestListValidated: StateFlow<Boolean> = _isInterestListValidated

    // 선택한 관심분야 리스트
    private val _selectedInterestList = MutableStateFlow<MutableList<String>>(mutableListOf())
    val selectedInterestList: StateFlow<List<String>> = _selectedInterestList

    fun addToInterestList(interest: String) {
        _selectedInterestList.value.add(interest)
    }

    fun removeFromInterestList(interest: String) {
        _selectedInterestList.value.remove(interest)
    }

    // 유효성 검사
    fun validateStep3UserInput(): Boolean {
        _isInterestListValidated.value = selectedInterestList.value.isNotEmpty()
        return isInterestListValidated.value
    }

    fun resetStep3States(){
        _isInterestListValidated.value = true
        _selectedInterestList.value = mutableListOf()
    }
}
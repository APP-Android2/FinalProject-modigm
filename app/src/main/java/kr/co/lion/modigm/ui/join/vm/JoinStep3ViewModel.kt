package kr.co.lion.modigm.ui.join.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kr.co.lion.modigm.util.InterestList

class JoinStep3ViewModel: ViewModel() {

    // 데이터는 추후 수정
    val interestList = MutableStateFlow(
        InterestList.entries.toTypedArray()
    )

    // 유효성 검사 여부
    private var _isValidate = MutableStateFlow<Boolean?>(null)
    val isValidate: StateFlow<Boolean?> = _isValidate

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
        _isValidate.value = false
        _selectedInterestList.value = mutableListOf()
    }
}
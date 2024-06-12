package kr.co.lion.modigm.ui.join.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.co.lion.modigm.util.InterestList

class JoinStep3ViewModel: ViewModel() {

    // 데이터는 추후 수정
    val interestList = MutableLiveData(
        InterestList.values()
    )

    // 유효성 검사 여부
    private var _isValidate = MutableLiveData<Boolean>()
    val isValidate: LiveData<Boolean> = _isValidate

    private val _selectedInterestList = MutableLiveData<MutableList<Int>>(mutableListOf())
    val selectedInterestList: LiveData<MutableList<Int>> = _selectedInterestList

    fun addInterest(interest: Int) {
        _selectedInterestList.value?.add(interest)
    }

    fun removeInterest(interest: Int) {
        _selectedInterestList.value?.remove(interest)
    }

    // 유효성 검사
    fun validate(): Boolean {
        _isValidate.value = !selectedInterestList.value.isNullOrEmpty()
        return isValidate.value!!
    }

    fun reset(){
        _isValidate = MutableLiveData<Boolean>()
        _selectedInterestList.value = mutableListOf()
    }
}
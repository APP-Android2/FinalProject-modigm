package kr.co.lion.modigm.ui.login.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.repository.UserInfoRepository

class FindEmailViewModel: ViewModel() {
    private val _repository = UserInfoRepository()

    val name = MutableLiveData<String>()
    val nameError = MutableLiveData<String>()

    val phone = MutableLiveData<String>()
    val phoneError = MutableLiveData<String>()

    private var _isComplete = MutableLiveData<Boolean>()
    val isComplete: MutableLiveData<Boolean> = _isComplete

    // 유효성 검사
    fun validateInput(): Boolean{
        if(name.value.isNullOrEmpty()){
            nameError.value = "이름을 입력해주세요."
            return false
        }
        if(phone.value.isNullOrEmpty()){
            phoneError.value = "전화번호를 입력해주세요."
            return false
        }
        return true
    }

    // 전화번호가 DB에 등록된 회원 정보에 있는지 확인하고 등록된 이름과 입력한 이름을 매칭
    fun checkNameAndPhone(){
        viewModelScope.launch {
            val result1 = _repository.checkUserByPhoneFindNameAndEmail(phone.value?:"")
            if(result1 != null){
                // 등록된 연락처라면 등록된 이름이 입력한 이름과 맞는지 확인
                val resultName = result1["name"]
                if(resultName == name.value){
                    _isComplete.value = true
                }else{
                    nameError.value = "등록되지 않은 이름입니다."
                }
            }else{
                phoneError.value = "등록되지 않은 전화번호입니다."
            }
        }
    }
}
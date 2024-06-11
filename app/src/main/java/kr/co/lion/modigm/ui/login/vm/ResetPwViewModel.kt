package kr.co.lion.modigm.ui.login.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ResetPwViewModel: ViewModel() {

    private val _auth = FirebaseAuth.getInstance()

    val newPassword = MutableLiveData<String>()
    val newPasswordError = MutableLiveData<String>()

    val newPasswordCheck = MutableLiveData<String>()
    val newPasswordCheckError = MutableLiveData<String>()

    private var _isComplete = MutableLiveData<Boolean>()
    val isComplete: LiveData<Boolean> = _isComplete

    // 유효성 검사
    fun validateInput(): Boolean{
        if(newPassword.value.isNullOrEmpty()){
            newPasswordError.value = "비밀번호를 입력해주세요."
            return false
        }
        if(newPasswordCheck.value.isNullOrEmpty()){
            newPasswordCheckError.value = "비밀번호를 확인해주세요."
            return false
        }
        return true
    }

    // 비밀번호 변경
    fun changePassword(){
        if(!newPassword.value.isNullOrEmpty()){
            _auth.currentUser?.updatePassword(newPassword.value!!)?.addOnCompleteListener {
                _isComplete.value = it.isSuccessful
            }
        }
    }
}
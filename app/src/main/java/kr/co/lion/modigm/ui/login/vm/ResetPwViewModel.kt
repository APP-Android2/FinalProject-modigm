package kr.co.lion.modigm.ui.login.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class ResetPwViewModel: ViewModel() {

    private val _auth = FirebaseAuth.getInstance()

    private val _oldPassword = MutableLiveData<String>()

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
        }else{
            if(!Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]{8,20}$", newPassword.value)){
                newPasswordError.value = "영문, 숫자, 특수문자가 포함된 비밀번호를 8~20자로 입력해주세요."
                return false
            }
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
                if(it.isSuccessful){
                    _auth.signOut()
                    _isComplete.value = true
                }else{
                    _isComplete.value = false
                }
            }
        }
    }
}
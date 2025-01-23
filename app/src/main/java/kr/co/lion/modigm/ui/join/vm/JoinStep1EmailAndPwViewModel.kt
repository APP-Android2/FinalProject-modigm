package kr.co.lion.modigm.ui.join.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class JoinStep1EmailAndPwViewModel @Inject constructor(
    private val _auth: FirebaseAuth
): ViewModel() {

    // 이메일
    private val _userInputEmail = MutableStateFlow("")
    val userInputEmail = _userInputEmail.asStateFlow()
    fun setUserInputEmail(email: String){
        _userInputEmail.value = email
    }
    private val _userInputEmailValidationMessage = MutableStateFlow("")
    val userInputEmailValidationMessage = _userInputEmailValidationMessage.asStateFlow()
    fun setUserInputEmailValidationMessage(message: String){
        _userInputEmailValidationMessage.value = message
    }

    // 비밀번호
    private val _userInputPassword = MutableStateFlow("")
    val userInputPassword = _userInputPassword.asStateFlow()
    fun setUserInputPassword(password: String){
        _userInputPassword.value = password
    }
    private val _userInputPwValidationMessage = MutableStateFlow("")
    val userInputPwValidationMessage = _userInputPwValidationMessage.asStateFlow()

    // 비밀번호 확인
    private val _userInputPasswordCheck = MutableStateFlow("")
    val userInputPasswordCheck = _userInputPasswordCheck.asStateFlow()
    fun setUserInputPasswordCheck(passwordCheck: String){
        _userInputPasswordCheck.value = passwordCheck
    }
    private val _userInputPwCheckValidationMessage = MutableStateFlow("")
    val userInputPwCheckValidationMessage = _userInputPwCheckValidationMessage.asStateFlow()

    // 입력한 내용 유효성 검사
    fun validateStep1UserInput(): Boolean {
        // 에러 초기화
        _userInputEmailValidationMessage.value = ""
        _userInputPwValidationMessage.value = ""
        _userInputPwCheckValidationMessage.value = ""

        var result = true

        if(_userInputEmail.value.isEmpty()){
            _userInputEmailValidationMessage.value = "이메일을 입력해주세요."
            result = false
        }
        if(_userInputEmail.value.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(_userInputEmail.value).matches()){
            _userInputEmailValidationMessage.value = "올바른 이메일 형식이 아닙니다."
            result = false
        }
        if(_userInputPassword.value.isEmpty()){
            _userInputPwValidationMessage.value = "비밀번호를 입력해주세요."
            result = false
        }
        if(_userInputPassword.value.isNotEmpty() && !Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]{8,20}$", userInputPassword.value)){
            _userInputPwValidationMessage.value = "영문, 숫자, 특수문자가 포함된 비밀번호를 8~20자로 입력해주세요."
            result = false
        }
        if(_userInputPasswordCheck.value.isEmpty()){
            _userInputPwCheckValidationMessage.value = "비밀번호 확인을 입력해주세요."
            result = false
        }
        if(_userInputPassword.value.isNotEmpty() && _userInputPasswordCheck.value.isNotEmpty() &&  _userInputPassword.value != _userInputPasswordCheck.value){
            _userInputPwCheckValidationMessage.value = "비밀번호가 일치하지 않습니다."
            result = false
        }

        return result
    }

    // 입력값 초기화
    fun resetStep1States(){
        _userInputEmail.value = ""
        _userInputEmailValidationMessage.value = ""
        _userInputPassword.value = ""
        _userInputPwValidationMessage.value = ""
        _userInputPasswordCheck.value = ""
        _userInputPwCheckValidationMessage.value = ""
        _isEmailVerified.value = false
    }



    // ================2. 이메일 인증 관련==============================================================

    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified
    fun resetEmailVerified(){
        _isEmailVerified.value = false
    }

    // 메일 인증 여부 체크
    fun checkFirebaseEmailValidation(moveNext: (boolean: Boolean) -> Unit) {
        viewModelScope.launch {
            _auth.currentUser?.let { user ->
                user.reload().await()
                (user.isEmailVerified).let { result ->
                    _isEmailVerified.value = result
                    moveNext(result)
                }
            }
        }
    }

    // 인증 메일 발송
    fun sendEmailVerification(){
        try {
            _auth.currentUser?.sendEmailVerification()
        }catch (e: Exception){
            Log.e("sendEmailAuth", "${e.message}")
            _userInputEmailValidationMessage.value = "인증 메일 발송 실패"
        }
    }

}
package kr.co.lion.modigm.ui.join.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.regex.Pattern

class JoinStep1ViewModel: ViewModel() {

    // 이메일
    val userEmail = MutableLiveData<String>()
    // 이메일 유효성 검사
    val emailValidation = MutableLiveData("")

    // 비밀번호
    val userPassword = MutableLiveData<String>()
    // 비밀번호 유효성 검사
    val pwValidation = MutableLiveData("")

    // 비밀번호 확인
    val userPasswordCheck = MutableLiveData<String>()
    // 비밀번호 확인 유효성 검사
    val pwCheckValidation = MutableLiveData("")

    // 입력한 내용 유효성 검사
    fun validate(): Boolean {
        // 에러 초기화
        emailValidation.value = ""
        pwValidation.value = ""
        pwCheckValidation.value = ""

        var result = true

        if(userEmail.value.isNullOrEmpty()){
            emailValidation.value = "이메일을 입력해주세요."
            result = false
        }
        if(!userEmail.value.isNullOrEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail.value).matches()){
            emailValidation.value = "올바른 이메일 형식이 아닙니다."
            result = false
        }
        if(userPassword.value.isNullOrEmpty()){
            pwValidation.value = "비밀번호를 입력해주세요."
            result = false
        }
        if(!userPassword.value.isNullOrEmpty() && !Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]{8,20}$", userPassword.value)){
            pwValidation.value = "영문, 숫자, 특수문자가 포함된 비밀번호를 8~20자로 입력해주세요."
            result = false
        }
        if(userPasswordCheck.value.isNullOrEmpty()){
            pwCheckValidation.value = "비밀번호 확인을 입력해주세요."
            result = false
        }
        if(!userPassword.value.isNullOrEmpty() && !userPasswordCheck.value.isNullOrEmpty() &&  userPassword.value != userPasswordCheck.value){
            pwCheckValidation.value = "비밀번호가 일치하지 않습니다."
            result = false
        }

        return result
    }

    // 입력값 초기화
    fun reset(){
        userEmail.value = ""
        emailValidation.value = ""
        userPassword.value = ""
        pwValidation.value = ""
        userPasswordCheck.value = ""
        pwCheckValidation.value = ""
    }

}
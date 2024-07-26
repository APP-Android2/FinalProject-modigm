package kr.co.lion.modigm.ui.join.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.regex.Pattern

class JoinStep1ViewModel: ViewModel() {

    // 이메일
    val userEmail = MutableStateFlow("")
    // 이메일 유효성 검사
    val emailValidation = MutableStateFlow("")

    // 비밀번호
    val userPassword = MutableStateFlow("")
    // 비밀번호 유효성 검사
    val pwValidation = MutableStateFlow("")

    // 비밀번호 확인
    val userPasswordCheck = MutableStateFlow("")
    // 비밀번호 확인 유효성 검사
    val pwCheckValidation = MutableStateFlow("")

    // 입력한 내용 유효성 검사
    fun validate(): Boolean {
        // 에러 초기화
        emailValidation.value = ""
        pwValidation.value = ""
        pwCheckValidation.value = ""

        var result = true

        if(userEmail.value.isEmpty()){
            emailValidation.value = "이메일을 입력해주세요."
            result = false
        }
        if(userEmail.value.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail.value).matches()){
            emailValidation.value = "올바른 이메일 형식이 아닙니다."
            result = false
        }
        if(userPassword.value.isEmpty()){
            pwValidation.value = "비밀번호를 입력해주세요."
            result = false
        }
        if(userPassword.value.isNotEmpty() && !Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]{8,20}$", userPassword.value)){
            pwValidation.value = "영문, 숫자, 특수문자가 포함된 비밀번호를 8~20자로 입력해주세요."
            result = false
        }
        if(userPasswordCheck.value.isEmpty()){
            pwCheckValidation.value = "비밀번호 확인을 입력해주세요."
            result = false
        }
        if(userPassword.value.isNotEmpty() && userPasswordCheck.value.isNotEmpty() &&  userPassword.value != userPasswordCheck.value){
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
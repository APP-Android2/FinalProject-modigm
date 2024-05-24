package kr.co.lion.modigm.ui.join.vm

import android.text.InputFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.regex.Pattern

class JoinStep2ViewModel: ViewModel() {

    // 이름
    val userName = MutableLiveData<String>()
    // 이름 유효성 검사
    val nameValidation = MutableLiveData("")

    // 전화번호
    val userPhone = MutableLiveData<String>()
    // 전화번호 유효성 검사
    val phoneValidation = MutableLiveData("")

    // 인증번호입력
    val inputSmsCode = MutableLiveData<String>()
    // 인증번호입력 유효성 검사
    val inputSmsCodeValidation = MutableLiveData("")

    // 인증번호
    private val _smsCode = MutableLiveData("1234")
    val smsCode: LiveData<String> = _smsCode


    // 유효성 검사
    fun validate(): Boolean {
        // 에러 표시 초기화
        nameValidation.value =""
        phoneValidation.value =""
        inputSmsCodeValidation.value =""
        var result = true

        if(userName.value.isNullOrEmpty()){
            nameValidation.value = "이름을 입력해주세요."
            result = false
        }
        if(userPhone.value.isNullOrEmpty()){
            phoneValidation.value = "전화번호를 입력해주세요."
            result = false
        }else{
            if(!Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", userPhone.value)){
                phoneValidation.value = "올바른 전화번호가 아닙니다."
                result = false
            }
        }
        if(inputSmsCode.value.isNullOrEmpty()){
            inputSmsCodeValidation.value = "인증번호를 입력해주세요."
            result = false
        }else{
            if(smsCode.value != inputSmsCode.value){
                inputSmsCodeValidation.value = "인증번호가 일치하지 않습니다."
                result = false
            }
        }

        return result
    }

    fun checkPhoneValidation(): Boolean {
        // 에러 표시 초기화
        phoneValidation.value =""
        var result = true

        if(userPhone.value.isNullOrEmpty()){
            userPhone.value = "전화번호를 입력해주세요."
            result = false
        }else{
            if(!Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", userPhone.value)){
                userPhone.value = "올바른 전화번호가 아닙니다."
                result = false
            }
        }
        return result
    }

    // 이름을 한글만 입력 가능하게
    fun filterOnlyKorean(): InputFilter {
        return InputFilter { source, start, end, dest, dstart, dend ->
            val ps = Pattern.compile("^[ㄱ-ㅣ가-힣]*$")
            if (!ps.matcher(source).matches()) {
                return@InputFilter ""
            }
            null
        }
    }
}
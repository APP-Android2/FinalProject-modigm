package kr.co.lion.modigm.ui.profile.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChangePwViewModel: ViewModel() {
    // 파이어베이스 인증
    private val _auth = FirebaseAuth.getInstance()
    // 현재 유저
    private val _user = _auth.currentUser

    // 현재 비밀번호
    val oldPw = MutableLiveData<String>()
    // 현재 비밀번호 에러 메시지
    val oldPwError = MutableLiveData<String>()

    // 새로운 비밀번호
    val newPw = MutableLiveData<String>()
    // 현재 비밀번호 에러 메시지
    val newPwError = MutableLiveData<String>()

    // 새로운 비밀번호 확인
    val newPwCheck = MutableLiveData<String>()
    // 현재 비밀번호 에러 메시지
    val newPwCheckError = MutableLiveData<String>()

    // 비밀번호 변경 결과
    private var _changePwResult = MutableLiveData<ChangePwErrorMessage>()
    val changePwResult: LiveData<ChangePwErrorMessage> = _changePwResult


    // 비밀번호 유효성 검사
    private fun checkValidation(): Boolean{
        oldPwError.value = ""
        newPwError.value = ""
        newPwCheckError.value = ""

        var result = false

        if(oldPw.value.isNullOrEmpty()) {
            oldPwError.value = "현재 비밀번호를 입력해주세요."
            result = true
        }
        if(newPw.value.isNullOrEmpty()) {
            newPwError.value = "새로운 비밀번호를 입력해주세요."
            result = true
        }
        if(newPwCheck.value.isNullOrEmpty()) {
            newPwCheckError.value = "새로운 비밀번호 확인을 입력해주세요."
            result = true
        }
        if(newPw.value != newPwCheck.value) {
            newPwCheckError.value = "새로운 비밀번호가 일치하지 않습니다."
            result = true
        }

        return result
    }

    // 비밀번호 변경
    suspend fun changePw() {
        // 유효성 검사
        if(checkValidation()){
            _changePwResult.value = ChangePwErrorMessage.VALIDATE_FAIL
            return
        }

        try{
            _auth.signInWithEmailAndPassword(_user?.email?:"", oldPw.value?:"").await()
        }catch (e: Exception){
            oldPwError.value = "현재 비밀번호가 일치하지 않습니다."
            _changePwResult.value = ChangePwErrorMessage.LOGIN_FAIL
            return
        }

        // 새로운 비밀번호로 업데이트
        try {
            _user?.updatePassword(newPw.value?:"")?.await()
            _changePwResult.value = ChangePwErrorMessage.CHANGE_PW_SUCCESS
        }catch (e: Exception){
            _changePwResult.value = ChangePwErrorMessage.CHANGE_PW_FAIL
        }
    }
}

enum class ChangePwErrorMessage(var str:String) {
    VALIDATE_FAIL("입력 내용을 다시 확인해주세요"),
    LOGIN_FAIL("현재 비밀번호가 일치하지 않습니다."),
    CHANGE_PW_FAIL("비밀번호 변경에 실패했습니다. 잠시 후 다시 시도해주세요"),
    CHANGE_PW_SUCCESS("비밀번호 변경 완료")
}
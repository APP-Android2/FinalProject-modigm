package kr.co.lion.modigm.ui.login.vm

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import kr.co.lion.modigm.repository.LoginRepository

class FindEmailViewModel : ViewModel() {

    private val tag by lazy { "FindEmailViewModel" }
    private val loginRepository by lazy { LoginRepository() }

    // 찾은 이메일
    private val _emailResult = MutableLiveData<String>()
    val email: LiveData<String> = _emailResult

    // 이름 에러
    private val _nameError = MutableLiveData<Throwable>()
    val nameError: LiveData<Throwable> = _nameError

    // 연락처 에러
    private val _phoneError = MutableLiveData<Throwable>()
    val phoneError: LiveData<Throwable> = _phoneError

    // 인증번호 에러
    private val _inputCodeError = MutableLiveData<Throwable>()
    val inputCodeError: LiveData<Throwable> = _inputCodeError

    // 전화번호 인증에 필요 onCodeSent에서 전달받음
    private val _verificationId = MutableLiveData<String>()
    val verificationId: LiveData<String> = _verificationId

    private val _resendToken = MutableLiveData<PhoneAuthProvider.ForceResendingToken>()
    private val resendToken: LiveData<PhoneAuthProvider.ForceResendingToken> get() = _resendToken

    // 이름, 연락처, 문자 발송까지 모두 확인되면
    private val _isComplete = MutableLiveData<Boolean>()
    val isComplete: MutableLiveData<Boolean> = _isComplete

    /**
     * isComplete 값을 설정하는 메서드
     * @param value 설정할 값
     */
    fun isCompleteTo(value: Boolean) {
        viewModelScope.launch {
            _isComplete.postValue(value)
        }
    }

    /**
     * 이름과 전화번호를 확인하고 인증 코드를 발송하는 메서드
     * @param activity 액티비티 컨텍스트
     * @param userName 사용자의 이름
     * @param userPhone 사용자의 전화번호
     */
    fun checkNameAndPhone(activity: Activity, userName: String, userPhone: String) {
        Log.d(tag, "checkNameAndPhone 호출됨. userName: $userName, userPhone: $userPhone")
        viewModelScope.launch {
            val resultPhone = loginRepository.getUserDataByUserPhone(userPhone)
            resultPhone.onSuccess { userData ->
                Log.d(tag, "전화번호 확인 성공. userData: $userData")
                if (userData.userName == userName) {
                    Log.d(tag, "이름 일치 확인됨. 인증 문자 발송 시작.")
                    // 이름이 일치하면 인증 문자 발송
                    val resultAuth = loginRepository.sendPhoneAuthCode(activity, userPhone)
                    resultAuth.onSuccess { result ->
                        val verificationId = result.first
                        val credential = result.second as? com.google.firebase.auth.PhoneAuthCredential
                        val token = result.third as? PhoneAuthProvider.ForceResendingToken
                        Log.d(tag, "인증 문자 발송 성공.")
                        _verificationId.postValue(verificationId)
                        _isComplete.postValue(true)

                    }.onFailure { e ->
                        Log.e(tag, "인증 문자 발송 실패. 오류: ${e.message}", e)
                        _phoneError.postValue(e)
                    }
                } else {
                    Log.e(tag, "이름 불일치. 입력한 이름: $userName, DB 이름: ${userData.userName}")
                    _nameError.postValue(Throwable("일치하는 이름이 없습니다."))
                }
            }.onFailure { e ->
                Log.e(tag, "전화번호 확인 실패. 오류: ${e.message}", e)
                _phoneError.postValue(Throwable("등록되지 않은 전화번호입니다."))
            }
        }
    }

    /**
     * 인증 코드를 확인하고 이메일을 찾는 메서드
     * @param verificationId 인증 ID
     * @param inputCode 입력한 인증 코드
     */
    fun checkCodeAndFindEmail(verificationId: String, inputCode: String) {
        Log.d(tag, "checkCodeAndFindEmail 호출됨. verificationId: $verificationId, inputCode: $inputCode")
        viewModelScope.launch {
            val result = loginRepository.getEmailByInputCode(verificationId, inputCode)
            result.onSuccess { email ->
                Log.d(tag, "인증번호 확인 성공. email: $email")
                _emailResult.value = email
                _isComplete.postValue(true)
            }.onFailure { e ->
                Log.e(tag, "인증번호 확인 실패. 오류: ${e.message}", e)
                _inputCodeError.postValue(Throwable("인증번호가 잘못되었습니다."))
            }
        }
    }

    fun clearData(){
        _emailResult.postValue("")
        _isComplete.postValue(false)
        _nameError.postValue(null)
        _phoneError.postValue(null)
        _inputCodeError.postValue(null)
        _verificationId.postValue("")
        _resendToken.postValue(null)
    }
}

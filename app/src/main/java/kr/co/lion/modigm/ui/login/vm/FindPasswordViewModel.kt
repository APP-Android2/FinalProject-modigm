package kr.co.lion.modigm.ui.login.vm

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.repository.LoginRepository

class FindPasswordViewModel: ViewModel() {

    // 태그
    private val logTag by lazy { FindPasswordViewModel::class.simpleName }

    // 로그인 레포지토리
    private val loginRepository by lazy { LoginRepository() }

    // 이메일 에러
    private val _emailInputError = MutableLiveData<Throwable?>()
    val emailInputError: LiveData<Throwable?> = _emailInputError

    // 연락처 에러
    private val _phoneInputError = MutableLiveData<Throwable?>()
    val phoneInputError: LiveData<Throwable?> = _phoneInputError

    // 인증번호 에러
    private val _authCodeError = MutableLiveData<Throwable?>()
    val authCodeError: LiveData<Throwable?> = _authCodeError

    // 전화번호 인증에 필요 onCodeSent에서 전달받음
    private val _verificationId = MutableLiveData<String>()
    val verificationId: LiveData<String> = _verificationId

    // 이름, 연락처, 문자 발송까지 모두 확인되면
    private val _isComplete = MutableLiveData<Boolean>()
    val isComplete: LiveData<Boolean> = _isComplete

    // 새 비밀번호 에러
    private val _newPasswordError = MutableLiveData<Throwable?>()
    val newPasswordError: MutableLiveData<Throwable?> = _newPasswordError

    // 새 비밀번호 확인 에러
    private val _newPasswordConfirmError = MutableLiveData<Throwable?>()
    val newPasswordConfirmError: MutableLiveData<Throwable?> = _newPasswordConfirmError

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
     * 이메일이 DB에 등록된 회원 정보에 있는지 확인하고 등록된 이메일과 입력한 이메일을 매칭 (FindPasswordFragment)
     * @param activity 액티비티 컨텍스트
     * @param userEmail 사용자의 이메일
     * @param userPhone 사용자의 전화번호
     */
    fun checkEmailAndPhone(activity: Activity, userEmail: String, userPhone: String) {
        viewModelScope.launch {
            val emailResult = loginRepository.getUserDataByUserEmail(userEmail)
            emailResult.onSuccess { userData ->
                if (userData.userEmail == userEmail) {
                    val authResult = loginRepository.sendPhoneAuthCode(activity, userPhone)
                    authResult.onSuccess { result ->
                        val (verificationId, credential, resendToken) = result
                        Log.d(logTag, "인증 문자 발송 성공.")
                        _verificationId.postValue(verificationId)
                        Log.d(logTag, "인증 문자 발송 성공. credential: $credential")
                        _isComplete.postValue(true)
                    }.onFailure { e ->
                        Log.e(logTag, "인증 문자 발송 실패. 오류: ${e.message}", e)
                        _phoneInputError.postValue(e)
                    }
                } else {
                    Log.e(logTag, "이메일 불일치. 입력한 이메일: $userEmail, DB 이메일: ${userData.userEmail}")
                    _emailInputError.postValue(Throwable("일치하는 이메일이 없습니다."))
                }
            }.onFailure {
                Log.e(logTag, "이메일 확인 실패. 오류: ${it.message}", it)
                _emailInputError.postValue(Throwable("등록되지 않은 이메일입니다."))
            }
        }
    }

    /**
     * 인증번호 확인 (FindPasswordAuthFragment)
     * @param verificationId 인증 ID
     * @param authCode 입력한 인증 코드
     */
    fun checkByAuthCode(verificationId: String, authCode: String) {
        Log.d(logTag, "checkCodeAndFindPW 호출됨. verificationId: $verificationId, authCode: $authCode")
        viewModelScope.launch {
            val signInResult = loginRepository.signInByAuthCode(verificationId, authCode)
            signInResult.onSuccess { signIn ->
                Log.d(logTag, "로그인 성공. signIn: $signIn")
                _verificationId.value = verificationId
                _isComplete.postValue(true)
            }.onFailure { e ->
                Log.e(logTag, "인증번호 확인 실패. 오류: ${e.message}", e)
                _authCodeError.postValue(Throwable("인증번호가 잘못되었습니다."))
            }
        }
    }

    /**
     * 비밀번호 변경
     * @param newPassword 새로운 비밀번호
     */
    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            if (newPassword.isNotEmpty()) {
                val updateResult = loginRepository.updatePassword(newPassword)
                updateResult.onSuccess {
                    Log.d(logTag, "비밀번호 변경 성공.")
                    _isComplete.postValue(true)
                }.onFailure { e ->
                    Log.e(logTag, "비밀번호 변경 실패. 오류: ${e.message}", e)
                    _newPasswordError.postValue(e)
                }
            }
        }
    }

    fun authLogout() {
        val result = loginRepository.authLogout()
        result.onSuccess {
            Log.d(logTag, "로그아웃 성공.")
        }.onFailure { e ->
            Log.e(logTag, "로그아웃 실패. 오류: ${e.message}", e)
        }
    }

    /**
     * 데이터 초기화 메서드
     */
    fun clearData() {
        _verificationId.postValue("")
        _isComplete.postValue(false)
        _emailInputError.postValue(null)
        _phoneInputError.postValue(null)
        _authCodeError.postValue(null)
        _newPasswordError.postValue(null)
        _newPasswordConfirmError.postValue(null)
    }
}

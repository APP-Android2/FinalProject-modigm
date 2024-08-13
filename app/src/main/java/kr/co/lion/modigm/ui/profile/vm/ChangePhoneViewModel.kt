package kr.co.lion.modigm.ui.profile.vm

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import kr.co.lion.modigm.repository.LoginRepository
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs
import kr.co.lion.modigm.util.toNationalPhoneNumber

class ChangePhoneViewModel : ViewModel() {

    private val tag by lazy { ChangePhoneViewModel::class.simpleName }
    private val loginRepository by lazy { LoginRepository() }

    // 전화번호 인증에 필요 onCodeSent에서 전달받음
    private val _verificationId = MutableLiveData<String>()
    val verificationId: LiveData<String> = _verificationId

    // 현재 사용자의 전화번호
    private val _currentUserPhone = MutableLiveData<String>()
    val currentUserPhone: LiveData<String> = _currentUserPhone


    // 비밀번호 에러
    private val _passwordInputError = MutableLiveData<Throwable?>()
    val passwordInputError: LiveData<Throwable?> = _passwordInputError

    // 소셜 재인증 에러
    private val _socialReAuthError = MutableLiveData<Throwable?>()
    val socialReAuthError: MutableLiveData<Throwable?> = _socialReAuthError

    // 연락처 에러
    private val _phoneInputError = MutableLiveData<Throwable?>()
    val phoneInputError: LiveData<Throwable?> = _phoneInputError

    // 인증번호 에러
    private val _authCodeInputError = MutableLiveData<Throwable?>()
    val authCodeInputError: LiveData<Throwable?> = _authCodeInputError


    // 비밀번호 확인 완료
    private val _isPasswordComplete = MutableLiveData<Boolean>()
    val isPasswordComplete: LiveData<Boolean> = _isPasswordComplete

    // 소셜 재인증 완료
    private val _isSocialReAuthComplete = MutableLiveData<Boolean>()
    val isSocialReAuthComplete: MutableLiveData<Boolean> = _isSocialReAuthComplete

    // 인증번호 발송 완료
    private val _isAuthCodeComplete = MutableLiveData<Boolean>()
    val isAuthCodeComplete: LiveData<Boolean> = _isAuthCodeComplete

    // 전화번호 변경 완료
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
     * isPasswordComplete 값을 설정하는 메서드
     * @param value 설정할 값
     */
    fun isPasswordCompleteTo(value: Boolean) {
        viewModelScope.launch {
            _isPasswordComplete.postValue(value)
        }
    }

    /**
     * isSocialReAuthComplete 값을 설정하는 메서드
     * @param value 설정할 값
     */
    fun isSocialReAuthCompleteTo(value: Boolean) {
        viewModelScope.launch {
            _isPasswordComplete.postValue(value)
        }
    }

    /**
     * 이메일 로그인 유저의 비밀번호를 확인하는 메서드
     * @param userPassword 사용자의 비밀번호
     */
    fun checkPassword(userPassword: String) {
        Log.d(tag, "checkPassword 호출됨. userPassword: $userPassword")
        viewModelScope.launch {
            val result = loginRepository.checkPassword(userPassword)
            result.onSuccess { currentUserPhone ->
                Log.d(tag, "currentUserPhone: $currentUserPhone")
                _currentUserPhone.postValue(currentUserPhone.toNationalPhoneNumber())
                Log.d(tag, "비밀번호 확인 성공.")
                _isPasswordComplete.postValue(true)

            }.onFailure { e ->
                Log.e(tag, "비밀번호 확인 실패. 오류: ${e.message}", e)
                _passwordInputError.postValue(Throwable("비밀번호가 일치하지 않습니다."))
            }
        }
    }

    // 소셜 로그인 유저가 제공한 이메일로 인증하는 메서드
    fun socialLoginReAuthenticate(context: Activity, currentUserProvider: String) {
        Log.d(tag, "socialLoginReAuthenticate 호출됨.")
        viewModelScope.launch {
            val result = when (currentUserProvider) {
                JoinType.KAKAO.provider -> {
                    loginRepository.reAuthenticateWithKakao(context)
                }
                JoinType.GITHUB.provider -> {
                    loginRepository.reAuthenticateWithGithub(context)
                }
                else -> {
                    throw IllegalArgumentException("지원하지 않는 제공자입니다.")
                }
            }
            result.onSuccess { currentUserPhone ->
                Log.d(tag, "소셜 로그인 재인증 성공")
                _currentUserPhone.postValue(currentUserPhone.toNationalPhoneNumber())
                _isSocialReAuthComplete.postValue(true)
            }.onFailure { e ->
                Log.e(tag, "소셜 로그인 재인증 실패: ${e.message}", e)
                _socialReAuthError.postValue(e)
            }
        }
    }

    /**
     * 이름과 전화번호를 확인하고 인증 코드를 발송하는 메서드
     * @param activity 액티비티 컨텍스트
     * @param userPhone 사용자의 전화번호
     */
    fun checkPhone(activity: Activity, userPhone: String) {
        Log.d(tag, "checkPhone 호출됨. userPhone: $userPhone")
        viewModelScope.launch {
            val resultAuth = loginRepository.sendPhoneAuthCode(activity, userPhone)
            resultAuth.onSuccess { result ->
                val verificationId = result.first
                val credential = result.second as? com.google.firebase.auth.PhoneAuthCredential
                val token = result.third as? PhoneAuthProvider.ForceResendingToken
                Log.d(tag, "인증 문자 발송 성공.")
                _verificationId.postValue(verificationId)
                _isAuthCodeComplete.postValue(true)
            }.onFailure { e ->
                Log.e(tag, "인증 문자 발송 실패. 오류: ${e.message}", e)
                _phoneInputError.postValue(Throwable("인증번호가 잘못되었습니다."))
            }
        }
    }

    /**
     * 인증 코드를 확인하고 전화번호를 변경하는 메서드
     * @param currentUserPhone 현재 사용자의 전화번호
     * @param newUserPhone 새로운 사용자의 전화번호
     * @param verificationId 인증 코드의 식별자
     * @param authCode 사용자가 입력한 인증 코드
     */
    fun updatePhone(currentUserPhone: String, newUserPhone: String, verificationId: String, authCode: String) {
        Log.d(tag, "checkCode 호출됨. authCode: $authCode")
        viewModelScope.launch {
            val userIdx = prefs.getInt("currentUserIdx")
            val resultDataSource = loginRepository.updatePhone(userIdx, currentUserPhone, newUserPhone, verificationId, authCode)
            resultDataSource.onSuccess {
                Log.d(tag, "전화번호 변경 성공.")
                _isComplete.postValue(true)
                }.onFailure { e ->
                Log.e(tag, "전화번호 변경 실패. 오류: ${e.message}", e)
                _authCodeInputError.postValue(e)
            }
        }
    }
    fun authLogout(){
        val result = loginRepository.authLogout()
        result.onSuccess {
            Log.d(tag, "로그아웃 성공.")
        }.onFailure { e ->
            Log.e(tag, "로그아웃 실패. 오류: ${e.message}", e)
        }
    }

    fun clearData(){
        _verificationId.postValue("")
        _currentUserPhone.postValue("")
        _isPasswordComplete.postValue(false)
        _isSocialReAuthComplete.postValue(false)
        _isAuthCodeComplete.postValue(false)
        _isComplete.postValue(false)
        _passwordInputError.postValue(null)
        _socialReAuthError.postValue(null)
        _phoneInputError.postValue(null)
        _authCodeInputError.postValue(null)

    }

}

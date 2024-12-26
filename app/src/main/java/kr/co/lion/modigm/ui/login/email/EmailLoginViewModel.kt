package kr.co.lion.modigm.ui.login.email

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kr.co.lion.modigm.repository.LoginRepository
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class EmailLoginViewModel: ViewModel() {

    private val loginRepository by lazy { LoginRepository() }

    private val _isLoading = MutableLiveData(false)
    val isLoading : LiveData<Boolean> = _isLoading

    // 이메일 로그인
    private val _emailLoginResult = MutableLiveData<Boolean>()
    val emailLoginResult: LiveData<Boolean> = _emailLoginResult

    private val _emailLoginError = MutableLiveData<Throwable?>()
    val emailLoginError: LiveData<Throwable?> = _emailLoginError

    fun emailLogin(email: String, password: String, autoLoginValue: Boolean) {
        _isLoading.value = true
        _emailLoginResult.postValue(false)
        viewModelScope.launch {
            val result = loginRepository.emailLogin(email, password)
            result.onSuccess { userIdx ->
                _isLoading.postValue(false)
                prefs.setBoolean(
                    key = "autoLogin",
                    value = autoLoginValue
                )
                prefs.setString(
                    key = "currentUserProvider",
                    value = JoinType.EMAIL.provider
                )
                prefs.setInt(
                    key = "currentUserIdx",
                    value = userIdx
                )
                _emailLoginResult.postValue(true)

                // 로그인 성공 후 즉시 FCM 토큰 등록
                registerFcmTokenToServer(userIdx)
            }.onFailure { e ->
                _isLoading.postValue(false)
                prefs.clearAllPrefs()
                _emailLoginResult.postValue(false)
                // 예외 처리 및 사용자에게 전달할 메시지 설정
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        "이메일 또는 비밀번호가 잘못되었습니다. 다시 확인해 주세요."
                    }

                    is FirebaseAuthInvalidUserException -> {
                        "존재하지 않는 계정입니다. 회원가입을 진행해 주세요."
                    }

                    is FirebaseTooManyRequestsException -> {
                        "로그인 요청이 많아 지연되었습니다. 잠시 후 다시 시도해 주세요."
                    }

                    is FirebaseNetworkException -> {
                        "네트워크 연결이 불안정합니다. 잠시 후 다시 시도해 주세요."
                    }

                    else -> {
                        "로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."
                    }
                }
                _emailLoginError.postValue(Exception(errorMessage, e))
            }
        }
    }

    // 뷰모델 데이터 초기화
    fun clearViewModelData() {
        _isLoading.postValue(false)
        _emailLoginResult.postValue(false)
        _emailLoginError.postValue(null)
    }

    private fun registerFcmTokenToServer(userIdx: Int) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }
            val token = task.result
            if (token != null) {
                viewModelScope.launch {
                    runCatching {
                        val response = loginRepository.registerFcmToken(userIdx, token)
                        response.onSuccess { result ->
                            if(result) {
                                Log.d("LoginViewModel", "FCM 토큰 등록 성공 userIdx: $userIdx, token: $token")
                            } else {
                                Log.e("LoginViewModel", "FCM 토큰 등록 실패 userIdx: $userIdx")
                            }
                        }
                    }
                }
            } else {
                Log.e("LoginViewModel", "FCM Token is null")
            }
        }
    }
}
package kr.co.lion.modigm.ui.login.email

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kr.co.lion.modigm.repository.LoginRepository
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class EmailLoginViewModel: ViewModel() {

    private val loginRepository by lazy { LoginRepository() }

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail

    fun onUserEmailChange(newEmail: String) {
        _userEmail.value = newEmail
    }

    private val _userPassword = MutableStateFlow("")
    val userPassword: StateFlow<String> = _userPassword

    fun onUserPasswordChange(newPassword: String) {
        _userPassword.value = newPassword
    }

    private val _isChecked = MutableStateFlow(false)
    val isChecked: StateFlow<Boolean> = _isChecked

    fun onCheckedChange(isChecked: Boolean) {
        _isChecked.value = isChecked
    }

    val isLoginEnabled: StateFlow<Boolean> = combine(_userEmail, _userPassword) { email, password ->
        email.isNotBlank() && password.isNotBlank()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _emailLoginResult = MutableStateFlow(false)
    val emailLoginResult: StateFlow<Boolean> = _emailLoginResult

    private val _emailLoginError = MutableStateFlow<Throwable?>(null)
    val emailLoginError: StateFlow<Throwable?> = _emailLoginError

    fun clearLoginError() {
        _emailLoginError.value = null
    }

    private val _emailInputError = MutableStateFlow<String>("")
    val emailInputError: StateFlow<String> = _emailInputError

    private val _passwordInputError = MutableStateFlow<String>("")
    val passwordInputError: StateFlow<String> = _passwordInputError

    fun clearAllInputError() {
        _emailInputError.value = ""
        _passwordInputError.value = ""
    }
    fun checkAllInputAndSetError(): Boolean {
        _emailInputError.value = if (!isEmailValid()) "올바른 이메일 형식이 아닙니다." else ""
        _passwordInputError.value = if (!isPasswordValid()) "비밀번호는 6자 이상이어야 합니다." else ""

        _focusField.value = when {
            !isEmailValid() -> FocusTarget.EMAIL_INPUT
            !isPasswordValid() -> FocusTarget.PASSWORD_INPUT
            else -> null
        }

        return isEmailValid() && isPasswordValid()
    }
    private fun isEmailValid(): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(_userEmail.value).matches()
    }
    private fun isPasswordValid(): Boolean {
        return _userPassword.value.length >= 6
    }

    private val _focusField = MutableStateFlow<FocusTarget?>(null)
    val focusField: StateFlow<FocusTarget?> = _focusField

    fun emailLogin(userEmail: String, userPassword: String, autoLoginValue: Boolean) {
        _isLoading.value = true
        _emailLoginResult.value = false
        viewModelScope.launch {
            val result = loginRepository.emailLogin(userEmail, userPassword)
            result.onSuccess { userIdx ->
                _isLoading.value = false
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
                registerFcmTokenToServer(userIdx)
                _emailLoginResult.value = true

            }.onFailure { e ->
                _isLoading.value = false
                prefs.clearAllPrefs()
                _emailLoginResult.value = false
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
                _emailLoginError.value = Throwable(errorMessage, e)
            }
        }
    }

    // 뷰모델 데이터 초기화
    fun clearViewModelData() {
        _isLoading.value = false
        _emailLoginResult.value = false
        _emailLoginError.value = null
        _emailInputError.value = ""
        _passwordInputError.value = ""
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


enum class FocusTarget { EMAIL_INPUT, PASSWORD_INPUT }
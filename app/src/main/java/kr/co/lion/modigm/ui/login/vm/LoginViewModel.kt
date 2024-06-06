package kr.co.lion.modigm.ui.login.vm

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.repository.UserInfoRepository
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication
import kr.co.lion.modigm.util.PreferenceUtil
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LoginViewModel : ViewModel() {
    private val userInfoRepository = UserInfoRepository() // UserInfoRepository 초기화
    private val prefs: PreferenceUtil = ModigmApplication.prefs // SharedPreferences 인스턴스 가져오기

    private val _loginResult = MutableLiveData<LoginResult>() // 로그인 결과를 저장하는 LiveData
    val loginResult: LiveData<LoginResult> = _loginResult // 외부에서 접근 가능한 로그인 결과 LiveData

    private val _kakaoLoginResult = MutableLiveData<LoginResult>() // 카카오 로그인 결과를 저장하는 LiveData
    val kakaoLoginResult: LiveData<LoginResult> = _kakaoLoginResult // 외부에서 접근 가능한 카카오 로그인 결과 LiveData

    private val _githubLoginResult = MutableLiveData<LoginResult>() // 깃허브 로그인 결과를 저장하는 LiveData
    val githubLoginResult: LiveData<LoginResult> = _githubLoginResult // 외부에서 접근 가능한 깃허브 로그인 결과 LiveData

    private val _loginFormState = MutableLiveData<LoginFormState>() // 로그인 폼 상태를 저장하는 LiveData
    val loginFormState: LiveData<LoginFormState> = _loginFormState // 외부에서 접근 가능한 로그인 폼 상태 LiveData

    private val _kakaoCustomToken = MutableLiveData<String?>() // 커스텀 토큰을 저장하는 LiveData
    val kakaoCustomToken: LiveData<String?> = _kakaoCustomToken // 외부에서 접근 가능한 커스텀 토큰 LiveData

    private val _credential = MutableLiveData<AuthCredential?>() // credential을 저장하는 LiveData
    val credential: LiveData<AuthCredential?> = _credential // 외부에서 접근 가능한 credential LiveData

    private val _joinType = MutableLiveData<JoinType>() // JoinType을 저장하는 LiveData
    val joinType: LiveData<JoinType> = _joinType // 외부에서 접근 가능한 JoinType LiveData

    // ----------------- 로그인 유효성 검사 -----------------

    // 로그인 데이터 변경 처리
    fun loginDataChanged(email: String, password: String) {
        val isEmailValid = isEmailValid(email)
        val isPasswordValid = isPasswordValid(password)
        val emailError = when {
            email.isEmpty() -> "이메일을 입력해주세요"
            !isEmailValid -> "형식에 맞는 이메일을 입력해주세요"
            else -> null
        }
        _loginFormState.value = LoginFormState(
            emailError = emailError,
            passwordError = if (!isPasswordValid && password.isNotEmpty()) "비밀번호는 6자리 이상 입력해주세요" else null,
            isDataValid = isEmailValid && password.length >= 6
        )
    }

    // 이메일 유효성 검사
    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // 비밀번호 유효성 검사
    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }
    // ----------------- 로그인 유효성 검사 끝-----------------

    // ----------------- 이메일/비밀번호 로그인 처리 -----------------
    fun login(email: String, password: String, autoLogin: Boolean) {
        _loginResult.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "자동 로그인 상태: $autoLogin")
                Log.d("LoginViewModel", "로그인 시도 중... 이메일: $email")
                val authResult = userInfoRepository.loginWithEmailPassword(email, password)
                val uid = authResult.getOrNull()?.user?.uid
                if (uid != null) {
                    Log.d("LoginViewModel", "로그인 성공 - 사용자 UID: $uid")
                    val isRegistered = userInfoRepository.isUserAlreadyRegistered(uid)
                    if (isRegistered) {
                        _loginResult.postValue(LoginResult.Success)
                        if (autoLogin) {
                            saveCurrentUserData(uid)
                        } else {
                            clearCurrentUserData()
                        }
                    } else {
                        _joinType.postValue(JoinType.EMAIL)
                        _loginResult.postValue(LoginResult.NeedSignUp)
                    }
                } else {
                    Log.e("LoginViewModel", "로그인 실패 - 사용자 UID를 가져올 수 없음")
                    _loginFormState.postValue(LoginFormState(emailError = "일치하는 이메일이 없습니다.", passwordError = null, isDataValid = false))
                    _loginResult.postValue(LoginResult.Error(Exception("로그인 실패 - 사용자 UID를 가져올 수 없음")))
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "로그인 시도 중 예외 발생", e)
                _loginFormState.postValue(LoginFormState(emailError = "비밀번호가 일치하지 않습니다.", passwordError = null, isDataValid = false))
                _loginResult.postValue(LoginResult.Error(e))
            }
        }
    }


    // ----------------- 이메일/비밀번호 로그인 처리 끝 -----------------

    // ----------------- Shared Prefereces 처리 -----------------

    // 유저 정보를 SharedPreferences에 저장
    private suspend fun saveCurrentUserData(uid:String) {
        val userData = userInfoRepository.loadUserData(uid)
        if (userData != null) {
            Log.d("LoginViewModel","saveCurrentUser 저장중 : $userData")
            prefs.setUserData("currentUserData", userData)
        }
    }

    // 유저 정보를 SharedPreferences에서 제거
    private fun clearCurrentUserData() {
        prefs.clearUserData("currentUserData")
    }

    // SharedPreferences에서 유저 정보를 불러옴
    private fun getCurrentUserData(): UserData? {
        return prefs.getUserData("currentUserData")
    }

    // 메인 화면 자동 로그인 처리 메소드
    fun attemptAutoLogin(context: Context) {
        val user = getCurrentUserData()

        Log.i("LoginViewModel", "attemptAutoLogin: 자동 로그인 시도 $user")
        if (user != null) {
            val userAccess = FirebaseAuth.getInstance().currentUser?.uid == user.userUid
            _loginResult.value = LoginResult.Loading
            viewModelScope.launch {
                try {
                    when (user.userProvider) {
                        "kakao" -> {
                            if (userAccess) {
                                Log.i("LoginViewModel", "attemptAutoLogin: 카카오 자동 로그인 성공")
                                _kakaoLoginResult.postValue(LoginResult.Success)
                                _loginResult.postValue(LoginResult.Success) // 추가
                            } else {
                                loginWithKakao(context, true)
                            }
                        }
                        "github" -> {
                            if (userAccess) {
                                Log.i("LoginViewModel", "attemptAutoLogin: 깃허브 자동 로그인 성공")
                                _githubLoginResult.postValue(LoginResult.Success)
                                _loginResult.postValue(LoginResult.Success) // 추가
                            } else {
                                loginWithGithub(context, true)
                            }
                        }
                        "email" -> {
                            if (userAccess) {
                                Log.i("LoginViewModel", "attemptAutoLogin: 이메일 자동 로그인 성공")
                                _loginResult.postValue(LoginResult.Success)
                            } else {
                                _loginResult.postValue(LoginResult.Error(Exception("이메일 자동 로그인 실패")))
                            }
                        }
                        else -> {
                            Log.e("LoginViewModel", "attemptAutoLogin: 알 수 없는 사용자 제공자")
                            _loginResult.postValue(LoginResult.Error(Exception("알 수 없는 사용자 제공자")))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "attemptAutoLogin: 자동 로그인 시도 중 에러 발생", e)
                    _loginResult.postValue(LoginResult.Error(e))
                }
            }
        } else {
            Log.e("LoginViewModel", "attemptAutoLogin: 자동 로그인 데이터 없음")
            _loginResult.postValue(LoginResult.Error(Exception("자동 로그인 데이터 없음")))
        }
    }


    // ----------------- Shared Prefereces 처리 끝 -----------------

    // ----------------- 카카오 로그인 처리 -----------------

    fun loginWithKakao(context: Context, autoLogin: Boolean) {
        _kakaoLoginResult.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                val token = if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                    loginWithKakaoTalk(context)
                } else {
                    loginWithKakaoAccount(context)
                }
                handleKakaoResponse(token, autoLogin)
            } catch (e: Exception) {
                Log.e("LoginViewModel", "loginWithKakao: 로그인 시도 중 에러 발생", e)
                _kakaoLoginResult.postValue(LoginResult.Error(e))
            }
        }
    }

    private suspend fun loginWithKakaoTalk(context: Context): OAuthToken = suspendCancellableCoroutine { cont ->
        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
            if (error != null) {
                Log.e("LoginViewModel", "loginWithKakaoTalk: 카카오톡 로그인 중 에러 발생", error)
                cont.resumeWithException(error)
            } else if (token != null) {
                Log.i("LoginViewModel", "loginWithKakaoTalk: 카카오톡 로그인 성공")
                cont.resume(token)
            } else {
                Log.e("LoginViewModel", "loginWithKakaoTalk: Unknown error")
                cont.resumeWithException(Throwable("Unknown error"))
            }
        }
    }

    private suspend fun loginWithKakaoAccount(context: Context): OAuthToken = suspendCancellableCoroutine { cont ->
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            if (error != null) {
                Log.e("LoginViewModel", "loginWithKakaoAccount: 카카오 계정 로그인 중 에러 발생", error)
                cont.resumeWithException(error)
            } else if (token != null) {
                Log.i("LoginViewModel", "loginWithKakaoAccount: 카카오 계정 로그인 성공")
                cont.resume(token)
            } else {
                Log.e("LoginViewModel", "loginWithKakaoAccount: Unknown error")
                cont.resumeWithException(Throwable("Unknown error"))
            }
        }
    }

    private suspend fun handleKakaoResponse(token: OAuthToken?, autoLogin: Boolean) {
        if (token != null) {
            try {
                val customToken = userInfoRepository.getKakaoCustomToken(token.accessToken)
                val uid = userInfoRepository.signInWithCustomToken(customToken)
                Log.i("LoginViewModel", "handleKakaoResponse: Firebase 인증 성공")
                Log.i("LoginViewModel", "Custom Token: $customToken")
                val isRegistered = userInfoRepository.isUserAlreadyRegistered(uid)

                if (autoLogin) {
                    saveCurrentUserData(uid)
                } else {
                    clearCurrentUserData()
                }

                if (isRegistered) {
                    _kakaoLoginResult.postValue(LoginResult.Success)
                } else {
                    _kakaoCustomToken.postValue(customToken)
                    _joinType.postValue(JoinType.KAKAO)
                    _kakaoLoginResult.postValue(LoginResult.NeedSignUp)
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "handleKakaoResponse: Firebase 인증 중 에러 발생", e)
                _kakaoLoginResult.postValue(LoginResult.Error(e))
            }
        } else {
            Log.e("LoginViewModel", "handleKakaoResponse: Invalid OAuthToken")
            _kakaoLoginResult.postValue(LoginResult.Error(Throwable("Invalid OAuthToken")))
        }
    }
    // ----------------- 카카오 로그인 처리 끝 -----------------

    // ----------------- 깃허브 로그인 처리 -----------------

    fun loginWithGithub(context: Context, autoLogin: Boolean) {
        _githubLoginResult.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                val authResult = userInfoRepository.signInWithGithub(context as Activity)
                Log.i("LoginViewModel", "GitHub 로그인 성공: ${authResult.user?.uid}")
                val credential = authResult.credential
                val uid = authResult.user?.uid
                if (credential != null && uid != null) {
                    val isRegistered = userInfoRepository.isUserAlreadyRegistered(uid)

                    if (autoLogin) {
                        saveCurrentUserData(uid)
                    } else {
                        clearCurrentUserData()
                    }

                    if (isRegistered) {
                        _githubLoginResult.postValue(LoginResult.Success)
                    } else {
                        _credential.postValue(credential)
                        _joinType.postValue(JoinType.GITHUB)
                        _githubLoginResult.postValue(LoginResult.NeedSignUp)
                    }
                } else {
                    _githubLoginResult.postValue(LoginResult.Error(Exception("GitHub 로그인 실패: credential 또는 UID가 null입니다.")))
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "GitHub 로그인 실패", e)
                _githubLoginResult.postValue(LoginResult.Error(e))
            }
        }
    }

    // ----------------- 깃허브 로그인 처리 끝 -----------------

}


// 로그인 결과를 나타내는 sealed class
sealed class LoginResult {
    object Loading : LoginResult() // 로딩 상태를 나타내는 객체
    object Success : LoginResult() // 성공 상태를 나타내는 객체
    object NeedSignUp : LoginResult() // 회원가입이 필요한 상태를 나타내는 객체
    data class Error(val exception: Throwable) : LoginResult() // 에러 상태를 나타내는 데이터 클래스, 발생한 예외를 포함
}

// 로그인 폼 상태를 나타내는 data class
data class LoginFormState(
    val emailError: String? = null, // 이메일 입력 에러 메시지, 없으면 null
    val passwordError: String? = null, // 비밀번호 입력 에러 메시지, 없으면 null
    val isDataValid: Boolean = false // 폼 데이터의 유효성을 나타내는 불리언 값, 기본값은 false
)

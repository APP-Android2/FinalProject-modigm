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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
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

    private val _kakaoLoginResult = MutableLiveData<LoginResult>() // 카카오 로그인 결과를 저장하는 LiveData
    val kakaoLoginResult: LiveData<LoginResult> = _kakaoLoginResult // 외부에서 접근 가능한 카카오 로그인 결과 LiveData

    private val _githubLoginResult = MutableLiveData<LoginResult>() // 깃허브 로그인 결과를 저장하는 LiveData
    val githubLoginResult: LiveData<LoginResult> = _githubLoginResult // 외부에서 접근 가능한 깃허브 로그인 결과 LiveData

    private val _autoLoginResult = MutableLiveData<LoginResult>() // 자동로그인 결과를 저장하는 LiveData
    val autoLoginResult: LiveData<LoginResult> = _autoLoginResult // 외부에서 접근 가능한 로그인 결과 LiveData

    private val _emailLoginResult = MutableLiveData<LoginResult>() // 이메일 로그인 결과를 저장하는 LiveData
    val emailLoginResult: LiveData<LoginResult> = _emailLoginResult // 외부에서 접근 가능한 이메일 로그인 결과 LiveData

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
        _emailLoginResult.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "로그인 시도 중... 이메일: $email")
                val authResult = userInfoRepository.loginWithEmailPassword(email, password)

                if (authResult.isFailure) {
                    val exception = authResult.exceptionOrNull()
                    when (exception) {
                        is FirebaseAuthInvalidUserException -> {
                            Log.e("LoginViewModel", "이메일이 존재하지 않습니다.", exception)
                            _loginFormState.postValue(LoginFormState(emailError = "일치하는 이메일이 없습니다.", passwordError = null))
                            _emailLoginResult.postValue(LoginResult.Error(exception))
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            Log.e("LoginViewModel", "비밀번호가 일치하지 않습니다.", exception)
                            _loginFormState.postValue(LoginFormState(passwordError = "비밀번호가 일치하지 않습니다.", emailError = null))
                            _emailLoginResult.postValue(LoginResult.Error(exception))
                        }
                        else -> {
                            Log.e("LoginViewModel", "로그인 시도 중 예외 발생", exception)
                            _emailLoginResult.postValue(LoginResult.Error(exception ?: Exception("알 수 없는 에러 발생")))
                        }
                    }
                } else {
                    if (authResult.isSuccess) {
                        val uid = authResult.getOrNull()?.user?.uid ?: ""
                        Log.d("LoginViewModel", "로그인 성공 - 사용자 UID: $uid")
                        val isRegistered = userInfoRepository.isUserAlreadyRegistered(uid)
                        if (isRegistered) {
                            saveCurrentUserData(uid, "email", email)
                            _emailLoginResult.postValue(LoginResult.Success)
                            if (autoLogin) {
                                prefs.setAutoLogin(true)  // 자동 로그인 설정 저장
                            }
                        } else {
                            _joinType.postValue(JoinType.EMAIL)
                            _emailLoginResult.postValue(LoginResult.NeedSignUp)
                        }
                    } else {
                        Log.e("LoginViewModel", "로그인 실패: 이메일이 존재하지 않음 (uid가 null)")
                        _loginFormState.postValue(LoginFormState(emailError = "일치하는 이메일이 없습니다.", passwordError = null))
                        _emailLoginResult.postValue(LoginResult.Error(Exception("로그인 실패: 이메일이 존재하지 않음")))
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "로그인 시도 중 예외 발생", e)
                _emailLoginResult.postValue(LoginResult.Error(e))
            }
        }
    }

    // ----------------- 이메일/비밀번호 로그인 처리 끝 -----------------

    // ----------------- Shared Preferences 처리 -----------------

    // 유저 정보를 SharedPreferences에 저장
    private suspend fun saveCurrentUserData(uid: String, provider: String, email: String) {
        val userData = userInfoRepository.loadUserData(uid)
        if (userData != null) {
            val updatedUserData = userData.copy(userProvider = provider, userEmail = email)
            prefs.setUserData("currentUserData", updatedUserData)
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
        val autoLogin = prefs.getAutoLogin()

        if (!autoLogin) {
            Log.i("LoginViewModel", "attemptAutoLogin: 자동 로그인 설정이 꺼져 있습니다.")
            _autoLoginResult.postValue(LoginResult.Error(Exception("자동 로그인 설정이 꺼져 있습니다.")))
            return
        }

        val user = getCurrentUserData()

        Log.i("LoginViewModel", "attemptAutoLogin: 자동 로그인 시도 $user")
        if (user != null) {
            val userAccess = FirebaseAuth.getInstance().currentUser?.uid == user.userUid
            _autoLoginResult.value = LoginResult.Loading
            viewModelScope.launch {
                try {
                    when (user.userProvider) {
                        "kakao" -> {
                            if (userAccess) {
                                Log.i("LoginViewModel", "attemptAutoLogin: 카카오 자동 로그인 성공")
                                _kakaoLoginResult.postValue(LoginResult.Success)
                            } else {
                                loginWithKakao(context)
                            }
                        }
                        "github" -> {
                            if (userAccess) {
                                Log.i("LoginViewModel", "attemptAutoLogin: 깃허브 자동 로그인 성공")
                                _githubLoginResult.postValue(LoginResult.Success)
                            } else {
                                loginWithGithub(context)
                            }
                        }
                        "email" -> {
                            if (userAccess) {
                                Log.i("LoginViewModel", "attemptAutoLogin: 이메일 자동 로그인 성공")
                                _emailLoginResult.postValue(LoginResult.Success)
                            } else {
                                _emailLoginResult.postValue(LoginResult.Error(Exception("이메일 자동 로그인 실패")))
                            }
                        }
                        else -> {
                            Log.e("LoginViewModel", "attemptAutoLogin: 알 수 없는 사용자 제공자")
                            _autoLoginResult.postValue(LoginResult.Error(Exception("알 수 없는 사용자 제공자")))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "attemptAutoLogin: 자동 로그인 시도 중 에러 발생", e)
                    _autoLoginResult.postValue(LoginResult.Error(e))
                }
            }
        } else {
            Log.e("LoginViewModel", "attemptAutoLogin: 자동 로그인 데이터 없음")
            _autoLoginResult.postValue(LoginResult.Error(Exception("자동 로그인 데이터 없음")))
        }
    }

    // ----------------- Shared Preferences 처리 끝 -----------------

    // ----------------- 카카오 로그인 처리 -----------------

    fun loginWithKakao(context: Context) {
        _kakaoLoginResult.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                val token = if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                    loginWithKakaoTalk(context)
                } else {
                    loginWithKakaoAccount(context)
                }
                handleKakaoResponse(token)
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

    private suspend fun handleKakaoResponse(token: OAuthToken?) {
        if (token != null) {
            try {
                val customToken = userInfoRepository.getKakaoCustomToken(token.accessToken)
                val uid = userInfoRepository.signInWithCustomToken(customToken)
                Log.i("LoginViewModel", "handleKakaoResponse: Firebase 인증 성공")
                Log.i("LoginViewModel", "Custom Token: $customToken")
                val isRegistered = userInfoRepository.isUserAlreadyRegistered(uid)
                // 카카오 사용자 정보 요청
                UserApiClient.instance.me { user, error ->
                    if (error != null) {
                        Log.e("LoginViewModel", "사용자 정보 요청 실패", error)
                        _kakaoLoginResult.postValue(LoginResult.Error(error))
                    } else if (user != null) {
                        val kakaoEmail = user.kakaoAccount?.email ?: ""
                        Log.i("LoginViewModel", "사용자 정보 요청 성공 - 이메일: $kakaoEmail")
                        viewModelScope.launch {
                            if (isRegistered) {
                                saveCurrentUserData(uid, "kakao", kakaoEmail)
                                prefs.setAutoLogin(true)  // 카카오 자동 로그인 설정 저장
                                _kakaoLoginResult.postValue(LoginResult.Success)
                            } else {
                                _kakaoCustomToken.postValue(customToken)
                                _joinType.postValue(JoinType.KAKAO)
                                _kakaoLoginResult.postValue(LoginResult.NeedSignUp)
                            }
                        }
                    }
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

    fun loginWithGithub(context: Context) {
        _githubLoginResult.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                val authResult = userInfoRepository.signInWithGithub(context as Activity)
                Log.i("LoginViewModel", "GitHub 로그인 성공: ${authResult.user?.uid}")
                val credential = authResult.credential
                val uid = authResult.user?.uid
                if (credential != null && uid != null) {
                    val isRegistered = userInfoRepository.isUserAlreadyRegistered(uid)

                    if (isRegistered) {
                        saveCurrentUserData(uid, "github", authResult.user?.email.toString())
                        prefs.setAutoLogin(true)  // 깃허브 자동 로그인 설정 저장
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

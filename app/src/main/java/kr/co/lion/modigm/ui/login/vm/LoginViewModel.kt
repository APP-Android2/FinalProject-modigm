package kr.co.lion.modigm.ui.login.vm

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kr.co.lion.modigm.repository.UserInfoRepository
import kr.co.lion.modigm.util.JoinType
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LoginViewModel : ViewModel() {
    private val userInfoRepository = UserInfoRepository() // UserInfoRepository 초기화

    private val _loginResult = MutableLiveData<LoginResult>() // 로그인 결과를 저장하는 LiveData
    val loginResult: LiveData<LoginResult> = _loginResult // 외부에서 접근 가능한 로그인 결과 LiveData

    private val _kakaoLoginResult = MutableLiveData<LoginResult>() // 카카오 로그인 결과를 저장하는 LiveData
    val kakaoLoginResult: LiveData<LoginResult> = _kakaoLoginResult // 외부에서 접근 가능한 카카오 로그인 결과 LiveData

    private val _githubLoginResult = MutableLiveData<LoginResult>() // 깃허브 로그인 결과를 저장하는 LiveData
    val githubLoginResult: LiveData<LoginResult> = _githubLoginResult // 외부에서 접근 가능한 깃허브 로그인 결과 LiveData

    private val _loginFormState = MutableLiveData<LoginFormState>() // 로그인 폼 상태를 저장하는 LiveData
    val loginFormState: LiveData<LoginFormState> = _loginFormState // 외부에서 접근 가능한 로그인 폼 상태 LiveData

    private val _customToken = MutableLiveData<String?>() // 커스텀 토큰을 저장하는 LiveData
    val customToken: LiveData<String?> = _customToken // 외부에서 접근 가능한 커스텀 토큰 LiveData

    private val _credential = MutableLiveData<AuthCredential?>() // credential을 저장하는 LiveData
    val credential: LiveData<AuthCredential?> = _credential // 외부에서 접근 가능한 credential LiveData

    private val _joinType = MutableLiveData<JoinType>() // JoinType을 저장하는 LiveData
    val joinType: LiveData<JoinType> = _joinType // 외부에서 접근 가능한 JoinType LiveData

    // ----------------- 로그인 유효성 검사 -----------------

    // 로그인 데이터 변경 처리
    fun loginDataChanged(email: String, password: String) {
        // 이메일 에러 메시지 설정
        val emailError = when {
            email.isEmpty() -> "이메일 주소를 입력하세요." // 이메일이 비어있을 때의 에러 메시지
            !isEmailValid(email) -> "유효한 이메일 주소를 입력하세요." // 이메일 형식이 유효하지 않을 때의 에러 메시지
            else -> null // 이메일이 유효하면 에러 메시지가 없음
        }

        if (emailError != null) {
            // 이메일 에러가 있을 경우 로그인 폼 상태를 이메일 에러만 설정하여 업데이트
            _loginFormState.value = LoginFormState(
                emailError = emailError,
                passwordError = null,
                isDataValid = false
            )
        } else {
            // 이메일이 유효할 때 비밀번호 에러 메시지 설정
            val passwordError = when {
                password.isEmpty() -> "비밀번호를 입력하세요." // 비밀번호가 비어있을 때의 에러 메시지
                !isPasswordValid(password) -> "비밀번호는 6자 이상이어야 합니다." // 비밀번호가 6자 이상이 아닐 때의 에러 메시지
                else -> null // 비밀번호가 유효하면 에러 메시지가 없음
            }

            // 로그인 폼 상태를 비밀번호 에러와 유효성 상태를 설정하여 업데이트
            _loginFormState.value = LoginFormState(
                emailError = null,
                passwordError = passwordError,
                isDataValid = passwordError == null
            )
        }
    }

    // 이메일 유효성 검사
    private fun isEmailValid(email: String): Boolean {
        // 이메일이 유효한 형식인지 검사
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // 비밀번호 유효성 검사
    private fun isPasswordValid(password: String): Boolean {
        // 비밀번호가 6자 이상인지 검사
        return password.length > 5
    }
    // ----------------- 로그인 유효성 검사 끝-----------------
    // ----------------- 이메일/비밀번호 로그인 처리 -----------------
    fun login(email: String, password: String) {
        _loginResult.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "로그인 시도 중... 이메일: $email")
                val authResult = userInfoRepository.loginWithEmailPassword(email, password)
                val userUid = authResult.getOrNull()?.user?.uid
                if (userUid != null) {
                    Log.d("LoginViewModel", "로그인 성공 - 사용자 UID: $userUid")
                    _loginResult.postValue(LoginResult.Success)
                    _customToken.postValue(userUid)
                } else {
                    Log.e("LoginViewModel", "로그인 실패 - 사용자 UID를 가져올 수 없음")
                    _loginResult.postValue(LoginResult.Error(Exception("로그인 실패 - 사용자 UID를 가져올 수 없음")))
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "로그인 시도 중 예외 발생", e)
                _loginResult.postValue(LoginResult.Error(e))
            }
        }
    }
    // ----------------- 이메일/비밀번호 로그인 처리 끝 -----------------

    // ----------------- 카카오 로그인 처리 -----------------

    // 카카오 로그인 처리
    fun loginWithKakao(context: Context) {
        // 카카오 로그인 진행 중임을 나타내는 상태 설정
        _kakaoLoginResult.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                // 카카오톡 로그인 가능 여부에 따라 로그인 방법 선택
                val token = if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                    loginWithKakaoTalk(context) // 카카오톡으로 로그인
                } else {
                    loginWithKakaoAccount(context) // 카카오 계정으로 로그인
                }
                // 로그인 응답 처리
                handleKakaoResponse(token)
            } catch (e: Exception) {
                // 로그인 시도 중 에러 발생 시 로그 출력 및 에러 상태 설정
                Log.e("LoginViewModel", "loginWithKakao: 로그인 시도 중 에러 발생", e)
                _kakaoLoginResult.postValue(LoginResult.Error(e))
            }
        }
    }

    // 카카오톡 로그인
    private suspend fun loginWithKakaoTalk(context: Context): OAuthToken = suspendCancellableCoroutine { cont ->
        // UserApiClient를 사용하여 카카오톡으로 로그인 시도
        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
            if (error != null) {
                // 로그인 중 에러 발생 시 로그 출력 및 예외로 종료
                Log.e("LoginViewModel", "loginWithKakaoTalk: 카카오톡 로그인 중 에러 발생", error)
                cont.resumeWithException(error)
            } else if (token != null) {
                // 로그인 성공 시 성공 로그 출력 및 토큰으로 이어서 처리
                Log.i("LoginViewModel", "loginWithKakaoTalk: 카카오톡 로그인 성공")
                cont.resume(token)
            } else {
                // 알 수 없는 에러 발생 시 로그 출력 및 예외로 종료
                Log.e("LoginViewModel", "loginWithKakaoTalk: Unknown error")
                cont.resumeWithException(Throwable("Unknown error"))
            }
        }
    }

    // 카카오 계정 로그인
    private suspend fun loginWithKakaoAccount(context: Context): OAuthToken = suspendCancellableCoroutine { cont ->
        // UserApiClient를 사용하여 카카오 계정으로 로그인 시도
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            if (error != null) {
                // 로그인 중 에러 발생 시 로그 출력 및 예외로 종료
                Log.e("LoginViewModel", "loginWithKakaoAccount: 카카오 계정 로그인 중 에러 발생", error)
                cont.resumeWithException(error)
            } else if (token != null) {
                // 로그인 성공 시 성공 로그 출력 및 토큰으로 이어서 처리
                Log.i("LoginViewModel", "loginWithKakaoAccount: 카카오 계정 로그인 성공")
                cont.resume(token)
            } else {
                // 알 수 없는 에러 발생 시 로그 출력 및 예외로 종료
                Log.e("LoginViewModel", "loginWithKakaoAccount: Unknown error")
                cont.resumeWithException(Throwable("Unknown error"))
            }
        }
    }

    // 카카오 로그인 응답 처리
    private suspend fun handleKakaoResponse(token: OAuthToken?) {
        if (token != null) {
            try {
                // Firebase Custom Token을 얻기 위해 Kakao 액세스 토큰 사용
                val customToken = userInfoRepository.getKakaoCustomToken(token.accessToken)

                // Firebase Custom Token으로 로그인
                userInfoRepository.signInWithCustomToken(customToken)

                // Firebase 인증 성공 로그 출력
                Log.i("LoginViewModel", "handleKakaoResponse: Firebase 인증 성공")
                Log.i("LoginViewModel", "Custom Token: $customToken")

                // 카카오 로그인 결과를 성공 상태로 업데이트
                _kakaoLoginResult.postValue(LoginResult.Success)

                // Custom Token을 LiveData에 저장
                _customToken.postValue(customToken)

                // 가입 유형을 KAKAO로 설정
                _joinType.postValue(JoinType.KAKAO)
            } catch (e: Exception) {
                // Firebase 인증 중 에러 발생 시 로그 출력 및 에러 상태로 업데이트
                Log.e("LoginViewModel", "handleKakaoResponse: Firebase 인증 중 에러 발생", e)
                _kakaoLoginResult.postValue(LoginResult.Error(e))
            }
        } else {
            // 유효하지 않은 OAuthToken 에러 로그 출력 및 에러 상태로 업데이트
            Log.e("LoginViewModel", "handleKakaoResponse: Invalid OAuthToken")
            _kakaoLoginResult.postValue(LoginResult.Error(Throwable("Invalid OAuthToken")))
        }
    }
    // ----------------- 카카오 로그인 처리 끝-----------------
    // ----------------- 깃허브 로그인 처리 -----------------

    // 깃허브 로그인 처리
    fun loginWithGithub(context: Context) {
        // 로그인 진행 중임을 나타내는 상태 설정
        _githubLoginResult.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                // 깃허브 로그인 시도 및 결과 대기
                val credential = userInfoRepository.signInWithGithub(context as Activity)
                Log.i("LoginViewModel", "GitHub credential: $credential")

                // 인증 정보가 null이 아닌 경우
                if (credential != null) {
                    _credential.postValue(credential) // 인증 정보를 LiveData에 저장
                    _joinType.postValue(JoinType.GITHUB) // 가입 유형을 GITHUB로 설정
                    _githubLoginResult.postValue(LoginResult.Success) // 로그인 성공 상태 설정
                } else {
                    // 인증 정보가 null인 경우 에러 상태 설정
                    _githubLoginResult.postValue(LoginResult.Error(Exception("GitHub token is null")))
                }
            } catch (e: Exception) {
                // 로그인 실패 시 에러 로그 출력 및 에러 상태 설정
                Log.e("LoginViewModel", "GitHub 로그인 실패", e)
                _githubLoginResult.postValue(LoginResult.Error(e))
            }
        }
    }
    // ----------------- 깃허브 로그인 처리 끝-----------------
}

// 로그인 결과를 나타내는 sealed class
sealed class LoginResult {
    object Loading : LoginResult() // 로딩 상태를 나타내는 객체
    object Success : LoginResult() // 성공 상태를 나타내는 객체
    data class Error(val exception: Throwable) : LoginResult() // 에러 상태를 나타내는 데이터 클래스, 발생한 예외를 포함
}

// 로그인 폼 상태를 나타내는 data class
data class LoginFormState(
    val emailError: String? = null, // 이메일 입력 에러 메시지, 없으면 null
    val passwordError: String? = null, // 비밀번호 입력 에러 메시지, 없으면 null
    val isDataValid: Boolean = false // 폼 데이터의 유효성을 나타내는 불리언 값, 기본값은 false
)

package kr.co.lion.modigm.ui.login.vm

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.repository.UserInfoRepository
import kr.co.lion.modigm.util.JoinType
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LoginViewModel : ViewModel() {
    private val userInfoRepository = UserInfoRepository()  // UserInfoRepository 초기화

    private val auth: FirebaseAuth by lazy { Firebase.auth }  // FirebaseAuth 초기화
    private val functions: FirebaseFunctions by lazy { Firebase.functions("asia-northeast3") }  // FirebaseFunctions 초기화

    private val _kakaoLoginResult = MutableLiveData<LoginResult>()  // 카카오 로그인 결과를 저장하는 LiveData
    val kakaoLoginResult: LiveData<LoginResult> = _kakaoLoginResult  // 외부에서 접근 가능한 카카오 로그인 결과 LiveData

    private val _githubLoginResult = MutableLiveData<LoginResult>()  // 깃허브 로그인 결과를 저장하는 LiveData
    val githubLoginResult: LiveData<LoginResult> = _githubLoginResult  // 외부에서 접근 가능한 깃허브 로그인 결과 LiveData

    private val _loginFormState = MutableLiveData<LoginFormState>()  // 로그인 폼 상태를 저장하는 LiveData
    val loginFormState: LiveData<LoginFormState> = _loginFormState  // 외부에서 접근 가능한 로그인 폼 상태 LiveData

    private val _customToken = MutableLiveData<String?>()  // 커스텀 토큰을 저장하는 LiveData
    val customToken: LiveData<String?> = _customToken  // 외부에서 접근 가능한 커스텀 토큰 LiveData

    private val _joinType = MutableLiveData<JoinType>()  // JoinType을 저장하는 LiveData
    val joinType: LiveData<JoinType> = _joinType  // 외부에서 접근 가능한 JoinType LiveData

    // ----------------- 로그인 유효성 검사 -----------------

    // 로그인 데이터 변경 처리
    fun loginDataChanged(email: String, password: String) {
        // 이메일 에러 메시지 설정
        val emailError = if (email.isEmpty()) {
            // 이메일이 비어있을 때의 에러 메시지
            "이메일 주소를 입력하세요."
        } else if (!isEmailValid(email)) {
            // 이메일 형식이 유효하지 않을 때의 에러 메시지
            "유효한 이메일 주소를 입력하세요."
        } else {
            // 이메일이 유효하면 'null'을 반환하여 에러 메시지가 없음을 나타낸다.
            null
        }

        // 이메일 에러가 있을 경우 로그인 폼 상태를 이메일 에러만 설정하여 업데이트
        if (emailError != null) {
            _loginFormState.value = LoginFormState(
                emailError = emailError,
                passwordError = null,
                isDataValid = false
            )
        } else {
            // 이메일이 유효할 때 비밀번호 에러 메시지 설정
            val passwordError = if (password.isEmpty()) {
                // 비밀번호가 비어있을 때의 에러 메시지
                "비밀번호를 입력하세요."
            } else if (!isPasswordValid(password)) {
                // 비밀번호가 6자 이상이 아닐 때의 에러 메시지
                "비밀번호는 6자 이상이어야 합니다."
            } else {
                // 비밀번호가 유효하면 'null'을 반환하여 에러 메시지가 없음을 나타낸다.
                null
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

    // ----------------- 카카오 로그인 처리 -----------------

    // 카카오 로그인 처리
    fun loginWithKakao(context: Context) {
        _kakaoLoginResult.value = LoginResult.Loading  // 로딩 상태 설정
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
                _kakaoLoginResult.postValue(LoginResult.Error(e))  // 에러 발생 시 에러 상태 설정
            }
        }
    }

    // 카카오톡 로그인
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

    // 카카오 계정 로그인
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

    // 카카오 로그인 응답 처리
    private suspend fun handleKakaoResponse(token: OAuthToken?) {
        if (token != null) {
            try {
                val customToken = getKakaoCustomToken(token.accessToken)
                auth.signInWithCustomToken(customToken).await()  // Firebase Custom Token으로 로그인

                Log.i("LoginViewModel", "handleKakaoResponse: Firebase 인증 성공")
                Log.i("LoginViewModel", "Custom Token: $customToken")  // 로그 추가
                _kakaoLoginResult.postValue(LoginResult.Success)  // 성공 상태 설정
                _customToken.postValue(customToken)
                _joinType.postValue(JoinType.KAKAO)
            } catch (e: Exception) {
                Log.e("LoginViewModel", "handleKakaoResponse: Firebase 인증 중 에러 발생", e)
                _kakaoLoginResult.postValue(LoginResult.Error(e))  // 에러 발생 시 에러 상태 설정
            }
        } else {
            Log.e("LoginViewModel", "handleKakaoResponse: Invalid OAuthToken")
            _kakaoLoginResult.postValue(LoginResult.Error(Throwable("Invalid OAuthToken")))
        }
    }

    // Firebase Functions를 통해 Custom Token 획득
    private suspend fun getKakaoCustomToken(accessToken: String): String {
        val data = hashMapOf("token" to accessToken)
        return try {
            val result = functions.getHttpsCallable("getKakaoCustomAuth").call(data).await()
            val customToken = result.data as Map<*, *>
            Log.i("LoginViewModel", "getKakaoCustomToken: 커스텀 토큰 획득 성공")
            customToken["custom_token"] as String
        } catch (e: Exception) {
            Log.e("LoginViewModel", "getKakaoCustomToken: 커스텀 토큰 획득 중 에러 발생", e)
            throw Exception("Failed to get custom token: ${e.message}", e)
        }
    }

    // ----------------- 깃허브 로그인 처리 -----------------

    // 깃허브 로그인 처리
    fun loginWithGithub(context: Context) {
        _githubLoginResult.value = LoginResult.Loading
        viewModelScope.launch {
            try {
                val provider = OAuthProvider.newBuilder("github.com")
                val authResult = auth.startActivityForSignInWithProvider(context as Activity, provider.build()).await()
                val token = authResult.user?.getIdToken(false)?.await()?.token
                Log.i("LoginViewModel", "GitHub Token: $token")  // 로그 추가
                if (token != null) {
                    _customToken.postValue(token)
                    _joinType.postValue(JoinType.GITHUB)
                    _githubLoginResult.postValue(LoginResult.Success)
                } else {
                    _githubLoginResult.postValue(LoginResult.Error(Exception("GitHub token is null")))
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "GitHub 로그인 실패", e)
                _githubLoginResult.postValue(LoginResult.Error(e))
            }
        }
    }
}

// 로그인 결과를 나타내는 sealed class
sealed class LoginResult {
    object Loading : LoginResult()
    object Success : LoginResult()
    data class Error(val exception: Throwable) : LoginResult()
}

// 로그인 폼 상태를 나타내는 data class
data class LoginFormState(
    val emailError: String? = null,
    val passwordError: String? = null,
    val isDataValid: Boolean = false
)

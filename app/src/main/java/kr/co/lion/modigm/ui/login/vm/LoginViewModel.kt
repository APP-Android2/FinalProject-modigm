package kr.co.lion.modigm.ui.login.vm

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
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

class LoginViewModel : ViewModel() {

    // 태그
    private val logTag by lazy { LoginViewModel::class.simpleName }

    // 로그인 레포지토리
    private val loginRepository by lazy { LoginRepository() }

    // 카카오 로그인 결과를 담는 LiveData
    private val _kakaoLoginResult = MutableLiveData<Boolean>()
    val kakaoLoginResult: LiveData<Boolean> = _kakaoLoginResult

    // 깃허브 로그인 결과를 담는 LiveData
    private val _githubLoginResult = MutableLiveData<Boolean>()
    val githubLoginResult: LiveData<Boolean> = _githubLoginResult

    // 이메일 로그인 결과를 담는 LiveData
    private val _emailLoginResult = MutableLiveData<Boolean>()
    val emailLoginResult: LiveData<Boolean> = _emailLoginResult

    // 이메일 자동로그인 결과를 담는 LiveData
    private val _emailAutoLoginResult = MutableLiveData<Boolean>()
    val emailAutoLoginResult: LiveData<Boolean> = _emailAutoLoginResult


    // 깃허브 회원가입 결과를 담는 LiveData
    private val _githubJoinResult = MutableLiveData<Boolean>()
    val githubJoinResult: LiveData<Boolean> = _githubJoinResult

    // 카카오 회원가입 결과를 담는 LiveData
    private val _kakaoJoinResult = MutableLiveData<Boolean>()
    val kakaoJoinResult: LiveData<Boolean> = _kakaoJoinResult


    // 카카오 로그인 에러를 담는 LiveData
    private val _kakaoLoginError = MutableLiveData<Throwable?>()
    val kakaoLoginError: LiveData<Throwable?> = _kakaoLoginError

    // 깃허브 로그인 에러를 담는 LiveData
    private val _githubLoginError = MutableLiveData<Throwable?>()
    val githubLoginError: LiveData<Throwable?> = _githubLoginError

    // 이메일 로그인 에러를 담는 LiveData
    private val _emailLoginError = MutableLiveData<Throwable?>()
    val emailLoginError: LiveData<Throwable?> = _emailLoginError

    // 자동 로그인 에러를 담는 LiveData
    private val _autoLoginError = MutableLiveData<Throwable?>()
    val autoLoginError: LiveData<Throwable?> = _autoLoginError

    // 현재 사용자의 인덱스를 가져오는 함수
    private fun getCurrentUserIdx(): Int {
        return prefs.getInt("currentUserIdx")
    }

    // 현재 사용자의 인덱스를 설정하는 함수
    private fun setCurrentUserIdx(userIdx: Int) {
        prefs.setInt("currentUserIdx", userIdx)
    }

    // 자동 로그인 상태를 가져오는 함수
    private fun getAutoLogin(): Boolean {
        return prefs.getBoolean("autoLogin")
    }

    // 자동 로그인 상태를 설정하는 함수
    private fun setAutoLogin(autoLogin: Boolean) {
        prefs.setBoolean("autoLogin", autoLogin)
    }

    // 현재 사용자의 제공자를 가져오는 함수
    private fun getCurrentUserProvider(): String {
        return prefs.getString("currentUserProvider")
    }

    // 현재 사용자의 제공자를 설정하는 함수
    private fun setCurrentUserProvider(userProvider: String) {
        prefs.setString("currentUserProvider", userProvider)
    }

    // 현재 사용자 정보를 초기화하는 함수
    private fun clearCurrentUser(){
        prefs.clearAllPrefs()
    }

    /**
     * 이메일과 비밀번호로 로그인
     * @param email 사용자의 이메일
     * @param password 사용자의 비밀번호
     * @param autoLogin 자동 로그인 설정 여부
     */
    fun emailLogin(email: String, password: String, autoLogin: Boolean) {
        _emailLoginResult.postValue(false) // 초기 상태 설정
        viewModelScope.launch {
            val result = loginRepository.emailLogin(email, password)
            result.onSuccess { userIdx ->
                setAutoLogin(autoLogin)
                setCurrentUserProvider(JoinType.EMAIL.provider)
                setCurrentUserIdx(userIdx)
                _emailLoginResult.postValue(true)

                // 로그인 성공 후 즉시 FCM 토큰 등록
                registerFcmTokenToServer(userIdx)
            }.onFailure { e ->
                clearCurrentUser()
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

    /**
     * 깃허브로 로그인
     * @param context 액티비티 컨텍스트
     */
    fun githubLogin(context: Activity) {
        _githubLoginResult.postValue(false) // 초기 상태 설정
        viewModelScope.launch {
            val githubLoginResult = loginRepository.githubLogin(context)
            githubLoginResult.onSuccess {
                if(it == 0){
                    _githubJoinResult.postValue(true)
                } else {
                    setAutoLogin(true)
                    setCurrentUserProvider(JoinType.GITHUB.provider)
                    setCurrentUserIdx(it)
                    _githubLoginResult.postValue(true)
                }

            }.onFailure { e ->
                clearCurrentUser()
                _githubLoginResult.postValue(false)
                _githubLoginError.postValue(e)
            }
        }
    }

    /**
     * 카카오로 로그인
     * @param context 컨텍스트
     */
    fun loginKakao(context: Context) {
        _kakaoLoginResult.postValue(false) // 초기 상태 설정
        viewModelScope.launch {
            val result = loginRepository.kakaoLogin(context)
            result.onSuccess {
                if(it == 0){
                    _kakaoJoinResult.postValue(true)
                } else {
                    setAutoLogin(true)
                    setCurrentUserProvider(JoinType.KAKAO.provider)
                    setCurrentUserIdx(it)
                    _kakaoLoginResult.postValue(true)
                }

            }.onFailure { e ->
                clearCurrentUser()
                _kakaoLoginResult.postValue(false)
                _kakaoLoginError.postValue(e)
            }
        }
    }

    /**
     * 자동 로그인 시도
     */
    fun tryAutoLogin() {
        Log.d(logTag, "tryAutoLogin 호출됨.")

        if (getAutoLogin()) {
            // 10초 후에 자동 로그인을 취소하는 핸들러 설정
            val handler = Handler(Looper.getMainLooper())
            val timeoutRunnable = Runnable {
                Log.e(logTag, "prefs autoLogin: ${getAutoLogin()}, prefs userIdx: ${getCurrentUserIdx()}")

                _autoLoginError.postValue(Exception("자동 로그인 시간 초과"))
            }

            // 8초 후에 timeoutRunnable 실행
            handler.postDelayed(timeoutRunnable, 8000L)

            viewModelScope.launch {
                val userIdx = getCurrentUserIdx()
                val result = loginRepository.autoLogin(userIdx)

                result.onSuccess {
                    // 자동 로그인 성공 시, 핸들러에 설정된 타임아웃 취소
                    handler.removeCallbacks(timeoutRunnable)
                    Log.d(logTag, "자동 로그인 성공. ${getCurrentUserProvider()}")
                    when (getCurrentUserProvider()) {
                        JoinType.GITHUB.provider -> _githubLoginResult.postValue(true)
                        JoinType.KAKAO.provider  -> _kakaoLoginResult.postValue(true)
                        JoinType.EMAIL.provider  -> _emailAutoLoginResult.postValue(true)
                    }
                }.onFailure { e ->
                    // 자동 로그인 실패 시, 핸들러에 설정된 타임아웃 취소
                    handler.removeCallbacks(timeoutRunnable)
                    Log.e(logTag, "자동 로그인 실패. 오류: ${e.message}", e)
                    when (getCurrentUserProvider()) {
                        JoinType.GITHUB.provider -> _githubLoginResult.postValue(false)
                        JoinType.KAKAO.provider  -> _kakaoLoginResult.postValue(false)
                        JoinType.EMAIL.provider  -> _emailAutoLoginResult.postValue(false)
                    }
                    Log.e(logTag, "prefs autoLogin: ${getAutoLogin()}, prefs userIdx: ${getCurrentUserIdx()}")
                    _autoLoginError.postValue(e)
                }
            }
        } else {
            Log.e(logTag, "자동 로그인이 설정되지 않음.")
        }
    }

    /**
     * 뷰모델 데이터를 초기화하는 함수
     */
    fun clearData() {
        _emailLoginResult.postValue(false)
        _githubLoginResult.postValue(false)
        _kakaoLoginResult.postValue(false)
        _githubJoinResult.postValue(false)
        _kakaoJoinResult.postValue(false)
        _emailLoginError.postValue(null)
        _githubLoginError.postValue(null)
        _kakaoLoginError.postValue(null)
    }

    // FCM 토큰을 서버에 등록
    fun registerFcmToken(userIdx: Int, fcmToken: String) {
        viewModelScope.launch {
            val result = loginRepository.registerFcmToken(userIdx, fcmToken)
            if (result) {
                Log.d("DetailViewModel", "FCM 토큰 등록 성공 userIdx: $userIdx")
            } else {
                Log.e("DetailViewModel", "FCM 토큰 등록 실패 userIdx: $userIdx")
            }
        }
    }

    /**
     * FCM 토큰을 가져와 서버에 등록하는 함수
     */
    private fun registerFcmTokenToServer(userIdx: Int) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("LoginViewModel", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("LoginViewModel", "FCM Token: $token")

            if (token != null) {
                // 코루틴 내에서 서버에 FCM 토큰을 등록
                viewModelScope.launch {
                    try {
                        val result = loginRepository.registerFcmToken(userIdx, token)
                        if (result) {
                            Log.d("LoginViewModel", "FCM 토큰 등록 성공 userIdx: $userIdx, token: $token")
                        } else {
                            Log.e("LoginViewModel", "FCM 토큰 등록 실패 userIdx: $userIdx")
                        }
                    } catch (e: Exception) {
                        Log.e("LoginViewModel", "Error registering FCM token", e)
                    }
                }
            } else {
                Log.e("LoginViewModel", "FCM Token is null")
            }
        }
    }

}
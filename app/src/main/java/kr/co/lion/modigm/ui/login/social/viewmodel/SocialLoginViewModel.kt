package kr.co.lion.modigm.ui.login.social.viewmodel

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kr.co.lion.modigm.repository.LoginRepository
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class SocialLoginViewModel : ViewModel() {

    private val loginRepository by lazy { LoginRepository() }

    private val _isLoading = MutableLiveData(false)
    val isLoading : LiveData<Boolean> = _isLoading

    // 카카오 로그인
    private val _kakaoLoginResult = MutableLiveData<Boolean>()
    val kakaoLoginResult: LiveData<Boolean> = _kakaoLoginResult

    private val _kakaoJoinResult = MutableLiveData<Boolean>()
    val kakaoJoinResult: LiveData<Boolean> = _kakaoJoinResult

    private val _kakaoLoginError = MutableLiveData<Throwable?>()
    val kakaoLoginError: LiveData<Throwable?> = _kakaoLoginError

    fun kakaoLogin(context: Context) {
        _isLoading.value = true
        _kakaoLoginResult.postValue(false) // 초기 상태 설정
        viewModelScope.launch {
            val result = loginRepository.kakaoLogin(context)
            result.onSuccess {
                _isLoading.postValue(false)
                if (it == 0) {
                    _kakaoJoinResult.postValue(true)
                } else {
                    prefs.setBoolean(
                        key = "autoLogin",
                        value = true
                    )
                    prefs.setString(
                        key = "currentUserProvider",
                        value = JoinType.KAKAO.provider
                    )
                    prefs.setInt(
                        key = "currentUserIdx",
                        value = it
                    )
                    _kakaoLoginResult.postValue(true)
                }
            }.onFailure { e ->
                _isLoading.postValue(false)
                prefs.clearAllPrefs()
                _kakaoLoginResult.postValue(false)
                _kakaoLoginError.postValue(e)
            }
        }
    }

    // 깃허브 로그인
    private val _githubLoginResult = MutableLiveData<Boolean>()
    val githubLoginResult: LiveData<Boolean> = _githubLoginResult

    private val _githubJoinResult = MutableLiveData<Boolean>()
    val githubJoinResult: LiveData<Boolean> = _githubJoinResult

    private val _githubLoginError = MutableLiveData<Throwable?>()
    val githubLoginError: LiveData<Throwable?> = _githubLoginError

    fun githubLogin(activity: Activity) {
        _isLoading.value = true
        _githubLoginResult.postValue(false) // 초기 상태 설정
        viewModelScope.launch {
            val githubLoginResult = loginRepository.githubLogin(activity)
            githubLoginResult.onSuccess {
                _isLoading.postValue(false)
                if (it == 0) {
                    _githubJoinResult.postValue(true)
                } else {
                    prefs.setBoolean(
                        key = "autoLogin",
                        value = true
                    )
                    prefs.setString(
                        key = "currentUserProvider",
                        value = JoinType.GITHUB.provider
                    )
                    prefs.setInt(
                        key = "currentUserIdx",
                        value = it
                    )
                    _githubLoginResult.postValue(true)
                }

            }.onFailure { e ->
                _isLoading.postValue(false)
                prefs.clearAllPrefs()
                _githubLoginResult.postValue(false)
                _githubLoginError.postValue(e)
            }
        }
    }



    // 자동 로그인
    private val _emailAutoLoginResult = MutableLiveData<Boolean>()
    val emailAutoLoginResult: LiveData<Boolean> = _emailAutoLoginResult

    private val _autoLoginError = MutableLiveData<Throwable?>()
    val autoLoginError: LiveData<Throwable?> = _autoLoginError

    fun tryAutoLogin() {
        _isLoading.value = true
        if (prefs.getBoolean("autoLogin")) {

            val timeoutHandler = Handler(Looper.getMainLooper())
            val timeoutRunnable = Runnable {
                _isLoading.value = false
                _autoLoginError.postValue(Exception("자동 로그인 시간 초과"))
            }

            timeoutHandler.postDelayed(timeoutRunnable, 8000L)

            viewModelScope.launch {
                val userIdx = prefs.getInt("currentUserIdx")
                val result = loginRepository.autoLogin(userIdx)

                result.onSuccess {
                    _isLoading.postValue(false)
                    // 자동 로그인 성공 시, 핸들러에 설정된 타임아웃 취소
                    timeoutHandler.removeCallbacks(timeoutRunnable)

                    when (prefs.getString(key = "currentUserProvider")) {
                        JoinType.GITHUB.provider -> _githubLoginResult.postValue(true)
                        JoinType.KAKAO.provider -> _kakaoLoginResult.postValue(true)
                        JoinType.EMAIL.provider -> _emailAutoLoginResult.postValue(true)
                    }
                }.onFailure { e ->
                    _isLoading.postValue(false)
                    // 자동 로그인 실패 시, 핸들러에 설정된 타임아웃 취소
                    timeoutHandler.removeCallbacks(timeoutRunnable)

                    when (prefs.getString(key = "currentUserProvider")) {
                        JoinType.GITHUB.provider -> _githubLoginResult.postValue(false)
                        JoinType.KAKAO.provider -> _kakaoLoginResult.postValue(false)
                        JoinType.EMAIL.provider -> _emailAutoLoginResult.postValue(false)
                    }

                    _autoLoginError.postValue(e)
                }
            }
        } else {
            _isLoading.value = false
        }
    }

    // 뷰모델 데이터 초기화
    fun clearViewModelData() {
        _isLoading.postValue(false)
        _githubLoginResult.postValue(false)
        _kakaoLoginResult.postValue(false)
        _githubJoinResult.postValue(false)
        _kakaoJoinResult.postValue(false)
        _emailAutoLoginResult.postValue(false)
        _githubLoginError.postValue(null)
        _kakaoLoginError.postValue(null)
        _autoLoginError.postValue(null)
    }

    fun registerFcmTokenToServer(userIdx: Int) {
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
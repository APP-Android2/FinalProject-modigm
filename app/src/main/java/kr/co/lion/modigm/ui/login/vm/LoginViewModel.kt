package kr.co.lion.modigm.ui.login.vm

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.repository.LoginRepository
import kr.co.lion.modigm.util.ModigmApplication

class LoginViewModel : ViewModel() {

    // LoginRepository 초기화
    private val loginRepository by lazy { LoginRepository() }

    // SharedPreferences 초기화
    private val prefs by lazy { ModigmApplication.prefs }

    // 이메일 로그인 결과를 담는 LiveData
    private val _emailLoginResult = MutableLiveData<Boolean>()
    val emailLoginResult: LiveData<Boolean> = _emailLoginResult

    // 이메일 로그인 에러를 담는 LiveData
    private val _emailLoginError = MutableLiveData<Throwable>()
    val emailLoginError: LiveData<Throwable> get() = _emailLoginError

    // 깃허브 로그인 결과를 담는 LiveData
    private val _githubLoginResult = MutableLiveData<Boolean>()
    val githubLoginResult: LiveData<Boolean> = _githubLoginResult

    // 깃허브 로그인 에러를 담는 LiveData
    private val _githubLoginError = MutableLiveData<Throwable>()
    val githubLoginError: LiveData<Throwable> get() = _githubLoginError

    // 카카오 로그인 결과를 담는 LiveData
    private val _kakaoLoginResult = MutableLiveData<Boolean>()
    val kakaoLoginResult: LiveData<Boolean> = _kakaoLoginResult

    // 카카오 로그인 에러를 담는 LiveData
    private val _kakaoLoginError = MutableLiveData<Throwable>()
    val kakaoLoginError: LiveData<Throwable> get() = _kakaoLoginError

    // 자동 로그인 에러를 담는 LiveData
    private val _autoLoginError = MutableLiveData<Throwable>()
    val autoLoginError: LiveData<Throwable> get() = _autoLoginError

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
        _emailLoginResult.value = false // 초기 상태 설정
        viewModelScope.launch {
            val result = loginRepository.emailLogin(email, password)
            result.onSuccess {
                setAutoLogin(autoLogin)
                if (autoLogin) {
                    setCurrentUserProvider("email")
                }
                setCurrentUserIdx(it)
                _emailLoginResult.postValue(true)
            }.onFailure { e ->
                clearCurrentUser()
                _emailLoginResult.postValue(false)
                _emailLoginError.postValue(e)
            }
        }
    }

    /**
     * 깃허브로 로그인
     * @param context 액티비티 컨텍스트
     */
    fun loginWithGithub(context: Activity) {
        _githubLoginResult.value = false // 초기 상태 설정
        viewModelScope.launch {
            val result = loginRepository.githubLogin(context)
            result.onSuccess {
                setAutoLogin(true)
                setCurrentUserProvider("github")
                setCurrentUserIdx(it)
                _githubLoginResult.postValue(true)
            }.onFailure { e ->
                _githubLoginResult.postValue(false)
                _githubLoginError.postValue(e)
            }
        }
    }

    /**
     * 카카오로 로그인
     * @param context 컨텍스트
     */
    fun loginWithKakao(context: Context) {
        _kakaoLoginResult.value = false // 초기 상태 설정
        viewModelScope.launch {
            val result = loginRepository.kakaoLogin(context)
            result.onSuccess {
                setAutoLogin(true)
                setCurrentUserProvider("kakao")
                setCurrentUserIdx(it)
                _kakaoLoginResult.postValue(true)
            }.onFailure { e ->
                _kakaoLoginResult.postValue(false)
                _kakaoLoginError.postValue(e)
            }
        }
    }

    /**
     * 자동 로그인 시도
     */
    fun attemptAutoLogin() {
        // 자동 로그인이 활성화 되어 있다면
        if(getAutoLogin()) {
            viewModelScope.launch {
                val userIdx = getCurrentUserIdx()
                val result = loginRepository.autoLogin(userIdx)
                result.onSuccess {
                    when (getCurrentUserProvider()) {
                        "github" -> _githubLoginResult.postValue(true)
                        "kakao"  -> _kakaoLoginResult.postValue(true)
                        "email"  -> _emailLoginResult.postValue(true)
                    }
                }.onFailure { e ->
                    when (getCurrentUserProvider()) {
                        "github" -> _githubLoginResult.postValue(false)
                        "kakao"  -> _kakaoLoginResult.postValue(false)
                        "email"  -> _emailLoginResult.postValue(false)
                    }
                    _autoLoginError.postValue(e)

                }
            }
        }
    }

    /**
     * 이메일 유효성을 검사하는 함수
     * @param email 이메일 주소
     * @return Boolean 이메일 유효성 검사 결과
     */
    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * 비밀번호 유효성을 검사하는 함수
     * @param password 비밀번호
     * @return Boolean 비밀번호 유효성 검사 결과
     */
    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * 뷰모델 데이터를 초기화하는 함수
     */
    fun clearData() {
        _emailLoginResult.value = false
        _kakaoLoginResult.value = false
        _githubLoginResult.value = false
    }

    /**
     * DAO 코루틴을 취소하는 함수
     */
    private fun closeDataSource() {
        viewModelScope.launch {
            loginRepository.closeDataSource()
        }
    }

    /**
     * 뷰모델이 삭제될 때 호출되는 함수
     */
    override fun onCleared() {
        super.onCleared()
        // DAO 코루틴 취소
        closeDataSource()
    }
}

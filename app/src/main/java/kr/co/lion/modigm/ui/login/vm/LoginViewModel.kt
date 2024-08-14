package kr.co.lion.modigm.ui.login.vm

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.repository.LoginRepository
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class LoginViewModel : ViewModel() {

    private val tag by lazy { LoginViewModel::class.simpleName }

    // LoginRepository 초기화
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
    val emailDialogError: LiveData<Throwable?> = _emailLoginError

    // 이메일 오류 결과를 담는 LiveData
    private val _emailInputError = MutableLiveData<Throwable?>()
    val emailInputError: LiveData<Throwable?> = _emailInputError

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
            result.onSuccess {
                setAutoLogin(autoLogin)
                if (autoLogin) {
                    setCurrentUserProvider(JoinType.EMAIL.provider)
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
        // 자동 로그인이 활성화 되어 있다면
        Log.d(tag, "tryAutoLogin 호출됨.")
        if(getAutoLogin()) {
            viewModelScope.launch {
                val userIdx = getCurrentUserIdx()
                val result = loginRepository.autoLogin(userIdx)
                result.onSuccess {
                    // 자동 로그인 성공
                    Log.d(tag, "자동 로그인 성공. ${getCurrentUserProvider()}")
                    when (getCurrentUserProvider()) {
                        JoinType.GITHUB.provider -> _githubLoginResult.postValue(true)
                        JoinType.KAKAO.provider  -> _kakaoLoginResult.postValue(true)
                        JoinType.EMAIL.provider  -> _emailAutoLoginResult.postValue(true)
                    }
                }.onFailure { e ->
                    // 자동 로그인 실패
                    Log.e(tag, "자동 로그인 실패. 오류: ${e.message}", e)
                    when (getCurrentUserProvider()) {
                        JoinType.GITHUB.provider -> _githubLoginResult.postValue(false)
                        JoinType.KAKAO.provider  -> _kakaoLoginResult.postValue(false)
                        JoinType.EMAIL.provider  -> _emailAutoLoginResult.postValue(false)
                    }
                }
            }
        }
    }

    /**
     * 로그아웃
     */
    fun authLogout() {
        val result = loginRepository.authLogout()
        result.onSuccess {
            Log.d(tag, "로그아웃 성공.")
        }.onFailure { e ->
            Log.e(tag, "로그아웃 실패. 오류: ${e.message}", e)
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
}
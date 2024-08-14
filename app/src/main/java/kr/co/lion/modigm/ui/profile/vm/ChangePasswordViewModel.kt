package kr.co.lion.modigm.ui.profile.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.repository.LoginRepository
import kr.co.lion.modigm.util.toNationalPhoneNumber

class ChangePasswordViewModel : ViewModel() {

    private val tag by lazy { ChangePasswordViewModel::class.simpleName }

    private val loginRepository by lazy { LoginRepository() }

    // 비밀번호 변경 완료
    private val _isComplete = MutableLiveData<Boolean>()
    val isComplete: MutableLiveData<Boolean> = _isComplete

    // 현재 비밀번호 에러
    private val _passwordInputError = MutableLiveData<Throwable?>()
    val passwordInputError: LiveData<Throwable?> = _passwordInputError

    // 새로운 비밀번호 에러
    private val _newPasswordInputError = MutableLiveData<Throwable?>()
    val newPasswordInputError: LiveData<Throwable?> = _newPasswordInputError

    // 새로운 비밀번호 확인 에러
    private val _newPasswordConfirmInputError = MutableLiveData<Throwable?>()
    val newPasswordConfirmInputError: LiveData<Throwable?> = _newPasswordConfirmInputError

    // 비밀번호 확인 완료
    private val _isCurrentPasswordComplete = MutableLiveData<Boolean>()
    val isCurrentPasswordComplete: LiveData<Boolean> = _isCurrentPasswordComplete

    // 현재 사용자의 전화번호
    private val _currentUserPhone = MutableLiveData<String>()
    val currentUserPhone: LiveData<String> = _currentUserPhone

    // 비밀번호 변경 완료
    fun isCompleteTo(value: Boolean) {
        viewModelScope.launch {
            _isComplete.postValue(value)
        }
    }

    // 현재 비밀번호 입력 완료
    fun isCurrentPasswordCompleteTo(value: Boolean) {
        viewModelScope.launch {
            _isCurrentPasswordComplete.postValue(value)
        }
    }
    /**
     * 이메일 로그인 유저의 비밀번호를 확인하는 메서드
     * @param userPassword 사용자의 비밀번호
     */
    fun checkPassword(userPassword: String) {
        Log.d(tag, "checkPassword 호출됨. userPassword: $userPassword")
        viewModelScope.launch {
            val result = loginRepository.checkPassword(userPassword)
            result.onSuccess { currentUserPhone ->
                Log.d(tag, "currentUserPhone: $currentUserPhone")
                _currentUserPhone.postValue(currentUserPhone.toNationalPhoneNumber())
                Log.d(tag, "비밀번호 확인 성공.")
                _isCurrentPasswordComplete.postValue(true)

            }.onFailure { e ->
                Log.e(tag, "비밀번호 확인 실패. 오류: ${e.message}", e)
                _passwordInputError.postValue(Throwable("비밀번호가 일치하지 않습니다."))
            }
        }
    }
    /**
     * 비밀번호를 변경하는 메서드
     * @param newPassword 변경할 비밀번호
     */
    fun updatePassword(newPassword: String) {
        Log.d(tag, "updatePassword 호출됨. newPassword: $newPassword")
        viewModelScope.launch {
            val result = loginRepository.updatePassword(newPassword)
            result.onSuccess {
                Log.d(tag, "비밀번호 변경 성공.")
                _isComplete.postValue(true)
            }.onFailure { e ->
                Log.e(tag, "비밀번호 변경 실패. 오류: ${e.message}", e)
            }
        }
    }
}
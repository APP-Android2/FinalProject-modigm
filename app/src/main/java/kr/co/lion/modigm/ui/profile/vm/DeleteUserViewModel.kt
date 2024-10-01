package kr.co.lion.modigm.ui.profile.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.repository.ProfileRepository
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class DeleteUserViewModel: ViewModel() {
    private val profileRepository = ProfileRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted = _isDeleted.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()
    suspend fun resetErrorMessage() {
        _errorMessage.emit(null)
    }

    fun deleteUserDate(userIdx: Int) {
        viewModelScope.launch {
            val result = profileRepository.deleteUserData(userIdx)
            result.onSuccess {
                // 파이어베이스 인증 정보 삭제
                auth.currentUser?.delete()?.await()
                prefs.clearAllPrefs()
                prefs.setBoolean("autoLogin", false)
                _isDeleted.emit(true)
            }.onFailure {
                _errorMessage.emit("회원 탈퇴에 실패했습니다. 잠시 후 시도해주세요.")
            }
        }
    }
}
package kr.co.lion.modigm.ui.detail.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.repository.DetailRepository

class DetailViewModel : ViewModel() {
    private val detailRepository = DetailRepository()

    // StudyData가 데이터 모델이라고 가정
    private val _contentData = MutableLiveData<StudyData?>() // 좀 더 구체적인 타입 사용
    val contentData: LiveData<StudyData?> = _contentData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // UserData가 데이터 모델이라고 가정
    private val _userNameData = MutableLiveData<UserData?>()
    val userNameData: LiveData<UserData?> = _userNameData

    // 기존 코드...
    private val _userProfilePicUrl = MutableLiveData<String?>()
    val userProfilePicUrl: LiveData<String?> = _userProfilePicUrl


    fun selectContentData(studyIdx: Int) {
        _isLoading.value = true // 작업 시작 시 로딩을 true로 정확히 설정
        viewModelScope.launch {
            try {
                val response = detailRepository.selectContentData(studyIdx)
                _contentData.value = response
                Log.d("DetailVM", "Data loaded successfully.")
            } catch (e: Exception) {
                Log.e("DetailVM Error", "Error in fetching data: ${e.message}")
                _contentData.value = null // 에러 발생 시 null 설정 또는 적절한 에러 상태 처리
            } finally {
                _isLoading.value = false // 로딩 상태가 올바르게 업데이트되도록 보장
            }
        }
    }

    fun loadUserDetailsByUid(uid: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userDetails = detailRepository.loadUserDetailsByUid(uid)
                userDetails?.let {
                    _userNameData.value = UserData(userName = it.userName, userProfilePic = it.userProfilePic)
                    _userProfilePicUrl.value = it.userProfilePic
                }
                Log.d("DetailVM", "User details loaded successfully.")
            } catch (e: Exception) {
                Log.e("DetailVM Error", "Error in fetching user details: ${e.message}")
                _userNameData.value = null
                _userProfilePicUrl.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStudyCanApplyByStudyIdx(studyIdx: Int, canApply: Boolean) {
        viewModelScope.launch {
            try {
                detailRepository.updateStudyCanApplyByStudyIdx(studyIdx, canApply)
                Log.d("DetailVM", "Study can apply status updated successfully for index $studyIdx")
                // LiveData를 사용하여 UI에 상태 변경 알림
            } catch (e: Exception) {
                Log.e("DetailVM Error", "Error updating study can apply status: ${e.message}")
            }
        }
    }
}

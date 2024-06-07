package kr.co.lion.modigm.ui.detail.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.repository.StudyRepository

class DetailViewModel : ViewModel() {
    private val studyRepository = StudyRepository()

    // StudyData가 데이터 모델이라고 가정
    private val _contentData = MutableLiveData<StudyData?>() // 좀 더 구체적인 타입 사용
    val contentData: LiveData<StudyData?> = _contentData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // UserData가 데이터 모델이라고 가정
//    private val _userNameData = MutableLiveData<UserData?>()
//    val userNameData: LiveData<UserData?> = _userNameData

    private val _userData = MutableLiveData<UserData?>()
    val userData: LiveData<UserData?> = _userData

    // 기존 코드...
    private val _userProfilePicUrl = MutableLiveData<String?>()
    val userProfilePicUrl: LiveData<String?> = _userProfilePicUrl

    private val _errorMessages = MutableLiveData<String>()
    val errorMessages: MutableLiveData<String> = _errorMessages

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: MutableLiveData<Boolean> get() = _updateResult


    fun selectContentData(studyIdx: Int) {
        _isLoading.value = true // 작업 시작 시 로딩을 true로 정확히 설정
        viewModelScope.launch {
            try {
                val response = studyRepository.selectContentData(studyIdx)
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
            studyRepository.loadUserDetailsByUid(uid)?.let {
                _userData.value = it
            } ?: run {
                _errorMessages.value = "Failed to load user details"
            }
            _isLoading.value = false
        }
    }


    fun updateStudyCanApplyByStudyIdx(studyIdx: Int, canApply: Boolean) {
        viewModelScope.launch {
            try {
                studyRepository.updateStudyCanApplyByStudyIdx(studyIdx, canApply)
                Log.d("DetailVM", "Study can apply status updated successfully for index $studyIdx")
                // LiveData를 사용하여 UI에 상태 변경 알림
            } catch (e: Exception) {
                Log.e("DetailVM Error", "Error updating study can apply status: ${e.message}")
            }
        }
    }

    fun updateStudyDataByStudyIdx(studyIdx: Int, studyData: StudyData) {
        viewModelScope.launch {
            try {
                val dataMap = studyData.toMap() // StudyData 객체를 Map으로 변환
                studyRepository.updateStudyDataByStudyIdx(studyIdx, dataMap)
                _updateResult.postValue(true)  // UI에 변경 사항 반영
                Log.d("DetailViewModel", "Study data updated successfully for studyIdx: $studyIdx")
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Update failed: ${e.message}")
                _updateResult.postValue(false)
            }
        }
    }

    fun StudyData.toMap(): Map<String, Any> = mapOf(
        "studyIdx" to studyIdx,
        "studyTitle" to studyTitle,
        "studyContent" to studyContent,
        "studyType" to studyType,
        "studyPlace" to studyPlace,
        "studyDetailPlace" to studyDetailPlace,
        "studyOnOffline" to studyOnOffline,
        "studyApplyMethod" to studyApplyMethod,
        "studyPic" to studyPic,
        "studySkillList" to studySkillList,
        "studyCanApply" to studyCanApply,
        "studyPic" to studyPic,
        "studyMaxMember" to studyMaxMember,
        "studyUidList" to studyUidList,
        "chatIdx" to chatIdx,
        "studyState" to studyState,
        "studyWriteUid" to studyWriteUid
    )

}
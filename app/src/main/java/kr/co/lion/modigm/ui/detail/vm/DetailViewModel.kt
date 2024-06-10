package kr.co.lion.modigm.ui.detail.vm

import android.net.Uri
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

    private val _userData = MutableLiveData<UserData?>()
    val userData: LiveData<UserData?> = _userData

    // 기존 코드...
    private val _userProfilePicUrl = MutableLiveData<String?>()
    val userProfilePicUrl: LiveData<String?> = _userProfilePicUrl

    private val _errorMessages = MutableLiveData<String>()
    val errorMessages: MutableLiveData<String> = _errorMessages

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: MutableLiveData<Boolean> get() = _updateResult

    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> = _imageUri

    private val _userImageUri = MutableLiveData<Uri>()
    val userImageUri: LiveData<Uri> = _userImageUri

    private val _studyUids = MutableLiveData<List<String>>()
    val studyUids: LiveData<List<String>> = _studyUids

    private val _userDetails = MutableLiveData<List<UserData>>()
    val userDetails: LiveData<List<UserData>> = _userDetails

    private val _removalStatus = MutableLiveData<Result<Boolean>>()
    val removalStatus: LiveData<Result<Boolean>> = _removalStatus

    private val _isLiked = MutableLiveData<Boolean>(false)
    val isLiked: LiveData<Boolean> get() = _isLiked

    private val _applyResult = MutableLiveData<Boolean>()
    val applyResult: LiveData<Boolean> = _applyResult

    private val _applyMembers = MutableLiveData<List<UserData>>()
    val applyMembers: LiveData<List<UserData>> get() = _applyMembers

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

    fun loadStudyPic(studyPic: String) {
        viewModelScope.launch {
            val result = studyRepository.loadStudyPicUrl(studyPic)
            result.onSuccess {
                _imageUri.postValue(it)
            }.onFailure {
                Log.e("ViewModel", "Failed to load cover image: ${it.message}")
            }
        }
    }

    fun loadUserPicUrl(userProfilePic: String) {
        viewModelScope.launch {
            val result = studyRepository.loadUserPicUrl(userProfilePic)
            result.onSuccess {
                _userImageUri.postValue(it)
            }.onFailure {
                Log.e("ViewModel", "Failed to load user image: ${it.message}")
            }
        }
    }


    // 특정 studyIdx의 studyState를 업데이트하는 함수
    fun updateStudyStateByStudyIdx(studyIdx: Int) {
        viewModelScope.launch {
            try {
                val studyData = studyRepository.selectContentData(studyIdx)
                if (studyData != null) {
                    studyRepository.updateStudyStateByStudyIdx(studyIdx, false)
                } else {
                    Log.e("ViewModel", "No study found with the given index.")
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed to update study state: ${e.message}")

            }
        }
    }

    fun loadStudyUids(studyIdx: Int) {
        viewModelScope.launch {
            _studyUids.value = studyRepository.getStudyUidListByStudyIdx(studyIdx)
        }
    }

    fun loadUserDetails(uids: List<String>) {
        viewModelScope.launch {
            _userDetails.value = uids.mapNotNull { uid ->
                studyRepository.getUserDetailsByUid(uid)
            }
        }
    }

    fun updateStudyUserList(userUid: String, studyIdx: Int) {
        viewModelScope.launch {
            val result = studyRepository.updateStudyUserList(userUid, studyIdx)
            if (result) {
                // 성공적으로 처리됐을 때 UI 업데이트
            } else {
                // 실패 처리
            }
        }
    }

    fun toggleLike(uid: String, studyIdx: Int) {
        viewModelScope.launch {
            val studyCollection = FirebaseFirestore.getInstance().collection("Study")
            val query = studyCollection.whereEqualTo("studyIdx", studyIdx)
            val querySnapshot = query.get().await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                val currentlyLiked = document.getBoolean("studyLikeState") ?: false
                val newLikeState = !currentlyLiked

                document.reference.update("studyLikeState", newLikeState).await()

                if (newLikeState) {
                    studyRepository.addLike(uid, studyIdx)
                } else {
                    studyRepository.removeLike(uid, studyIdx)
                }

                _isLiked.postValue(newLikeState)
            } else {
                studyRepository.removeLike(uid, studyIdx)
                Log.e("DetailViewModel", "No matching document found for studyIdx: $studyIdx")
            }
        }
    }

    fun loadInitialLikeState(studyIdx: Int) {
        viewModelScope.launch {
            val studyCollection = FirebaseFirestore.getInstance().collection("Study")
            val query = studyCollection.whereEqualTo("studyIdx", studyIdx)
            val querySnapshot = query.get().await()

            if (!querySnapshot.isEmpty) {
                // 첫 번째 문서에서 좋아요 상태 가져오기
                val document = querySnapshot.documents[0]
                val isLiked = document.getBoolean("studyLikeState") ?: false
                _isLiked.postValue(isLiked)
            } else {
                Log.e("ViewModel", "No study found with idx: $studyIdx")
                _isLiked.postValue(false) // 문서가 없으면 기본값으로 false 설정
            }
        }
    }

    fun applyToStudy(studyIdx: Int, uid: String) {
        viewModelScope.launch {
            studyRepository.applyToStudy(studyIdx, uid)
            _applyResult.postValue(true)
        }
    }

    fun joinStudy(studyIdx: Int, uid: String) {
        viewModelScope.launch {
            studyRepository.joinStudy(studyIdx, uid)
        }
    }

    fun loadApplyMembers(studyIdx: Int) {
        Log.d("DetailViewModel", "Loading apply members for studyIdx: $studyIdx")
        studyRepository.fetchStudyApplyMembers(studyIdx) { members ->
            Log.d("DetailViewModel", "Loaded members: $members")
            _applyMembers.value = members
        }
    }

    fun removeUserFromApplyList(studyIdx: Int, userUid: String, callback: (Boolean) -> Unit) {
        studyRepository.removeUserFromStudyApplyList(studyIdx, userUid, callback)
    }

}
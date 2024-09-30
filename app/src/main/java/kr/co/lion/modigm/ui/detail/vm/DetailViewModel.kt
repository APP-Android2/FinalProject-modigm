package kr.co.lion.modigm.ui.detail.vm

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.repository.DetailRepository
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.repository.WriteRepository
import kr.co.lion.modigm.ui.notification.FCMService

class DetailViewModel: ViewModel() {

    private val detailRepository = DetailRepository()
    private val studyRepository = StudyRepository()
    private val writeRepository = WriteRepository()

    private val _studyData = MutableStateFlow<StudyData?>(null)
    val studyData: StateFlow<StudyData?> = _studyData

    private val _memberCount = MutableStateFlow(0)
    val memberCount: StateFlow<Int> = _memberCount

    private val _allStudyDetails = MutableStateFlow<List<Triple<StudyData, Int, Boolean>>>(emptyList())
    val allStudyDetails: StateFlow<List<Triple<StudyData, Int, Boolean>>> = _allStudyDetails

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val _studyTechList = MutableStateFlow<List<Int>>(emptyList())
    val studyTechList: StateFlow<List<Int>> get() = _studyTechList

    private val _updateResult = MutableStateFlow<Boolean?>(null)
    val updateResult: StateFlow<Boolean?> = _updateResult

    private val _studyPic = MutableStateFlow<String?>(null)
    val studyPic: StateFlow<String?> = _studyPic

    private val _studySkills = MutableStateFlow<List<Int>>(emptyList())
    val studySkills: StateFlow<List<Int>> get() = _studySkills

    private val _studyMembers = MutableStateFlow<List<UserData>>(emptyList())
    val studyMembers: StateFlow<List<UserData>> = _studyMembers

    private val _removeUserResult = MutableSharedFlow<Boolean>()
    val removeUserResult: SharedFlow<Boolean> = _removeUserResult

    private val _addUserResult = MutableSharedFlow<Boolean>()
    val addUserResult: SharedFlow<Boolean> = _addUserResult

    private val _studyRequestMembers = MutableStateFlow<List<UserData>>(emptyList())
    val studyRequestMembers: StateFlow<List<UserData>> = _studyRequestMembers

    private val _acceptUserResult = MutableSharedFlow<Boolean>()
    val acceptUserResult: SharedFlow<Boolean> = _acceptUserResult

    private val _removeUserFromApplyResult = MutableSharedFlow<Boolean>()
    val removeUserFromApplyResult: SharedFlow<Boolean> = _removeUserFromApplyResult

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    // 알림 전송 결과를 저장하는 플로우 추가
    private val _notificationResult = MutableSharedFlow<Boolean>()
    val notificationResult: SharedFlow<Boolean> = _notificationResult

    private val _isUserAlreadyMember = MutableStateFlow(false)
    val isUserAlreadyMember: StateFlow<Boolean> = _isUserAlreadyMember

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    // 각각의 데이터 로딩 상태를 저장하는 변수들
    private val _isStudyDataLoaded = MutableStateFlow(false)
    private val _isMemberCountLoaded = MutableStateFlow(false)
    private val _isUserDataLoaded = MutableStateFlow(false)
    private val _isStudyPicLoaded = MutableStateFlow(false)
    private val _isTechListLoaded = MutableStateFlow(false)

    // 모든 데이터를 로드했는지 확인하는 플래그
    val isDataFullyLoaded: StateFlow<Boolean> = combine(
        _isStudyDataLoaded,
        _isMemberCountLoaded,
        _isUserDataLoaded,
        _isStudyPicLoaded,
        _isTechListLoaded
    ) { studyLoaded, memberCountLoaded, userLoaded, picLoaded, techLoaded ->
        studyLoaded && memberCountLoaded && userLoaded && picLoaded && techLoaded
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun clearData() {
        _studyData.value = null
        _memberCount.value = 0
        _userData.value = null
        _studyTechList.value = emptyList()
        _studyPic.value = null
        _studyMembers.value = emptyList()
    }

    fun clearLoadingState() {
        _isLoading.value = true
        _isStudyDataLoaded.value = false
        _isMemberCountLoaded.value = false
        _isUserDataLoaded.value = false
        _isStudyPicLoaded.value = false
        _isTechListLoaded.value = false
    }



    fun loadStudyData(studyIdx: Int) {
        clearLoadingState()  // 새로운 데이터를 로드하기 전에 상태 초기화
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // 1. 스터디 데이터 로드
                detailRepository.getStudyById(studyIdx).collect { studyData ->
                    _studyData.value = studyData
                    _isStudyDataLoaded.value = true

                    // studyData에서 userIdx를 가져옵니다.
                    studyData?.let { study ->
                        val userIdx = study.userIdx

                        // 2. 글 작성자 데이터 로드
                        val userDeferred = async { getUserById(userIdx) }

                        // 3. 멤버 수 로드
                        val memberCountDeferred = async { countMembersByStudyIdx(studyIdx) }

                        // 4. 스터디 이미지 로드
                        val studyPicDeferred = async { getStudyPic(studyIdx) }

                        // 5. 스터디 기술 목록 로드
                        val techDeferred = async { getTechIdxByStudyIdx(studyIdx) }

                        // 모든 데이터 로드 완료까지 대기
                        awaitAll(userDeferred, memberCountDeferred, studyPicDeferred, techDeferred)
                    }
                }
            }catch (throwable: Throwable) {
                Log.e("DetailViewModel", "Error loading study data", throwable)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 특정 studyIdx에 대한 스터디 데이터를 가져오는 메소드
    suspend fun getStudy(studyIdx: Int) {
        try {
            detailRepository.getStudyById(studyIdx).collect { data ->
                _studyData.value = data
                _isStudyDataLoaded.value = true
            }
        } catch (throwable: Throwable) {
            Log.e("DetailViewModel", "Error fetching study data", throwable)
        }
    }

    // 특정 studyIdx에 대한 스터디 멤버 수를 가져오는 메소드
    suspend fun countMembersByStudyIdx(studyIdx: Int) {
        try {
            detailRepository.countMembersByStudyIdx(studyIdx).collect { count ->
                _memberCount.value = count
                _isMemberCountLoaded.value = true
            }
        } catch (throwable: Throwable) {
            Log.e("DetailViewModel", "Error counting members", throwable)
        }
    }


    fun fetchMembersInfo(studyIdx: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // studyIdx에 해당하는 userIdx 리스트 가져오기
                val userIds = detailRepository.getUserIdsByStudyIdx(studyIdx)
                Log.d("SqlDetailViewModel", "Fetched user IDs: $userIds")

                if (userIds.isEmpty()) {
                    Log.d("SqlDetailViewModel", "No user IDs found for studyIdx: $studyIdx")
                    return@launch
                }

                // 해당 userIdx들에 해당하는 사용자 정보 가져오기
                val users = mutableListOf<UserData>()

                userIds.forEach { userIdx ->
                    detailRepository.getUserById(userIdx).collect { user ->
                        user?.let {
                            users.add(it)
                            Log.d("SqlDetailViewModel", "Fetched user data: $user")
                        }
                    }
                }

                // 결과를 MutableStateFlow에 할당
                _studyMembers.value = users
                Log.d("SqlDetailViewModel", "Final user list size: ${users.size}")

            } catch (throwable: Throwable) {
                Log.e("SqlDetailViewModel", "Error fetching members info", throwable)
            }
        }
    }


    // 특정 studyIdx에 대한 스터디 이미지를 가져오는 메소드
    suspend fun getStudyPic(studyIdx: Int) {
        try {
            detailRepository.getStudyPicByStudyIdx(studyIdx).collect { pic ->
                _studyPic.value = pic
                _isStudyPicLoaded.value = true
            }
        } catch (throwable: Throwable) {
            Log.e("DetailViewModel", "Error fetching study pic", throwable)
        }
    }

    suspend fun getUserById(userIdx: Int) {
        Log.e("DetailViewModel", "getUserById 호출됨: userIdx: $userIdx") // 로그 추가
        try {
            detailRepository.getUserById(userIdx).collect { user ->
                Log.e("DetailViewModel", "유저 데이터 가져옴: $user") // 결과 확인 로그
                _userData.value = user
                _isUserDataLoaded.value = true
            }
        } catch (throwable: Throwable) {
            Log.e("DetailViewModel", "Error fetching user data", throwable)
        }
    }


    suspend fun getTechIdxByStudyIdx(studyIdx: Int) {
        try {
            detailRepository.getTechIdxByStudyIdx(studyIdx).collect { techList ->
                _studyTechList.value = techList
                _isTechListLoaded.value = true
            }
        } catch (throwable: Throwable) {
            Log.e("DetailViewModel", "Error fetching tech list", throwable)
        }
    }

    // studyState 값을 업데이트하는 메소드 추가
    fun updateStudyState(studyIdx: Int, newState: Boolean) {
        viewModelScope.launch {
            val result = detailRepository.updateStudyState(studyIdx, newState)
            _updateResult.emit(result)
        }
    }

    // 스터디 데이터를 업데이트하는 함수
    fun updateStudyData(studyData: StudyData) {
        viewModelScope.launch {
            val result = detailRepository.updateStudy(studyData)
            _updateResult.value = result
        }
    }

    fun insertSkills(studyIdx: Int, skills: List<Int>) {
        viewModelScope.launch {
            detailRepository.insertSkills(studyIdx, skills)
        }
    }

    // updateResult 값을 업데이트하는 메서드
    fun setUpdateResult(value: Boolean) {
        _updateResult.value = value
    }

    // updateResult 값을 초기화하는 메서드
    fun clearUpdateResult() {
        _updateResult.value = null  // 초기화할 때 null 사용
    }

    // 특정 사용자를 스터디에서 삭제하는 메소드
    fun removeUserFromStudy(studyIdx: Int, userIdx: Int) {
        viewModelScope.launch {
            val result = detailRepository.removeUserFromStudy(studyIdx, userIdx)
            _removeUserResult.emit(result)
        }
    }

    // 사용자가 이미 스터디에 참여 중인지 확인하는 함수
//    fun checkIfUserAlreadyMember(studyIdx: Int, userIdx: Int) {
//        viewModelScope.launch {
//            try {
//                // DetailRepository를 통해 해당 사용자가 스터디 멤버인지 체크
//                val isMember = detailRepository.isUserAlreadyMember(studyIdx, userIdx).firstOrNull() ?: false
//                _isUserAlreadyMember.value = isMember
//            } catch (e: Exception) {
//                Log.e("DetailViewModel", "Error checking if user is already a member", e)
//                _isUserAlreadyMember.value = false
//            }
//        }
//    }
    // Boolean 값을 반환하는 메소드로 수정
    suspend fun checkIfUserAlreadyMember(studyIdx: Int, userIdx: Int): Boolean {
        return try {
            detailRepository.isUserAlreadyMember(studyIdx, userIdx).firstOrNull() ?: false
        } catch (e: Exception) {
            Log.e("DetailViewModel", "Error checking if user is already a member", e)
            false
        }
    }


    // 사용자가 신청할 때 알림을 전송하고 데이터를 저장하는 메서드
    suspend fun addUserToStudyOrRequest(studyIdx: Int, userIdx: Int, applyMethod: String, context: Context, view: View, studyTitle: String): Boolean {
        return withContext(Dispatchers.IO) {
            var success = false

            // 사용자가 이미 참여 중인지 확인
            val isAlreadyMember = detailRepository.isUserAlreadyMember(studyIdx, userIdx).firstOrNull() ?: false

            // 이미 신청된 상태인지 확인
            val isAlreadyApplied = detailRepository.isAlreadyApplied(userIdx, studyIdx)

            withContext(Dispatchers.Main) {
                if (isAlreadyMember) {
                    // 이미 참여 중이면 스낵바로 알림 표시 후 리턴
                    showSnackbar(view, "이미 참여중인 스터디입니다.", context)
                    return@withContext false
                } else if (isAlreadyApplied) {
                    // 이미 신청한 경우
                    showSnackbar(view, "이미 신청한 스터디입니다.", context)
                    return@withContext false
                } else {
                    true // 이미 참여 중이거나 신청한 상태가 아닐 경우 진행
                }
            }

            // 신청자의 사용자 정보 가져오기
            val applyUserData = detailRepository.getUserById(userIdx).firstOrNull()
            if (applyUserData == null) {
                Log.e("DetailViewModel", "Failed to fetch applicant user data for userIdx: $userIdx")
                return@withContext false
            }

            // 기존 신청이 없는 경우 신청 진행
            success = if (applyMethod == "선착순") {
                detailRepository.addUserToStudy(studyIdx, userIdx)
            } else {
                detailRepository.addUserToStudyRequest(studyIdx, userIdx)
            }

            if (success) {
                // 신청 성공 시, 알림 전송 로직 진행
                val title = "신청 완료"
                val body = "${studyTitle} 스터디 신청이 성공적으로 완료되었습니다."
                sendPushNotification(context, userIdx, title, body, studyIdx)

                // 글 작성자에게도 알림 전송
                val studyData = detailRepository.getStudyById(studyIdx).firstOrNull()
                val writerUserIdx = studyData?.userIdx
                if (writerUserIdx != null && writerUserIdx != userIdx) {
                    val writerFcmToken = detailRepository.getUserFcmToken(writerUserIdx)
                    if (writerFcmToken != null) {
                        val writerTitle = "새로운 스터디 신청"
                        val writerBody = "${applyUserData.userName}님이 ${studyData?.studyTitle} 스터디에 신청했습니다."
                        FCMService.sendNotificationToToken(context, writerFcmToken, writerTitle, writerBody, studyIdx)
                        // 알림 저장
                        val coverPhotoUrl = getCoverPhotoUrl(studyIdx)
                        detailRepository.insertNotification(writerUserIdx, writerTitle, writerBody, coverPhotoUrl, studyIdx)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    showSnackbar(view, "성공적으로 신청되었습니다.", context)
                } else {
                    showSnackbar(view, "신청에 실패하였습니다.", context)
                }
            }

            success // 성공 여부 반환
        }
    }

    suspend fun isAlreadyApplied(userIdx: Int, studyIdx: Int): Boolean {
        return detailRepository.isAlreadyApplied(userIdx, studyIdx)
    }



    fun showSnackbar(view: View, message: String, context: Context) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        val textSizeInPx = dpToPx(context, 14f)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx)
        snackbar.show()
    }



    // dp를 px로 변환하는 함수
    private fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    // 푸시 알림 전송 및 데이터 저장 메서드
    fun sendPushNotification(context: Context, userIdx: Int, title: String, body: String, studyIdx: Int) {
        Log.e("DetailViewModel", "sendPushNotification 호출됨: userIdx: $userIdx, title: $title, body: $body") // 로그 추가
        viewModelScope.launch {
            val userFcmToken = detailRepository.getUserFcmToken(userIdx)

            if (userFcmToken != null) {
                Log.e("DetailViewModel", "FCM 토큰 가져옴: $userFcmToken") // 토큰 확인 로그
                val result = FCMService.sendNotificationToToken(context, userFcmToken, title, body,studyIdx)
                if (result) {
                    Log.d("DetailViewModel", "Notification sent successfully to userIdx: $userIdx")
                    Log.e("DetailViewModel", "Notification sent successfully to userIdx: $userIdx") // 성공 로그

                    // 알림 내용과 이미지 URL을 데이터베이스에 저장
                    val coverPhotoUrl = getCoverPhotoUrl(studyIdx)
                    val saveResult = detailRepository.insertNotification(userIdx, title, body, coverPhotoUrl, studyIdx) // studyIdx 포함
                    if (saveResult) {
                        Log.d("DetailViewModel", "Notification saved successfully to database for userIdx: $userIdx")
                    } else {
                        Log.e("DetailViewModel", "Failed to save notification to database for userIdx: $userIdx")
                    }
                } else {
                    Log.e("DetailViewModel", "Failed to send notification to userIdx: $userIdx")
                }
            } else {
                Log.e("DetailViewModel", "Failed to retrieve FCM token for userIdx: $userIdx")
            }
        }
    }


    // 사용자가 거절되었을 때 알림을 전송하고 데이터를 저장하는 메서드
    fun notifyUserRejected(context: Context, userIdx: Int, studyIdx: Int, studyTitle: String) {
        viewModelScope.launch {
            val userFcmToken = detailRepository.getUserFcmToken(userIdx)

            if (userFcmToken != null) {
                val title = "신청 거절"
                val body = "귀하의 신청이 $studyTitle 스터디에서 거절되었습니다."
                val result = FCMService.sendNotificationToToken(context, userFcmToken, title, body,studyIdx)

                if (result) {
                    Log.d("DetailViewModel", "Notification sent successfully to userIdx: $userIdx")

                    // 알림 내용을 데이터베이스에 저장
                    val coverPhotoUrl = getCoverPhotoUrl(studyIdx)
                    val saveResult = detailRepository.insertNotification(userIdx, title, body, coverPhotoUrl,studyIdx)
                    if (saveResult) {
                        Log.d("DetailViewModel", "Notification saved successfully to database for userIdx: $userIdx")
                    } else {
                        Log.e("DetailViewModel", "Failed to save notification to database for userIdx: $userIdx")
                    }
                } else {
                    Log.e("DetailViewModel", "Failed to send notification to userIdx: $userIdx")
                }
            } else {
                Log.e("DetailViewModel", "Failed to retrieve FCM token for userIdx: $userIdx")
            }
        }
    }

    // 사용자가 승인되었을 때 알림을 전송하고 데이터를 저장하는 메서드
    fun notifyUserAccepted(context: Context, userIdx: Int, studyIdx: Int, studyTitle: String) {
        viewModelScope.launch {
            val userFcmToken = detailRepository.getUserFcmToken(userIdx)

            if (userFcmToken != null) {
                val title = "신청 승인"
                val body = "귀하의 신청이 $studyTitle 스터디에서 승인되었습니다."
                val result = FCMService.sendNotificationToToken(context, userFcmToken, title, body,studyIdx)

                if (result) {
                    Log.d("DetailViewModel", "Notification sent successfully to userIdx: $userIdx")

                    // 알림 내용을 데이터베이스에 저장
                    val coverPhotoUrl = getCoverPhotoUrl(studyIdx)
                    val saveResult = detailRepository.insertNotification(userIdx, title, body, coverPhotoUrl,studyIdx)
                    if (saveResult) {
                        Log.d("DetailViewModel", "Notification saved successfully to database for userIdx: $userIdx")
                    } else {
                        Log.e("DetailViewModel", "Failed to save notification to database for userIdx: $userIdx")
                    }
                } else {
                    Log.e("DetailViewModel", "Failed to send notification to userIdx: $userIdx")
                }
            } else {
                Log.e("DetailViewModel", "Failed to retrieve FCM token for userIdx: $userIdx")
            }
        }
    }
// 사용자가 내보내졌을 때 푸시 알림 전송하고 데이터를 저장하는 메서드
fun notifyUserKicked(context: Context, userIdx: Int, studyIdx: Int, studyTitle: String) {
    viewModelScope.launch {
        val userFcmToken = detailRepository.getUserFcmToken(userIdx)

        if (userFcmToken != null) {
            val title = "스터디 탈퇴 알림"
            val body = "$studyTitle 스터디에서 내보내졌습니다."
            val result = FCMService.sendNotificationToToken(context, userFcmToken, title, body,studyIdx)

            if (result) {
                Log.d("DetailViewModel", "Notification sent successfully to userIdx: $userIdx")

                // 알림 내용을 데이터베이스에 저장
                val coverPhotoUrl = getCoverPhotoUrl(studyIdx)
                val saveResult = detailRepository.insertNotification(userIdx, title, body, coverPhotoUrl,studyIdx)
                if (saveResult) {
                    Log.d("DetailViewModel", "Notification saved successfully to database for userIdx: $userIdx")
                } else {
                    Log.e("DetailViewModel", "Failed to save notification to database for userIdx: $userIdx")
                }
            } else {
                Log.e("DetailViewModel", "Failed to send notification to userIdx: $userIdx")
            }
        } else {
            Log.e("DetailViewModel", "Failed to retrieve FCM token for userIdx: $userIdx")
        }
    }
}

    // Cover Photo URL 가져오는 메서드
    private suspend fun getCoverPhotoUrl(studyIdx: Int): String {
        return detailRepository.getStudyPicByStudyIdx(studyIdx).firstOrNull() ?: ""
    }



    fun fetchStudyRequestMembers(studyIdx: Int) {
        viewModelScope.launch {
            val members = detailRepository.getStudyRequestMembers(studyIdx)
            _studyRequestMembers.value = members
        }
    }

    fun acceptUser(studyIdx: Int, userIdx: Int) {
        viewModelScope.launch {
            val result = detailRepository.acceptUser(studyIdx, userIdx)
            _acceptUserResult.emit(result)

            if (result) {
                // 신청자 리스트를 다시 로드
                fetchStudyRequestMembers(studyIdx)
                // 스터디 멤버 리스트도 갱신
                fetchMembersInfo(studyIdx)
            }
        }
    }

    // 특정 사용자를 스터디 요청에서 삭제하는 메소드
    fun removeUserFromApplyList(studyIdx: Int, userIdx: Int) {
        viewModelScope.launch {
            val result = detailRepository.removeUserFromStudyRequest(studyIdx, userIdx)
            _removeUserFromApplyResult.emit(result)

            if (result) {
                // 신청자 리스트를 다시 로드
                fetchStudyRequestMembers(studyIdx)
            }
        }
    }

    fun fetchUserProfile(userIdx: Int) {
        viewModelScope.launch {
            try {
                detailRepository.getUserById(userIdx).collect { user ->
                    _userData.value = user
                    // 다른 필요한 데이터 로드 로직을 여기서 추가로 처리
                }
            } catch (throwable: Throwable) {
                Log.e("DetailViewModel", "Error fetching user profile", throwable)
            }
        }
    }

    // 좋아요 상태 확인 메소드
    fun checkIfLiked(userIdx: Int, studyIdx: Int) {
        viewModelScope.launch {
            val result = studyRepository.getFavoriteStudyData(userIdx).getOrNull()?.any { it.first.studyIdx == studyIdx } ?: false
            _isLiked.value = result
        }
    }

    // 좋아요 상태 토글 메소드
    fun toggleFavoriteStatus(userIdx: Int, studyIdx: Int) {
        viewModelScope.launch {
            if (_isLiked.value) {
                val result = studyRepository.removeFavorite(userIdx, studyIdx).getOrNull()
                if (result == true) {
                    _isLiked.value = false
                }
            } else {
                val result = studyRepository.addFavorite(userIdx, studyIdx).getOrNull()
                if (result == true) {
                    _isLiked.value = true
                }
            }
        }
    }

    // studyCanApply 값을 업데이트하는 메소드
    fun updateStudyCanApplyInBackground(studyIdx: Int, canApply: Boolean) {
        viewModelScope.launch {
            try {
                val newState = if (canApply) "모집중" else "모집완료"
                val result = detailRepository.updateStudyCanApplyField(studyIdx, newState)
                if (result) {
                    // DB 업데이트 성공 시, 별도의 UI 갱신을 하지 않음
                    Log.d("SqlDetailViewModel", "Study state updated successfully.")
                } else {
                    // 실패 시, 사용자에게 오류를 알리거나, 로그를 남기거나, 상태 복구 등의 처리
                    Log.e("SqlDetailViewModel", "Failed to update study state.")
                }
            } catch (e: Exception) {
                Log.e("SqlDetailViewModel", "Error updating studyCanApply", e)
            }
        }
    }

    fun uploadImageToS3(context: Context, uri: Uri, onSuccess: (String) -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch {
            val result = writeRepository.uploadImageToS3(context, uri)
            result.onSuccess { imageUrl ->
                onSuccess(imageUrl)  // 성공 시 이미지 URL을 콜백으로 전달
            }.onFailure { e ->
                onFailure(e)  // 실패 시 오류를 콜백으로 전달
            }
        }
    }

    fun updateStudyPic(studyIdx: Int, imageUrl: String) {
        viewModelScope.launch {
            val result = detailRepository.updateStudyPic(studyIdx, imageUrl)
            if (result) {
                Log.d("DetailViewModel", "Study pic updated successfully")
            } else {
                Log.e("DetailViewModel", "Failed to update study pic")
            }
        }
    }

    // S3에서 이미지 삭제하는 메서드
    fun deleteImageFromS3(fileName: String) {
        viewModelScope.launch {
            val isDeleted = detailRepository.deleteImageFromS3(fileName)
            if (isDeleted) {
                Log.d("DetailViewModel", "Image deleted successfully from S3")
                // 추가로 필요한 로직 (예: UI 업데이트 등)
            } else {
                Log.e("DetailViewModel", "Failed to delete image from S3")
            }
        }
    }

}
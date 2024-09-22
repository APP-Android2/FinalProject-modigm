package kr.co.lion.modigm.ui.detail.vm

import android.content.Context
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.repository.DetailRepository
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.ui.notification.FCMService

class DetailViewModel: ViewModel() {

    private val detailRepository = DetailRepository()
    private val studyRepository = StudyRepository()

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
    val updateResult: StateFlow<Boolean?> get() = _updateResult

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

    private fun checkAllDataLoaded() {
        _isLoading.value = !_isStudyDataLoaded.value || !_isMemberCountLoaded.value || !_isUserDataLoaded.value || !_isStudyPicLoaded.value || !_isTechListLoaded.value
    }

    fun clearData() {
        _studyData.value = null
        _memberCount.value = 0
        _userData.value = null
        _studyTechList.value = emptyList()
        _studyPic.value = null
        _studyMembers.value = emptyList()
    }

    fun loadStudyData(studyIdx: Int, userIdx: Int) {
        _isLoading.value = true

        viewModelScope.launch {
            // 1. 스터디 데이터 로드
            launch {
                getStudy(studyIdx)
            }

            // 2. 멤버 수 로드
            launch {
                countMembersByStudyIdx(studyIdx)
            }

            // 3. 글 작성자 데이터 로드
            launch {
                getUserById(userIdx)
            }

            // 4. 스터디 이미지 로드
            launch {
                getStudyPic(studyIdx)
            }

            // 5. 스터디 기술 목록 로드
            launch {
                getTechIdxByStudyIdx(studyIdx)
            }
        }
    }

    // 특정 studyIdx에 대한 스터디 데이터를 가져오는 메소드
    suspend fun getStudy(studyIdx: Int) {
        try {
            detailRepository.getStudyById(studyIdx).collect { data ->
                _studyData.value = data
                _isStudyDataLoaded.value = true
                checkAllDataLoaded()
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
                checkAllDataLoaded()
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
                checkAllDataLoaded()
            }
        } catch (throwable: Throwable) {
            Log.e("DetailViewModel", "Error fetching study pic", throwable)
        }
    }

    suspend fun getUserById(userIdx: Int) {
        try {
            detailRepository.getUserById(userIdx).collect { user ->
                _userData.value = user
                _isUserDataLoaded.value = true
                checkAllDataLoaded()
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
                checkAllDataLoaded()
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

    // 업데이트 결과 초기화 함수
    fun clearUpdateResult() {
        _updateResult.value = null
    }

    // 특정 사용자를 스터디에서 삭제하는 메소드
    fun removeUserFromStudy(studyIdx: Int, userIdx: Int) {
        viewModelScope.launch {
            val result = detailRepository.removeUserFromStudy(studyIdx, userIdx)
            _removeUserResult.emit(result)
        }
    }

    // 사용자가 이미 스터디에 참여 중인지 확인하는 함수
    fun checkIfUserAlreadyMember(studyIdx: Int, userIdx: Int) {
        viewModelScope.launch {
            try {
                // DetailRepository를 통해 해당 사용자가 스터디 멤버인지 체크
                val isMember = detailRepository.isUserAlreadyMember(studyIdx, userIdx).firstOrNull() ?: false
                _isUserAlreadyMember.value = isMember
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Error checking if user is already a member", e)
                _isUserAlreadyMember.value = false
            }
        }
    }




    // FCM 토큰을 서버에 등록
    fun registerFcmToken(userIdx: Int, fcmToken: String) {
        viewModelScope.launch {
            val result = detailRepository.registerFcmToken(userIdx, fcmToken)
            if (result) {
                Log.d("DetailViewModel", "FCM 토큰 등록 성공 userIdx: $userIdx")
            } else {
                Log.e("DetailViewModel", "FCM 토큰 등록 실패 userIdx: $userIdx")
            }
        }
    }

    // 사용자가 신청할 때 알림을 전송하고 데이터를 저장하는 메서드
    fun addUserToStudyOrRequest(studyIdx: Int, userIdx: Int, applyMethod: String, context: Context, view: View, studyTitle: String) {
        viewModelScope.launch {
            // 사용자가 이미 참여 중인지 확인
            val isAlreadyMember = detailRepository.isUserAlreadyMember(studyIdx, userIdx).firstOrNull() ?: false

            // 이미 신청된 상태인지 확인
            val isAlreadyApplied = detailRepository.isAlreadyApplied(userIdx, studyIdx)

            if (isAlreadyMember) {
                // 이미 스터디에 참여 중인 경우
                withContext(Dispatchers.Main) {
                    val snackbar = Snackbar.make(
                        view,
                        "이미 참여중인 스터디입니다.",
                        Snackbar.LENGTH_LONG
                    )
                    val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    val textSizeInPx = dpToPx(context, 14f)
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx)
                    snackbar.show()
                }
                return@launch
            }

            if (isAlreadyApplied) {
                // 이미 신청한 경우
                withContext(Dispatchers.Main) {
                    val snackbar = Snackbar.make(
                        view,
                        "이미 신청한 스터디입니다.",
                        Snackbar.LENGTH_LONG
                    )
                    val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    val textSizeInPx = dpToPx(context, 14f)
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx)
                    snackbar.show()
                }
                return@launch
            }

            // 신청자의 사용자 정보 가져오기
            val applyUserData = detailRepository.getUserById(userIdx).firstOrNull()
            if (applyUserData == null) {
                Log.e("DetailViewModel", "Failed to fetch applicant user data for userIdx: $userIdx")
                return@launch
            }

            // 기존 신청이 없는 경우 신청 진행
            val success = if (applyMethod == "선착순") {
                detailRepository.addUserToStudy(studyIdx, userIdx)
            } else {
                detailRepository.addUserToStudyRequest(studyIdx, userIdx)
            }

            if (success) {
                // 신청 성공 시, 알림 전송 로직 진행
                val title = "신청 완료"
                val body = "${studyTitle}스터디 신청이 성공적으로 완료되었습니다."
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
                _addUserResult.emit(true)
            } else {
                _addUserResult.emit(false)
            }
        }
    }


    // dp를 px로 변환하는 함수
    private fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    // 푸시 알림 전송 및 데이터 저장 메서드
    fun sendPushNotification(context: Context, userIdx: Int, title: String, body: String, studyIdx: Int) {
        viewModelScope.launch {
            val userFcmToken = detailRepository.getUserFcmToken(userIdx)

            if (userFcmToken != null) {
                val result = FCMService.sendNotificationToToken(context, userFcmToken, title, body,studyIdx)
                if (result) {
                    Log.d("DetailViewModel", "Notification sent successfully to userIdx: $userIdx")

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
}
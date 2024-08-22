package kr.co.lion.modigm.ui.detail.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.model.SqlUserData
import kr.co.lion.modigm.repository.SqlDetailRepository
import kr.co.lion.modigm.repository.StudyListRepository

class SqlDetailViewModel: ViewModel() {

    private val sqlDetailRepository = SqlDetailRepository()
    private val studyListRepository = StudyListRepository()

    private val _studyData = MutableStateFlow<SqlStudyData?>(null)
    val studyData: StateFlow<SqlStudyData?> = _studyData

    private val _memberCount = MutableStateFlow(0)
    val memberCount: StateFlow<Int> = _memberCount

    private val _allStudyDetails = MutableStateFlow<List<Triple<SqlStudyData, Int, Boolean>>>(emptyList())
    val allStudyDetails: StateFlow<List<Triple<SqlStudyData, Int, Boolean>>> = _allStudyDetails

    private val _userData = MutableStateFlow<SqlUserData?>(null)
    val userData: StateFlow<SqlUserData?> = _userData

    private val _studyTechList = MutableStateFlow<List<Int>>(emptyList())
    val studyTechList: StateFlow<List<Int>> get() = _studyTechList

    private val _updateResult = MutableStateFlow<Boolean?>(null)
    val updateResult: StateFlow<Boolean?> get() = _updateResult

    private val _studyPic = MutableStateFlow<String?>(null)
    val studyPic: StateFlow<String?> = _studyPic

    private val _studySkills = MutableStateFlow<List<Int>>(emptyList())
    val studySkills: StateFlow<List<Int>> get() = _studySkills

    private val _studyMembers = MutableStateFlow<List<SqlUserData>>(emptyList())
    val studyMembers: StateFlow<List<SqlUserData>> = _studyMembers

    private val _removeUserResult = MutableSharedFlow<Boolean>()
    val removeUserResult: SharedFlow<Boolean> = _removeUserResult

    private val _addUserResult = MutableSharedFlow<Boolean>()
    val addUserResult: SharedFlow<Boolean> = _addUserResult

    private val _studyRequestMembers = MutableStateFlow<List<SqlUserData>>(emptyList())
    val studyRequestMembers: StateFlow<List<SqlUserData>> = _studyRequestMembers

    private val _acceptUserResult = MutableSharedFlow<Boolean>()
    val acceptUserResult: SharedFlow<Boolean> = _acceptUserResult

    private val _removeUserFromApplyResult = MutableSharedFlow<Boolean>()
    val removeUserFromApplyResult: SharedFlow<Boolean> = _removeUserFromApplyResult

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    fun clearData() {
        _studyData.value = null
        _memberCount.value = 0
        _userData.value = null
        _studyTechList.value = emptyList()
        _studyPic.value = null
        _studyMembers.value = emptyList()
    }

    // 특정 studyIdx에 대한 스터디 데이터를 가져오는 메소드
    fun getStudy(studyIdx: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                sqlDetailRepository.getStudyById(studyIdx).collect { data ->
                    _studyData.value = data
                    data?.let { getStudyPic(it.studyIdx) } // 데이터 가져온 후 이미지 로드
                }
            } catch (throwable: Throwable) {
                Log.e("DetailViewModel", "Error fetching study data", throwable)
            }
        }
    }

    // 특정 studyIdx에 대한 스터디 멤버 수를 가져오는 메소드
    fun countMembersByStudyIdx(studyIdx: Int) {
        viewModelScope.launch {
            try {
                sqlDetailRepository.countMembersByStudyIdx(studyIdx).collect { count ->
                    _memberCount.value = count
                }
            } catch (throwable: Throwable) {
                Log.e("DetailViewModel", "Error counting members", throwable)
            }
        }
    }
    fun fetchMembersInfo(studyIdx: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // studyIdx에 해당하는 userIdx 리스트 가져오기
                val userIds = sqlDetailRepository.getUserIdsByStudyIdx(studyIdx)
                Log.d("SqlDetailViewModel", "Fetched user IDs: $userIds")

                if (userIds.isEmpty()) {
                    Log.d("SqlDetailViewModel", "No user IDs found for studyIdx: $studyIdx")
                    return@launch
                }

                // 해당 userIdx들에 해당하는 사용자 정보 가져오기
                val users = mutableListOf<SqlUserData>()

                userIds.forEach { userIdx ->
                    sqlDetailRepository.getUserById(userIdx).collect { user ->
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
    fun getStudyPic(studyIdx: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            sqlDetailRepository.getStudyPicByStudyIdx(studyIdx).collect { pic ->
                _studyPic.value = pic
            }
        }
    }

    fun getUserById(userIdx: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _userData.value = null  // 데이터 로드 전에 null로 초기화
            try {
                sqlDetailRepository.getUserById(userIdx).collect { user ->
                    if (user != null) {
                        _userData.value = user
                        Log.d("DetailViewModel", "User data fetched successfully: $user")
                    } else {
                        Log.e("DetailViewModel", "No user data found for userIdx: $userIdx")
                    }
                }
            } catch (throwable: Throwable) {
                Log.e("DetailViewModel", "Error fetching user data", throwable)
            }
        }
    }

    fun getTechIdxByStudyIdx(studyIdx: Int) {
        viewModelScope.launch {
            sqlDetailRepository.getTechIdxByStudyIdx(studyIdx).collect { techList ->
                _studyTechList.value = techList
            }
        }
    }

    // studyState 값을 업데이트하는 메소드 추가
    fun updateStudyState(studyIdx: Int, newState: Int) {
        viewModelScope.launch {
            val result = sqlDetailRepository.updateStudyState(studyIdx, newState)
            _updateResult.emit(result)
        }
    }

    // 스터디 데이터를 업데이트하는 함수
    fun updateStudyData(studyData: SqlStudyData) {
        viewModelScope.launch {
            val result = sqlDetailRepository.updateStudy(studyData)
            _updateResult.value = result
        }
    }

    fun insertSkills(studyIdx: Int, skills: List<Int>) {
        viewModelScope.launch {
            sqlDetailRepository.insertSkills(studyIdx, skills)
        }
    }

    // 업데이트 결과 초기화 함수
    fun clearUpdateResult() {
        _updateResult.value = null
    }

    // 특정 사용자를 스터디에서 삭제하는 메소드
    fun removeUserFromStudy(studyIdx: Int, userIdx: Int) {
        viewModelScope.launch {
            val result = sqlDetailRepository.removeUserFromStudy(studyIdx, userIdx)
            _removeUserResult.emit(result)
        }
    }

    fun addUserToStudyOrRequest(studyIdx: Int, userIdx: Int, applyMethod: String) {
        viewModelScope.launch {
            val result = if (applyMethod == "선착순") {
                sqlDetailRepository.addUserToStudy(studyIdx, userIdx)
            } else {
                sqlDetailRepository.addUserToStudyRequest(studyIdx, userIdx)
            }
            _addUserResult.emit(result)
        }
    }

    fun fetchStudyRequestMembers(studyIdx: Int) {
        viewModelScope.launch {
            val members = sqlDetailRepository.getStudyRequestMembers(studyIdx)
            _studyRequestMembers.value = members
        }
    }

    fun acceptUser(studyIdx: Int, userIdx: Int) {
        viewModelScope.launch {
            val result = sqlDetailRepository.acceptUser(studyIdx, userIdx)
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
            val result = sqlDetailRepository.removeUserFromStudyRequest(studyIdx, userIdx)
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
                sqlDetailRepository.getUserById(userIdx).collect { user ->
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
            val result = studyListRepository.getFavoriteStudyData(userIdx).getOrNull()?.any { it.first.studyIdx == studyIdx } ?: false
            _isLiked.value = result
        }
    }

    // 좋아요 상태 토글 메소드
    fun toggleFavoriteStatus(userIdx: Int, studyIdx: Int) {
        viewModelScope.launch {
            if (_isLiked.value) {
                val result = studyListRepository.removeFavorite(userIdx, studyIdx).getOrNull()
                if (result == true) {
                    _isLiked.value = false
                }
            } else {
                val result = studyListRepository.addFavorite(userIdx, studyIdx).getOrNull()
                if (result == true) {
                    _isLiked.value = true
                }
            }
        }
    }
}
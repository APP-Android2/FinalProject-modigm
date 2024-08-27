package kr.co.lion.modigm.ui.profile.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.repository.ProfileRepository

class ProfileViewModel : ViewModel() {
    private val profileRepository = ProfileRepository()

    // 사용자 uid
    private val _profileUid = MutableStateFlow(-1)
    val profileUid: MutableStateFlow<Int> = _profileUid

    // UserIdx
    private val _profileUserIdx = MutableStateFlow<Int?>(-1)
    val profileUserIdx: MutableStateFlow<Int?> = _profileUserIdx

    // 프로필 사진
    private val _profileUserImage = MutableStateFlow<String?>(null)
    val profileUserImage: MutableStateFlow<String?> = _profileUserImage

    // 사용자 이름
    private val _profileName = MutableStateFlow<String?>(null)
    val profileName: MutableStateFlow<String?> = _profileName

    // 자기소개
    private val _profileIntro = MutableStateFlow<String?>(null)
    val profileIntro: MutableStateFlow<String?> = _profileIntro

    // 관심분야 리스트 (콤마로 연결된 문자열)
    private val _profileInterests = MutableStateFlow<String?>(null)
    val profileInterests: MutableStateFlow<String?> = _profileInterests

    // 링크 리스트
    private val _profileLinkList = MutableStateFlow<List<String>>(emptyList())
    val profileLinkList: MutableStateFlow<List<String>> get() = _profileLinkList

    // 사용자가 참여한 스터디 제목
    private val _profilePartStudyTitle = MutableStateFlow<String?>(null)
    val profilePartStudyTitle: MutableStateFlow<String?> = _profilePartStudyTitle

    // 사용자가 진행한 스터디 제목
    private val _profileHostStudyTitle = MutableStateFlow<String?>(null)
    val profileHostStudyTitle: MutableStateFlow<String?> = _profileHostStudyTitle

    // 사용자가 참여한 스터디 리스트
    private val _profilePartStudyList = MutableStateFlow<List<SqlStudyData>?>(null)
    val profilePartStudyList: MutableStateFlow<List<SqlStudyData>?> = _profilePartStudyList

    // 사용자가 진행한 스터디 리스트
    private val _profileHostStudyList = MutableStateFlow<List<SqlStudyData>?>(null)
    val profileHostStudyList: MutableStateFlow<List<SqlStudyData>?> = _profileHostStudyList


    /** functions **/

    // 유저 기본 정보를 불러온다.
    fun loadUserData() = viewModelScope.launch {
        val userIdx = _profileUserIdx.value

        try {
            profileRepository.loadUserData(userIdx).collect { user ->
                // 사용자 이름
                _profileName.value = user?.userName
                _profileHostStudyTitle.value = "${user?.userName}님이 진행한 스터디"
                _profilePartStudyTitle.value = "${user?.userName}님이 참여한 스터디"
                // 자기소개
                _profileIntro.value = user?.userIntro
                // 관심분야 리스트
                _profileInterests.value = user?.userInterests
                // 프로필 사진
                _profileUserImage.value = user?.userProfilePic
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "loadUserData(): ${e.message}")
        }
    }

    // 유저의 자기소개 링크를 불러온다.
    fun loadUserLinkListData() = viewModelScope.launch {
        val userIdx = _profileUserIdx.value

        try {
            profileRepository.loadUserLinkData(userIdx).collect { linkList ->
                _profileLinkList.value = linkList
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "loadUserLinkListData(): ${e.message}")
        }
    }

    // 사용자가 진행한 스터디 목록 (3개만)
    fun loadHostStudyList(userIdx: Int) = viewModelScope.launch {
        try {
            profileRepository.loadSmallHostStudyList(userIdx).collect { studyList ->
                _profileHostStudyList.value = studyList
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "loadPartStudyList(): ${e.message}")
        }
    }

    // 사용자가 진행하지 않고 단순 참여한 스터디 목록 (3개만)
    fun loadPartStudyList(userIdx: Int) = viewModelScope.launch {
        try {
            profileRepository.loadSmallPartStudyList(userIdx).collect { studyList ->
                _profilePartStudyList.value = studyList
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "loadHostStudyList(): ${e.message}")
        }
    }
}
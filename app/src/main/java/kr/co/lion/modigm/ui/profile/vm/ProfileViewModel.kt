package kr.co.lion.modigm.ui.profile.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.repository.ProfileRepository

class ProfileViewModel : ViewModel() {
    private val profileRepository = ProfileRepository()

    // 사용자 uid
    private val _profileUid = MutableLiveData<Int>()
    val profileUid: MutableLiveData<Int> = _profileUid

    // UserIdx
    private val _profileUserIdx = MutableLiveData<Int>()
    val profileUserIdx: MutableLiveData<Int> = _profileUserIdx

    // 프로필 사진
    private val _profileUserImage = MutableLiveData<String>()
    val profileUserImage: MutableLiveData<String> = _profileUserImage

    // 사용자 이름
    private val _profileName = MutableLiveData<String>()
    val profileName: MutableLiveData<String> = _profileName

    // 자기소개
    private val _profileIntro = MutableLiveData<String>()
    val profileIntro: MutableLiveData<String> = _profileIntro

    // 관심분야 리스트 (콤마로 연결된 문자열)
    private val _profileInterests = MutableLiveData<String>()
    val profileInterests: MutableLiveData<String> = _profileInterests

    // 링크 리스트
    private val _profileLinkList = MutableLiveData<List<String>>()
    val profileLinkList: MutableLiveData<List<String>> = _profileLinkList

    // 사용자가 참여한 스터디 제목
    private val _profilePartStudyTitle = MutableLiveData<String>()
    val profilePartStudyTitle: MutableLiveData<String> = _profilePartStudyTitle

    // 사용자가 진행한 스터디 제목
    private val _profileHostStudyTitle = MutableLiveData<String>()
    val profileHostStudyTitle: MutableLiveData<String> = _profileHostStudyTitle

    // 사용자가 참여한 스터디 리스트
    private val _profilePartStudyList = MutableLiveData<List<SqlStudyData>>()
    val profilePartStudyList: MutableLiveData<List<SqlStudyData>> = _profilePartStudyList

    // 사용자가 진행한 스터디 리스트
    private val _profileHostStudyList = MutableLiveData<List<SqlStudyData>>()
    val profileHostStudyList: MutableLiveData<List<SqlStudyData>> = _profileHostStudyList


    /** functions **/

    // 유저 기본 정보를 불러온다.
    fun loadUserData() = viewModelScope.launch {
        val userIdx = _profileUserIdx.value

        try {
            val response = profileRepository.loadUserData(userIdx)

            // 사용자 이름
            _profileName.value = response?.userName
            _profileHostStudyTitle.value = "${response?.userName}님이 진행한 스터디"
            _profilePartStudyTitle.value = "${response?.userName}님이 참여한 스터디"
            // 자기소개
            _profileIntro.value = response?.userIntro
            // 관심분야 리스트
            _profileInterests.value = response?.userInterests
            // 프로필 사진
            _profileUserImage.value = response?.userProfilePic
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "loadUserData(): ${e.message}")
        }
    }

    // 유저의 자기소개 링크를 불러온다.
    fun loadUserLinkListData() = viewModelScope.launch {
        val userIdx = _profileUserIdx.value

        try {
            _profileLinkList.value = profileRepository.loadUserLinkData(userIdx)
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "loadUserLinkListData(): ${e.message}")
        }
    }

    // 사용자가 진행한 스터디 목록 (3개만)
    fun loadHostStudyList(userIdx: Int) = viewModelScope.launch {
        try {
            val response = profileRepository.loadSmallHostStudyList(userIdx)

            _profileHostStudyList.value = response

        } catch (e: Exception) {
            Log.e("profilevm", "loadPartStudyList(): ${e.message}")
        }
    }

    // 사용자가 진행하지 않고 단순 참여한 스터디 목록 (3개만)
    fun loadPartStudyList(userIdx: Int) = viewModelScope.launch {
        try {
            val response = profileRepository.loadSmallPartStudyList(userIdx)

            _profilePartStudyList.value = response

        } catch (e: Exception) {
            Log.e("profilevm", "loadHostStudyList(): ${e.message}")
        }
    }
}
package kr.co.lion.modigm.ui.profile.vm

import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.model.SqlUserLinkData
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.ProfileRepository
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.repository.UserInfoRepository
import kr.co.lion.modigm.util.ModigmApplication

class ProfileViewModel : ViewModel() {
    private val profileRepository = ProfileRepository()
    private val studyRepository = StudyRepository()

    // 사용자 uid
    private val _profileUid = MutableLiveData<Int>()
    val profileUid: MutableLiveData<Int> = _profileUid

    // 사용자 uid
    private val _profileUserIdx = MutableLiveData<Int>()
    val profileUserIdx: MutableLiveData<Int> = _profileUserIdx

    // 사용자 uid
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
    private val _profileLinkList = MutableLiveData<List<SqlUserLinkData>>()
    val profileLinkList: MutableLiveData<List<SqlUserLinkData>> = _profileLinkList

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

    // 참여한 스터디 리스트를 불러온다.
    fun loadHostStudyList(userIdx: Int) = viewModelScope.launch {
        try {
            val response = profileRepository.loadPartStudyList(userIdx)

            // 사용자 이름
            _profileHostStudyList.value = response

        } catch (e: Exception) {
            Log.e("profilevm", "loadPartStudyList(): ${e.message}")
        }
    }

    // 참여한 스터디 리스트를 불러온다.
    fun loadPartStudyList(uid: String) = viewModelScope.launch {
        try {
            val response = studyRepository.loadStudyHostDataByUid(uid)

            // 사용자 이름
            //_profilePartStudyList.value = response

        } catch (e: Exception) {
            Log.e("profilevm", "loadHostStudyList(): ${e.message}")
        }
    }
}
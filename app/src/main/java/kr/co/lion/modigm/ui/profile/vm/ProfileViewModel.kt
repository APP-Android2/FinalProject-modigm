package kr.co.lion.modigm.ui.profile.vm

import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
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

    // 사용자가 참여한 스터디 리스트
    private val _profilePartStudyList = MutableLiveData<List<StudyData>>()
    val profilePartStudyList: MutableLiveData<List<StudyData>> = _profilePartStudyList

    // 사용자가 진행한 스터디 리스트
    private val _profileHostStudyList = MutableLiveData<List<StudyData>>()
    val profileHostStudyList: MutableLiveData<List<StudyData>> = _profileHostStudyList


    /** functions **/

    // 유저 기본 정보를 불러온다.
    fun loadUserData(context: Context, imageView: ImageView) = viewModelScope.launch {
        val uid = _profileUid.value
        val currentUser = ModigmApplication.prefs.getUserData("currentUserData")

        // Uid가 현재 로그인된 사용자 uid와 같을 경우 SharedPreference에서 정보를 가지고 온다.
        // 일단 돌아가지 않게 설정
        if (false /*uid == currentUser?.userUid*/) {
            // 사용자 이름
            _profileName.value = currentUser?.userName
            // 자기소개
            _profileIntro.value = currentUser?.userIntro
            // 관심분야 리스트
            //_profileInterests.value = currentUser?.userInterests
            // 링크 리스트
            _profileLinkList.value = currentUser?.userLinkList
            // 프로필 사진
            // userRepository.loadUserProfilePic(context, currentUser?.userProfilePic!!, imageView)
        } else {
            // Uid가 현재 로그인된 사용자 uid와 다를 경우 데이터베이스에서 정보를 가지고 온다.
            try {
                val response = profileRepository.loadUserData(uid)

                // 사용자 이름
                _profileName.value = response?.userName
                // 자기소개
                _profileIntro.value = response?.userIntro
                // 관심분야 리스트
                _profileInterests.value = response?.userInterests
                // 프로필 사진
                //profileRepository.loadUserProfilePic(context, response?.userProfilePic!!, imageView)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "loadUserData(): ${e.message}")
            }
        }
    }

    // 유저의 자기소개 링크를 불러온다.
    fun loadUserLinkListData() = viewModelScope.launch {
        val uid = _profileUid.value

        try {
            val response = profileRepository.loadUserData(uid)

            // 링크 리스트
            //_profileLinkList.value = response?.userLinkList
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "loadUserData(): ${e.message}")
        }


    }

    // 참여한 스터디 리스트를 불러온다.
    fun loadPartStudyList(uid: String) = viewModelScope.launch {
        try {
            val response = studyRepository.loadStudyPartDataByUid(uid)

            // 사용자 이름
            _profilePartStudyList.value = response

        } catch (e: Exception) {
            Log.e("profilevm", "loadPartStudyList(): ${e.message}")
        }
    }

    // 참여한 스터디 리스트를 불러온다.
    fun loadHostStudyList(uid: String) = viewModelScope.launch {
        try {
            val response = studyRepository.loadStudyHostDataByUid(uid)

            // 사용자 이름
            _profileHostStudyList.value = response

        } catch (e: Exception) {
            Log.e("profilevm", "loadHostStudyList(): ${e.message}")
        }
    }
}
package kr.co.lion.modigm.ui.profile.vm

import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.repository.UserInfoRepository

class ProfileViewModel : ViewModel() {
    private val userRepository = UserInfoRepository()
    private val studyRepository = StudyRepository()

    // 사용자 이름
    private val _profileName = MutableLiveData<String>()
    val profileName: MutableLiveData<String> = _profileName

    // 자기소개
    private val _profileIntro = MutableLiveData<String>()
    val profileIntro: MutableLiveData<String> = _profileIntro

    // 관심분야 리스트
    private val _profileInterestList = MutableLiveData<List<Int>>()
    val profileInterestList: MutableLiveData<List<Int>> = _profileInterestList

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
    fun loadUserData(uid: String, context: Context, imageView: ImageView) = viewModelScope.launch {
        try {
            val response = userRepository.loadUserData(uid)

            // 사용자 이름
            _profileName.value = response?.userName
            // 자기소개
            _profileIntro.value = response?.userIntro
            // 관심분야 리스트
            _profileInterestList.value = response?.userInterestList
            // 링크 리스트
            _profileLinkList.value = response?.userLinkList
            // 프로필 사진
            userRepository.loadUserProfilePic(context, response?.userProfilePic!!, imageView)
        } catch (e: Exception) {
            Log.e("profilevm", "loadUserData(): ${e.message}")
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
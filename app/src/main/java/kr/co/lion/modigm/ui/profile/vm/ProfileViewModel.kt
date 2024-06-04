package kr.co.lion.modigm.ui.profile.vm

import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.repository.UserInfoRepository
import kr.co.lion.modigm.util.Interest

class ProfileViewModel : ViewModel() {
    private val userRepository = UserInfoRepository()
    private val studyRepository = StudyRepository()

    // 사용자 이름
    private val _profileName = MutableLiveData<String>()
    val profileName: MutableLiveData<String> = _profileName

    // 자기소개
    private val _profileIntro = MutableLiveData<String>()
    val profileIntro: MutableLiveData<String> = _profileIntro

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
    fun loadUserData(uid: String, context: Context, imageView: ImageView, chipGroup: ChipGroup) = viewModelScope.launch {
        try {
            val response = userRepository.loadUserData(uid)

            // 사용자 이름
            _profileName.value = response?.userName
            // 자기소개
            _profileIntro.value = response?.userIntro
            // 링크 리스트
            _profileLinkList.value = response?.userLinkList
            // 프로필 사진
            userRepository.loadUserProfilePic(context, response?.userProfilePic!!, imageView)
            // 관심분야
            for (interestNum in response.userInterestList) {
                chipGroup.addView(Chip(context).apply {
                    // chip 텍스트 설정: 저장되어 있는 숫자로부터 enum 클래스를 불러오고 저장된 str 보여주기
                    text = Interest.fromNum(interestNum)!!.str
                    // 자동 padding 없애기
                    setEnsureMinTouchTargetSize(false)
                    // 배경 흰색으로 지정
                    setChipBackgroundColorResource(android.R.color.white)
                    // 클릭 불가
                    isClickable = false
                    // chip에서 X 버튼 보이게 하기
                    //isCloseIconVisible = true
                    // X버튼 누르면 chip 없어지게 하기
                    //setOnCloseIconClickListener { fragmentProfileBinding.chipGroupProfile.removeView(this) }
                })
            }
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
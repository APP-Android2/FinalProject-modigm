package kr.co.lion.modigm.ui.profile.vm

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.SqlUserData
import kr.co.lion.modigm.model.SqlUserLinkData
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.repository.ProfileRepository
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.repository.UserInfoRepository
import kr.co.lion.modigm.ui.profile.ProfileFragment
import kr.co.lion.modigm.util.Interest
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication

class EditProfileViewModel: ViewModel() {
    private val profileRepository = ProfileRepository()

    // 프로필 사진
    private val _editProfilePic = MutableLiveData<String>()
    val editProfilePic: MutableLiveData<String> = _editProfilePic

    // 사용자 이름
    private val _editProfileName = MutableLiveData<String>()
    val editProfileName: MutableLiveData<String> = _editProfileName

    // 자기소개
    private val _editProfileIntro = MutableLiveData<String>()
    val editProfileIntro: MutableLiveData<String> = _editProfileIntro

    // 이메일
    private val _editProfileEmail = MutableLiveData<String>()
    val editProfileEmail: MutableLiveData<String> = _editProfileEmail

    // 로그인 방법
    private val _editProfileProvider = MutableLiveData<String>()
    val editProfileProvider: MutableLiveData<String> = _editProfileProvider

    // 전화번호
    private val _editProfilePhone = MutableLiveData<String>()
    val editProfilePhone: MutableLiveData<String> = _editProfilePhone

    // 링크 (입력)
    private val _editProfileNewLink = MutableLiveData<String>()
    val editProfileNewLink: MutableLiveData<String> = _editProfileNewLink

    // 관심분야 리스트
    private val _editProfileInterests = MutableLiveData<String>()
    val editProfileInterests: MutableLiveData<String> = _editProfileInterests

    // 링크 리스트
    private val _editProfileLinkList = MutableLiveData<List<String>>()
    val editProfileLinkList: MutableLiveData<List<String>> = _editProfileLinkList


    /** functions **/

    // 유저 기본 정보를 불러온다.
    fun loadUserData() = viewModelScope.launch {
        val userIdx = 9689//ModigmApplication.prefs.getUserData("currentUserData")?.userIdx

        try {
            val response = profileRepository.loadUserData(userIdx)

            // 프로필 사진
            _editProfilePic.value = response?.userProfilePic
            // 사용자 이름
            _editProfileName.value = response?.userName
            // 이메일
            _editProfileEmail.value = response?.userEmail
            // 로그인 방법
            _editProfileProvider.value = response?.userProvider
            // 전화번호
            _editProfilePhone.value = response?.userPhone
            // 자기소개
            _editProfileIntro.value = response?.userIntro
            // 관심분야 리스트
            _editProfileInterests.value = response?.userInterests
        } catch (e: Exception) {
            Log.e("EditProfileViewModel", "loadUserData(): ${e.message}")
        }
    }

    // 유저의 자기소개 링크를 불러온다.
    fun loadUserLinkData() = viewModelScope.launch {
        val userIdx = 9689//ModigmApplication.prefs.getUserData("currentUserData")?.userIdx

        try {
            _editProfileLinkList.value = profileRepository.loadUserLinkData(userIdx)
        } catch (e: Exception) {
            Log.e("EditProfileViewModel", "loadUserLinkListData(): ${e.message}")
        }
    }

    fun addLinkToList() {
        val newLink = editProfileNewLink.value
        if (!newLink.isNullOrEmpty()) {
            _editProfileLinkList.value = _editProfileLinkList.value?.toMutableList()?.apply {
                add(newLink)
            }
        }
    }

    fun removeLinkFromList(link: String) {
        _editProfileLinkList.value = _editProfileLinkList.value?.filter { it != link }
    }

    fun updateUserData(profileFragment: ProfileFragment) = viewModelScope.launch {
        // 데이터를 객체에 담는다
        val user = SqlUserData(
            userIdx = 9689,//ModigmApplication.prefs.getInt("currentUserData"),
            userProfilePic = _editProfilePic.value!!,
            userIntro = _editProfileIntro.value!!,
            userInterests = _editProfileInterests.value!!
        )

        // 데이터베이스 업데이트
        profileRepository.updateUserData(user)

        // 프로필 화면 재로드
        profileFragment.updateViews()
    }

    fun updateUserLinkData(profileFragment: ProfileFragment) = viewModelScope.launch {
        val userIdx = 9689//ModigmApplication.prefs.getUserData("currentUserData")?.userIdx

        // 데이터베이스 업데이트
        profileRepository.updateUserLinkData(userIdx, _editProfileLinkList.value!!)

        // 프로필 화면 재로드
        profileFragment.updateViews()
    }

    // 링크 목록 순서 변경
    fun reorderLinks(from: Int, to: Int) {
        val updatedList = editProfileLinkList.value?.toMutableList() ?: return
        val movedLink = updatedList.removeAt(from)
        updatedList.add(to, movedLink)
        editProfileLinkList.value = updatedList
    }
}
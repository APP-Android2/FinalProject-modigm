package kr.co.lion.modigm.ui.profile.vm

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.repository.ProfileRepository
import kr.co.lion.modigm.ui.profile.ProfileFragment
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class EditProfileViewModel: ViewModel() {
    private val profileRepository = ProfileRepository()
    var picChanged = false

    // 프로필 사진 Uri
    private val _editProfilePicUri = MutableStateFlow<Uri?>(null)
    val editProfilePicUri: MutableStateFlow<Uri?> = _editProfilePicUri

    // 프로필 사진 Url
    private val _editProfilePicUrl = MutableStateFlow<String?>(null)
    val editProfilePicUrl: MutableStateFlow<String?> = _editProfilePicUrl

    // 사용자 이름
    private val _editProfileName = MutableStateFlow<String?>(null)
    val editProfileName: MutableStateFlow<String?> = _editProfileName

    // 자기소개
    private val _editProfileIntro = MutableStateFlow<String?>(null)
    val editProfileIntro: MutableStateFlow<String?> = _editProfileIntro

    // 이메일
    private val _editProfileEmail = MutableStateFlow<String?>(null)
    val editProfileEmail: MutableStateFlow<String?> = _editProfileEmail

    // 로그인 방법
    private val _editProfileProvider = MutableStateFlow<String?>(null)
    val editProfileProvider: MutableStateFlow<String?> = _editProfileProvider

    // 전화번호
    private val _editProfilePhone = MutableStateFlow<String?>(null)
    val editProfilePhone: MutableStateFlow<String?> = _editProfilePhone

    // 링크 (입력)
    private val _editProfileNewLink = MutableStateFlow<String?>(null)
    val editProfileNewLink: MutableStateFlow<String?> = _editProfileNewLink

    // 관심분야 리스트
    private val _editProfileInterests = MutableStateFlow<String?>(null)
    val editProfileInterests: MutableStateFlow<String?> = _editProfileInterests

    // 링크 리스트
    private val _editProfileLinkList = MutableStateFlow<List<String>>(emptyList())
    val editProfileLinkList: MutableStateFlow<List<String>> = _editProfileLinkList

    /** functions **/

    // 유저 기본 정보를 불러온다.
    fun loadUserData() = viewModelScope.launch {
        val userIdx = prefs.getInt("currentUserIdx")

        try {
            profileRepository.loadUserData(userIdx).collect { user ->
                // 프로필 사진
                _editProfilePicUrl.value = user?.userProfilePic
                // 사용자 이름
                _editProfileName.value = user?.userName
                // 이메일
                _editProfileEmail.value = user?.userEmail
                // 로그인 방법
                _editProfileProvider.value = user?.userProvider
                // 전화번호
                _editProfilePhone.value = user?.userPhone
                // 자기소개
                _editProfileIntro.value = user?.userIntro
                // 관심분야 리스트
                _editProfileInterests.value = user?.userInterests
            }
        } catch (e: Exception) {
            Log.e("EditProfileViewModel", "loadUserData(): ${e.message}")
        }
    }

    // 유저의 자기소개 링크를 불러온다.
    fun loadUserLinkData() = viewModelScope.launch {
        val userIdx = prefs.getInt("currentUserIdx")

        try {
            profileRepository.loadUserLinkData(userIdx).collect { linkList ->
                _editProfileLinkList.value = linkList
            }
        } catch (e: Exception) {
            Log.e("EditProfileViewModel", "loadUserLinkListData(): ${e.message}")
        }
    }

    fun addLinkToList() {
        val newLink = editProfileNewLink.value
        if (!newLink.isNullOrEmpty()) {
            // 현재 링크 리스트를 가져와서 새로운 링크를 추가한 리스트를 만든다
            val updatedList = _editProfileLinkList.value.toMutableList()
            updatedList.add(newLink)
            // 새로운 리스트로 업데이트한다
            _editProfileLinkList.value = updatedList
        }
    }

    fun removeLinkFromList(link: String) {
        // 현재 링크 리스트에서 해당 링크를 제외한 리스트를 만든다
        val updatedList = _editProfileLinkList.value.filter { it != link }
        // 새로운 리스트로 업데이트한다
        _editProfileLinkList.value = updatedList
    }

    fun updateUserData(picChanged: Boolean, context: Context) = viewModelScope.launch {
        if (picChanged) {
            profileRepository.uploadProfilePic(_editProfilePicUri.value!!, context).collect { profileUrl ->
                _editProfilePicUrl.value = profileUrl
            }
        }

        // 데이터를 객체에 담는다
        val user = UserData(
            userIdx = prefs.getInt("currentUserIdx"),
            userProfilePic = _editProfilePicUrl.value!!,
            userIntro = _editProfileIntro.value!!,
            userInterests = _editProfileInterests.value!!
        )

        // 데이터베이스 업데이트
        profileRepository.updateUserData(user)
    }

    fun updateUserLinkData() = viewModelScope.launch {
        val userIdx = prefs.getInt("currentUserIdx")

        // 데이터베이스 업데이트
        profileRepository.updateUserLinkData(userIdx, _editProfileLinkList.value)
    }

    // 링크 목록 순서 변경
    fun reorderLinks(from: Int, to: Int) {
        val updatedList = editProfileLinkList.value.toMutableList()
        val movedLink = updatedList.removeAt(from)
        updatedList.add(to, movedLink)
        editProfileLinkList.value = updatedList
    }
}
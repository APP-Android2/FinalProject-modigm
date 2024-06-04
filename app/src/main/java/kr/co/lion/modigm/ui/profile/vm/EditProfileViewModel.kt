package kr.co.lion.modigm.ui.profile.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.repository.UserInfoRepository

class EditProfileViewModel: ViewModel() {
    private val userRepository = UserInfoRepository()
    private val studyRepository = StudyRepository()

    // 사용자 이름
    private val _editProfileName = MutableLiveData<String>()
    val editProfileName: MutableLiveData<String> = _editProfileName

    // 자기소개
    private val _editProfileIntro = MutableLiveData<String>()
    val editProfileIntro: MutableLiveData<String> = _editProfileIntro

    // 이메일
    private val _editProfileEmail = MutableLiveData<String>()
    val editProfileEmail: MutableLiveData<String> = _editProfileEmail

    // 전화번호
    private val _editProfilePhone = MutableLiveData<String>()
    val editProfilePhone: MutableLiveData<String> = _editProfilePhone

    // 링크 (입력)
    private val _editProfileNewLink = MutableLiveData<String>()
    val editProfileNewLink: MutableLiveData<String> = _editProfileNewLink

    // 관심분야 리스트
    private val _editProfileInterestList = MutableLiveData<List<Int>>()
    val editProfileInterestList: MutableLiveData<List<Int>> = _editProfileInterestList

    // 링크 리스트
    private val _editProfileLinkList = MutableLiveData<List<String>>()
    val editProfileLinkList: MutableLiveData<List<String>> = _editProfileLinkList


}
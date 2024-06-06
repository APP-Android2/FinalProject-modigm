package kr.co.lion.modigm.ui.profile.vm

import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.repository.UserInfoRepository
import kr.co.lion.modigm.util.Interest
import kr.co.lion.modigm.util.JoinType

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

    // 로그인 방법
    private val _editProfileProvider = MutableLiveData<JoinType>()
    val editProfileProvider: MutableLiveData<JoinType> = _editProfileProvider

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


    /** functions **/

    // 유저 기본 정보를 불러온다.
    fun loadUserData(user: FirebaseUser, context: Context, imageView: ImageView) = viewModelScope.launch {
        try {
            val response = userRepository.loadUserData(user.uid)

            // 사용자 이름
            _editProfileName.value = response?.userName
            // 자기소개
            _editProfileIntro.value = response?.userIntro
            // 이메일
            _editProfileEmail.value = user.email
            // 로그인 방법
            _editProfileProvider.value = JoinType.getType(response?.userProvider!!)
            // 전화번호
            _editProfilePhone.value = response?.userPhone
            // 입력창의 링크
            // 흠..
            // 관심분야 리스트
            _editProfileInterestList.value = response?.userInterestList
            // 링크 리스트
            _editProfileLinkList.value = response?.userLinkList
            // 프로필 사진
            userRepository.loadUserProfilePic(context, response?.userProfilePic!!, imageView)
        } catch (e: Exception) {
            Log.e("profilevm", "loadUserData(): ${e.message}")
        }
    }

}
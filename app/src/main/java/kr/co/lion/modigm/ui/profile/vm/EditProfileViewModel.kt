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


    /** functions **/

    // 유저 기본 정보를 불러온다.
    fun loadUserData(user: FirebaseUser, context: Context, imageView: ImageView, chipGroup: ChipGroup) = viewModelScope.launch {
        try {
            val response = userRepository.loadUserData(user.uid)

            // 사용자 이름
            _editProfileName.value = response?.userName
            // 자기소개
            _editProfileIntro.value = response?.userIntro
            // 이메일
            _editProfileEmail.value = user.email
            // 전화번호
            _editProfilePhone.value = user.phoneNumber
            // 입력창의 링크
            // 흠..
            // 관심분야 리스트
            _editProfileInterestList.value = response?.userInterestList
            // 링크 리스트
            _editProfileLinkList.value = response?.userLinkList
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

}
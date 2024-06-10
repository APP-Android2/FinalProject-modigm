package kr.co.lion.modigm.ui.profile.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ChangePwViewModel: ViewModel() {
    // 파이어베이스 인증
    private val _auth = FirebaseAuth.getInstance()
    // 현재 유저
    private val _user = _auth.currentUser

    // 현재 비밀번호
    val oldPw = MutableLiveData<String>()
    // 현재 비밀번호 에러 메시지
    val oldPwError = MutableLiveData<String>()

    // 새로운 비밀번호
    val newPw = MutableLiveData<String>()
    // 현재 비밀번호 에러 메시지
    val newPwError = MutableLiveData<String>()

    // 새로운 비밀번호 확인
    val newPwCheck = MutableLiveData<String>()
    // 현재 비밀번호 에러 메시지
    val newPwCheckError = MutableLiveData<String>()

}
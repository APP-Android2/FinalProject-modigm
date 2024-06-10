package kr.co.lion.modigm.ui.profile.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ChangePhoneViewModel: ViewModel() {
    private val _auth = FirebaseAuth.getInstance()
    private val _user = _auth.currentUser

    // 변경할 전화번호
    val userPhone = MutableLiveData<String>()

    // 입력할 인증 코드
    val phoneAuth = MutableLiveData<String>()

}
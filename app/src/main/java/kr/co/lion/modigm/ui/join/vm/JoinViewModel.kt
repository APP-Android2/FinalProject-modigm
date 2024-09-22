package kr.co.lion.modigm.ui.join.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.repository.JoinUserRepository
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class JoinViewModel @Inject constructor(
    private val _joinUserRepository: JoinUserRepository,
    private val _auth: FirebaseAuth
) : ViewModel() {

    // 파이어베이스에 연동된 유저
    private var _user: MutableStateFlow<FirebaseUser?> = MutableStateFlow(null)
    val user: StateFlow<FirebaseUser?> = _user

    // provider(제공자 이름, email, kakao, github)
    private var _userProvider: MutableStateFlow<String> = MutableStateFlow("")
    val userProvider: StateFlow<String> = _userProvider
    fun setUserProvider(provider:String){
        _userProvider.value = provider
    }

    // email
    private var _userEmail: MutableStateFlow<String?> = MutableStateFlow(null)
    val userEmail: StateFlow<String?> = _userEmail
    fun setUserEmail(){
        if(_auth.currentUser != null){
            _userEmail.value = _auth.currentUser?.email ?: ""
        }
    }

    // auth에 등록된 이메일, 뒤로가기로 이메일을 수정한 경우 비교용
    private var _verifiedEmail: MutableStateFlow<String> = MutableStateFlow("")
    val verifiedEmail: StateFlow<String> = _verifiedEmail

    // 전화번호 인증
    private var _phoneVerification: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val phoneVerification: StateFlow<Boolean> = _phoneVerification
    fun setPhoneVerified(verified:Boolean){
        _phoneVerification.value = verified
    }

    // 인증된 전화번호
    private var _verifiedPhoneNumber: MutableStateFlow<String> = MutableStateFlow("")
    val verifiedPhoneNumber: StateFlow<String> = _verifiedPhoneNumber
    fun setVerifiedPhoneNumber(phone:String){
        _verifiedPhoneNumber.value = phone
    }

    // 이미 전화번호가 등록되었는지 여부
    private var _isPhoneAlreadyRegistered: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var isPhoneAlreadyRegistered: StateFlow<Boolean> = _isPhoneAlreadyRegistered
    fun setIsPhoneAlreadyRegistered(isRegistered:Boolean){
        _isPhoneAlreadyRegistered.value = isRegistered
    }

    // 이미 등록된 전화번호 계정의 이메일
    private var _alreadyRegisteredUserEmail: MutableStateFlow<String> = MutableStateFlow("")
    val alreadyRegisteredUserEmail: StateFlow<String> = _alreadyRegisteredUserEmail
    // 이미 등록된 전화번호 계정의 프로바이더
    private var _alreadyRegisteredUserProvider: MutableStateFlow<String> = MutableStateFlow("")
    val alreadyRegisteredUserProvider: StateFlow<String> = _alreadyRegisteredUserProvider

    fun setAlreadyRegisteredUser(email:String, provider:String){
        _alreadyRegisteredUserEmail.value = email
        _alreadyRegisteredUserProvider.value = provider
    }

    // 회원가입 완료 여부
    private var _joinCompleted: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val joinCompleted: StateFlow<Boolean> = _joinCompleted

    // 회원 객체 생성을 위한 정보
    private val _uid: MutableStateFlow<String?> = MutableStateFlow(null)
    fun setUserUid() {
        if (_auth.currentUser != null) {
            _uid.value = _auth.currentUser?.uid
        }
    }

    private val _email: MutableStateFlow<String> = MutableStateFlow("")
    private val _password: MutableStateFlow<String> = MutableStateFlow("")
    fun setEmailAndPw(email:String, pw:String){
        _email.value = email
        _password.value = pw
    }

    private val _userName: MutableStateFlow<String> = MutableStateFlow("")
    private val _phoneNumber: MutableStateFlow<String> = MutableStateFlow("")
    fun setUserNameAndPhoneNumber(name:String, phone:String){
        _userName.value = name
        _phoneNumber.value = phone
    }

    private val _interests: MutableStateFlow<MutableList<String>?> = MutableStateFlow(null)
    fun setInterests(interests:MutableList<String>){
        _interests.value = interests
    }

    // FirebaseAuth에 이메일 계정 등록
    suspend fun createEmailUser(): String {
        // 오류 메시지
        var error = ""
        try {
            val authResult = _auth.createUserWithEmailAndPassword(_email.value, _password.value).await()
            _user.value = authResult.user
            _uid.value = authResult.user?.uid?:""
            _verifiedEmail.value = _email.value
        }catch (e:FirebaseAuthException){
            if(e.errorCode=="ERROR_EMAIL_ALREADY_IN_USE"){
                error = "이미 등록되어 있는 이메일 계정입니다."
            }
        }
        return error
    }

    // 회원가입 이탈 시 이미 Auth에 등록되어있는 인증 정보 삭제
    fun deleteCurrentUser(){
        // 인증 정보 삭제
        _auth.currentUser?.delete()
    }

    // UserInfoData 객체 생성
    private fun createUserInfoData(): UserData {
        // 각 화면에서 응답받은 정보 가져와서 객체 생성 후 return
        return UserData(
            -1,
            _uid.value?:"",
            _userName.value,
            _phoneNumber.value,
            "",
            "",
            _userEmail.value?:_email.value,
            _userProvider.value,
            _interests.value?.joinToString(",")?:"",
            Date(System.currentTimeMillis())
        )
    }

    // 회원 가입 완료
    fun completeJoinUser(){
        viewModelScope.launch {
            _user.value = _auth.currentUser

            val user = createUserInfoData()
            val result = _joinUserRepository.insetUserData(user)
            result.onSuccess { userIdx ->
                // SharedPreferences에 유저 idx 저장
                 prefs.setInt("currentUserIdx", userIdx)

                _joinCompleted.value = true
            }.onFailure {
                _joinCompleted.value = false
                throw Exception("회원가입에 실패했습니다. 잠시 후 다시 시도해주세요.")
            }
        }
    }

    // 상태값 초기화
    fun reset(){
        _user.value = null

        _userProvider.value = ""

        _userEmail.value = null

        _verifiedEmail.value = ""

        _phoneVerification.value = false

        _verifiedPhoneNumber.value = ""

        _isPhoneAlreadyRegistered.value = false

        _alreadyRegisteredUserEmail.value = ""

        _alreadyRegisteredUserProvider.value = ""

        _joinCompleted.value = false

        _uid.value = null

        _email.value = ""
        _password.value = ""

        _userName.value = ""
        _phoneNumber.value = ""

        _interests.value = null
    }

    fun signOut(){
        if(_auth.currentUser != null){
            _auth.signOut()
        }
    }

}
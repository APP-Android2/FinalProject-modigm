package kr.co.lion.modigm.ui.join.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.repository.UserInfoRepository
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class JoinViewModel : ViewModel() {

    // 리포지터리
    private val _userInfoRepository = UserInfoRepository()
    // 파이어베이스 인증
    private val _auth = FirebaseAuth.getInstance()

    // 파이어베이스에 연동된 유저
    private var _user: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val user: LiveData<FirebaseUser?> = _user

    // provider(제공자 이름, email, kakao, github)
    private var _userProvider: MutableLiveData<String> = MutableLiveData()
    val userProvider: LiveData<String> = _userProvider
    fun setUserProvider(provider:String){
        _userProvider.value = provider
    }

    // email
    private var _userEmail: MutableLiveData<String> = MutableLiveData()
    val userEmail: LiveData<String> = _userEmail
    fun setUserEmail(){
        if(_auth.currentUser != null){
            _userEmail.value = _auth.currentUser?.email
        }
    }

    // auth에 등록된 이메일, 뒤로가기로 이메일을 수정한 경우 비교용
    private var _verifiedEmail: MutableLiveData<String> = MutableLiveData()
    val verifiedEmail: LiveData<String> = _verifiedEmail

    // 전화번호 인증
    private var _phoneVerification: MutableLiveData<Boolean> = MutableLiveData()
    val phoneVerification: LiveData<Boolean> = _phoneVerification
    fun setPhoneVerified(verified:Boolean){
        _phoneVerification.value = verified
    }

    // 인증된 전화번호
    private var _verifiedPhoneNumber: MutableLiveData<String> = MutableLiveData()
    val verifiedPhoneNumber: LiveData<String> = _verifiedPhoneNumber
    fun setVerifiedPhoneNumber(phone:String){
        _verifiedPhoneNumber.value = phone
    }

    // 이미 전화번호가 등록되었는지 여부
    var isPhoneAlreadyRegistered = MutableLiveData(false)

    // 이미 등록된 전화번호 계정의 이메일
    private var _alreadyRegisteredUserEmail: MutableLiveData<String> = MutableLiveData()
    val alreadyRegisteredUserEmail: LiveData<String> = _alreadyRegisteredUserEmail
    // 이미 등록된 전화번호 계정의 프로바이더
    private var _alreadyRegisteredUserProvider: MutableLiveData<String> = MutableLiveData()
    val alreadyRegisteredUserProvider: LiveData<String> = _alreadyRegisteredUserProvider

    fun setAlreadyRegisteredUser(email:String, provider:String){
        _alreadyRegisteredUserEmail.value = email
        _alreadyRegisteredUserProvider.value = provider
    }

    private val _phoneCredential: MutableLiveData<AuthCredential> = MutableLiveData()

    fun setPhoneCredential(credential: AuthCredential){
        _phoneCredential.value = credential
    }

    // 회원가입 완료 여부
    private var _joinCompleted = MutableLiveData(false)
    val joinCompleted: LiveData<Boolean> = _joinCompleted

    // 회원 객체 생성을 위한 정보
    private val _uid = MutableLiveData<String>()
    fun setUserUid() {
        if (_auth.currentUser != null) {
            _uid.value = _auth.currentUser?.uid
        }
    }

    private val _email = MutableLiveData<String>()
    private val _password = MutableLiveData<String>()
    fun setEmailAndPw(email:String, pw:String){
        _email.value = email
        _password.value = pw
    }

    private val _userName = MutableLiveData<String>()
    private val _phoneNumber = MutableLiveData<String>()
    fun setUserNameAndPhoneNumber(name:String, phone:String){
        _userName.value = name
        _phoneNumber.value = phone
    }

    private val _interests = MutableLiveData<MutableList<Int>>()
    fun setInterests(interests:MutableList<Int>){
        _interests.value = interests
    }

    // FirebaseAuth에 이메일 계정 등록
    suspend fun createEmailUser(): String {
        // 오류 메시지
        var error = ""
        try {
            val authResult = _auth.createUserWithEmailAndPassword(_email.value?:"", _password.value?:"").await()
            _user.value = authResult.user
            _uid.value = authResult.user?.uid?:""
            _verifiedEmail.value = _email.value?:""
            //userCredential.user?.delete()
            //_auth.signOut()
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
        if(_auth.currentUser != null){
            _auth.currentUser?.delete()
        }
    }

    // UserInfoData 객체 생성
    private fun createUserInfoData(): UserData {
        val user = UserData()
        user.userName = _userName.value?:""
        user.userPhone = _phoneNumber.value?:""
        user.userInterestList = _interests.value?: mutableListOf()
        user.userUid = _uid.value?:""

        user.userProvider = _userProvider.value?:""
        user.userEmail = _userEmail.value?:_email.value?:""
        // 각 화면에서 응답받은 정보 가져와서 객체 생성 후 return
        return user
    }

    // 이메일 계정 회원 가입 완료
    suspend fun completeJoinEmailUser(){
        viewModelScope.launch {
            _user.value = _auth.currentUser

            // UserInfoData 객체 생성
            val user = createUserInfoData()
            // 파이어스토어에 데이터 저장
            _userInfoRepository.insetUserData(user)

            // SharedPreferences에 유저 정보 저장
            prefs.setUserData("currentUserData", user)
            // SharedPreferences에 uid값 저장
            prefs.setString("uid", user.userUid)

            _joinCompleted.value = true
        }
    }

    // SNS 계정 회원 가입 완료
    fun completeJoinSnsUser(){
        viewModelScope.launch {
            _user.value = _auth.currentUser

            // UserInfoData 객체 생성
            val user = createUserInfoData()
            // 파이어스토어에 데이터 저장
            _userInfoRepository.insetUserData(user)

            // SharedPreferences에 유저 정보 저장
            prefs.setUserData("currentUserData", user)
            // SharedPreferences에 uid값 저장
            prefs.setString("uid", user.userUid)

            _joinCompleted.value = true
        }
    }
}
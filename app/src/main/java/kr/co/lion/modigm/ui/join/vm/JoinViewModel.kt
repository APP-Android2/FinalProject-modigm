package kr.co.lion.modigm.ui.join.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.repository.JoinRepository
import kr.co.lion.modigm.repository.LoginRepository
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class JoinViewModel @Inject constructor(
    private val _joinRepository: JoinRepository,
    private val _loginRepository: LoginRepository,
    private val _firebaseAuth: FirebaseAuth
) : ViewModel() {

    // 파이어베이스에 연동된 유저
    private var _firebaseUser: MutableStateFlow<FirebaseUser?> = MutableStateFlow(null)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser

    // provider(제공자 이름, email, kakao, github)
    private var _userProvider: MutableStateFlow<String> = MutableStateFlow("")
    fun setUserProvider(provider:String){
        _userProvider.value = provider
    }

    // sns계정의 email
    private var _snsUserEmail: MutableStateFlow<String?> = MutableStateFlow(null)
    fun setSnsUserEmail(){
        if(_firebaseAuth.currentUser != null){
            _snsUserEmail.value = _firebaseAuth.currentUser?.email ?: ""
        }
    }

    // auth에 등록된 이메일, 뒤로가기로 이메일을 수정한 경우 비교용
    private var _verifiedEmail: MutableStateFlow<String> = MutableStateFlow("")
    val verifiedEmail: StateFlow<String> = _verifiedEmail

    // 전화번호 인증 여부
    private var _isPhoneNumberVerified: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isPhoneNumberVerified: StateFlow<Boolean> = _isPhoneNumberVerified
    fun setPhoneNumberVerificationState(verified:Boolean){
        _isPhoneNumberVerified.value = verified
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
    private var _isJoinCompleted: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isJoinCompleted: StateFlow<Boolean> = _isJoinCompleted

    // 회원 객체 생성을 위한 정보
    private val _userUid: MutableStateFlow<String?> = MutableStateFlow(null)
    fun setUserUid() {
        if (_firebaseAuth.currentUser != null) {
            _userUid.value = _firebaseAuth.currentUser?.uid
        }
    }

    private val _userEmail: MutableStateFlow<String> = MutableStateFlow("")
    private val _userPassword: MutableStateFlow<String> = MutableStateFlow("")
    fun setEmailAndPw(email:String, pw:String){
        _userEmail.value = email
        _userPassword.value = pw
    }

    private val _userName: MutableStateFlow<String> = MutableStateFlow("")
    private val _userPhone: MutableStateFlow<String> = MutableStateFlow("")
    fun setUserNameAndUserPhone(name:String, phone:String){
        _userName.value = name
        _userPhone.value = phone
    }

    private val _userInterests: MutableStateFlow<MutableList<String>?> = MutableStateFlow(null)
    fun setUserInterests(selectedInterests:MutableList<String>){
        _userInterests.value = selectedInterests
    }

    // FirebaseAuth에 이메일 계정 등록
    suspend fun registerEmailUserToFirebaseAuth(): String {
        // 오류 메시지
        var error = ""
        try {
            val authResult = _firebaseAuth.createUserWithEmailAndPassword(_userEmail.value, _userPassword.value).await()
            _firebaseUser.value = authResult.user
            _userUid.value = authResult.user?.uid?:""
            _verifiedEmail.value = _userEmail.value
        }catch (e:FirebaseAuthException){
            if(e.errorCode=="ERROR_EMAIL_ALREADY_IN_USE"){
                error = "이미 등록되어 있는 이메일 계정입니다."
            }
        }
        return error
    }

    // 회원가입 이탈 시 이미 Auth에 등록되어있는 인증 정보 삭제
    fun deleteCurrentRegisteredFirebaseUser(){
        // 인증 정보 삭제
        CoroutineScope(Dispatchers.IO).launch {
            _firebaseAuth.currentUser?.delete()?.addOnSuccessListener {
                Log.d("JoinViewModel", "deleteCurrentUser: 인증 정보 삭제 성공")
            }
        }
    }

    // UserInfoData 객체 생성
    private fun createUserInfoData(): UserData {
        // 각 화면에서 응답받은 정보 가져와서 객체 생성 후 return
        return UserData(
            -1,
            _userUid.value?:"",
            _userName.value,
            _userPhone.value,
            "",
            "",
            _snsUserEmail.value?:_userEmail.value,
            _userProvider.value,
            _userInterests.value?.joinToString(",")?:"",
            LocalDateTime.now()
        )
    }

    // 회원 가입 완료
    fun completeJoinProcess(handler: CoroutineExceptionHandler){
        viewModelScope.launch(handler) {
            _firebaseUser.value = _firebaseAuth.currentUser

            val user = createUserInfoData()
            val result = _joinRepository.insetUserData(user)
            result.onSuccess { userIdx ->
                // SharedPreferences에 유저 idx 저장
                 prefs.setInt("currentUserIdx", userIdx)
                // FCM토큰 등록
                if(_userProvider.value== JoinType.EMAIL.provider){
                    registerFcmTokenToServer(userIdx)
                }
                // 회원가입 완료 처리
                _isJoinCompleted.value = true
            }.onFailure {
                _isJoinCompleted.value = false
                throw Exception("회원가입에 실패했습니다. 잠시 후 다시 시도해주세요.")
            }
        }
    }

    // 상태값 초기화
    fun resetViewModelStates(){
        _firebaseUser.value = null

        _userProvider.value = ""

        _snsUserEmail.value = null

        _verifiedEmail.value = ""

        _isPhoneNumberVerified.value = false

        _verifiedPhoneNumber.value = ""

        _isPhoneAlreadyRegistered.value = false

        _alreadyRegisteredUserEmail.value = ""

        _alreadyRegisteredUserProvider.value = ""

        _isJoinCompleted.value = false

        _userUid.value = null

        _userEmail.value = ""
        _userPassword.value = ""

        _userName.value = ""
        _userPhone.value = ""

        _userInterests.value = null
    }

    fun signOutCurrentFirebaseUser(){
        if(_firebaseAuth.currentUser != null){
            _firebaseAuth.signOut()
        }
    }

    /**
     * FCM 토큰을 가져와 서버에 등록하는 함수
     */
    private fun registerFcmTokenToServer(userIdx: Int) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("LoginViewModel", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("LoginViewModel", "FCM Token: $token")

            if (token != null) {
                // 코루틴 내에서 서버에 FCM 토큰을 등록
                viewModelScope.launch {
                    try {
                        val result = _loginRepository.registerFcmToken(userIdx, token)
                        if (result) {
                            Log.d("LoginViewModel", "FCM 토큰 등록 성공 userIdx: $userIdx, token: $token")
                        } else {
                            Log.e("LoginViewModel", "FCM 토큰 등록 실패 userIdx: $userIdx")
                        }
                    } catch (e: Exception) {
                        Log.e("LoginViewModel", "Error registering FCM token", e)
                    }
                }
            } else {
                Log.e("LoginViewModel", "FCM Token is null")
            }
        }
    }

}
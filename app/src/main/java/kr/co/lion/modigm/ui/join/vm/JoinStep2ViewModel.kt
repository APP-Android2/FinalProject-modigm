package kr.co.lion.modigm.ui.join.vm

import android.app.Activity
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.repository.JoinUserRepository
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class JoinStep2ViewModel @Inject constructor(
    private val _db: JoinUserRepository,
    private val _auth: FirebaseAuth
): ViewModel() {
    // ================0. SMS 인증 코드 관련 프로그래스 바 이벤트======================================================
    val showCallback = MutableStateFlow<(() -> Unit)?>(null)
    fun showLoading(){
        showCallback.value?.invoke()
    }

    val hideCallback = MutableStateFlow<(() -> Unit)?>(null)
    fun hideLoading(){
        hideCallback.value?.invoke()
    }

    // ================1. 유효성 검사 관련==============================================================

    // 이름
    val userName = MutableStateFlow("")
    // 이름 유효성 검사
    val nameValidation = MutableStateFlow("")

    // 전화번호
    val userPhone = MutableStateFlow("")
    // 전화번호 유효성 검사
    val phoneValidation = MutableStateFlow("")

    // 인증번호입력
    val inputSmsCode = MutableStateFlow("")
    // 인증번호입력 유효성 검사
    val inputSmsCodeValidation = MutableStateFlow("")

    // 유효성 검사
    fun validate(): Boolean {
        // 에러 표시 초기화
        nameValidation.value =""
        phoneValidation.value =""
        inputSmsCodeValidation.value =""
        var result = true

        if(userName.value.isEmpty()){
            nameValidation.value = "이름을 입력해주세요."
            result = false
        }
        if(userPhone.value.isEmpty()){
            phoneValidation.value = "전화번호를 입력해주세요."
            result = false
        }else{
            if(!Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", userPhone.value)){
                phoneValidation.value = "올바른 전화번호가 아닙니다."
                result = false
            }else if(!_isCodeSent.value){
                phoneValidation.value = "인증하기 버튼을 눌러서 인증을 진행해주세요."
                result = false
            }
        }
        if(inputSmsCode.value.isEmpty()){
            inputSmsCodeValidation.value = "인증번호를 입력해주세요."
            result = false
        }
        return result
    }

    // 인증하기 버튼 눌렀을 때 유효성 검사
    fun checkPhoneValidation(): Boolean {
        // 인증번호 입력칸 초기화
        inputSmsCode.value = ""
        // 에러 표시 초기화
        phoneValidation.value =""
        var result = true

        if(userPhone.value.isEmpty()){
            phoneValidation.value = "전화번호를 입력해주세요."
            result = false
        }else{
            if(!Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", userPhone.value)){
                phoneValidation.value = "올바른 전화번호가 아닙니다."
                result = false
            }
        }
        return result
    }

    // ================2. 전화번호 인증 관련==============================================================

    private val _authButtonText = MutableStateFlow("인증하기")
    val authButtonText: StateFlow<String> = _authButtonText

    private val _authExpired = MutableStateFlow(true)
    val authExpired: StateFlow<Boolean> = _authExpired

    // 인증문자 발송 여부
    private val _isCodeSent = MutableStateFlow(false)
    val isCodeSent: StateFlow<Boolean> = _isCodeSent

    // 인증 ID(인증 코드 내용 아님)
    private val _verificationId = MutableStateFlow("")
    val verificationId: StateFlow<String> = _verificationId

    // 올바른 전화번호 확인 여부
    // onVerificationCompleted, onVerificationFailed에서 확인
    private val _isVerifiedPhone = MutableStateFlow(false)
    val isVerifiedPhone: StateFlow<Boolean> = _isVerifiedPhone

    // 인증 에러 메시지
    private val _errorMessage = MutableStateFlow("")

    // 나중에 이메일 계정과 합칠 때 필요한 전화번호 인증 credential
    private var _credential = MutableStateFlow<AuthCredential?>(null)

    // 이미 등록된 전화번호 계정이 있는지 여부
    private val _alreadyRegisteredUser = MutableStateFlow(false)

    // 이미 등록된 전화번호 계정의 이메일
    private val _alreadyRegisteredUserEmail = MutableStateFlow("")
    val alreadyRegisteredUserEmail: StateFlow<String> = _alreadyRegisteredUserEmail

    // 이미 등록된 전화번호 계정의 프로바이더
    private val _alreadyRegisteredUserProvider = MutableStateFlow("")
    val alreadyRegisteredUserProvider: StateFlow<String> = _alreadyRegisteredUserProvider

    // 문자 수신 60초 타이머
    val setTimer: CountDownTimer by lazy {
        _authExpired.value = false
        object : CountDownTimer(60000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                _authButtonText.value = "${millisUntilFinished/1000}"
            }

            override fun onFinish() {
                _authExpired.value = true
                _authButtonText.value = "인증하기"
            }
        }
    }

    // 전화번호 인증
    suspend fun createPhoneUser(): String {
        // DB에 해당 번호로 등록된 계정이 있는지 확인한다.
        val checkResult = _db.checkUserByPhone(userPhone.value)

        checkResult.onSuccess { resultMap ->
            if(resultMap != null){
                _errorMessage.value = "이미 해당 번호로 가입한 계정이 있습니다."
                _alreadyRegisteredUserProvider.value = resultMap["userProvider"] ?: ""
                _alreadyRegisteredUserEmail.value = resultMap["userEmail"] ?: ""
                return _errorMessage.value
            }
        }
        // 오류 메시지
        if(_isCodeSent.value){
            try{
                _errorMessage.value = ""
                val phoneCredential = PhoneAuthProvider.getCredential(_verificationId.value, inputSmsCode.value)
                val linkedProviders = _auth.currentUser?.providerData?.map { it.providerId }
                if (linkedProviders != null) {
                    for(provider in linkedProviders){
                        if(provider == "phone"){
                            _auth.currentUser?.unlink("phone")
                            break
                        }
                    }
                }
                _auth.currentUser?.linkWithCredential(phoneCredential)?.await()
            }catch (e: FirebaseAuthException){
                _errorMessage.value = e.message.toString()
                inputSmsCodeValidation.value = _errorMessage.value
            }
        }
        return _errorMessage.value
    }

    // 전화 인증 발송
    fun sendCode(activity: Activity){
        // 전화 인증 여부를 초기화
        _isVerifiedPhone.value = false
        _authExpired.value = false

        // 전화번호 앞에 "+82 " 국가코드 붙여주기
        val setNumber = userPhone.value.replaceRange(0,1,"+82 ")

        _auth.setLanguageCode("kr")

        val options = PhoneAuthOptions.newBuilder(_auth)
            .setPhoneNumber(setNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // 전화 인증코드 발송 콜백
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onCodeAutoRetrievalTimeOut(p0: String) {
            // 제한 시간이 경과된 경우
            super.onCodeAutoRetrievalTimeOut(p0)
            _isVerifiedPhone.value = false
        }

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // 입력한 전화번호가 정상적으로 확인될 경우(인증이 완료된게 아님, 실제 번호일때만 호출됨)
            _isVerifiedPhone.value = true
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // 입력한 전화번호 또는 인증번호가 잘못되었을 경우
            _isVerifiedPhone.value = false
            phoneValidation.value = e.message ?: "인증에 실패했습니다."
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // verificationId는 문자로 받는 코드가 아니었다
            _verificationId.value = verificationId
            _isCodeSent.value = true
            inputSmsCode.value = ""
            setTimer.start()
        }
    }

    // ================3. 초기화 ==============================================================
    fun reset(){
        userName.value = ""
        nameValidation.value = ""
        userPhone.value = ""
        phoneValidation.value = ""
        inputSmsCode.value = ""
        inputSmsCodeValidation.value = ""

        _isCodeSent.value = false
        _verificationId.value = ""
        _isVerifiedPhone.value = false
        _errorMessage.value = ""
        _credential.value = null
        _alreadyRegisteredUser.value = false
        _alreadyRegisteredUserEmail.value = ""
        _alreadyRegisteredUserProvider.value = ""
        _authButtonText.value = "인증하기"
        _authExpired.value = true
    }

    fun cancelTimer(){
        setTimer.cancel()
        _authButtonText.value = "인증하기"
        _authExpired.value = true
    }
}
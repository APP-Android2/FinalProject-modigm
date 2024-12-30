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
import kr.co.lion.modigm.repository.JoinRepository
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class JoinStep2ViewModel @Inject constructor(
    private val _db: JoinRepository,
    private val _auth: FirebaseAuth
): ViewModel() {
    // ================0. SMS 인증 코드 관련 프로그래스 바 이벤트======================================================
    val showLoadingCallback = MutableStateFlow<(() -> Unit)?>(null)
    fun showLoading(){
        showLoadingCallback.value?.invoke()
    }

    val hideLoadingCallback = MutableStateFlow<(() -> Unit)?>(null)
    fun hideLoading(){
        hideLoadingCallback.value?.invoke()
    }

    // ================1. 유효성 검사 관련==============================================================

    // 이름
    val userInputName = MutableStateFlow("")
    // 이름 유효성 검사
    val userInputNameValidation = MutableStateFlow("")

    // 전화번호
    val userInputPhone = MutableStateFlow("")
    // 전화번호 유효성 검사
    val userInputPhoneValidation = MutableStateFlow("")

    // 인증번호입력
    val userInputSmsCode = MutableStateFlow("")
    // 인증번호입력 유효성 검사
    val userInputSmsCodeValidation = MutableStateFlow("")

    // 유효성 검사
    fun validateStep2UserInput(): Boolean {
        // 에러 표시 초기화
        userInputNameValidation.value =""
        userInputPhoneValidation.value =""
        userInputSmsCodeValidation.value =""
        var result = true

        if(userInputName.value.isEmpty()){
            userInputNameValidation.value = "이름을 입력해주세요."
            result = false
        }
        if(userInputPhone.value.isEmpty()){
            userInputPhoneValidation.value = "전화번호를 입력해주세요."
            result = false
        }else{
            if(!Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", userInputPhone.value)){
                userInputPhoneValidation.value = "올바른 전화번호가 아닙니다."
                result = false
            }else if(!_isPhoneAuthCodeSent.value){
                userInputPhoneValidation.value = "인증하기 버튼을 눌러서 인증을 진행해주세요."
                result = false
            }
        }
        if(userInputSmsCode.value.isEmpty()){
            userInputSmsCodeValidation.value = "인증번호를 입력해주세요."
            result = false
        }
        return result
    }

    // 인증하기 버튼 눌렀을 때 유효성 검사
    fun checkUserInputPhoneValidation(): Boolean {
        // 인증번호 입력칸 초기화
        userInputSmsCode.value = ""
        // 에러 표시 초기화
        userInputPhoneValidation.value =""
        var result = true

        if(userInputPhone.value.isEmpty()){
            userInputPhoneValidation.value = "전화번호를 입력해주세요."
            result = false
        }else{
            if(!Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", userInputPhone.value)){
                userInputPhoneValidation.value = "올바른 전화번호가 아닙니다."
                result = false
            }
        }
        return result
    }

    // ================2. 전화번호 인증 관련==============================================================

    private val _phoneAuthButtonText = MutableStateFlow("인증하기")
    val phoneAuthButtonText: StateFlow<String> = _phoneAuthButtonText

    private val _isPhoneAuthExpired = MutableStateFlow(true)
    val isPhoneAuthExpired: StateFlow<Boolean> = _isPhoneAuthExpired

    // 인증문자 발송 여부
    private val _isPhoneAuthCodeSent = MutableStateFlow(false)
    val isPhoneAuthCodeSent: StateFlow<Boolean> = _isPhoneAuthCodeSent
    fun resetPhoneAuthCodeSentState(){
        _isPhoneAuthCodeSent.value = false
    }

    // 인증 ID(인증 코드 내용 아님)
    private val _phoneAuthVerificationId = MutableStateFlow("")

    // 올바른 전화번호 확인 여부
    // onVerificationCompleted, onVerificationFailed에서 확인
    private val _isVerifiedPhone = MutableStateFlow(false)
    val isVerifiedPhone: StateFlow<Boolean> = _isVerifiedPhone

    // 인증 에러 메시지
    private val _phoneAuthErrorMessage = MutableStateFlow("")

    // 나중에 이메일 계정과 합칠 때 필요한 전화번호 인증 credential
    private var _phoneAuthCredential = MutableStateFlow<AuthCredential?>(null)

    // 이미 등록된 전화번호 계정이 있는지 여부
    private val _isAlreadyRegisteredPhoneUser = MutableStateFlow(false)

    // 이미 등록된 전화번호 계정의 이메일
    private val _alreadyRegisteredUserEmail = MutableStateFlow("")
    val alreadyRegisteredUserEmail: StateFlow<String> = _alreadyRegisteredUserEmail

    // 이미 등록된 전화번호 계정의 프로바이더
    private val _alreadyRegisteredUserProvider = MutableStateFlow("")
    val alreadyRegisteredUserProvider: StateFlow<String> = _alreadyRegisteredUserProvider

    // 문자 수신 60초 타이머
    val phoneAuthTimer: CountDownTimer by lazy {
        _isPhoneAuthExpired.value = false
        object : CountDownTimer(60000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                _phoneAuthButtonText.value = "${millisUntilFinished/1000}"
            }

            override fun onFinish() {
                _isPhoneAuthExpired.value = true
                _phoneAuthButtonText.value = "인증하기"
            }
        }
    }

    // 전화번호 인증계정을 앞선 이메일(SNS)계정과 연결
    suspend fun linkWithPhoneAuthCredential(): String {
        // DB에 해당 번호로 등록된 계정이 있는지 확인한다.
        _db.checkUserByPhone(userInputPhone.value).onSuccess { resultMap ->
            if(resultMap != null){
                _phoneAuthErrorMessage.value = "이미 해당 번호로 가입한 계정이 있습니다."
                _alreadyRegisteredUserProvider.value = resultMap["userProvider"] ?: ""
                _alreadyRegisteredUserEmail.value = resultMap["userEmail"] ?: ""
                return _phoneAuthErrorMessage.value
            }
        }
        // 오류 메시지
        if(_isPhoneAuthCodeSent.value){
            try{
                _phoneAuthErrorMessage.value = ""
                val phoneCredential = PhoneAuthProvider.getCredential(_phoneAuthVerificationId.value, userInputSmsCode.value)
                val linkedNumber = _auth.currentUser?.providerData?.find { it.providerId == PhoneAuthProvider.PROVIDER_ID }?.phoneNumber
                if (!linkedNumber.isNullOrEmpty()) {
                    _auth.currentUser?.reload()?.await()
                    _auth.currentUser?.unlink(PhoneAuthProvider.PROVIDER_ID)?.await()
                }
                _auth.currentUser?.linkWithCredential(phoneCredential)?.await()
            }catch (e: FirebaseAuthException){
                _phoneAuthErrorMessage.value = e.message.toString()
                userInputSmsCodeValidation.value = _phoneAuthErrorMessage.value
            }
        }
        return _phoneAuthErrorMessage.value
    }

    // 전화 인증 발송
    fun sendPhoneAuthCode(activity: Activity, startSmsReceiver: ()->Unit){
        // 전화 인증 여부를 초기화
        _isVerifiedPhone.value = false
        _isPhoneAuthExpired.value = false

        // 전화번호 앞에 "+82 " 국가코드 붙여주기
        val setNumber = userInputPhone.value.replaceRange(0,1,"+82 ")

        _auth.setLanguageCode("kr")

        val options = PhoneAuthOptions.newBuilder(_auth)
            .setPhoneNumber(setNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(phoneAuthCallbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        startSmsReceiver()
    }

    // 전화 인증코드 발송 콜백
    private val phoneAuthCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

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
            userInputPhoneValidation.value = e.message ?: "인증에 실패했습니다."
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // verificationId는 문자로 받는 코드가 아니었다
            _phoneAuthVerificationId.value = verificationId
            _isPhoneAuthCodeSent.value = true
            userInputSmsCode.value = ""
            phoneAuthTimer.start()
        }
    }

    // ================3. 초기화 ==============================================================
    fun resetStep2States(){
        userInputName.value = ""
        userInputPhone.value = ""
        userInputSmsCode.value = ""
        userInputNameValidation.value = ""
        userInputPhoneValidation.value = ""
        userInputSmsCodeValidation.value = ""

        _isPhoneAuthCodeSent.value = false
        _phoneAuthVerificationId.value = ""
        _isVerifiedPhone.value = false
        _phoneAuthErrorMessage.value = ""
        _phoneAuthCredential.value = null
        _isAlreadyRegisteredPhoneUser.value = false
        _alreadyRegisteredUserEmail.value = ""
        _alreadyRegisteredUserProvider.value = ""
        _phoneAuthButtonText.value = "인증하기"
        _isPhoneAuthExpired.value = true
    }

    fun cancelPhoneAuthTimer(){
        phoneAuthTimer.cancel()
        _phoneAuthButtonText.value = "인증하기"
        _isPhoneAuthExpired.value = true
    }
}
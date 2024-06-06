package kr.co.lion.modigm.ui.join.vm

import android.app.Activity
import android.os.CountDownTimer
import android.text.InputFilter
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.repository.UserInfoRepository
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class JoinStep2ViewModel: ViewModel() {
    // ================1. 유효성 검사 관련==============================================================

    // 이름
    val userName = MutableLiveData<String>()
    // 이름 유효성 검사
    val nameValidation = MutableLiveData("")

    // 전화번호
    val userPhone = MutableLiveData<String>()
    // 전화번호 유효성 검사
    val phoneValidation = MutableLiveData("")

    // 인증번호입력
    val inputSmsCode = MutableLiveData<String>()
    // 인증번호입력 유효성 검사
    val inputSmsCodeValidation = MutableLiveData("")

    // 유효성 검사
    fun validate(): Boolean {
        // 에러 표시 초기화
        nameValidation.value =""
        phoneValidation.value =""
        inputSmsCodeValidation.value =""
        var result = true

        if(userName.value.isNullOrEmpty()){
            nameValidation.value = "이름을 입력해주세요."
            result = false
        }
        if(userPhone.value.isNullOrEmpty()){
            phoneValidation.value = "전화번호를 입력해주세요."
            result = false
        }else{
            if(!Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", userPhone.value)){
                phoneValidation.value = "올바른 전화번호가 아닙니다."
                result = false
            }else if(!_isCodeSent.value!!){
                phoneValidation.value = "인증하기 버튼을 눌러서 인증을 진행해주세요."
                result = false
            }
        }
        if(inputSmsCode.value.isNullOrEmpty()){
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

        if(userPhone.value.isNullOrEmpty()){
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

    // 이름을 한글만 입력 가능하게
    fun filterOnlyKorean(): InputFilter {
        return InputFilter { source, _, _, _, _, _ ->
            val ps = Pattern.compile("^[ㄱ-ㅣ가-힣]*$")
            if (!ps.matcher(source).matches()) {
                return@InputFilter ""
            }
            null
        }
    }

    // ================2. 전화번호 인증 관련==============================================================

    private val _auth = FirebaseAuth.getInstance()

    private val _db = UserInfoRepository()

    private val _authButtonText = MutableLiveData("인증하기")
    val authButtonText: LiveData<String> = _authButtonText

    private val _authExpired = MutableLiveData(true)
    val authExpired: LiveData<Boolean> = _authExpired

    // 인증문자 발송 여부
    private val _isCodeSent = MutableLiveData(false)
    val isCodeSent: LiveData<Boolean> = _isCodeSent

    // 인증 ID(인증 코드 내용 아님)
    private val _verificationId = MutableLiveData("")
    val verificationId: LiveData<String> = _verificationId

    // 올바른 전화번호 확인 여부
    // onVerificationCompleted, onVerificationFailed에서 확인
    private val _isVerifiedPhone = MutableLiveData(false)
    val isVerifiedPhone: LiveData<Boolean> = _isVerifiedPhone

    // 인증 에러 메시지
    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> = _errorMessage

    // 나중에 이메일 계정과 합칠 때 필요한 전화번호 인증 credential
    private var _credential = MutableLiveData<AuthCredential>()
    val credential: LiveData<AuthCredential> = _credential

    // 이미 등록된 전화번호 계정이 있는지 여부
    private val _alreadyRegisteredUser = MutableLiveData(false)

    // 이미 등록된 전화번호 계정의 이메일
    private val _alreadyRegisteredUserEmail = MutableLiveData("")
    val alreadyRegisteredUserEmail: LiveData<String> = _alreadyRegisteredUserEmail

    // 이미 등록된 전화번호 계정의 프로바이더
    private val _alreadyRegisteredUserProvider = MutableLiveData("")
    val alreadyRegisteredUserProvider: LiveData<String> = _alreadyRegisteredUserProvider

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
        val checkResult = _db.checkUserByPhone(userPhone.value?:"")

        if(checkResult != null){
            _errorMessage.value = "이미 해당 번호로 가입한 계정이 있습니다."
            _alreadyRegisteredUserProvider.value = checkResult["provider"]
            _alreadyRegisteredUserEmail.value = checkResult["email"]
        }
        // 오류 메시지
        if(_isCodeSent.value!!){
            try{
                _errorMessage.value = ""
                val phoneCredential = PhoneAuthProvider.getCredential(_verificationId.value?:"", inputSmsCode.value?:"")
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
                e.errorCode
                inputSmsCodeValidation.value = _errorMessage.value
            }
        }
        return _errorMessage.value?:""
    }

    // 전화 인증 발송
    fun sendCode(activity: Activity){
        // 전화 인증 여부를 초기화
        _isVerifiedPhone.value = false
        _authExpired.value = false

        // 전화번호 앞에 "+82 " 국가코드 붙여주기
        val setNumber = userPhone.value?.replaceRange(0,1,"+82 ")

        _auth.setLanguageCode("kr")

        if(setNumber == null) return
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
            phoneValidation.value = e.message
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
        _credential = MutableLiveData<AuthCredential>()
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
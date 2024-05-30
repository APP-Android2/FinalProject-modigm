package kr.co.lion.modigm.ui.join.vm

import android.app.Activity
import android.text.InputFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
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
        return InputFilter { source, start, end, dest, dstart, dend ->
            val ps = Pattern.compile("^[ㄱ-ㅣ가-힣]*$")
            if (!ps.matcher(source).matches()) {
                return@InputFilter ""
            }
            null
        }
    }

    // ================2. 전화번호 인증 관련==============================================================

    private val _auth = FirebaseAuth.getInstance()

    // 인증문자 발송 여부
    private val _isCodeSent = MutableLiveData(false)
    val isCodeSent: LiveData<Boolean> = _isCodeSent

    // 인증 ID(인증 코드 내용 아님)
    private var _verificationId = ""
    // 인증 여부
    private val _phoneVerified = MutableLiveData(false)
    val phoneVerified: LiveData<Boolean> = _phoneVerified

    // 인증 에러 메시지
    private var _errorMessage = ""

    // 나중에 이메일 계정과 합칠 때 필요한 credential
    private val _credential = MutableLiveData<PhoneAuthCredential>()
    val credential: LiveData<PhoneAuthCredential> = _credential

    // 이미 등록된 전화번호 계정이 있는지 여부
    var alreadyRegisteredUser = false

    // 이미 등록된 전화번호 계정의 이메일
    var alreadyRegisteredUserEmail = ""

    // 이미 등록된 전화번호 계정의 프로바이더
    var alreadyRegisteredUserProvider = ""

    // 전화번호 인증
    suspend fun createPhoneUser(): String {
        // 오류 메시지
        if(_isCodeSent.value!!){
            try{
                _credential.value = PhoneAuthProvider.getCredential(_verificationId, inputSmsCode.value!!)

                // 로그인 결과를 담아서 이미 등록된 유저인지 확인한다.
                val signInResult = _auth.signInWithCredential(_credential.value!!).await()
                alreadyRegisteredUser = signInResult.additionalUserInfo?.isNewUser != true
                if(alreadyRegisteredUser){
                    _errorMessage = "이미 해당 번호로 가입한 계정이 있습니다."

                    // 프로바이더 확인
                    for(provider in signInResult.user?.providerData!!){
                        if(provider.providerId == "password"){
                            alreadyRegisteredUserProvider = "email"
                            alreadyRegisteredUserEmail = provider.email!!
                        }
                    }
                    // 중복인 경우에는 이미 등록된 계정을 지우면 안되기 때문에 로그아웃만 하기
                    _auth.signOut()
                }else{
                    // 중복이 아닐 경우에는 나중에 이메일 계정과 합칠 때
                    // credential 사용하기 위해 signin된 계정을 다시 지워놓기
                    signInResult.user?.delete()?.await()
                }


            }catch (e: FirebaseAuthException){
                _errorMessage = e.message.toString()
                inputSmsCodeValidation.value = _errorMessage
            }
        }
        return _errorMessage
    }

    // 전화 인증 발송
    fun sendCode(activity: Activity){
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
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // 전화번호 인증 성공
            _phoneVerified.value = true
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // 전화번호 인증 실패
            _phoneVerified.value = false
            phoneValidation.value = e.message
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // verificationId는 문자로 받는 코드가 아니었다
            _verificationId = verificationId
            _isCodeSent.value = true
            inputSmsCode.value = ""
        }
    }
}
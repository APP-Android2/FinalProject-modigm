package kr.co.lion.modigm.ui.profile.vm

import android.app.Activity
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.repository.UserInfoRepository
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class ChangePhoneViewModel: ViewModel() {
    private val _db = UserInfoRepository()
    private val _auth = FirebaseAuth.getInstance()

    // 변경할 전화번호
    val userPhone = MutableLiveData<String>()
    // 전화번호 에러 메시지
    val userPhoneErrorMessage = MutableLiveData<String>()

    // 인증 버튼의 텍스트
    val phoneAuthButtonText = MutableLiveData("인증")

    // 입력할 인증 코드
    val phoneAuth = MutableLiveData<String>()
    // 인증 에러 메시지
    val phoneAuthErrorMessage = MutableLiveData<String>()

    // 전화번호 인증에 필요한 varificationId
    private var _verificationId = MutableLiveData<String>()
    val verificationId: MutableLiveData<String> = _verificationId

    // 인증 필요 여부
    private var _isVerified = MutableLiveData<Boolean>()
    val isVerified: MutableLiveData<Boolean> = _isVerified

    // 문자 발송 중 여부
    private var _isSendingCode = MutableLiveData<Boolean>()
    val isSendingCode: MutableLiveData<Boolean> = _isSendingCode

    // 문자 발송 되었음 여부
    private var _isCodeSent = MutableLiveData<Boolean>()
    val isCodeSent: MutableLiveData<Boolean> = _isCodeSent

    // 확인 버튼 활성화
    private var _isButtonEnabled = MutableLiveData<Boolean>()
    val isButtonEnabled: MutableLiveData<Boolean> = _isButtonEnabled

    // 전화번호 확인
    fun validatePhone(): Boolean{
        userPhoneErrorMessage.value = ""

        if(userPhone.value.isNullOrEmpty()) {
            userPhoneErrorMessage.value = "전화번호를 입력해주세요."
            return false
        }else{
            if(!Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", userPhone.value)){
                userPhoneErrorMessage.value = "올바른 전화번호가 아닙니다."
                return false
            }
        }

        return true
    }

    // 인증번호 확인
    fun validateAuth(): Boolean{
        if(phoneAuth.value.isNullOrEmpty()){
            phoneAuthErrorMessage.value = "인증번호를 입력해주세요."
            return true
        }

        return false
    }


    // 전화 인증 SMS 발송 코드========================================================

    // 전화 인증 발송
    fun sendCode(activity: Activity){
        // 전화 인증 여부를 초기화
        _isVerified.value = false
        // 문자 발송 중
        _isSendingCode.value = true

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
            // 전화 인증 여부를 초기화
            _isVerified.value = false
            // 문자 발송 중
            _isSendingCode.value = false
            _isCodeSent.value = false
        }

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // 입력한 전화번호가 정상적으로 확인될 경우(인증이 완료된게 아님, 실제 번호일때만 호출됨)
            // 문자 발송 중
            _isSendingCode.value = false
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // 입력한 전화번호 또는 인증번호가 잘못되었을 경우
            phoneAuthErrorMessage.value = e.message
            // 전화 인증 여부를 초기화
            _isVerified.value = false
            // 문자 발송 중
            _isSendingCode.value = false
            _isCodeSent.value = false
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // verificationId는 문자로 받는 코드가 아니었다
            _verificationId.value = verificationId
            _isSendingCode.value = false
            _isCodeSent.value = true
            phoneAuth.value = ""
            setTimer.start()
        }
    }

    // 문자 수신 60초 타이머
    val setTimer: CountDownTimer by lazy {
        _isSendingCode.value = true
        object : CountDownTimer(60000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                phoneAuthButtonText.value = "${millisUntilFinished/1000}"
            }

            override fun onFinish() {
                _isVerified.value = false
                _isSendingCode.value = false
                phoneAuthButtonText.value = "인증"
            }
        }
    }

    fun cancelTimer(){
        setTimer.cancel()
        phoneAuthButtonText.value = "인증"
        _isVerified.value = false
        _isSendingCode.value = false
    }


    // 전화번호 변경
    suspend fun changePhone() {
        if(_isCodeSent.value == true){
            try{
                phoneAuthErrorMessage.value = ""
                val phoneCredential = PhoneAuthProvider.getCredential(_verificationId.value?:"", phoneAuth.value?:"")
                // 현재 연결된 전화번호 연결을 해제한다.
                val linkedProviders = _auth.currentUser?.providerData?.map { it.providerId }
                if (linkedProviders != null) {
                    for(provider in linkedProviders){
                        if(provider == "phone"){
                            _auth.currentUser?.unlink("phone")?.await()
                            break
                        }
                    }
                }
                // 새로운 전화번호와 연결한다.
                _auth.currentUser?.linkWithCredential(phoneCredential)?.await()
                // 파이어스토어에 저장된 정보를 업데이트 한다
                _db.updatePhone(_auth.currentUser?.uid?:"", userPhone.value?:"")
                // SharedPreferences에 저장된 정보를 업데이트 한다.
                //val userData = prefs.getUserData("currentUserData")
//                userData?.userPhone = userPhone.value?:""
//                if (userData != null) {
//                    //prefs.setUserData("currentUserData", userData)
//                }

                _isVerified.value = true
            }catch (e: FirebaseAuthException){
                phoneAuthErrorMessage.value = e.message.toString()
            }
        }
    }

}
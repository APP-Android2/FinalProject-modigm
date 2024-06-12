package kr.co.lion.modigm.ui.login.vm

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import kr.co.lion.modigm.repository.UserInfoRepository
import java.util.concurrent.TimeUnit

class FindPwViewModel: ViewModel() {
    private val _auth = FirebaseAuth.getInstance()
    private val _repository = UserInfoRepository()

    // 이메일
    val email = MutableLiveData<String>()
    val emailError = MutableLiveData<String>()

    // 연락처
    val phone = MutableLiveData<String>()
    val phoneError = MutableLiveData<String>()

    // 전화번호 인증에 필요 onCodeSent에서 전달받음
    private var _verificationId = MutableLiveData<String>()
    val verificationId: LiveData<String> = _verificationId

    // 이름, 연락처, 문자 발송까지 모두 확인 되면
    private var _isComplete = MutableLiveData<Boolean>()
    val isComplete: LiveData<Boolean> = _isComplete
    fun resetComplete(){
        _isComplete.value = false
    }


    // 유효성 검사
    fun validateInput():Boolean {
        if(email.value.isNullOrEmpty()){
            emailError.value = "이메일을 입력해주세요."
            return false
        }
        if(phone.value.isNullOrEmpty()){
            phoneError.value = "연락처를 입력해주세요."
            return false
        }
        return true
    }

    // 이메일이 DB에 등록된 회원 정보에 있는지 확인하고 등록된 이메일과 입력한 이메일을 매칭
    fun checkEmailAndPhone(activity: Activity){
        viewModelScope.launch {
            val result1 = _repository.checkUserByPhoneFindNameAndEmail(phone.value?:"")
            if(result1 != null){
                // 등록된 연락처라면 등록된 이름이 입력한 이름과 맞는지 확인
                val resultName = result1["email"]
                if(resultName == email.value){
                    // 확인이 완료되면 인증 문자 발송
                    sendCode(activity)
                }else{
                    emailError.value = "등록되지 않은 이메일입니다."
                }
            }else{
                phoneError.value = "등록되지 않은 전화번호입니다."
            }
        }
    }

    // 인증 문자 발송
    private fun sendCode(activity: Activity){
        // 전화번호 앞에 "+82 " 국가코드 붙여주기
        val setNumber = phone.value?.replaceRange(0,1,"+82 ")?:""

        _auth.setLanguageCode("kr")

        if(setNumber.isNullOrEmpty()) return
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
            // 입력한 전화번호가 정상적으로 확인될 경우(인증이 완료된게 아님, 실제 번호일때만 호출됨)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // 입력한 전화번호가 잘못되었을 경우
            phoneError.value = "전화번호가 잘못되었습니다."
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // verificationId는 문자로 받는 코드가 아니었다
            _verificationId.value = verificationId
            _isComplete.value = true
        }
    }
}
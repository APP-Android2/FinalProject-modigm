package kr.co.lion.modigm.ui.join

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class PhoneAuth {
    private val _auth = FirebaseAuth.getInstance()

    // 전화번호 인증
    private var _isCodeSent = false

    private var _smsCode = ""

    private var _phoneVerificated = false

    private val _credential = MutableLiveData<PhoneAuthCredential>()
    val credential: LiveData<PhoneAuthCredential> = _credential

    // 전화번호 인증
    suspend fun createPhoneUser(inputCode:String): String {
        // 오류 메시지
        var error = ""
        if(_isCodeSent){
            try{
                _credential.value = PhoneAuthProvider.getCredential(_smsCode, inputCode)
                // _auth.signInWithCredential(credential).await()
            }catch (e: FirebaseAuthException){
                error = e.message.toString()
                e.errorCode
            }
        }
        return error
    }

    // 전화 인증 발송
    fun sendCode(activity: Activity, phoneNumber: String){
        // 전화번호 앞에 "+82 " 국가코드 붙여주기
        val setNumber = phoneNumber.replaceRange(0,1,"+82 ")

        _auth.setLanguageCode("kr")

        val options = PhoneAuthOptions.newBuilder(_auth)
            .setPhoneNumber(setNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // 전화 인증 콜백
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // 전화번호 인증 성공
            _phoneVerificated = true
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // 전화번호 인증 실패
            _phoneVerificated = false
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            _smsCode = verificationId
            _isCodeSent = true
        }
    }
}
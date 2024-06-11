package kr.co.lion.modigm.ui.login.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FindPwAuthViewModel: ViewModel() {
    private val _auth = FirebaseAuth.getInstance()

    // 입력한 인증 번호
    val inputCode = MutableLiveData<String>()
    // 입력한 인증 번호 에러
    val inputCodeError = MutableLiveData<String>()


    // 인증에 필요한 verficationId
    private val _verificationId = MutableLiveData<String>()
    fun setVerificationId(id:String){
        _verificationId.value = id
    }

    // 인증 완료 여부
    private val _isComplete = MutableLiveData<Boolean>()
    val isComplete: LiveData<Boolean> = _isComplete

    // 유효성 검사
    fun validateInput(): Boolean{
        if(inputCode.value.isNullOrEmpty()){
            inputCodeError.value = "인증번호를 입력해주세요."
            return false
        }
        return true
    }

    fun checkCodeAndFindEmail(){
        viewModelScope.launch {
            try{
                val phoneCredential = PhoneAuthProvider.getCredential(_verificationId.value?:"", inputCode.value?:"")
                val result = _auth.signInWithCredential(phoneCredential).await()
                if(result.user != null){
                    _isComplete.value = true
                }else{
                    _isComplete.value = false
                    inputCodeError.value = "인증에 실패했습니다."
                }
            }catch (e: FirebaseAuthException){
                inputCodeError.value = "인증번호가 잘못되었습니다."
                return@launch
            }
        }
    }
}
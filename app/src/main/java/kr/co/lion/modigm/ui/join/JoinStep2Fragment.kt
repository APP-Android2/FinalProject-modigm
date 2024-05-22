package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kr.co.lion.modigm.databinding.FragmentJoinStep2Binding
import java.util.regex.Pattern


class JoinStep2Fragment : Fragment() {

    val binding: FragmentJoinStep2Binding by lazy {
        FragmentJoinStep2Binding.inflate(layoutInflater)
    }

    var authenticationValue: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        settingTextInputUserName()
        settingTextInputUserPhone()
        settingButtonPhoneAuth()
        return binding.root
    }

    private fun settingTextInputUserName(){

        // 한글 만 입력 되도록
        val filterAlphaNum = InputFilter { source, start, end, dest, dstart, dend ->
            val ps = Pattern.compile("^[ㄱ-ㅣ가-힣]*$")
            if (!ps.matcher(source).matches()) {
                return@InputFilter ""
            }
            null
        }

        // 아래와 같이 EditText에 적용 한다.
        binding.textinputJoinUserName.setFilters(arrayOf(filterAlphaNum))
    }

    private fun settingTextInputUserPhone(){
        // 번호 입력 시 자동으로 하이픈을 넣어줌
        binding.textinputJoinUserPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    private fun settingButtonPhoneAuth(){
        binding.textInputLayoutJoinUserPhone.error = ""

        binding.buttonJoinPhoneAuth.setOnClickListener {
            val phone = binding.textinputJoinUserPhone.text.toString()
            // 번호 유효성 검사
            if(phone.isEmpty()){
                binding.textInputLayoutJoinUserPhone.error = "전화번호를 입력해주세요."
                return@setOnClickListener
            }
            if(!Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", phone)){
                binding.textInputLayoutJoinUserPhone.error = "올바른 전화번호가 아닙니다."
                return@setOnClickListener
            }
            binding.linearLayoutJoinPhoneAuth.visibility = View.VISIBLE
            // 번호 인증 api 호출
            callAuth(phone)
        }
    }

    // 번호 인증 api 호출
    private fun callAuth(phoneNumber:String){
        // 인증번호 발송
        authenticationValue = "1234"
    }

    // 입력한 내용 유효성 검사
    fun validate(): Boolean {
        // 에러 표시 초기화
        binding.textInputLayoutJoinUserName.error =""
        binding.textInputLayoutJoinUserPhone.error =""
        binding.textInputLayoutJoinPhoneAuth.error =""

        val name = binding.textinputJoinUserName.text.toString()
        val phone = binding.textinputJoinUserPhone.text.toString()
        val auth = binding.textinputJoinPhoneAuth.text.toString()
        var result = true

        if(name.isEmpty()){
            binding.textInputLayoutJoinUserName.error = "이름을 입력해주세요."
            result = false
        }
        if(phone.isEmpty()){
            binding.textInputLayoutJoinUserPhone.error = "전화번호를 입력해주세요."
            result = false
        }else{
            if(!Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", phone)){
                binding.textInputLayoutJoinUserPhone.error = "올바른 전화번호가 아닙니다."
                result = false
            }
        }
        if(auth.isEmpty()){
            binding.textInputLayoutJoinPhoneAuth.error = "인증번호를 입력해주세요."
            result = false
        }
        if(auth != authenticationValue){
            binding.textInputLayoutJoinPhoneAuth.error = "인증번호가 일치하지 않습니다."
            result = false
        }

        return result
    }

}
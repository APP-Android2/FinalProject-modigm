package kr.co.lion.modigm.ui.join

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.co.lion.modigm.databinding.FragmentJoinStep1Binding
import java.util.regex.Pattern

class JoinStep1Fragment : Fragment() {

    val binding: FragmentJoinStep1Binding by lazy {
        FragmentJoinStep1Binding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

    // 입력한 내용 유효성 검사
    fun validate(): Boolean {
        // 에러 표시 초기화
        binding.textInputLayoutJoinUserEmail.error =""
        binding.textInputLayoutJoinUserPassword.error =""
        binding.textInputLayoutJoinUserPasswordCheck.error =""

        val email = binding.textInputJoinUserEmail.text.toString()
        val password = binding.textInputJoinUserPassword.text.toString()
        val passwordCheck = binding.textInputJoinUserPasswordCheck.text.toString()
        var result = true

        if(email.isEmpty()){
            binding.textInputLayoutJoinUserEmail.error = "이메일을 입력해주세요."
            result = false
        }
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.textInputLayoutJoinUserEmail.error = "올바른 이메일 형식이 아닙니다."
            result = false
        }
        if(password.isEmpty()){
            binding.textInputLayoutJoinUserPassword.error = "비밀번호를 입력해주세요."
            result = false
        }
        if(!Pattern.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]{8,20}$", password)){
            binding.textInputLayoutJoinUserPassword.error = "영문, 숫자, 특수문자가 포함된 비밀번호를 8~20자로 입력해주세요."
            result = false
        }
        if(passwordCheck.isEmpty()){
            binding.textInputLayoutJoinUserPasswordCheck.error = "비밀번호 확인을 입력해주세요."
            result = false
        }
        if(password.isNotEmpty() && passwordCheck.isNotEmpty() &&  password != passwordCheck){
            binding.textInputLayoutJoinUserPasswordCheck.error = "비밀번호가 일치하지 않습니다."
            result = false
        }

        return result
    }

    fun getJoinUserEmail(): String {
        return binding.textInputJoinUserEmail.text.toString()
    }

    fun getJoinUserPassword(): String {
        return binding.textInputJoinUserPassword.text.toString()
    }

    fun getJoinUserPasswordCheck(): String {
        return binding.textInputJoinUserPasswordCheck.text.toString()
    }

}
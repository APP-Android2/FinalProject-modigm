package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinStep2Binding
import kr.co.lion.modigm.ui.join.vm.JoinStep2ViewModel


class JoinStep2Fragment : Fragment() {

    lateinit var binding: FragmentJoinStep2Binding

    private val joinStep2ViewModel: JoinStep2ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_join_step2, container, false)
        binding.viewModel = joinStep2ViewModel
        binding.lifecycleOwner = this

        settingTextInputLayoutError()
        settingTextInputUserPhone()
        settingButtonPhoneAuth()
        settingCollector()

        return binding.root
    }

    // 에러 메시지 설정
    private fun settingTextInputLayoutError(){
        lifecycleScope.launch {
            joinStep2ViewModel.nameValidation.collect{
                if(it.isNotEmpty()){
                    binding.textInputLayoutJoinUserName.error = it
                }
            }
        }
        lifecycleScope.launch {
            joinStep2ViewModel.phoneValidation.collect{
                if(it.isNotEmpty()){
                    binding.textInputLayoutJoinUserPhone.error = it
                }
            }
        }
        lifecycleScope.launch {
            joinStep2ViewModel.inputSmsCodeValidation.collect{
                if(it.isNotEmpty()){
                    binding.textInputLayoutJoinPhoneAuth.error = it
                }
            }
        }
    }

    private fun settingTextInputUserPhone(){
        // 번호 입력 시 자동으로 하이픈을 넣어줌
        binding.textinputJoinUserPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    private fun settingButtonPhoneAuth(){
        binding.textInputLayoutJoinUserPhone.error = ""

        binding.buttonJoinPhoneAuth.setOnClickListener {
            // 전화번호 유효성 검사 먼저 한 후
            if(!joinStep2ViewModel.checkPhoneValidation()) return@setOnClickListener

            // 응답한 전화번호로 인증번호 SMS 보내기
            joinStep2ViewModel.sendCode(requireActivity())
        }
    }

    private fun settingCollector(){
        // 인증 코드 발송이 성공하면 인증번호 입력 창 보여주기
        lifecycleScope.launch {
            joinStep2ViewModel.isCodeSent.collect {
                if(it){
                    binding.linearLayoutJoinPhoneAuth.visibility = View.VISIBLE
                    binding.textinputJoinPhoneAuth.requestFocus()
                }else{
                    binding.linearLayoutJoinPhoneAuth.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            joinStep2ViewModel.authExpired.collect {
                if(it){
                    binding.buttonJoinPhoneAuth.setBackgroundColor(requireContext().getColor(R.color.pointColor))
                    binding.buttonJoinPhoneAuth.isClickable = true
                }else{
                    binding.buttonJoinPhoneAuth.setBackgroundColor(requireContext().getColor(R.color.textGray))
                    binding.buttonJoinPhoneAuth.isClickable = false
                }
            }
        }
    }

}
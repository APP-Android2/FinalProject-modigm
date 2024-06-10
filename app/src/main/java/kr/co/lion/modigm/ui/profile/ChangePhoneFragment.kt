package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChangePhoneBinding
import kr.co.lion.modigm.ui.profile.vm.ChangePhoneViewModel
import kr.co.lion.modigm.util.FragmentName

class ChangePhoneFragment : Fragment() {

    lateinit var binding: FragmentChangePhoneBinding
    private val changePhoneViewModel: ChangePhoneViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_change_phone, container, false)
        binding.changePhoneViewModel = changePhoneViewModel
        binding.lifecycleOwner = this

        settingToolbar()
        settingTextInputUserPhone()
        settingErrorMessageObservers()
        settingPhoneAuthObservers()
        settingButtonChangePhoneAuth()
        settingChangePWButtonDone()

        return binding.root
    }

    private fun settingToolbar(){
        with(binding.toolbarChangePhone){
            title = "전화번호 변경"
            setNavigationIcon(R.drawable.icon_arrow_back_24px)
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack(FragmentName.CHANGE_PHONE.str, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }
    }

    private fun settingTextInputUserPhone(){
        // 번호 입력 시 자동으로 하이픈을 넣어줌
        binding.textinputChangeUserPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    private fun settingErrorMessageObservers(){
        // 전화번호 에러 메시지
        changePhoneViewModel.userPhoneErrorMessage.observe(viewLifecycleOwner){
            binding.textInputLayoutChangeUserPhone.error = it
        }

        // 인증번호 에러 메시지
        changePhoneViewModel.phoneAuthErrorMessage.observe(viewLifecycleOwner){
            binding.textInputLayoutChangePhoneAuth.error = it
        }
    }

    private fun settingPhoneAuthObservers(){
        // 문자가 발송되면 인증번호 입력창 띄우기
        changePhoneViewModel.isCodeSent.observe(viewLifecycleOwner){
            if(it){
                binding.linearLayoutChangePhoneAuth.visibility = View.VISIBLE
                binding.textinputChangePhoneAuth.requestFocus()
            }
        }

        // 인증번호 클릭 후 60초간 클릭 비활성화 및 색 변경
        changePhoneViewModel.isSendingCode.observe(viewLifecycleOwner){
            with(binding.buttonChangePhoneAuth){
                if(it){
                    setBackgroundColor(requireContext().getColor(R.color.textGray))
                    isClickable = false
                }else{
                    setBackgroundColor(requireContext().getColor(R.color.pointColor))
                    isClickable = true
                }
            }
        }

        // 전화번호 연결이 완료되면 나온다.
        changePhoneViewModel.isVerified.observe(viewLifecycleOwner){
            if(it){
                parentFragmentManager.popBackStack(FragmentName.CHANGE_PHONE.str, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                binding.changePWButtonDone.isClickable = true
            }
        }
    }

    private fun settingButtonChangePhoneAuth(){
        binding.buttonChangePhoneAuth.setOnClickListener {
            // 전화번호 유효성 검사
            val result = changePhoneViewModel.validatePhone()
            if(!result) return@setOnClickListener

            // 인증 번호 발송
            changePhoneViewModel.sendCode(requireActivity())
        }
    }

    private fun settingChangePWButtonDone(){
        binding.changePWButtonDone.setOnClickListener {
            binding.changePWButtonDone.isClickable = false
            // 인증번호 입력칸이 비어있으면 안됨
            if(changePhoneViewModel.validateAuth()){
                binding.changePWButtonDone.isClickable = true
                return@setOnClickListener
            }
            lifecycleScope.launch {
                changePhoneViewModel.changePhone()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        changePhoneViewModel.cancelTimer()
    }

}
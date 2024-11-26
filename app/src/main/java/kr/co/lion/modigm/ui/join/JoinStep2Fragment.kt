package kr.co.lion.modigm.ui.join

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.gms.auth.api.phone.SmsRetriever
import dagger.hilt.android.AndroidEntryPoint
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinStep2Binding
import kr.co.lion.modigm.ui.DBBaseFragment
import kr.co.lion.modigm.ui.join.vm.JoinStep2ViewModel
import kr.co.lion.modigm.util.SmsReceiver
import kr.co.lion.modigm.util.collectWhenStarted

@AndroidEntryPoint
class JoinStep2Fragment : DBBaseFragment<FragmentJoinStep2Binding>(R.layout.fragment_join_step2) {

    private val joinStep2ViewModel: JoinStep2ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        binding.viewModel = joinStep2ViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingTextInputLayoutError()
        settingTextInputUserPhone()
        settingButtonPhoneAuth()
        settingCollector()
        joinStep2ViewModel.inputSmsCode.value = SmsReceiver.smsCode.value
    }

    // 에러 메시지 설정
    private fun settingTextInputLayoutError(){

        // 이름 에러
        collectWhenStarted(joinStep2ViewModel.nameValidation){
            binding.textInputLayoutJoinUserName.error = it
        }

        collectWhenStarted(joinStep2ViewModel.phoneValidation){
            binding.textInputLayoutJoinUserPhone.error = it
        }

        collectWhenStarted(joinStep2ViewModel.inputSmsCodeValidation){
            binding.textInputLayoutJoinPhoneAuth.error = it
        }

    }

    private fun settingTextInputUserPhone(){
        // 번호 입력 시 자동으로 하이픈을 넣어줌
        binding.textinputJoinUserPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    private fun settingButtonPhoneAuth(){
        binding.textInputLayoutJoinUserPhone.error = ""

        binding.buttonJoinPhoneAuth.setOnClickListener {
            joinStep2ViewModel.showLoading()
            // 전화번호 유효성 검사 먼저 한 후
            if(!joinStep2ViewModel.checkPhoneValidation()){
                joinStep2ViewModel.hideLoading()
                return@setOnClickListener
            }

            // 응답한 전화번호로 인증번호 SMS 보내기
            joinStep2ViewModel.sendCode(requireActivity()){
                startSmsReceiver()
            }
        }
    }

    private fun settingCollector(){
        // 인증 코드 발송이 성공하면 인증번호 입력 창 보여주기
        collectWhenStarted(joinStep2ViewModel.isCodeSent) {
            joinStep2ViewModel.hideLoading()
            if(it){
                binding.linearLayoutJoinPhoneAuth.visibility = View.VISIBLE
                binding.textinputJoinPhoneAuth.requestFocus()
            }else{
                binding.linearLayoutJoinPhoneAuth.visibility = View.GONE
            }
        }

        collectWhenStarted(joinStep2ViewModel.authExpired) {
            if(it){
                binding.buttonJoinPhoneAuth.setBackgroundColor(requireContext().getColor(R.color.pointColor))
                binding.buttonJoinPhoneAuth.isClickable = true
            }else{
                binding.buttonJoinPhoneAuth.setBackgroundColor(requireContext().getColor(R.color.textGray))
                binding.buttonJoinPhoneAuth.isClickable = false
            }
        }

        // SmsReceiver에서 받은 인증 코드를 입력창에 넣어줌
        collectWhenStarted(SmsReceiver.smsCode){
            joinStep2ViewModel.inputSmsCode.value = it
        }
    }

    private var smsReceiver: SmsReceiver? = null

    private fun startSmsReceiver(){
        SmsRetriever. getClient(requireContext()).startSmsRetriever().also { task ->
            task.addOnSuccessListener {
                smsReceiver = SmsReceiver()

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    requireContext().registerReceiver(smsReceiver, smsReceiver!!.doFilter(),
                        Context.RECEIVER_NOT_EXPORTED)
                }else{
                    requireContext().registerReceiver(smsReceiver, smsReceiver!!.doFilter())
                }
            }
            task.addOnFailureListener {
                stopSmsReceiver()
            }
        }
    }

    private fun stopSmsReceiver(){
        if(smsReceiver != null) {
            requireContext().unregisterReceiver(smsReceiver)
            smsReceiver = null
        }
    }

    override fun onStop() {
        super.onStop()
        stopSmsReceiver()
    }

}
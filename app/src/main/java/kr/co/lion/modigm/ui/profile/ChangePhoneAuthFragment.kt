package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentChangePhoneAuthBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.login.CustomCancelDialog
import kr.co.lion.modigm.ui.login.CustomUpdatePasswordDialog
import kr.co.lion.modigm.ui.profile.vm.ChangePhoneViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.shake
import kr.co.lion.modigm.util.toEditable

class ChangePhoneAuthFragment : VBBaseFragment<FragmentChangePhoneAuthBinding>(FragmentChangePhoneAuthBinding::inflate) {

    // 뷰모델
    private val viewModel: ChangePhoneViewModel by viewModels()

    // 태그
    private val logTag by lazy { ChangePhoneAuthFragment::class.simpleName }

    private val currentUserPhone by lazy {
        arguments?.getString("currentUserPhone") ?: ""
    }

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeViewModel()
        backButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 타이머 취소
        setTimer.cancel()
        // 뷰모델 초기화
        viewModel.clearData()
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView() {
        with(binding) {
            Log.d(logTag, currentUserPhone)
            textInputEditChangePhone.text = currentUserPhone.toEditable()

            // 번호 입력 시 자동으로 하이픈을 넣어줌
            textInputEditChangePhoneNew.addTextChangedListener(
                PhoneNumberFormattingTextWatcher()
            )

            // 실시간 텍스트 변경 감지 설정
            textInputEditChangePhoneNew.addTextChangedListener(inputWatcher)
            textInputEditChangePhoneAuth.addTextChangedListener(inputWatcher)

            // 툴바 설정
            with(toolbarChangePhoneAuth) {
                title = "전화번호 변경"
                setNavigationIcon(R.drawable.icon_arrow_back_24px)
                setNavigationOnClickListener {
                    showCancelDialog()
                }
            }

            textViewChangePhoneAuthTime.visibility = View.GONE

            // 인증 버튼
            with(buttonChangePhoneAuth) {
                isEnabled = false // 버튼을 처음에 비활성화
                setOnClickListener {

                    // 유효성 검사
                    if (!checkPhoneInput()) {
                        return@setOnClickListener
                    }

                    val userPhone = textInputEditChangePhoneNew.text.toString()
                    viewModel.checkPhone(requireActivity(), userPhone)
                }
            }

            // 완료 버튼
            with(buttonChangePhoneAuthDone) {
                isEnabled = false // 버튼을 처음에 비활성화
                setOnClickListener {

                    if (!checkAuthCode()) {
                        return@setOnClickListener
                    }
                    val currentUserPhone = textInputEditChangePhone.text.toString()
                    val newUserPhone = textInputEditChangePhoneNew.text.toString()
                    val verificationId = viewModel.verificationId.value ?: ""
                    val authCode = textInputEditChangePhoneAuth.text.toString()
                    viewModel.updatePhone(currentUserPhone, newUserPhone, verificationId, authCode)
                }
            }
        }
    }

    // 뷰모델 관찰 설정
    private fun observeViewModel() {
        with(binding) {
            // 유효성 검사
            viewModel.phoneInputError.observe(viewLifecycleOwner) { error ->
                if (error != null) {
                    textInputLayoutChangePhoneNew.error = error.message
                    textInputEditChangePhoneNew.requestFocus()
                    textInputLayoutChangePhoneNew.shake()
                }
            }
            // 인증번호 유효성 검사
            viewModel.authCodeInputError.observe(viewLifecycleOwner) { error ->
                if (error != null) {
                    textInputLayoutChangePhoneAuth.error = error.message
                    textInputEditChangePhoneAuth.requestFocus()
                    textInputLayoutChangePhoneAuth.shake()
                }
            }
            // 인증번호 발송 완료
            viewModel.isAuthCodeComplete.observe(viewLifecycleOwner) { isAuthCodeComplete ->
                if (isAuthCodeComplete) {
                    linearLayoutChangePhoneAuth.visibility = View.VISIBLE
                    textViewChangePhoneAuthTime.visibility = View.VISIBLE
                    // 1분 타이머 시작
                    setTimer.start()
                }
            }
            // 전화번호 변경 완료
            viewModel.isComplete.observe(viewLifecycleOwner) { isComplete ->
                if (isComplete) {
                    changePhoneCompleteDialog()
                }
            }
        }
    }

    // 유효성 검사 및 버튼 활성화/비활성화 업데이트
    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            with(binding) {
                // 인증 버튼 활성화
                buttonChangePhoneAuth.isEnabled =
                        // 전화번호가 비어있지 않을 때
                    !textInputEditChangePhoneNew.text.isNullOrEmpty()
                // 최종 확인 버튼 활성화
                buttonChangePhoneAuthDone.isEnabled =
                        // 인증번호가 비어있지 않을 때
                    !textInputEditChangePhoneAuth.text.isNullOrEmpty()
            }
        }

        override fun afterTextChanged(p0: Editable?) {}

    }

    // 새 전화번호 유효성 검사
    private fun checkPhoneInput(): Boolean {
        with(binding) {
            val currentUserPhone = textInputEditChangePhone.text.toString()
            val phoneEditText = textInputEditChangePhoneNew
            val phoneInputLayout = textInputLayoutChangePhoneNew
            val phoneText = phoneEditText.text.toString()

            // 에러 메시지를 설정하고 포커스와 흔들기 동작을 수행하는 함수
            fun showError(message: String) {
                phoneInputLayout.error = message
                phoneEditText.requestFocus()
                phoneEditText.shake()
            }

            return when {
                phoneText == currentUserPhone -> {
                    showError("현재 전화번호와 동일합니다.")
                    false
                }

                phoneText.isEmpty() -> {
                    showError("연락처를 입력해주세요.")
                    false
                }

                phoneText.length <= 11 -> {
                    showError("올바른 연락처를 입력해주세요.")
                    false
                }

                else -> {
                    phoneInputLayout.error = null
                    true
                }
            }
        }
    }

    // 인증번호 유효성 검사
    private fun checkAuthCode(): Boolean {
        with(binding) {
            if (textInputEditChangePhoneAuth.text.isNullOrEmpty()) {
                textInputLayoutChangePhoneAuth.error = "인증번호를 입력해주세요."
                textInputEditChangePhoneAuth.requestFocus()
                textInputLayoutChangePhoneAuth.shake()
                return false
            }
            return true
        }
    }

    // 전화번호 변경 완료 다이얼로그
    private fun changePhoneCompleteDialog() {
        viewModel.isCompleteTo(false)
        val dialog = CustomUpdatePasswordDialog(requireContext())
        with(dialog) {
            setTitle("전화번호 변경")
            setMessage("전화번호 변경을 완료했습니다")
            setPositiveButton("확인") {
                parentFragmentManager.popBackStack(FragmentName.EDIT_PROFILE.str, 0)
            }
            show()
        }

    }

    // 뒤로가기 다이얼로그 표시
    private fun showCancelDialog() {
        val dialog = CustomCancelDialog(requireContext())
        with(dialog){
            setTitle("뒤로가기")
            setMessage("변경을 취소하시겠습니까?")
            setPositiveButton("확인") {
                lifecycleScope.launch {
                    parentFragmentManager.popBackStack(FragmentName.EDIT_PROFILE.str, 0)
                }
            }
            setNegativeButton("취소") {
                dismiss()
            }
            show()
        }
    }

    // 문자 인증 60초 타이머
    private val setTimer: CountDownTimer by lazy {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                with(binding) {
                    buttonChangePhoneAuth.text = "재발송"
                    textViewChangePhoneAuthTime.text = String.format("%02d:%02d초", minutes, seconds)
                }
            }

            override fun onFinish() {
                with(binding) {
                    textViewChangePhoneAuthTime.visibility = View.GONE
                    buttonChangePhoneAuth.isEnabled = true
                }
            }
        }
    }

    // 뒤로가기 버튼 처리
    private fun backButton() {
        // 백버튼 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // 뒤로가기 처리
            showCancelDialog()
        }
    }
}
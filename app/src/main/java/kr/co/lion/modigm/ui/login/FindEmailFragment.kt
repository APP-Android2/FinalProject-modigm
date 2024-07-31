package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindEmailBinding
import kr.co.lion.modigm.ui.ViewBindingFragment
import kr.co.lion.modigm.ui.login.vm.FindEmailViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.shake

class FindEmailFragment :
    ViewBindingFragment<FragmentFindEmailBinding>(FragmentFindEmailBinding::inflate) {

    private val viewModel: FindEmailViewModel by viewModels()

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        observeViewModel()
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView() {
        with(binding) {

            // 실시간 텍스트 변경 감지 설정
            textInputEditFindEmailName.addTextChangedListener(inputWatcher)
            textInputEditFindEmailPhone.addTextChangedListener(inputWatcher)

            // 툴바
            with(toolbarFindEmail) {
                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack(FragmentName.OTHER_LOGIN.str,0)
                }
            }

            // 다음 버튼
            with(buttonFindEmailNext) {
                isEnabled = false  // 버튼을 처음에 비활성화
                setOnClickListener {

                    // 유효성 검사
                    if (!checkAllInputs()) {
                        return@setOnClickListener
                    }
                    // 입력한 이름이 계정 정보와 일치하는지 확인하고 인증 문자 발송
                    val name = textInputEditFindEmailName.text.toString()
                    val phone = textInputEditFindEmailPhone.text.toString()
                    viewModel.checkNameAndPhone(requireActivity(), name, phone)
                }
            }

            // 번호 입력 시 자동으로 하이픈을 넣어줌
            textInputEditFindEmailPhone.addTextChangedListener(
                PhoneNumberFormattingTextWatcher()
            )
        }
    }

    private fun observeViewModel() {
        with(binding) {
            viewModel.isComplete.observe(viewLifecycleOwner) { isComplete ->
                if (isComplete) {
                    moveToNext()
                }
            }
            viewModel.nameError.observe(viewLifecycleOwner) { error ->
                Log.e("FindEmailFragment", "nameError 발생")
                textInputLayoutFindEmailName.error = error.message
                textInputEditFindEmailName.requestFocus()
                textInputEditFindEmailName.shake()
            }
            viewModel.phoneError.observe(viewLifecycleOwner) { error ->
                textInputLayoutFindEmailPhone.error = error.message
                textInputEditFindEmailPhone.requestFocus()
                textInputEditFindEmailPhone.shake()
            }
        }

    }

    // 유효성 검사 및 버튼 활성화/비활성화 업데이트
    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            with(binding) {
                buttonFindEmailNext.isEnabled =
                    !textInputEditFindEmailName.text.isNullOrEmpty() && !textInputEditFindEmailPhone.text.isNullOrEmpty()
            }

        }

        override fun afterTextChanged(p0: Editable?) {}
    }


    // 유효성 검사
    private fun checkAllInputs(): Boolean {
        return checkName() && checkPhone()
    }

    // 이름 유효성 검사
    private fun checkName(): Boolean {
        with(binding) {
            val nameEditText = textInputEditFindEmailName
            val nameInputLayout = textInputLayoutFindEmailName
            val nameText = nameEditText.text.toString()

            // 에러 메시지를 설정하고 포커스와 흔들기 동작을 수행하는 함수
            fun showError(message: String) {
                nameInputLayout.error = message
                nameEditText.requestFocus()
                nameEditText.shake()
            }

            return when {
                nameText.isEmpty() -> {
                    showError("이름을 입력해주세요.")
                    false
                }

                !isValidKoreanName(nameText) -> {
                    showError("올바른 이름을 입력해주세요.")
                    false
                }

                else -> {
                    nameInputLayout.error = null
                    true
                }
            }
        }
    }


    // 연락처 유효성 검사
    private fun checkPhone(): Boolean {
        with(binding) {
            val phoneEditText = textInputEditFindEmailPhone
            val phoneInputLayout = textInputLayoutFindEmailPhone
            val phoneText = phoneEditText.text.toString()

            // 에러 메시지를 설정하고 포커스와 흔들기 동작을 수행하는 함수
            fun showError(message: String) {
                phoneInputLayout.error = message
                phoneEditText.requestFocus()
                phoneEditText.shake()
            }

            return when {
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

    private fun isValidKoreanName(name: String): Boolean {
        // 한글 자음과 모음 정의
        val consonants = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
        val vowels = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ"

        // 이름의 각 글자가 자음이나 모음 단독으로 이루어져 있는 경우
        for (char in name) {
            if (consonants.contains(char) || vowels.contains(char)) {
                return false
            }

            // 각 글자가 한글 음절인지 확인
            if (!char.isHangulSyllable()) {
                return false
            }
        }
        return true
    }

    // 한글 음절인지 확인하는 확장 함수
    private fun Char.isHangulSyllable(): Boolean {
        return this in '\uAC00'..'\uD7A3'
    }


    private fun moveToNext() {
        // 완료 여부는 초기화해서 popStackBack으로 돌아와도 문제 없게
        viewModel.isCompleteTo(false)
        // 다음 화면에 verficationId를 전달하여 번호 인증에 사용
        val fragment = FindEmailAuthFragment().apply {
            arguments = Bundle().apply {
                putString("verificationId", viewModel.verificationId.value)
            }
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, fragment)
            addToBackStack(FragmentName.FIND_EMAIL_AUTH.str)
        }
    }
}

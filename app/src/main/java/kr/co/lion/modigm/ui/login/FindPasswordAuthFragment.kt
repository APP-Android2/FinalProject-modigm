package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindPasswordAuthBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.login.vm.FindPasswordViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.shake

class FindPasswordAuthFragment : VBBaseFragment<FragmentFindPasswordAuthBinding>(FragmentFindPasswordAuthBinding::inflate) {

    private val viewModel: FindPasswordViewModel by viewModels()

    private val verificationId by lazy {
        arguments?.getString("verificationId") ?: ""
    }

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
            textInputEditFindPasswordAuthCode.addTextChangedListener(inputWatcher)

            // 툴바
            with(toolbarFindPasswordAuth) {
                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {

                    // 취소 다이얼로그
                    showCancelDialog()
                }
            }

            // 인증 버튼
            with(buttonFindPasswordAuthOK) {
                isEnabled = false // 버튼을 처음에 비활성화
                setOnClickListener {
                    // 유효성 검사
                    if (!checkAuthCode()) {
                        return@setOnClickListener
                    }

                    // 인증 번호 확인
                    val authCode = textInputEditFindPasswordAuthCode.text.toString()
                    Log.d("FindPwAuthFragment", "인증 버튼 클릭됨. authCode: $authCode")
                    viewModel.checkByAuthCode(verificationId, authCode)
                }
            }
        }
    }

    private fun observeViewModel() {
        with(binding) {
            viewModel.isComplete.observe(viewLifecycleOwner) { isComplete ->
                if (isComplete) {
                    moveToNext()
                }
            }
            // 유효성 검사
            viewModel.authCodeError.observe(viewLifecycleOwner) { error ->
                if (error != null) {
                    textInputLayoutFindPasswordAuthCode.error = error.message
                    textInputEditFindPasswordAuthCode.requestFocus()
                    textInputLayoutFindPasswordAuthCode.shake()
                }

            }

        }
    }

    // 유효성 검사 및 버튼 활성화/비활성화 업데이트
    private val inputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            with(binding) {
                buttonFindPasswordAuthOK.isEnabled =
                    !textInputEditFindPasswordAuthCode.text.isNullOrEmpty()
            }

        }

        override fun afterTextChanged(p0: Editable?) {}
    }

    // 유효성 검사
    private fun checkAuthCode(): Boolean {
        with(binding) {
            if (textInputEditFindPasswordAuthCode.text.isNullOrEmpty()) {
                textInputLayoutFindPasswordAuthCode.error = "인증번호를 입력해주세요."
                textInputEditFindPasswordAuthCode.requestFocus()
                textInputLayoutFindPasswordAuthCode.shake()
                return false
            }
            return true
        }

    }

    private fun moveToNext() {
        // 완료 여부는 초기화해서 popStackBack으로 돌아와도 문제 없게
        viewModel.isCompleteTo(false)
        // 다음 화면에 verficationId를 전달하여 번호 인증에 사용
        val fragment = FindUpdatePasswordFragment().apply {
            arguments = Bundle().apply {
                putString("verificationId", viewModel.verificationId.value)
            }
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, fragment)
            addToBackStack(FragmentName.RESET_PW.str)
        }
    }

    // 뒤로가기 다이얼로그 표시
    private fun showCancelDialog() {
        val dialog = CustomCancelDialog(requireContext())
        dialog.setTitle("뒤로가기")
        dialog.setPositiveButton("확인") {
            parentFragmentManager.popBackStack(FragmentName.OTHER_LOGIN.str,0)
        }
        dialog.setNegativeButton("취소") {

            dialog.dismiss()
        }
        dialog.show()
    }
}
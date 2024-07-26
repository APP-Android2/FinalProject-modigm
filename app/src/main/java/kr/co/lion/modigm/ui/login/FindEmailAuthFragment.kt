package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindEmailAuthBinding
import kr.co.lion.modigm.ui.login.vm.FindEmailAuthViewModel
import kr.co.lion.modigm.util.hideSoftInput

class FindEmailAuthFragment : Fragment(R.layout.fragment_find_email_auth) {

    private val viewModel: FindEmailAuthViewModel by viewModels()

    private val verificationId by lazy {
        arguments?.getString("verificationId")
    }

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바인딩
        val binding = FragmentFindEmailAuthBinding.bind(view)

        viewModel.setVerificationId(verificationId?:"")

        initView(binding)
        settingObserver(binding)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 뷰모델 클리어 함수 구현 요망
    }

    // --------------------------------- LC END ---------------------------------

    // 초기 뷰 세팅
    private fun initView(binding: FragmentFindEmailAuthBinding) {
        with(binding) {

            // 툴바
            with(toolbarFindEmailAuth) {
                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

            // 완료 버튼
            with(buttonFindEmailAuthOK) {
                setOnClickListener {
                    binding.textInputEditFindPassCode.clearFocus()
                    requireActivity().hideSoftInput()
                    isClickable = false
                    // 유효성 검사
                    val validate = viewModel!!.validateInput()
                    if(!validate){
                        isClickable = true
                        return@setOnClickListener
                    }
                    // 인증번호 확인
                    viewModel!!.checkCodeAndFindEmail()
                    isClickable = true
                }
            }
        }
    }

    private fun settingObserver(binding: FragmentFindEmailAuthBinding){
        // 유효성 검사
        viewModel.inputCodeError.observe(viewLifecycleOwner) {
            binding.textInputEditFindPassCode.error = it
            binding.textInputEditFindPassCode.requestFocus()
        }
        // 인증번호 확인해서 메일 찾았는지 여부
        viewModel.email.observe(viewLifecycleOwner){
            if(!it.isNullOrEmpty()){
                // 다이얼로그
                showFindEmailDialog(viewModel.email.value?:"")
            }
            binding.buttonFindEmailAuthOK.isClickable = true
        }
    }

    // 이메일 다이얼로그 표시
    private fun showFindEmailDialog(email:String) {
        val dialog = CustomFindEmailDialog(requireContext())
        dialog.setTitle("이메일 찾기")
        dialog.setEmail("$email 입니다.")
        dialog.setPositiveButton("확인") {
            // Handle button click
            parentFragmentManager.commit {
                replace(R.id.containerMain, OtherLoginFragment())
            }
        }
        dialog.show()
    }
}
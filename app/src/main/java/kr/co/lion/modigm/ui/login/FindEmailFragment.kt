package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindEmailBinding
import kr.co.lion.modigm.ui.login.vm.FindEmailViewModel
import kr.co.lion.modigm.util.FragmentName

class FindEmailFragment : Fragment(R.layout.fragment_find_email) {

    private val viewModel: FindEmailViewModel by viewModels()

    // --------------------------------- LC START ---------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바인딩
        val binding = FragmentFindEmailBinding.bind(view)

        initView(binding)
        settingObserver(binding)
    }

    // --------------------------------- LC START ---------------------------------

    // 초기 뷰 세팅
    private fun initView(binding: FragmentFindEmailBinding) {
        with(binding) {
            // 툴바
            with(toolbarFindEmail) {
                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

            // 다음 버튼
            with(buttonFindEmailNext) {
                setOnClickListener {
                    isClickable=false
                    // 유효성 검사
                    val validate = viewModel!!.validateInput()
                    if(!validate) {
                        isClickable=true
                        return@setOnClickListener
                    }
                    // 입력한 이름이 계정 정보와 일치하는지 확인하고 인증 문자 발송
                    viewModel!!.checkNameAndPhone(requireActivity())
                    isClickable=true
                }
            }

            // 번호 입력 시 자동으로 하이픈을 넣어줌
            textInputEditFindEmailPhone.addTextChangedListener(
                PhoneNumberFormattingTextWatcher()
            )
        }
    }

    private fun settingObserver(binding: FragmentFindEmailBinding){
        // 유효성 검사
        viewModel.nameError.observe(viewLifecycleOwner) {
            binding.textInputEditFindEmailName.error = it
            binding.textInputEditFindEmailName.requestFocus()
        }
        viewModel.phoneError.observe(viewLifecycleOwner) {
            binding.textInputEditFindEmailPhone.error = it
            binding.textInputEditFindEmailPhone.requestFocus()
        }

        // 다음으로 이동
        viewModel.isComplete.observe(viewLifecycleOwner) {
            if(it){
                moveToNext()
            }
        }
    }

    private fun moveToNext(){
        // 완료 여부는 초기화해서 popStackBack으로 돌아와도 문제 없게
        viewModel.isComplete.value = false
        // 다음 화면에 verficationId를 전달하여 번호 인증에 사용
        val fragment = FindEmailAuthFragment().apply {
            arguments = Bundle().apply {
                putString("verificationId", viewModel.verificationId.value)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.containerMain, fragment)
            .addToBackStack(FragmentName.FIND_EMAIL_AUTH.str)
            .commit()
    }

}

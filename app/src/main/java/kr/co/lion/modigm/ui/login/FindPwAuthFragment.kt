package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindPwAuthBinding
import kr.co.lion.modigm.ui.login.vm.FindPwAuthViewModel
import kr.co.lion.modigm.util.FragmentName

class FindPwAuthFragment : Fragment(R.layout.fragment_find_pw_auth) {

    private val viewModel: FindPwAuthViewModel by viewModels()
    lateinit var binding: FragmentFindPwAuthBinding

    private val verificationId by lazy {
        arguments?.getString("verificationId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_find_pw_auth, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.setVerificationId(verificationId?:"")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        settingObserver()
    }

    // 초기 뷰 세팅
    private fun initView() {
        with(binding) {
            // 툴바
            with(toolbarFindPwAuth) {
                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

            // 인증 버튼
            with(buttonFindPwAuthOK) {
                setOnClickListener {
                    isClickable = false
                    // 유효성 검사
                    val validate = viewModel.validateInput()
                    if(!validate){
                        isClickable = true
                        return@setOnClickListener
                    }

                    // 인증 번호 확인
                    viewModel.checkCodeAndFindEmail()
                    isClickable = true
                }
            }
        }
    }

    private fun settingObserver(){
        // 유효성 검사
        viewModel.inputCodeError.observe(viewLifecycleOwner) {
            binding.textInputEditFindPwPassCode.error = it
            binding.textInputEditFindPwPassCode.requestFocus()
        }
        // 인증번호 확인이 완료되면 다음으로 이동
        viewModel.isComplete.observe(viewLifecycleOwner){
            if(it){
                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, ResetPwFragment())
                    .addToBackStack(FragmentName.RESET_PW.str)
                    .commit()
            }
            binding.buttonFindPwAuthOK.isClickable = true
        }
    }
}
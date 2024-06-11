package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentResetPwBinding
import kr.co.lion.modigm.ui.login.vm.ResetPwViewModel

class ResetPwFragment : Fragment(R.layout.fragment_reset_pw) {

    private val viewModel: ResetPwViewModel by viewModels()

    lateinit var binding: FragmentResetPwBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reset_pw, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

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
            with(toolbarResetPw) {
                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

            // 다음 버튼
            with(binding.buttonFindPwResetOK) {
                setOnClickListener {
                    isClickable = false
                    // 유효성 검사
                    val validate = viewModel.validateInput()
                    if(!validate){
                        isClickable = true
                        return@setOnClickListener
                    }
                    // 비밀번호 변경
                    viewModel.changePassword()
                }
            }
        }
    }

    private fun settingObserver(){
        // 유효성 검사
        viewModel.newPasswordError.observe(viewLifecycleOwner) {
            binding.resetPwInputNewPw.error = it
            binding.resetPwInputNewPw.requestFocus()
        }
        viewModel.newPasswordCheckError.observe(viewLifecycleOwner) {
            binding.resetPwInputNewPwCheck.error = it
            binding.resetPwInputNewPwCheck.requestFocus()
        }

        // 비밀번호 변경 완료 여부
        viewModel.isComplete.observe(viewLifecycleOwner){
            if(it){
                parentFragmentManager.beginTransaction()
                    .replace(R.id.containerMain, OtherLoginFragment())
                    .commit()
            }
        }
    }

}
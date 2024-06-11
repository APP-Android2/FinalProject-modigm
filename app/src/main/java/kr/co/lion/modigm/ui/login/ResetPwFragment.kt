package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentResetPwBinding
import kr.co.lion.modigm.ui.login.vm.LoginViewModel

class ResetPwFragment : Fragment(R.layout.fragment_reset_pw) {

    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentResetPwBinding.bind(view)
        initView(binding)
    }

    // 초기 뷰 세팅
    private fun initView(binding: FragmentResetPwBinding) {
        with(binding) {


            // 툴바
            with(toolbarResetPw) {

                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

            // 다음 버튼
            with(binding) {
                buttonFindPwResetOK.setOnClickListener {
                    // 다이얼로그
                }
            }


        }
    }

}
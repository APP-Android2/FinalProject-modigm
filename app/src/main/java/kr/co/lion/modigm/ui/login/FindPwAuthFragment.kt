package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindPwAuthBinding
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.util.FragmentName

class FindPwAuthFragment : Fragment(R.layout.fragment_find_pw_auth) {

    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentFindPwAuthBinding.bind(view)
        initView(binding)
    }

    // 초기 뷰 세팅
    private fun initView(binding: FragmentFindPwAuthBinding) {
        with(binding) {


            // 툴바
            with(toolbarFindPwAuth) {

                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

            // 인증 버튼
            with(binding) {
                buttonFindPwAuthOK.setOnClickListener {
                    parentFragmentManager.commit {
                        replace(R.id.containerMain, ResetPwFragment())
                        addToBackStack(FragmentName.RESET_PW.str)
                    }
                }
            }
        }
    }
}
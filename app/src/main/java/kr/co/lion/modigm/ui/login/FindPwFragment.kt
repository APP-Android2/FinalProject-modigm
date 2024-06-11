package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindPwBinding
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.util.FragmentName

class FindPwFragment : Fragment(R.layout.fragment_find_pw) {

    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentFindPwBinding.bind(view)
        initView(binding)
    }

    // 초기 뷰 세팅
    private fun initView(binding: FragmentFindPwBinding) {
        with(binding) {


            // 툴바
            with(toolbarFindPw) {

                // 뒤로가기 버튼 클릭 시
                setNavigationOnClickListener {
                    parentFragmentManager.popBackStack()
                }
            }

            // 다음 버튼
            with(binding) {
                buttonFindPwNext.setOnClickListener {
                    parentFragmentManager.commit {
                        replace(R.id.containerMain, FindPwAuthFragment())
                        addToBackStack(FragmentName.FIND_PW_AUTH.str)
                    }
                }
            }


        }
    }
}
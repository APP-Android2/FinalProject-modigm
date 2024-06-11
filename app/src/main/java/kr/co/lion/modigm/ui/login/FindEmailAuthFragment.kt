package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentFindEmailAuthBinding
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.util.FragmentName

class FindEmailAuthFragment : Fragment(R.layout.fragment_find_email_auth) {

    private val viewModel: LoginViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentFindEmailAuthBinding.bind(view)
        initView(binding)
    }

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

            // 인증 버튼
            with(binding) {
                buttonFindEmailAuthOK.setOnClickListener {
                    val passCode = textInputEditFindPassCode.text


                    // 다이얼로그
                    showFindEmailDialog()

                }
            }
        }
    }

    // 이메일 다이얼로그 표시
    private fun showFindEmailDialog() {
        val dialog = CustomFindEmailDialog(requireContext())
        dialog.apply {
            setTitle("이메일 찾기")
            setMessage("회원님의 휴대전화로 가입하신 이메일은 ${""} 입니다.")
            setPositiveButton("확인", onClickListener = {
                parentFragmentManager.commit {
                    replace(R.id.containerMain,OtherLoginFragment())
                    addToBackStack(FragmentName.OTHER_LOGIN.str)

                }
            })
        }.show()
    }
}
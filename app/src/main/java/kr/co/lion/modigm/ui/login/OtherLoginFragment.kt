package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentOtherLoginBinding
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.util.FragmentName

class OtherLoginFragment : Fragment(R.layout.fragment_other_login) {

    private val viewModel: LoginViewModel by viewModels()

    // 프래그먼트 뷰 생성 시 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentOtherLoginBinding.bind(view)
        initView(binding) // 초기 뷰 설정
    }

    // 초기 뷰 설정 메소드
    private fun initView(binding: FragmentOtherLoginBinding) {
        with(binding){

            // 로그인 폼 상태 관찰 및 UI 업데이트
            viewModel.loginFormState.observe(viewLifecycleOwner, Observer {
                val loginState = it ?: return@Observer

                // 로그인 버튼 활성화 여부 설정
                buttonOtherLogin.isEnabled = loginState.isDataValid

                // 이메일, 비밀번호 입력 오류 메시지 설정
                textInputLayoutOtherEmail.error = loginState.emailError
                textInputLayoutOtherPassword.error = loginState.passwordError
            })

            // 이메일 필드 포커스 변경 시 검증
            textInputEditOtherEmail.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.loginDataChanged(
                        textInputEditOtherEmail.text.toString(),
                        textInputEditOtherPassword.text.toString()
                    )
                }
            }

            // 비밀번호 필드 포커스 변경 시 검증
            textInputEditOtherPassword.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.loginDataChanged(
                        textInputEditOtherEmail.text.toString(),
                        textInputEditOtherPassword.text.toString()
                    )
                }
            }

            // 로그인 버튼 클릭 시 메인 화면으로 이동
            buttonOtherLogin.setOnClickListener {
                parentFragmentManager.commit {
                    replace(R.id.containerMain, BottomNaviFragment())
                }
            }

            // 회원가입 버튼 클릭 시 회원가입 화면으로 이동 및 스택에 추가
            buttonOtherJoin.setOnClickListener {
                parentFragmentManager.commit {
                    replace(R.id.containerMain, JoinFragment())
                    addToBackStack(FragmentName.JOIN.str)
                }
            }
        }
    }
}



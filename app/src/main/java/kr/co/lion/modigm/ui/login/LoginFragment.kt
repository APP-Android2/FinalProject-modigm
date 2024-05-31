package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentLoginBinding
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.ui.study.StudyFragment
import kr.co.lion.modigm.util.FragmentName


class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentLoginBinding.bind(view)

        // 초기 뷰 세팅
        initView(binding)
    }


    // 초기 뷰 세팅
    private fun initView(binding: FragmentLoginBinding) {

        // 바인딩
        with(binding){
            // 배경

            // 로고

            // 카카오 로그인 버튼
            with(imageButtonLoginKakao){
                setOnClickListener{
                    parentFragmentManager.commit {

                        // 스터디 목록 화면으로 이동 (임시)
                        parentFragmentManager.commit {
                            replace(R.id.containerMain, BottomNaviFragment())
                        }
                    }
                }
            }

            // 다른 방법으로 로그인 텍스트 버튼
            with(textButtonLoginOther){
                setOnClickListener{

                    // 다른 방법으로 로그인 화면으로 이동
                    parentFragmentManager.commit {
                        replace(R.id.containerMain, OtherLoginFragment())
                        addToBackStack(FragmentName.OTHER_LOGIN.str)
                    }
                }
            }
        }
    }
}
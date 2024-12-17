package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.view.View
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinCompleteBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.login.EmailLoginFragment
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.util.JoinType

class JoinCompleteFragment : VBBaseFragment<FragmentJoinCompleteBinding>(FragmentJoinCompleteBinding::inflate) {

    private val joinType: JoinType by lazy {
        JoinType.getType(arguments?.getString("joinType")?:"")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingCompleteButton()
    }

    private fun settingCompleteButton(){
        binding.buttonJoinComplete.setOnClickListener {
            when(joinType){
                // 이메일 계정 회원가입인 경우에는 로그인 화면으로 돌아오기
                JoinType.EMAIL ->{
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.containerMain, EmailLoginFragment())
                        .commit()
                }
                // SNS 계정인 경우에는 메인으로 넘어가기
                else -> {
                    val bottomNaviFragment = BottomNaviFragment().apply {
                        arguments = Bundle().apply {
                            putString("joinType", joinType.provider)
                        }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.containerMain, bottomNaviFragment)
                        .commit()
                }
            }
        }
    }

}
package kr.co.lion.modigm.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.commit
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentOtherLoginBinding
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.util.FragmentName

class OtherLoginFragment : Fragment(R.layout.fragment_other_login) {

    // view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentOtherLoginBinding.bind(view)
        // 초기 뷰 세팅
        initView(binding)
    }


    // 초기 뷰 세팅
    fun initView(binding: FragmentOtherLoginBinding) {

        // 바인딩
        with(binding){

            // 로그인 버튼
            with(buttonOtherLogin) {


            }

            // 회원가입 버튼
            with(buttonOtherJoin) {
                setOnClickListener{
                    parentFragmentManager.commit {
                        replace(R.id.containerMain, JoinFragment())
                        addToBackStack(FragmentName.JOIN.str)
                    }
                }
            }
        }
    }

}
package kr.co.lion.modigm.ui.join

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinDuplicateBinding
import kr.co.lion.modigm.ui.login.LoginFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType

class JoinDuplicateFragment : Fragment() {

    val binding by lazy {
        FragmentJoinDuplicateBinding.inflate(layoutInflater)
    }

    private val provider by lazy {
        arguments?.getString("provider")
    }

    private val email by lazy {
        arguments?.getString("email")
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        settingToolBar()
        settingExistingUserInfo()
        settingButtonJoinDupLogin()

        return binding.root
    }

    private fun settingToolBar(){
        with(binding.toolbarJoinDup){
            setNavigationIcon(R.drawable.arrow_back_24px)
            setNavigationOnClickListener {
                parentFragmentManager.popBackStack(FragmentName.JOIN_DUPLICATE.str, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }
    }

    private fun settingExistingUserInfo(){
        binding.textViewJoinDupUser.text = email
        when(provider){
            JoinType.KAKAO.provider -> {
                binding.imageViewJoinDupServiceType.setImageResource(JoinType.KAKAO.icon)
            }
            JoinType.EMAIL.provider -> {
                binding.imageViewJoinDupServiceType.setImageResource(JoinType.EMAIL.icon)
            }
        }
    }

    private fun settingButtonJoinDupLogin(){
        // 로그인 화면으로 돌아가거나 추후 가능할 경우 sns로그인 api 연동 예정
        binding.buttonJoinDupLogin.setOnClickListener {
            // popBackStack에서 name값을 null로 넣어주면 기존의 backstack을 모두 없애준다.
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

            parentFragmentManager.beginTransaction()
                .replace(R.id.containerMain, LoginFragment())
                .commit()
        }
    }

}
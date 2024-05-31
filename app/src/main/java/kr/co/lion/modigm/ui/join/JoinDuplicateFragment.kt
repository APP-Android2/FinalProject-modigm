package kr.co.lion.modigm.ui.join

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinDuplicateBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.util.FragmentName

class JoinDuplicateFragment : Fragment() {

    val binding by lazy {
        FragmentJoinDuplicateBinding.inflate(layoutInflater)
    }

    val provider by lazy {
        arguments?.getString("provider")
    }

    val email by lazy {
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
                (requireActivity() as MainActivity).removeFragment(FragmentName.JOIN_DUPLICATE)
            }
        }
    }

    private fun settingExistingUserInfo(){
        binding.textViewJoinDupUser.text = email
        when(provider){
            "kakao" -> {
                binding.imageViewJoinDupServiceType.setImageResource(R.drawable.kakaotalk_sharing_btn_small)
            }
            "email" -> {
                binding.imageViewJoinDupServiceType.setImageResource(R.drawable.email_login_logo)
            }
        }
    }

    private fun settingButtonJoinDupLogin(){
        // 로그인 화면으로 돌아가거나 추후 가능할 경우 sns로그인 api 연동 예정
        binding.buttonJoinDupLogin.setOnClickListener {
            // popBackStack에서 name값을 null로 넣어주면 기존의 backstack을 모두 없애준다.
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            (requireActivity() as MainActivity).replaceFragment(FragmentName.LOGIN, false, true, null)
        }
    }

}
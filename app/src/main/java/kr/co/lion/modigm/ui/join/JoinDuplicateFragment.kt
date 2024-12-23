package kr.co.lion.modigm.ui.join

import android.os.Build.VERSION
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinDuplicateBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.join.vm.JoinStep1ViewModel
import kr.co.lion.modigm.ui.join.vm.JoinStep2ViewModel
import kr.co.lion.modigm.ui.join.vm.JoinStep3ViewModel
import kr.co.lion.modigm.ui.login.social.SocialLoginFragment
import kr.co.lion.modigm.util.JoinType

class JoinDuplicateFragment : VBBaseFragment<FragmentJoinDuplicateBinding>(FragmentJoinDuplicateBinding::inflate) {

    private val currentUser by lazy {
        if(VERSION.SDK_INT >= 33){
            arguments?.getParcelable("user", FirebaseUser::class.java)
        }else{
            arguments?.getParcelable("user")
        }
    }

    private val provider by lazy {
        arguments?.getString("provider")
    }

    private val email by lazy {
        arguments?.getString("email")
    }

    private val viewModelStep1: JoinStep1ViewModel by activityViewModels()
    private val viewModelStep2: JoinStep2ViewModel by activityViewModels()
    private val viewModelStep3: JoinStep3ViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inflate the layout for this fragment
        settingToolBar()
        settingExistingUserInfo()
        settingButtonJoinDupLogin()
        backButton()
    }

    private fun settingToolBar(){
        with(binding.toolbarJoinDup){
            setNavigationIcon(R.drawable.arrow_back_24px)
            setNavigationOnClickListener {
                backPressedCallback.handleOnBackPressed()
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
            JoinType.GITHUB.provider -> {
                binding.imageViewJoinDupServiceType.setImageResource(JoinType.GITHUB.icon)
            }
        }
    }

    private fun settingButtonJoinDupLogin(){
        // 로그인 화면으로 돌아가거나 추후 가능할 경우 sns로그인 api 연동 예정
        binding.buttonJoinDupLogin.setOnClickListener {
            // 로그인 화면으로 돌아가면 입력값 초기화 및 계정(이메일,SNS) 삭제
            viewModelStep1.reset()
            viewModelStep2.reset()
            viewModelStep3.reset()

            CoroutineScope(Dispatchers.IO).launch {
                currentUser?.delete()?.addOnSuccessListener {
                    Log.d("JoinDuplicateFragment", "deleteCurrentUser: 인증 정보 삭제 성공")
                }
            }

            // popBackStack에서 name값을 null로 넣어주면 기존의 backstack을 모두 없애준다.
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

            parentFragmentManager.beginTransaction()
                .replace(R.id.containerMain, SocialLoginFragment())
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 백버튼 콜백 제거
        backPressedCallback.remove()
    }

    // 백버튼 콜백
    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {

                val joinFragment = parentFragmentManager.fragments[0]

                parentFragmentManager.commit {
                    joinFragment?.let {
                        show(joinFragment)
                    }
                    hide(this@JoinDuplicateFragment)
                }

            }
        }
    }

    // 백버튼 종료 동작
    private fun backButton() {
        // 백버튼 콜백을 안전하게 추가
        backPressedCallback.let { callback ->
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        }
    }

}
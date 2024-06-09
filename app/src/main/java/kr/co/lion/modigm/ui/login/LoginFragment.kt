package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.common.KakaoSdk
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentLoginBinding
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.vm.LoginResult
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()  // LoginViewModel 인스턴스 생성

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentLoginBinding.bind(view)  // 뷰 바인딩 설정

        // Kakao SDK 초기화
        KakaoSdk.init(requireContext(), BuildConfig.KAKAO_NATIVE_APP_KEY)

        auth = FirebaseAuth.getInstance()

        // 초기 뷰 설정
        initView(binding)

        // ViewModel의 데이터 변경 관찰
        observeViewModel()
    }

    // ViewModel의 데이터 변경을 관찰하는 메서드
    private fun observeViewModel() {
        // 카카오 로그인 데이터 관찰
        viewModel.kakaoLoginResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is LoginResult.Loading -> {
                    Log.i("LoginFragment", "카카오 로그인 진행 중...")
                }
                is LoginResult.Success -> {
                    Log.i("LoginFragment", "카카오 로그인 성공")
                    // 스터디 목록 화면으로 이동
                    navigateToBottomNaviFragment()
                }
                is LoginResult.NeedSignUp -> {
                    Log.i("LoginFragment", "카카오 로그인 성공, 회원가입 필요")
                    viewModel.kakaoCustomToken.observe(viewLifecycleOwner, Observer { token ->
                        Log.i("LoginFragment", "커스텀 토큰 업데이트됨: $token")
                        val joinType = viewModel.joinType.value ?: JoinType.KAKAO
                        if (token != null) {
                            navigateToJoinFragment(token, joinType)
                        }
                    })
                }
                is LoginResult.Error -> {
                    Log.e("LoginFragment", "카카오 로그인 실패", result.exception)
                }
            }
        })

        // 깃허브 로그인 데이터 관찰
        viewModel.githubLoginResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is LoginResult.Loading -> {
                    Log.i("LoginFragment", "깃허브 로그인 진행 중...")
                }
                is LoginResult.Success -> {
                    Log.i("LoginFragment", "깃허브 로그인 성공")
                    // 스터디 목록 화면으로 이동
                    navigateToBottomNaviFragment()
                }
                is LoginResult.NeedSignUp -> {
                    Log.i("LoginFragment", "깃허브 로그인 성공, 회원가입 필요")
                    viewModel.credential.observe(viewLifecycleOwner, Observer { credential ->
                        Log.i("LoginFragment", "자격 증명 업데이트됨: $credential")
                        val joinType = viewModel.joinType.value ?: JoinType.GITHUB
                        if (credential != null) {
                            navigateToJoinFragment(credential, joinType)
                        }
                    })
                }
                is LoginResult.Error -> {
                    Log.e("LoginFragment", "깃허브 로그인 실패", result.exception)
                }
            }
        })
    }

    // 초기 뷰 설정
    private fun initView(binding: FragmentLoginBinding) {
        // 카카오 로그인 버튼 클릭 리스너 설정
        binding.imageButtonLoginKakao.setOnClickListener {
            Log.i("LoginFragment", "카카오 로그인 버튼 클릭됨")
            val autoLogin = binding.checkboxAutoLogin.isChecked
            viewModel.loginWithKakao(requireContext(), autoLogin)
        }

        // 깃허브 로그인 버튼 클릭 리스너 설정
        binding.imageButtonLoginGithub.setOnClickListener {
            Log.i("LoginFragment", "깃허브 로그인 버튼 클릭됨")
            val autoLogin = binding.checkboxAutoLogin.isChecked
            viewModel.loginWithGithub(requireContext(), autoLogin)
        }

        // 다른 방법으로 로그인 버튼 클릭 리스너 설정
        binding.textButtonLoginOther.setOnClickListener {
            Log.i("LoginFragment", "다른 방법으로 로그인 버튼 클릭됨")
            parentFragmentManager.commit {
                replace(R.id.containerMain, OtherLoginFragment())
                addToBackStack(null)
            }
        }
    }

    // 회원가입 화면으로 이동하는 메서드
    private fun navigateToJoinFragment(token: Any, joinType: JoinType) {

        // 회원가입으로 넘겨줄 데이터
        val bundle = Bundle().apply {
            when (token) {
                is String -> {
                    putString("customToken", token)
                }
                is AuthCredential -> {
                    putParcelable("credential", token)
                }
            }
            putString("joinType", joinType.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, JoinFragment().apply { arguments = bundle })
            addToBackStack(FragmentName.JOIN.str)
        }
    }
    private fun navigateToBottomNaviFragment() {
        parentFragmentManager.commit {
            replace(R.id.containerMain, BottomNaviFragment())
            addToBackStack(FragmentName.BOTTOM_NAVI.str)
        }
    }
}

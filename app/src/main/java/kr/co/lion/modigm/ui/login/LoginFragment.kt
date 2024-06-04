package kr.co.lion.modigm.ui.login

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.common.KakaoSdk
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentLoginBinding
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.vm.LoginResult
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
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
        viewModel.kakaoLoginResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is LoginResult.Loading -> {
                    Log.i("LoginFragment", "카카오 로그인 중...")
                }
                is LoginResult.Success -> {
                    Log.i("LoginFragment", "카카오 로그인 성공")

                    // 로그인 성공 후 커스텀 토큰과 JoinType을 받아 회원가입 화면으로 이동
                    viewModel.customToken.observe(viewLifecycleOwner, Observer { token ->
                        Log.i("LoginFragment", "Custom Token Updated: $token")

                        val customToken = viewModel.customToken.value
                        val joinType = JoinType.KAKAO
                        if (customToken != null) {
                            navigateToJoinFragment(customToken, joinType)
                        }
                    })

                }
                is LoginResult.Error -> {
                    Log.e("LoginFragment", "카카오 로그인 실패", result.exception)
                }
            }
        })

        viewModel.githubLoginResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is LoginResult.Loading -> {
                    Log.i("LoginFragment", "깃허브 로그인 중...")
                }
                is LoginResult.Success -> {
                    Log.i("LoginFragment", "깃허브 로그인 성공")

                    // 로그인 성공 후 커스텀 토큰과 JoinType을 받아 회원가입 화면으로 이동
                    viewModel.customToken.observe(viewLifecycleOwner, Observer { token ->
                        Log.i("LoginFragment", "Custom Token Updated: $token")
                        val customToken = viewModel.customToken.value
                        val joinType = JoinType.GITHUB
                        if (customToken != null) {
                            navigateToJoinFragment(customToken, joinType)
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
            viewModel.loginWithKakao(requireContext())
        }

        // 깃허브 로그인 버튼 클릭 리스너 설정
        binding.imageButtonLoginGithub.setOnClickListener {
            Log.i("LoginFragment", "깃허브 로그인 버튼 클릭됨")
            viewModel.loginWithGithub(requireContext())
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
    private fun navigateToJoinFragment(customToken: String, joinType: JoinType) {
        // 로그 추가
        Log.d("LoginFragment", "navigateToJoinFragment - customToken: $customToken, joinType: ${joinType.provider}")

        val bundle = Bundle().apply {
            putString("customToken", customToken)
            putString("joinType", joinType.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, JoinFragment().apply { arguments = bundle })
        }
    }
}

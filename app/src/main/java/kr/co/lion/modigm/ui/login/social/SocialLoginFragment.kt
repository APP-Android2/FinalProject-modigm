package kr.co.lion.modigm.ui.login.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.kakao.sdk.common.KakaoSdk
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.CustomLoginErrorDialog
import kr.co.lion.modigm.ui.login.email.EmailLoginFragment
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.ui.study.CustomExitDialogFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.showLoginSnackBar

class SocialLoginFragment : Fragment() {

    private val viewModel: SocialLoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KakaoSdk.init(requireContext(), BuildConfig.KAKAO_NATIVE_APP_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val isLoading by viewModel.isLoading.observeAsState(false)
                val kakaoLoginResult by viewModel.kakaoLoginResult.observeAsState(false)
                val githubLoginResult by viewModel.githubLoginResult.observeAsState(false)
                val kakaoJoinResult by viewModel.kakaoJoinResult.observeAsState(false)
                val githubJoinResult by viewModel.githubJoinResult.observeAsState(false)
                val emailLoginResult by viewModel.emailAutoLoginResult.observeAsState(false)
                val kakaoLoginError by viewModel.kakaoLoginError.observeAsState()
                val githubLoginError by viewModel.githubLoginError.observeAsState()
                val autoLoginError by viewModel.autoLoginError.observeAsState()

                LaunchedEffect(kakaoLoginResult) {
                    if (kakaoLoginResult) {
                        navigateToBottomNaviFragment(JoinType.KAKAO)
                    }
                }

                LaunchedEffect(githubLoginResult) {
                    if (githubLoginResult) {
                        navigateToBottomNaviFragment(JoinType.GITHUB)
                    }
                }

                LaunchedEffect(kakaoJoinResult) {
                    if (kakaoJoinResult) {
                        navigateToJoinFragment(JoinType.KAKAO)
                    }
                }

                LaunchedEffect(githubJoinResult) {
                    if (githubJoinResult) {
                        navigateToJoinFragment(JoinType.GITHUB)
                    }
                }

                LaunchedEffect(emailLoginResult) {
                    if (emailLoginResult) {
                        navigateToBottomNaviFragment(JoinType.EMAIL)
                    }
                }

                LaunchedEffect(kakaoLoginError) {
                    kakaoLoginError?.let { error ->
                        showLoginErrorDialog(error)
                    }
                }

                LaunchedEffect(githubLoginError) {
                    githubLoginError?.let { error ->
                        showLoginErrorDialog(error)
                    }
                }

                LaunchedEffect(autoLoginError) {
                    autoLoginError?.let { error ->
                        requireActivity().showLoginSnackBar(error.message.toString(), null)
                    }
                }

                SocialLoginScreen(
                    isLoading = isLoading,
                    onKakaoLoginClick = { viewModel.kakaoLogin(requireContext()) },
                    onGithubLoginClick = { viewModel.githubLogin(requireActivity()) },
                    onNavigateEmailLoginClick = { navigateToEmailLoginFragment() },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        autoLogin()
        backButton()
    }

    private fun autoLogin() {
        viewModel.tryAutoLogin()
    }

    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            private var doubleClickStatus = false
            override fun handleOnBackPressed() {
                if (doubleClickStatus) {
                    showAppExitDialog()
                } else {
                    doubleClickStatus = true
                    requireActivity().showLoginSnackBar("한 번 더 누르면 앱이 종료됩니다.", null)
                    view?.postDelayed({ doubleClickStatus = false }, 2000)
                }
            }
        }
    }

    private fun showAppExitDialog() {
        val dialog = CustomExitDialogFragment()
        dialog.show(parentFragmentManager, "AppExitDialog")
    }

    private fun backButton() {
        backPressedCallback.let { callback ->
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backPressedCallback.remove()
        viewModel.clearViewModelData()
    }

    private fun showLoginErrorDialog(e: Throwable) {
        val message = if (e.message != null) {
            e.message.toString()
        } else {
            "알 수 없는 오류!"
        }
        showLoginErrorDialog(message)
    }

    private fun showLoginErrorDialog(message: String) {
        val dialog = CustomLoginErrorDialog(requireContext())
        with(dialog) {
            setTitle("오류")
            setMessage(message)
            setPositiveButton("확인") {
                dismiss()
            }
            show()
        }
    }

    private fun navigateToJoinFragment(joinType: JoinType) {
        val bundle = Bundle().apply {
            putString("joinType", joinType.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, JoinFragment().apply { arguments = bundle })
            addToBackStack(FragmentName.JOIN.str)
        }
    }

    private fun navigateToBottomNaviFragment(joinType: JoinType) {
        viewModel.registerFcmTokenToServer()
        val bundle = Bundle().apply {
            putString("joinType", joinType.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, BottomNaviFragment().apply { arguments = bundle })
            addToBackStack(FragmentName.BOTTOM_NAVI.str)
        }
    }

    private fun navigateToEmailLoginFragment() {
        parentFragmentManager.commit {
            replace(R.id.containerMain, EmailLoginFragment())
            addToBackStack(FragmentName.EMAIL_LOGIN.str)
        }
    }
}
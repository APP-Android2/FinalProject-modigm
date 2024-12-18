package kr.co.lion.modigm.ui.login.social

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.common.KakaoSdk
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentSocialLoginBinding
import kr.co.lion.modigm.ui.VBBaseFragment
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.CustomLoginErrorDialog
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.ui.study.CustomExitDialogFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs
import kr.co.lion.modigm.util.showLoginSnackBar

class SocialLoginFragment :
    VBBaseFragment<FragmentSocialLoginBinding>(FragmentSocialLoginBinding::inflate) {

    // 뷰모델
    private val viewModel: LoginViewModel by viewModels()

    // 태그
    private val logTag by lazy { SocialLoginFragment::class.simpleName }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kakao SDK 초기화
        KakaoSdk.init(requireContext(), BuildConfig.KAKAO_NATIVE_APP_KEY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        autoLogin()

        observeViewModel()

        backButton()
    }

    private fun initView() {
        val initializer = SocialLoginViewInitializer(this, binding, viewModel)
        initializer.apply {
            initBlurBackground()
            initKakaoLoginButton()
            initGithubLoginButton()
            initEmailLoginButton()
            initScrollArrow()
        }
    }

    private fun autoLogin() {
        val autoLogin = prefs.getBoolean("autoLogin")
        if (autoLogin) {
            showLoginLoading()
            viewModel.tryAutoLogin()
        }
    }

    private fun observeViewModel() {
        // 카카오 로그인 데이터 관찰
        viewModel.kakaoLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                hideLoginLoading()
                Log.i(logTag, "카카오 로그인 성공")
                val joinType = JoinType.KAKAO

                // FCM 토큰 등록
                val userIdx = prefs.getInt("currentUserIdx", 0)
                if (userIdx > 0) {
                    registerFcmTokenToServer(userIdx)
                }

                goToBottomNaviFragment(joinType)
            }
        }
        // 깃허브 로그인 데이터 관찰
        viewModel.githubLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                hideLoginLoading()
                Log.i(logTag, "깃허브 로그인 성공")
                val joinType = JoinType.GITHUB

                // FCM 토큰 등록
                val userIdx = prefs.getInt("currentUserIdx", 0)
                if (userIdx > 0) {
                    registerFcmTokenToServer(userIdx)
                }

                goToBottomNaviFragment(joinType)
            }
        }
        // 카카오 회원가입 데이터 관찰
        viewModel.kakaoJoinResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                hideLoginLoading()
                Log.i(logTag, "카카오 회원가입으로 이동")
                val joinType = JoinType.KAKAO
                goToJoinFragment(joinType)
            }
        }
        // 깃허브 회원가입 데이터 관찰
        viewModel.githubJoinResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                hideLoginLoading()
                Log.i(logTag, "깃허브 회원가입으로 이동")
                val joinType = JoinType.GITHUB

                goToJoinFragment(joinType)
            }
        }
        // 이메일 자동로그인 데이터 관찰
        viewModel.emailAutoLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                hideLoginLoading()
                Log.i(logTag, "이메일 로그인 성공")

                val userIdx = prefs.getInt("currentUserIdx", 0)
                Log.d("SocialLoginFragment", "UserIdx after login: $userIdx")  // UserIdx 로그 추가

                // FCM 토큰 등록
                if (userIdx > 0) {
                    Log.d("SocialLoginFragment", "Calling registerFcmTokenToServer")  // 로그 추가
                    registerFcmTokenToServer(userIdx)
                } else {
                    Log.e("SocialLoginFragment", "UserIdx is not valid")
                }

                val joinType = JoinType.EMAIL
                goToBottomNaviFragment(joinType)
            }
        }
        // 카카오 로그인 실패 시 에러 처리
        viewModel.kakaoLoginError.observe(viewLifecycleOwner) { e ->
            if (e != null) {
                hideLoginLoading()
                showLoginErrorDialog(e)
            }
        }
        // 깃허브 로그인 실패 시 에러 처리
        viewModel.githubLoginError.observe(viewLifecycleOwner) { e ->
            if (e != null) {
                hideLoginLoading()
                showLoginErrorDialog(e)
            }
        }
        viewModel.autoLoginError.observe(viewLifecycleOwner) { e ->
            if (e != null) {
                hideLoginLoading()
                requireActivity().showLoginSnackBar(e.message.toString(), null)
            }
        }
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

    private fun goToJoinFragment(joinType: JoinType) {
        // 회원가입으로 넘겨줄 데이터
        val bundle = Bundle().apply {
            putString("joinType", joinType.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, JoinFragment().apply { arguments = bundle })
            addToBackStack(FragmentName.JOIN.str)
        }
    }

    private fun goToBottomNaviFragment(joinType: JoinType) {

        val bundle = Bundle().apply {
            putString("joinType", joinType.provider)
        }
        parentFragmentManager.commit {
            replace(R.id.containerMain, BottomNaviFragment().apply { arguments = bundle })
            addToBackStack(FragmentName.BOTTOM_NAVI.str)
        }
    }

    // 로딩 화면 표시
    private fun showLoginLoading() {
        with(binding) {
            layoutLoginLoadingBackground.visibility = View.VISIBLE
        }
    }

    // 로딩 화면 숨기기
    private fun hideLoginLoading() {
        with(binding) {
            layoutLoginLoadingBackground.visibility = View.GONE
        }
    }

    // FCM 토큰을 가져와 서버에 등록
    private fun registerFcmTokenToServer(userIdx: Int) {
        Log.d("SocialLoginFragment", "Attempting to fetch FCM Token...")
        FirebaseMessaging.getInstance().deleteToken() // 기존 토큰 삭제 (필요한 경우)
            .addOnCompleteListener { deleteTask ->
                if (!deleteTask.isSuccessful) {
                    Log.e("SocialLoginFragment", "FCM 토큰 삭제 실패", deleteTask.exception)
                    return@addOnCompleteListener
                }

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        // 여기에서 실패 원인을 로그로 찍음
                        Log.e(
                            "SocialLoginFragment",
                            "Fetching FCM registration token failed",
                            task.exception
                        )
                        return@addOnCompleteListener
                    }

                    val token = task.result
                    Log.d("SocialLoginFragment", "FCM Token: $token")

                    // 토큰이 null이 아닌지 확인하고 서버에 등록하는 로직
                    if (token != null) {
                        Log.d("SocialLoginFragment", "FCM Token: $token")
                        // FCM 토큰을 ViewModel을 통해 서버에 등록
                        viewModel.registerFcmToken(userIdx, token)
                    } else {
                        Log.e("SocialLoginFragment", "FCM Token is null")
                    }
                }
            }
    }

    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            private var doubleClickStatus = false

            override fun handleOnBackPressed() {
                // 백버튼을 두 번 눌렀을 때 앱 종료
                if (doubleClickStatus) showAppExitDialog() else return
                if (!doubleClickStatus) {
                    doubleClickStatus = true
                    // Snackbar를 표시하여 사용자에게 알림
                    requireActivity().showLoginSnackBar("한 번 더 누르면 앱이 종료됩니다.", null)
                    // 2초 후에 doubleBackToExitPressedOnce 플래그 초기화
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
        viewModel.clearData()
    }
}
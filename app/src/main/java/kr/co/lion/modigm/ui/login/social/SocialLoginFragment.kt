package kr.co.lion.modigm.ui.login.social

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.common.KakaoSdk
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentSocialLoginBinding
import kr.co.lion.modigm.ui.DBBaseFragment
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.CustomLoginErrorDialog
import kr.co.lion.modigm.ui.login.EmailLoginFragment
import kr.co.lion.modigm.ui.login.vm.LoginViewModel
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.ui.study.CustomExitDialogFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs
import kr.co.lion.modigm.util.showLoginSnackBar

class SocialLoginFragment :
    DBBaseFragment<FragmentSocialLoginBinding>(R.layout.fragment_social_login) {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KakaoSdk.init(requireContext(), BuildConfig.KAKAO_NATIVE_APP_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = super.onCreateView(inflater, container, savedInstanceState)
        binding.apply {
            loginViewModel = viewModel
        }
        binding.imageViewSocialLoginBackground.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BackgroundImage()
            }
        }

        binding.imageViewLoginLogo.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LogoImage()
            }
        }

        binding.textViewLoginTitle.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LoginTitle()
            }

        }
        binding.textViewLoginSubTitle.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LoginSubTitleText()
            }
        }

        binding.imageButtonLoginKakao.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KakaoLoginButton(
                    onClick = { viewModel.kakaoLogin(requireContext()) }

                )
            }
        }

        binding.imageButtonLoginGithub.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                GithubLoginButton(
                    onClick = { viewModel.githubLogin(requireActivity()) }
                )
            }
        }

        binding.textButtonLoginEmail.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                EmailLoginButton(
                    onClick = {
                        parentFragmentManager.commit {
                            replace<EmailLoginFragment>(R.id.containerMain)
                            addToBackStack(FragmentName.EMAIL_LOGIN.str)
                        }
                    }
                )
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        autoLogin()

        observeViewModel()

        backButton()
    }

    private fun initView() {
        val initializer = SocialLoginViewInitializer(
            binding = binding,
        )
        initializer.apply {
            initScrollArrow()
        }
    }

    private fun autoLogin() {
        viewModel.tryAutoLogin()
    }

    private fun observeViewModel() {
        // 카카오 로그인 데이터 관찰
        viewModel.kakaoLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                val joinType = JoinType.KAKAO

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
                val joinType = JoinType.GITHUB

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
                val joinType = JoinType.KAKAO
                goToJoinFragment(joinType)
            }
        }
        // 깃허브 회원가입 데이터 관찰
        viewModel.githubJoinResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                val joinType = JoinType.GITHUB
                goToJoinFragment(joinType)
            }
        }
        // 이메일 자동로그인 데이터 관찰
        viewModel.emailAutoLoginResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                val userIdx = prefs.getInt("currentUserIdx", 0)

                if (userIdx > 0) {
                    registerFcmTokenToServer(userIdx)
                }
                val joinType = JoinType.EMAIL
                goToBottomNaviFragment(joinType)
            }
        }
        // 카카오 로그인 실패 시 에러 처리
        viewModel.kakaoLoginError.observe(viewLifecycleOwner) { e ->
            if (e != null) {
                showLoginErrorDialog(e)
            }
        }
        // 깃허브 로그인 실패 시 에러 처리
        viewModel.githubLoginError.observe(viewLifecycleOwner) { e ->
            if (e != null) {
                showLoginErrorDialog(e)
            }
        }
        viewModel.autoLoginError.observe(viewLifecycleOwner) { e ->
            if (e != null) {
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
                if (doubleClickStatus) {
                    showAppExitDialog()
                } else {
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
        viewModel.clearViewModelData()
    }

    @Composable
    fun BackgroundImage() {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 배경 이미지
            Image(
                painter = painterResource(id = R.drawable.background_login2),
                contentDescription = "Social Login Background",
                modifier = Modifier
                    .fillMaxSize()
                    .blur(5.dp),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
        }
    }

    @Composable
    fun LogoImage() {
        Image(
            painter = painterResource(id = R.drawable.logo_modigm),
            contentDescription = "Login Logo",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }

    @Composable
    fun LoginTitle() {
        Text(
            text = "모우다임",
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .padding(top = 60.dp),
            fontFamily = FontFamily(Font(R.font.one_mobile_pop_otf)),
            fontSize = 70.sp,
            color = Color.White,
            fontWeight = FontWeight.Normal
        )
    }

    @Composable
    fun LoginSubTitleText() {
        Text(
            text = "개발자 스터디의 새로운 패러다임",
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .padding(top = 10.dp),
            fontFamily = FontFamily(Font(R.font.one_mobile_pop_otf)),
            fontSize = 22.sp,
            color = Color.White,
            fontWeight = FontWeight.Normal
        )
    }

    @Composable
    fun KakaoLoginButton(onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(0.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.kakao_login_large_wide),
                contentDescription = "카카오 로그인",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentScale = ContentScale.FillBounds
            )
        }
    }

    @Composable
    fun GithubLoginButton(onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_github_logo),
                    contentDescription = "깃허브 로고",
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterStart),
                    tint = Color.White
                )
                // 텍스트: 가운데 정렬
                Text(
                    text = "깃허브 로그인",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 12.dp)
                )
            }
        }
    }

    @Composable
    fun EmailLoginButton(onClick: () -> Unit) {
        Column (modifier = Modifier
            .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            TextButton(
                onClick = onClick,
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .padding(top = 20.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(0.dp),
            ) {
                Text(
                    text = "다른 방법으로 로그인",
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.one_mobile_pop_otf)),
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }
        }
    }
}
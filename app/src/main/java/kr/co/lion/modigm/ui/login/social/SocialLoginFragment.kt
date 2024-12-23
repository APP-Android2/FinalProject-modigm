package kr.co.lion.modigm.ui.login.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.kakao.sdk.common.KakaoSdk
import kotlinx.coroutines.launch
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.join.JoinFragment
import kr.co.lion.modigm.ui.login.CustomLoginErrorDialog
import kr.co.lion.modigm.ui.login.email.EmailLoginFragment
import kr.co.lion.modigm.ui.login.social.viewmodel.SocialLoginViewModel
import kr.co.lion.modigm.ui.study.BottomNaviFragment
import kr.co.lion.modigm.ui.study.CustomExitDialogFragment
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs
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
                SocialLoginScreen(viewModel = viewModel)
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
    fun SocialLoginScreen(viewModel: SocialLoginViewModel) {
        val scrollState = rememberScrollState()
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
                val joinType = JoinType.KAKAO

                val userIdx = prefs.getInt("currentUserIdx", 0)
                if (userIdx > 0) {
                    viewModel.registerFcmTokenToServer(userIdx)
                }
                goToBottomNaviFragment(joinType)
            }
        }

        LaunchedEffect(githubLoginResult) {
            if (githubLoginResult) {
                val joinType = JoinType.GITHUB

                val userIdx = prefs.getInt("currentUserIdx", 0)
                if (userIdx > 0) {
                    viewModel.registerFcmTokenToServer(userIdx)
                }

                goToBottomNaviFragment(joinType)
            }
        }

        LaunchedEffect(kakaoJoinResult) {
            if (kakaoJoinResult) {
                val joinType = JoinType.KAKAO
                goToJoinFragment(joinType)
            }
        }

        LaunchedEffect(githubJoinResult) {
            if (githubJoinResult) {
                val joinType = JoinType.GITHUB
                goToJoinFragment(joinType)
            }
        }

        LaunchedEffect(emailLoginResult) {
            if (emailLoginResult) {
                val userIdx = prefs.getInt("currentUserIdx", 0)

                if (userIdx > 0) {
                    viewModel.registerFcmTokenToServer(userIdx)
                }
                val joinType = JoinType.EMAIL
                goToBottomNaviFragment(joinType)
            }
        }

        LaunchedEffect(kakaoLoginError) {
            kakaoLoginError?.let { e ->
                showLoginErrorDialog(e)
            }
        }

        LaunchedEffect(githubLoginError) {
            githubLoginError?.let { e ->
                showLoginErrorDialog(e)
            }
        }

        LaunchedEffect(autoLoginError) {
            autoLoginError?.let { e ->
                requireActivity().showLoginSnackBar(e.message.toString(), null)
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 배경 이미지
            BackgroundImage()

            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxSize()
                    .padding(30.dp)
            ) {
                LogoImage()
                LoginTitle()
                LoginSubTitleText()
                KakaoLoginButton(onClick = { viewModel.kakaoLogin(requireContext()) })
                GithubLoginButton(onClick = { viewModel.githubLogin(requireActivity()) })
                EmailLoginButton(onClick = {
                    parentFragmentManager.commit {
                        replace(R.id.containerMain, EmailLoginFragment())
                        addToBackStack(FragmentName.EMAIL_LOGIN.str)
                    }
                })
            }

            ScrollArrow(
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.BottomCenter))

            LoadingBackground(isLoading = isLoading)
        }
    }


    @Composable
    fun BackgroundImage() {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
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
                .fillMaxSize()
                .padding(top = 100.dp)
                .size(150.dp, 150.dp),

            alignment = Alignment.Center
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

    @Composable
    fun ScrollArrow(
        scrollState: ScrollState,
        modifier: Modifier = Modifier
    ) {
        val isVisible = remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(scrollState) {
            snapshotFlow { scrollState.canScrollForward }
                .collect { canScrollForward ->
                    isVisible.value = canScrollForward
                }
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (isVisible.value) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_down_24px),
                    contentDescription = "Scroll Arrow",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                        .animateEnterExit(),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }
    }

    @Composable
    fun Modifier.animateEnterExit(): Modifier {
        val infiniteTransition = rememberInfiniteTransition(label = "")
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )
        return this.offset(y = offsetY.dp)
    }

    @Composable
    fun LoadingBackground(isLoading: Boolean) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
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
}
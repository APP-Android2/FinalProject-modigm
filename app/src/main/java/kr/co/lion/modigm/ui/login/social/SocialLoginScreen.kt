package kr.co.lion.modigm.ui.login.social

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.login.social.component.GithubLoginButton
import kr.co.lion.modigm.ui.login.social.component.KakaoLoginButton
import kr.co.lion.modigm.ui.login.social.component.NavigateToEmailLoginButton
import kr.co.lion.modigm.ui.login.social.component.SocialLoginLoading
import kr.co.lion.modigm.util.JoinType

@Composable
fun SocialLoginScreen(
    isLoading: Boolean,
    kakaoLoginResult: Boolean,
    githubLoginResult: Boolean,
    kakaoJoinResult: Boolean,
    githubJoinResult: Boolean,
    emailLoginResult: Boolean,
    kakaoLoginError: Throwable?,
    githubLoginError: Throwable?,
    autoLoginError: Throwable?,
    onKakaoLoginClick: () -> Unit,
    onGithubLoginClick: () -> Unit,
    onNavigateEmailLoginClick: () -> Unit,
    onNavigateToJoinFragment: (JoinType) -> Unit,
    onNavigateToBottomNaviFragment: (JoinType) -> Unit,
    showLoginErrorDialog: (Throwable) -> Unit,
    showSnackBar: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(kakaoLoginResult) {
        if (kakaoLoginResult) {
            onNavigateToBottomNaviFragment(JoinType.KAKAO)
        }
    }

    LaunchedEffect(githubLoginResult) {
        if (githubLoginResult) {
            onNavigateToBottomNaviFragment(JoinType.GITHUB)
        }
    }

    LaunchedEffect(kakaoJoinResult) {
        if (kakaoJoinResult) {
            onNavigateToJoinFragment(JoinType.KAKAO)
        }
    }

    LaunchedEffect(githubJoinResult) {
        if (githubJoinResult) {
            onNavigateToJoinFragment(JoinType.GITHUB)
        }
    }

    LaunchedEffect(emailLoginResult) {
        if (emailLoginResult) {
            onNavigateToBottomNaviFragment(JoinType.EMAIL)
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
            showSnackBar(error.message.toString())
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BackgroundImage()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(30.dp)
        ) {
            LogoImage()
            SocialLoginTitle()
            SocialLoginSubTitle()
            KakaoLoginButton(onClick = onKakaoLoginClick)
            GithubLoginButton(onClick = onGithubLoginClick)
            NavigateToEmailLoginButton(onClick = onNavigateEmailLoginClick)
        }
        SocialLoginScrollArrow(
            scrollState = scrollState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        SocialLoginLoading(isLoading = isLoading)
    }
}

@Composable
fun BackgroundImage() {
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
fun SocialLoginTitle() {
    Text(
        text = "모우다임",
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .padding(top = 60.dp),
        fontFamily = FontFamily(Font(R.font.one_mobile_pop_otf)),
        fontSize = dpToSp(70.dp),
        color = Color.White,
        fontWeight = FontWeight.Normal
    )
}

@Composable
fun SocialLoginSubTitle() {
    Text(
        text = "개발자 스터디의 새로운 패러다임",
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .padding(top = 10.dp),
        fontFamily = FontFamily(Font(R.font.one_mobile_pop_otf)),
        fontSize = dpToSp(22.dp),
        color = Color.White,
        fontWeight = FontWeight.Normal
    )
}

@Composable
fun SocialLoginScrollArrow(
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
        ),
        label = ""
    )
    return this.offset(y = offsetY.dp)
}

@Composable
fun dpToSp(dp: Dp) = with(LocalDensity.current) { dp.toSp() }
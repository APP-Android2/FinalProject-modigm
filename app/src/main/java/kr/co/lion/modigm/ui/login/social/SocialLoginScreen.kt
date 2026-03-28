package kr.co.lion.modigm.ui.login.social

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.login.component.LoginLoading
import kr.co.lion.modigm.ui.login.component.ScrollArrow
import kr.co.lion.modigm.ui.login.component.SocialLoginButton
import kr.co.lion.modigm.ui.login.util.dpToSp

@Composable
fun SocialLoginScreen(
    isLoading: Boolean,
    onKakaoLoginClick: () -> Unit,
    onGithubLoginClick: () -> Unit,
    onNavigateEmailLoginClick: () -> Unit,

    ) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
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
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(30.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_modigm),
                contentDescription = "로그인 로고 이미지",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp)
                    .size(150.dp, 150.dp),
                alignment = Alignment.Center
            )
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

            SocialLoginButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                onClick = onKakaoLoginClick,
                content = {
                    Image(
                        painter = painterResource(id = R.drawable.kakao_login_large_wide),
                        contentDescription = "카카오 로그인",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        contentScale = ContentScale.FillBounds
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            )
            SocialLoginButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp)
                    .height(48.dp),
                onClick = onGithubLoginClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterStart),
                            painter = painterResource(id = R.drawable.icon_github_logo),
                            contentDescription = "깃허브 로고 아이콘",
                            tint = Color.White
                        )
                        Text(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(x = 12.dp),
                            text = "깃허브 로그인",
                            fontSize = dpToSp(16.dp),
                            color = Color.White
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextButton(
                    onClick = onNavigateEmailLoginClick,
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
                        fontSize = dpToSp(16.dp),
                        fontFamily = FontFamily(Font(R.font.one_mobile_pop_otf)),
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }
            }
        }

        ScrollArrow(
            scrollState = scrollState,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )

        LoginLoading(
            modifier = Modifier
                .fillMaxSize(),
            isLoading = isLoading
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SocialLoginScreenPreview() {
    SocialLoginScreen(
        isLoading = false,
        onKakaoLoginClick = {},
        onGithubLoginClick = {},
        onNavigateEmailLoginClick = {},
    )
}
package kr.co.lion.modigm.ui.login.email

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.login.email.component.EmailAutoLoginCheckBox
import kr.co.lion.modigm.ui.login.email.component.EmailLoginJoinButton
import kr.co.lion.modigm.ui.login.email.component.EmailLoginLoading
import kr.co.lion.modigm.ui.login.email.component.EmailLoginScrollArrow
import kr.co.lion.modigm.ui.login.email.component.EmailTextField
import kr.co.lion.modigm.ui.login.email.component.PasswordTextField
import kr.co.lion.modigm.ui.login.social.dpToSp
import kr.co.lion.modigm.util.JoinType

@Composable
fun EmailLoginScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    emailLoginResult: Boolean,
    emailLoginError: Throwable?,
    onNavigateToBottomNaviFragment: (JoinType) -> Unit,
    onNavigateToFindEmailFragment: () -> Unit,
    onNavigateToFindPasswordFragment: () -> Unit,
    onEmailLoginButtonClick: (String, String, Boolean) -> Unit,
    onNavigateToSocialLoginFragment: () -> Unit,
    onNavigateToJoinFragment: (JoinType) -> Unit,
    showLoginErrorDialog: (Throwable) -> Unit,
) {
    val scrollState = rememberScrollState()
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val isChecked = remember { mutableStateOf(false) }

    val isLoginButtonEnabled = remember(email.value, password.value) {
        email.value.isNotBlank() && password.value.isNotBlank()
    }

    LaunchedEffect(emailLoginResult) {
        if (emailLoginResult) {
            onNavigateToBottomNaviFragment(JoinType.EMAIL)
        }
    }

    LaunchedEffect(emailLoginError) {
        emailLoginError?.let { error ->
            showLoginErrorDialog(error)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "모우다임",
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .padding(top = 100.dp),
                fontFamily = FontFamily(Font(R.font.one_mobile_pop_otf)),
                fontSize = dpToSp(70.dp),
                color = Color.Black,
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
                color = Color.Black,
                fontWeight = FontWeight.Normal
            )

            EmailTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                email = email.value,
                onEmailChange = { email.value = it }
            )

            PasswordTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                password = password.value,
                onPasswordChange = { password.value = it }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, end = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EmailAutoLoginCheckBox(
                    modifier = Modifier,
                    isChecked = isChecked
                )

                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        modifier = Modifier.padding(start = 0.dp, end = 15.dp),
                        onClick = { onNavigateToFindEmailFragment() },
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            text = "이메일 찾기",
                            color = Color.Black,
                            fontSize = dpToSp(16.dp)
                        )
                    }
                    VerticalDivider(
                        modifier = Modifier
                            .height(15.dp),
                        thickness = 1.dp,
                        color = Color.Black,
                    )
                    TextButton(
                        modifier = Modifier
                            .padding(start = 15.dp, end = 0.dp),
                        onClick = { onNavigateToFindPasswordFragment() },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "비밀번호 찾기",
                            color = Color.Black,
                            fontSize = 16.sp,
                        )
                    }
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                onClick = {
                    onEmailLoginButtonClick(
                        email.value,
                        password.value,
                        isChecked.value
                    )
                },
                colors = buttonColors(Color(ContextCompat.getColor(LocalContext.current, R.color.pointColor))),
                enabled = isLoginButtonEnabled
            ) {
                Text(
                    text = "로그인",
                    fontSize = dpToSp(16.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp, end = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    modifier = Modifier,
                    onClick = { onNavigateToSocialLoginFragment() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_back_24px),
                        contentDescription = "돌아가기",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "돌아가기",
                        fontSize = dpToSp(16.dp),
                        color = Color.Black
                    )
                }
                EmailLoginJoinButton(
                    modifier = Modifier,
                    onNavigateToJoinFragment = onNavigateToJoinFragment
                )
            }
        }

        EmailLoginScrollArrow(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            scrollState = scrollState,
        )

        EmailLoginLoading(
            modifier = Modifier
                .fillMaxSize(),
            isLoading = isLoading
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmailLoginScreenPreview() {
    EmailLoginScreen(
        isLoading = false,
        emailLoginResult = false,
        emailLoginError = null,
        onNavigateToBottomNaviFragment = {},
        showLoginErrorDialog = {},
        onNavigateToFindEmailFragment = {},
        onNavigateToFindPasswordFragment = {},
        onEmailLoginButtonClick = { email, password, autoLoginValue -> println("email: $email, password: $password, autoLoginValue: $autoLoginValue") },
        onNavigateToSocialLoginFragment = {},
        onNavigateToJoinFragment = {},
    )
}

@Composable
fun dpToSp(dp: Dp) = with(LocalDensity.current) { dp.toSp() }


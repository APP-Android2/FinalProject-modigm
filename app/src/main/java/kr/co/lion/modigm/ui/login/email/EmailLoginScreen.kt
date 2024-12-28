package kr.co.lion.modigm.ui.login.email

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.login.email.component.EmailAutoLoginCheckBox
import kr.co.lion.modigm.ui.login.email.component.EmailLoginButton
import kr.co.lion.modigm.ui.login.email.component.EmailLoginJoinButton
import kr.co.lion.modigm.ui.login.email.component.EmailLoginLoading
import kr.co.lion.modigm.ui.login.email.component.EmailLoginScrollArrow
import kr.co.lion.modigm.ui.login.email.component.EmailTextField
import kr.co.lion.modigm.ui.login.email.component.FindEmailTextButton
import kr.co.lion.modigm.ui.login.email.component.FindPasswordTextButton
import kr.co.lion.modigm.ui.login.email.component.NavigateToSocialLoginButton
import kr.co.lion.modigm.ui.login.email.component.PasswordTextField
import kr.co.lion.modigm.util.JoinType

@Composable
fun EmailLoginScreen(
    isLoading: Boolean,
    emailLoginResult: Boolean,
    emailLoginError: Throwable?,
    onNavigateToBottomNaviFragment: (JoinType) -> Unit,
    onNavigateToFindEmailFragment: () -> Unit,
    onNavigateToFindPasswordFragment: () -> Unit,
    onEmailLoginButtonClick: () -> Unit,
    onNavigateToSocialLoginFragment: () -> Unit,
    onNavigateToJoinFragment: (JoinType) -> Unit,
    showLoginErrorDialog: (Throwable) -> Unit,
) {
    val scrollState = rememberScrollState()
    val isChecked = remember { mutableStateOf(false) }

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
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            EmailLoginTitle()
            EmailLoginSubTitle()
            EmailTextField()
            PasswordTextField()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, end = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EmailAutoLoginCheckBox(isChecked = isChecked)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FindEmailTextButton(onNavigateToFindEmailFragment = onNavigateToFindEmailFragment)
                    Text(
                        text = "|",
                        fontSize = dpToSp(16.dp)
                    )
                    FindPasswordTextButton(onNavigateToFindPasswordFragment = onNavigateToFindPasswordFragment)
                }
            }
            EmailLoginButton(onEmailLoginButtonClick = onEmailLoginButtonClick)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp, end = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigateToSocialLoginButton(onNavigateToSocialLoginFragment = onNavigateToSocialLoginFragment)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EmailLoginJoinButton(onNavigateToJoinFragment = onNavigateToJoinFragment)
                }
            }
        }
        EmailLoginScrollArrow(
            scrollState = scrollState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        EmailLoginLoading(isLoading = isLoading)
    }
}

@Composable
fun EmailLoginTitle() {
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
}

@Composable
fun EmailLoginSubTitle() {
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
        onEmailLoginButtonClick = {},
        onNavigateToSocialLoginFragment = {},
        onNavigateToJoinFragment = {},
    )
}

@Composable
fun dpToSp(dp: Dp) = with(LocalDensity.current) { dp.toSp() }


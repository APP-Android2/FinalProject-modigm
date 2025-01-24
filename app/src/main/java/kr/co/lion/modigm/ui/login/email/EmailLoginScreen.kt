package kr.co.lion.modigm.ui.login.email

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.login.component.EmailTextField
import kr.co.lion.modigm.ui.login.component.LoginLoading
import kr.co.lion.modigm.ui.login.component.PasswordTextField
import kr.co.lion.modigm.ui.login.component.ScrollArrow
import kr.co.lion.modigm.ui.login.util.dpToSp
import kr.co.lion.modigm.util.JoinType

@Composable
fun EmailLoginScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onFindEmailButtonClick: () -> Unit,
    onFindPasswordButtonClick: () -> Unit,
    onEmailLoginButtonClick: (String, String, Boolean) -> Unit,
    onBackButtonClick: () -> Unit,
    onJoinButtonClick: (JoinType) -> Unit,
) {
    val scrollState = rememberScrollState()
    val userEmail = remember { mutableStateOf("") }
    val userPassword = remember { mutableStateOf("") }
    val isChecked = remember { mutableStateOf(false) }

    val isLoginButtonEnabled = remember(userEmail.value, userPassword.value) {
        userEmail.value.isNotBlank() && userPassword.value.isNotBlank()
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
                userEmail = userEmail.value,
                onValueChange = { userEmail.value = it },
                placeholder = { Text(text = "이메일") },
            )

            PasswordTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                userPassword = userPassword.value,
                onValueChange = { userPassword.value = it },
                placeholder = { Text(text = "비밀번호") },
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, end = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked.value,
                        onCheckedChange = { isChecked.value = it },
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 0.dp, bottom = 0.dp, start = 0.dp, end = 0.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .size(8.dp)
                            .clickable { isChecked.value = !isChecked.value }
                    )
                    Text(
                        text = "자동 로그인",
                        fontSize = dpToSp(16.dp),
                        modifier = Modifier.clickable { isChecked.value = !isChecked.value }
                    )
                }

                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        modifier = Modifier.padding(start = 0.dp, end = 15.dp),
                        onClick = { onFindEmailButtonClick() },
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
                        onClick = { onFindPasswordButtonClick() },
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
                        userEmail.value,
                        userPassword.value,
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
                    onClick = { onBackButtonClick() },
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
                Row(
                    modifier = modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier
                            .padding(end = 4.dp),
                        text = "계정이 없으신가요?",
                        fontSize = dpToSp(16.dp),
                        color = Color.Black
                    )
                    TextButton(
                        modifier = Modifier,
                        onClick = { onJoinButtonClick(JoinType.EMAIL) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 4.dp),
                            painter = painterResource(R.drawable.icon_person_add_24px),
                            contentDescription = "회원가입",
                            tint = Color.Black
                        )
                        Text(
                            modifier = Modifier,
                            text = "회원가입",
                            fontSize = dpToSp(16.dp),
                            color = Color.Black
                        )
                    }
                }
            }
        }

        ScrollArrow(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            scrollState = scrollState,
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
fun EmailLoginScreenPreview() {
    EmailLoginScreen(
        isLoading = false,
        onFindEmailButtonClick = {},
        onFindPasswordButtonClick = {},
        onEmailLoginButtonClick = { email, password, autoLoginValue -> println("email: $email, password: $password, autoLoginValue: $autoLoginValue") },
        onBackButtonClick = {},
        onJoinButtonClick = {},
    )
}


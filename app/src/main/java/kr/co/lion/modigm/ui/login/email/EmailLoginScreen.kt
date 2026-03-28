package kr.co.lion.modigm.ui.login.email

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.login.component.EmailTextField
import kr.co.lion.modigm.ui.login.component.ErrorAlertDialog
import kr.co.lion.modigm.ui.login.component.LoginLoading
import kr.co.lion.modigm.ui.login.component.PasswordTextField
import kr.co.lion.modigm.ui.login.component.ScrollArrow
import kr.co.lion.modigm.ui.login.util.dpToSp

@Composable
fun EmailLoginScreen(
    navigateToBottomNavi: () -> Unit,
    navigateToFindEmail: () -> Unit,
    navigateToFindPassword: () -> Unit,
    navigateToSocialLogin: () -> Unit,
    navigateToJoin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmailLoginViewModel = viewModel()
) {
    val userEmail = viewModel.userEmail.collectAsState()
    val userPassword = viewModel.userPassword.collectAsState()
    val isChecked = viewModel.isChecked.collectAsState()
    val isLoginButtonEnabled = viewModel.isLoginEnabled.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()

    val emailLoginResult = viewModel.emailLoginResult.collectAsState()

    LaunchedEffect(emailLoginResult) {
        if (emailLoginResult.value) {
            navigateToBottomNavi()
        }
    }

    val emailLoginError = viewModel.emailLoginError.collectAsState()
    val showDialog = remember { mutableStateOf(false) }
    val dialogMessage = remember { mutableStateOf("") }

    LaunchedEffect(emailLoginError.value) {
        emailLoginError.value?.let { error ->
            dialogMessage.value = error.message ?: "알 수 없는 오류입니다."
            showDialog.value = true
        }
    }
    if (showDialog.value) {
        ErrorAlertDialog(
            title = "오류",
            message = dialogMessage.value,
            confirmButtonText = "확인",
            onConfirmClick = {
                showDialog.value = false
                viewModel.clearLoginError()
            },
            onDismissRequest = {
                showDialog.value = false
                viewModel.clearLoginError()
            }
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                emailFocusRequester.requestFocus()
                keyboardController?.show()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.clearViewModelData()
        }
    }
    val emailInputError = viewModel.emailInputError.collectAsState()
    val passwordInputError = viewModel.passwordInputError.collectAsState()

    LaunchedEffect(emailInputError.value, passwordInputError.value) {
        when {
            emailInputError.value != "" -> {
                emailFocusRequester.requestFocus()
                keyboardController?.show()
            }
            passwordInputError.value != "" -> {
                passwordFocusRequester.requestFocus()
                keyboardController?.show()
            }
        }
    }
    val focusField = viewModel.focusField.collectAsState()

    LaunchedEffect(focusField.value) {
        when (focusField.value) {
            FocusTarget.EMAIL_INPUT -> emailFocusRequester.requestFocus()
            FocusTarget.PASSWORD_INPUT -> passwordFocusRequester.requestFocus()
            null -> Unit
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
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
                    .padding(top = 30.dp)
                    .focusRequester(emailFocusRequester)
                    .focusable(),
                userEmail = userEmail.value,
                onValueChange = {
                    viewModel.onUserEmailChange(it)
                    viewModel.clearAllInputError()
                },
                placeholder = { Text(text = "이메일") },
                errorMessage = emailInputError.value,
            )

            PasswordTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .focusRequester(passwordFocusRequester)
                    .focusable(),
                userPassword = userPassword.value,
                onValueChange = {
                    viewModel.onUserPasswordChange(it)
                    viewModel.clearAllInputError()
                },
                placeholder = { Text(text = "비밀번호") },
                errorMessage = passwordInputError.value,
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
                        onCheckedChange = { viewModel.onCheckedChange(it) },
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 0.dp, bottom = 0.dp, start = 0.dp, end = 0.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .size(8.dp)
                            .clickable { viewModel.onCheckedChange(!isChecked.value) }
                    )
                    Text(
                        text = "자동 로그인",
                        fontSize = dpToSp(16.dp),
                        modifier = Modifier.clickable { viewModel.onCheckedChange(!isChecked.value) }
                    )
                }

                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        modifier = Modifier.padding(start = 0.dp, end = 15.dp),
                        onClick = { navigateToFindEmail() },
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
                        onClick = { navigateToFindPassword() },
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
                    keyboardController?.hide()

                    if(viewModel.checkAllInputAndSetError()) {
                        viewModel.emailLogin(
                            userEmail = userEmail.value,
                            userPassword = userPassword.value,
                            autoLoginValue = isChecked.value
                        )
                    }
                },
                colors = buttonColors(Color(ContextCompat.getColor(LocalContext.current, R.color.pointColor))),
                enabled = isLoginButtonEnabled.value
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
                    onClick = { navigateToSocialLogin() },
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
                        onClick = { navigateToJoin() },
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
            isLoading = isLoading.value
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmailLoginScreenPreview() {
    EmailLoginScreen(
        navigateToBottomNavi = {},
        navigateToFindEmail = {},
        navigateToFindPassword = {},
        navigateToSocialLogin = {},
        navigateToJoin = {},
    )
}


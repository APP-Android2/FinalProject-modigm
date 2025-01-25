package kr.co.lion.modigm.ui.join

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.join.component.EmailTextInputField
import kr.co.lion.modigm.ui.join.component.PasswordTextInputField
import kr.co.lion.modigm.ui.join.vm.JoinStep1EmailAndPwViewModel

@Composable
fun JoinStep1EmailAndPwScreen(
    joinStep1EmailAndPwViewModel: JoinStep1EmailAndPwViewModel
){

    val scrollState = rememberScrollState()
    val emailTextValueState = joinStep1EmailAndPwViewModel.userInputEmail.collectAsStateWithLifecycle()
    val emailValidationMessageState = joinStep1EmailAndPwViewModel.userInputEmailValidationMessage.collectAsStateWithLifecycle()
    val passwordTextValueState = joinStep1EmailAndPwViewModel.userInputPassword.collectAsStateWithLifecycle()
    val passwordValidationMessageState = joinStep1EmailAndPwViewModel.userInputPwValidationMessage.collectAsStateWithLifecycle()
    val passwordCheckTextValueState = joinStep1EmailAndPwViewModel.userInputPasswordCheck.collectAsStateWithLifecycle()
    val passwordCheckValidationMessageState = joinStep1EmailAndPwViewModel.userInputPwCheckValidationMessage.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 40.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = stringResource(R.string.JOIN_STEP1_TITLE),
            style = TextStyle(fontSize = 26.sp)
        )
        EmailTextInputField(
            inputTitle = stringResource(R.string.JOIN_STEP1_EMAIL_LABEL),
            textValue = emailTextValueState.value,
            onValueChange = { joinStep1EmailAndPwViewModel.setUserInputEmail(it) },
            isError = emailValidationMessageState.value.isNotEmpty(),
            errorMessage = emailValidationMessageState.value,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.padding(top = 40.dp)
        )
        PasswordTextInputField(
            inputTitle = stringResource(R.string.JOIN_STEP1_PASSWORD_LABEL),
            textValue = passwordTextValueState.value,
            onValueChange = { joinStep1EmailAndPwViewModel.setUserInputPassword(it) },
            isError = passwordValidationMessageState.value.isNotEmpty(),
            errorMessage = passwordValidationMessageState.value,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(R.string.JOIN_STEP1_PASSWORD_GUIDE),
            color = colorResource(R.color.redColor),
            modifier = Modifier.padding(start = 15.dp),
        )
        PasswordTextInputField(
            inputTitle = stringResource(R.string.JOIN_STEP1_PASSWORD_CHECK_LABEL),
            textValue = passwordCheckTextValueState.value,
            onValueChange = { joinStep1EmailAndPwViewModel.setUserInputPasswordCheck(it) },
            isError = passwordCheckValidationMessageState.value.isNotEmpty(),
            errorMessage = passwordCheckValidationMessageState.value,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
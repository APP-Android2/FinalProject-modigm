package kr.co.lion.modigm.ui.join

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.join.component.NameTextInputField
import kr.co.lion.modigm.ui.join.component.PhoneAuthTextInputField
import kr.co.lion.modigm.ui.join.component.PhoneTextInputField

@Composable
fun JoinStep2NameAndPhoneScreen(
    inputName: String,
    nameValidationMessage: String,
    inputPhoneNumber: String,
    phoneValidationMessage: String,
    inputPhoneAuthCode: String,
    phoneAuthCodeValidationMessage: String,
    phoneAuthButtonText: String,
    isPhoneAuthCodeSent: Boolean,
    isPhoneAuthExpired: Boolean,
    setUserInputName: (String) -> Unit,
    setUserInputPhone: (String) -> Unit,
    phoneAuthButtonClickEvent: () -> Unit,
    setUserInputSmsCode: (String) -> Unit,
    cancelPhoneAuth: () -> Unit,
){

    DisposableEffect(Unit) {
        onDispose {
            cancelPhoneAuth()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 40.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.JOIN_STEP2_TITLE),
            style = TextStyle(fontSize = 26.sp)
        )

        NameTextInputField(
            inputTitle = stringResource(R.string.JOIN_STEP2_NAME_LABEL),
            textValue = inputName,
            onValueChange = setUserInputName,
            isError = nameValidationMessage.isNotEmpty(),
            errorMessage = nameValidationMessage,
            modifier = Modifier.padding(top = 16.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            )
        )

        Text(
            text = stringResource(R.string.JOIN_STEP2_NAME_GUIDE),
            color = colorResource(R.color.redColor),
            modifier = Modifier.padding(start = 15.dp),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            PhoneTextInputField(
                inputTitle = stringResource(R.string.JOIN_STEP2_PHONE_LABEL),
                textValue = inputPhoneNumber,
                onValueChange = setUserInputPhone,
                isError = phoneValidationMessage.isNotEmpty(),
                errorMessage = phoneValidationMessage,
                modifier = Modifier.weight(2f),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Phone
                )
            )

            Button(
                onClick = phoneAuthButtonClickEvent,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 10.dp)
                    .weight(1f),
                enabled = isPhoneAuthExpired,
                colors = ButtonColors(
                    containerColor = colorResource(R.color.pointColor),
                    contentColor = colorResource(R.color.white),
                    disabledContainerColor = colorResource(R.color.textGray),
                    disabledContentColor = colorResource(R.color.black)
                )
            ) {
                Text(phoneAuthButtonText)
            }
        }

        if(isPhoneAuthCodeSent){
            PhoneAuthTextInputField(
                inputTitle = stringResource(R.string.JOIN_STEP2_PHONE_AUTH_LABEL),
                textValue = inputPhoneAuthCode,
                onValueChange = setUserInputSmsCode,
                isError = phoneAuthCodeValidationMessage.isNotEmpty(),
                errorMessage = phoneAuthCodeValidationMessage,
                modifier = Modifier.padding(top = 16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        }
    }
}
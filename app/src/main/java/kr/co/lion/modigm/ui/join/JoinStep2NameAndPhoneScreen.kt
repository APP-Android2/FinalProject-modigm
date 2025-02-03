package kr.co.lion.modigm.ui.join

import android.app.Activity
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.join.component.NameTextInputField
import kr.co.lion.modigm.ui.join.component.PhoneAuthTextInputField
import kr.co.lion.modigm.ui.join.component.PhoneTextInputField
import kr.co.lion.modigm.ui.join.vm.JoinStep2NameAndPhoneViewModel

@Composable
fun JoinStep2NameAndPhoneScreen(
    joinStep2NameAndPhoneViewModel: JoinStep2NameAndPhoneViewModel,
    requireActivity: Activity
){

    val nameTextValueState = joinStep2NameAndPhoneViewModel.userInputName.collectAsStateWithLifecycle()
    val nameValidationMessageState = joinStep2NameAndPhoneViewModel.userInputNameValidation.collectAsStateWithLifecycle()
    val phoneTextValueState = joinStep2NameAndPhoneViewModel.userInputPhone.collectAsStateWithLifecycle()
    val phoneValidationMessageState = joinStep2NameAndPhoneViewModel.userInputPhoneValidation.collectAsStateWithLifecycle()
    val phoneAuthTextValueState = joinStep2NameAndPhoneViewModel.userInputSmsCode.collectAsStateWithLifecycle()
    val phoneAuthValidationMessageState = joinStep2NameAndPhoneViewModel.userInputSmsCodeValidation.collectAsStateWithLifecycle()
    val phoneAuthButtonText = joinStep2NameAndPhoneViewModel.phoneAuthButtonText.collectAsStateWithLifecycle()
    val phoneAuthCodeSentState = joinStep2NameAndPhoneViewModel.isPhoneAuthCodeSent.collectAsStateWithLifecycle()
    val phoneAuthExpiredState = joinStep2NameAndPhoneViewModel.isPhoneAuthExpired.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            joinStep2NameAndPhoneViewModel.phoneAuthTimer.cancel()
            joinStep2NameAndPhoneViewModel.stopSmsReceiver(requireActivity)
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
            textValue = nameTextValueState.value,
            onValueChange = {
                joinStep2NameAndPhoneViewModel.setUserInputName(it)
            },
            isError = nameValidationMessageState.value.isNotEmpty(),
            errorMessage = nameValidationMessageState.value,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            modifier = Modifier.padding(top = 16.dp)
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
                textValue = phoneTextValueState.value,
                onValueChange = {
                    joinStep2NameAndPhoneViewModel.setUserInputPhone(it)
                },
                isError = phoneValidationMessageState.value.isNotEmpty(),
                errorMessage = phoneValidationMessageState.value,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Phone
                ),
                modifier = Modifier.weight(2f)
            )
            Button(
                onClick = {
                    joinStep2NameAndPhoneViewModel.phoneAuthButtonClickEvent(requireActivity)
                },
                colors = ButtonColors(
                    containerColor = colorResource(R.color.pointColor),
                    contentColor = colorResource(R.color.white),
                    disabledContainerColor = colorResource(R.color.textGray),
                    disabledContentColor = colorResource(R.color.black)
                ),
                enabled = phoneAuthExpiredState.value,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 10.dp)
                    .weight(1f)
            ) {
                Text(phoneAuthButtonText.value)
            }
        }
        if(phoneAuthCodeSentState.value){
            PhoneAuthTextInputField(
                inputTitle = stringResource(R.string.JOIN_STEP2_PHONE_AUTH_LABEL),
                textValue = phoneAuthTextValueState.value,
                onValueChange = {
                    joinStep2NameAndPhoneViewModel.setUserInputSmsCode(it)
                },
                isError = phoneAuthValidationMessageState.value.isNotEmpty(),
                errorMessage = phoneAuthValidationMessageState.value,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
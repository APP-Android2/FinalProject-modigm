package kr.co.lion.modigm.ui.join.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import kr.co.lion.modigm.R

@Composable
fun EmailTextInputField(
    inputTitle: String,
    textValue: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Default,
){
    Column(
        modifier = modifier
    ) {
        Text(
            text = inputTitle,
            style = TextStyle(
                fontSize = 16.sp,
            )
        )
        OutlinedTextField(
            value = textValue,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    stringResource(R.string.JOIN_STEP1_INPUT_PLACEHOLDER_TEXT, inputTitle),
                    color = colorResource(R.color.textGray)
                )
            },
            trailingIcon = {
                if(textValue.isEmpty()) return@OutlinedTextField
                IconButton(
                    onClick = {
                        onValueChange("")
                    }
                ){
                    Icon(
                        imageVector  = Icons.Filled.Clear,
                        contentDescription = stringResource(R.string.PASSWORD_TOGGLE_ICON_DESCRIPTION)
                    )
                }
            },
            singleLine = true,
            isError = isError,
            supportingText = {
                if(isError){
                    Text(
                        text = errorMessage,
                        color = colorResource(R.color.redColor)
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = colorResource(R.color.pointColor),
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = imeAction
            ),
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun PasswordTextInputField(
    inputTitle: String,
    textValue: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Default
){
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val image = if (passwordVisible){
        Icons.Filled.Visibility
    } else {
        Icons.Filled.VisibilityOff
    }

    Column(
        modifier = modifier
    ) {
        Text(
            text = inputTitle,
            style = TextStyle(
                fontSize = 16.sp,
            )
        )
        OutlinedTextField(
            value = textValue,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    stringResource(R.string.JOIN_STEP1_INPUT_PLACEHOLDER_TEXT, inputTitle),
                    color = colorResource(R.color.textGray)
                )
            },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ){
                    Icon(
                        imageVector  = image,
                        contentDescription = stringResource(R.string.PASSWORD_TOGGLE_ICON_DESCRIPTION)
                    )
                }
            },
            singleLine = true,
            isError = isError,
            supportingText = {
                if(isError){
                    Text(
                        text = errorMessage,
                        color = colorResource(R.color.redColor)
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = colorResource(R.color.pointColor),
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = imeAction
            ),
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}
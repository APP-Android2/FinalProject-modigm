package kr.co.lion.modigm.ui.join.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import kr.co.lion.modigm.R

@Composable
fun PhoneAuthTextField(
    inputTitle: String,
    textValue: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Default
    ),
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
                val emptyValue = stringResource(R.string.JOIN_TEXT_RESET_VALUE)
                IconButton(
                    onClick = {
                        onValueChange(emptyValue)
                    }
                ){
                    Icon(
                        imageVector  = Icons.Filled.Clear,
                        contentDescription = stringResource(R.string.CLEAR_ICON_DESCRIPTION)
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
            keyboardOptions = keyboardOptions,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}
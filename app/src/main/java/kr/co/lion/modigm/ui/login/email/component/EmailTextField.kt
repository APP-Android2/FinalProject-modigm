package kr.co.lion.modigm.ui.login.email.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.login.email.dpToSp

@Composable
fun EmailTextField(
    modifier: Modifier = Modifier,
    email: String,
    onEmailChange: (String) -> Unit
) {
    val isError by remember { mutableStateOf(false) }
    val pointColor = Color(ContextCompat.getColor(LocalContext.current, R.color.pointColor))

    OutlinedTextField(
        value = email,
        onValueChange = { onEmailChange(it) },
        modifier = modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(fontSize = dpToSp(16.dp)),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = pointColor,
            unfocusedIndicatorColor = Color.Black,
            errorContainerColor = Color.White,
        ),
        placeholder = { Text(text = "이메일") },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.icon_mail_24px),
                contentDescription = "이메일"
            )
        },
        trailingIcon = {
            if (email.isNotEmpty()) {
                IconButton(onClick = { onEmailChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "이메일 초기화"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        isError = isError,
    )

}
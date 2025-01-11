package kr.co.lion.modigm.ui.login.email.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.login.email.dpToSp

@Composable
fun EmailLoginButton(
    modifier: Modifier = Modifier,
    onEmailLoginButtonClick: () -> Unit,
    isEnabled: Boolean
) {
    Button(
        onClick = { onEmailLoginButtonClick() },
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        colors = buttonColors(Color(ContextCompat.getColor(LocalContext.current, R.color.pointColor))),
        enabled = isEnabled
    ) {
        Text(
            text = "로그인",
            fontSize = dpToSp(16.dp)
        )
    }
}
package kr.co.lion.modigm.ui.login.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SocialLoginButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    colors: ButtonColors,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(8.dp),
        content = { content() }
    )
}

@Preview(showBackground = true)
@Composable
fun SocialLoginButtonPreview() {
    SocialLoginButton(
        onClick = {},
        colors = ButtonDefaults.buttonColors(),
        content = {}
    )
}
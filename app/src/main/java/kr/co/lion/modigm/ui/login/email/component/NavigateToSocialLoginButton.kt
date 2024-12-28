package kr.co.lion.modigm.ui.login.email.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.login.email.dpToSp

@Composable
fun NavigateToSocialLoginButton(
    onNavigateToSocialLoginFragment: () -> Unit
) {
    TextButton(
        onClick = { onNavigateToSocialLoginFragment() },
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
}
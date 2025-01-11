package kr.co.lion.modigm.ui.login.email.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kr.co.lion.modigm.ui.login.email.dpToSp

@Composable
fun FindEmailTextButton(
    modifier: Modifier = Modifier,
    onNavigateToFindEmailFragment: () -> Unit,
) {
    TextButton(
        modifier = modifier.padding(end = 4.dp),
        onClick = { onNavigateToFindEmailFragment() },
    ) {
        Text(
            text = "이메일 찾기",
            color = Color.Black,
            fontSize = dpToSp(16.dp)
        )
    }
}
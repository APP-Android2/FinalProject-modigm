package kr.co.lion.modigm.ui.login.email.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FindPasswordTextButton(
    modifier: Modifier = Modifier,
    onNavigateToFindPasswordFragment: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        onClick = { onNavigateToFindPasswordFragment() },
        contentPadding = PaddingValues(start = 16.dp, end = 0.dp),
    ) {
        Text(
            text = "비밀번호 찾기",
            color = Color.Black,
            fontSize = 16.sp,
        )
    }
}
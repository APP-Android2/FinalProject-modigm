package kr.co.lion.modigm.ui.login.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LoginLoading(
    modifier: Modifier = Modifier,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = modifier
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit) { },
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginLoadingPreview() {
    LoginLoading(isLoading = true)
}
package kr.co.lion.modigm.ui.login.social.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.lion.modigm.R

@Composable
fun EmailLoginButton(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .padding(top = 20.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(0.dp),
        ) {
            Text(
                text = "다른 방법으로 로그인",
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.one_mobile_pop_otf)),
                fontWeight = FontWeight.Normal,
                color = Color.White
            )
        }
    }
}
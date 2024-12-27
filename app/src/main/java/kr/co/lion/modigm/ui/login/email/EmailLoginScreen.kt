package kr.co.lion.modigm.ui.login.email

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kr.co.lion.modigm.R

@Composable
fun EmailLoginScreen(
    viewModel: EmailLoginViewModel
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            EmailLoginTitle()
            EmailLoginSubTitle()
        }
    }
}

@Composable
fun EmailLoginTitle() {
    Text(
        text = "모우다임",
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .padding(top = 100.dp),
        fontFamily = FontFamily(Font(R.font.one_mobile_pop_otf)),
        fontSize = dpToSp(70.dp),
        color = Color.Black,
        fontWeight = FontWeight.Normal
    )
}

@Composable
fun EmailLoginSubTitle() {
    Text(
        text = "개발자 스터디의 새로운 패러다임",
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .padding(top = 10.dp),
        fontFamily = FontFamily(Font(R.font.one_mobile_pop_otf)),
        fontSize = dpToSp(22.dp),
        color = Color.Black,
        fontWeight = FontWeight.Normal
    )
}

@Preview
@Composable
fun EmailLoginScreenPreview() {
    EmailLoginScreen(viewModel = EmailLoginViewModel())
}

@Composable
fun dpToSp(dp: Dp) = with(LocalDensity.current) { dp.toSp() }


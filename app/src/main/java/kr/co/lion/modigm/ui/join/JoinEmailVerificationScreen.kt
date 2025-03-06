package kr.co.lion.modigm.ui.join

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kr.co.lion.modigm.R

@Composable
fun JoinEmailVerificationScreen(){

    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.auth_email_send_animation))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 40.dp)
    ) {

        Text(
            text = stringResource(R.string.JOIN_EMAIL_VERIFICATION_TITLE),
            fontSize = 26.sp
        )

        Text(
            text = stringResource(R.string.JOIN_EMAIL_VERIFICATION_DESCRIPTION),
            modifier = Modifier.padding(top = 40.dp),
            fontSize = 20.sp
        )

        LottieAnimation(
            composition = lottieComposition,
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally),
            isPlaying = true,
            iterations = LottieConstants.IterateForever
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewJoinEmailVerificationScreen(){
    JoinEmailVerificationScreen()
}
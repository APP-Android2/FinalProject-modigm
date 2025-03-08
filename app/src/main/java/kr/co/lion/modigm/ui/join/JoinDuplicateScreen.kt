package kr.co.lion.modigm.ui.join

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.lion.modigm.R
import kr.co.lion.modigm.util.JoinType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinDuplicateScreen(
    joinType: String,
    email: String,
    popBackStack: () -> Unit,
    backToLogin: () -> Unit,
){
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back_24px),
                        contentDescription = "",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable {
                                popBackStack()
                            }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                )
            )
        },
        containerColor = Color.White
    ) { paddingValue ->
        Column(
            modifier = Modifier
                .padding(paddingValue)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "이미 가입한 계정이 있습니다.",
                modifier = Modifier.padding(top = 40.dp),
                fontSize = 26.sp
            )

            Text(
                text = "이전에 가입한 계정을 확인해주세요.",
                modifier = Modifier.padding(top = 10.dp),
                fontSize = 18.sp
            )

            Card(
                modifier = Modifier
                    .padding(40.dp)
                    .align(Alignment.CenterHorizontally),
                colors = CardColors(
                    containerColor = Color(0xFFDDDDDD),
                    contentColor = Color.Black,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier.padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(JoinType.getType(joinType).icon),
                        contentDescription = "provider",
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 10.dp)
                    )
                    Text(
                        text = email,
                        fontSize = 16.sp
                    )
                }
            }

            Button(
                onClick = backToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                colors = ButtonColors(
                    containerColor = colorResource(R.color.pointColor),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color.Transparent
                )
            ) {
                Text(
                    text = "기존 계정으로 로그인",
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewJoinDuplicateScreen(){
    JoinDuplicateScreen(
        joinType = "kakao",
        email = "test@kakao1234.co.kr",
        popBackStack = {},
        backToLogin = {}
    )
}
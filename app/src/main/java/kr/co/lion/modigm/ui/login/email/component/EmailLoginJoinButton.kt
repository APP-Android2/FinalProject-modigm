package kr.co.lion.modigm.ui.login.email.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.login.email.dpToSp
import kr.co.lion.modigm.util.JoinType

@Composable
fun EmailLoginJoinButton(
    modifier: Modifier = Modifier,
    onNavigateToJoinFragment: (JoinType) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .padding(end = 4.dp),
            text = "계정이 없으신가요?",
            fontSize = dpToSp(16.dp),
            color = Color.Black
        )
        TextButton(
            modifier = Modifier,
            onClick = { onNavigateToJoinFragment(JoinType.EMAIL) },
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                modifier = Modifier.padding(end = 4.dp),
                painter = painterResource(R.drawable.icon_person_add_24px),
                contentDescription = "회원가입",
                tint = Color.Black
            )
            Text(
                modifier = Modifier,
                text = "회원가입",
                fontSize = dpToSp(16.dp),
                color = Color.Black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmailLoginJoinButtonPreview() {
    EmailLoginJoinButton(
        onNavigateToJoinFragment = {}
    )
}
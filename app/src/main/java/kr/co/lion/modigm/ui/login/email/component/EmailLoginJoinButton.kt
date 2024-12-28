package kr.co.lion.modigm.ui.login.email.component

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
import kr.co.lion.modigm.util.JoinType

@Composable
fun EmailLoginJoinButton(
    onNavigateToJoinFragment: (JoinType) -> Unit
) {
    Text(
        text = "계정이 없으신가요?",
        fontSize = dpToSp(16.dp),
        color = Color.Black
    )
    TextButton(
        onClick = { onNavigateToJoinFragment(JoinType.EMAIL) }
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_person_add_24px),
            contentDescription = "회원가입",
            tint = Color.Black
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = "회원가입",
            fontSize = dpToSp(16.dp),
            color = Color.Black
        )
    }
}
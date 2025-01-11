package kr.co.lion.modigm.ui.login.email.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kr.co.lion.modigm.ui.login.email.dpToSp

@Composable
fun EmailAutoLoginCheckBox(
    modifier: Modifier = Modifier,
    isChecked: MutableState<Boolean>,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked.value,
            onCheckedChange = { isChecked.value = it },
            modifier = Modifier
                .size(20.dp)
                .padding(top = 0.dp, bottom = 0.dp, start = 0.dp, end = 0.dp)
        )
        Spacer(
            modifier = Modifier
                .size(8.dp)
                .clickable { isChecked.value = !isChecked.value }
        )
        Text(
            text = "자동 로그인",
            fontSize = dpToSp(16.dp),
            modifier = Modifier.clickable { isChecked.value = !isChecked.value }
        )
    }
}
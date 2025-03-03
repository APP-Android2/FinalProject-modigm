package kr.co.lion.modigm.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import kr.co.lion.modigm.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModigmTopAppBar(
    title: String,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp),
        title = { Text(text = title) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.icon_settings_24px),
                    contentDescription = "Settings"
                )
            }
        }
    )
}

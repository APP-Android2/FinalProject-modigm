package kr.co.lion.modigm.ui.login.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.login.util.animateEnterExit

@Composable
fun ScrollArrow(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val isVisible = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.canScrollForward }
            .collect { canScrollForward ->
                isVisible.value = canScrollForward
            }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (isVisible.value) {
            Image(
                painter = painterResource(id = R.drawable.arrow_down_24px),
                contentDescription = "Scroll Arrow",
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                    .animateEnterExit(),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}
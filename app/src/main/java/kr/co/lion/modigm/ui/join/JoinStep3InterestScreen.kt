package kr.co.lion.modigm.ui.join

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.lion.modigm.R
import kr.co.lion.modigm.util.Interest

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JoinStep3InterestScreen(
    isInterestSelected: Boolean,
    removeFromInterestList: (String) -> Unit,
    addToInterestList: (String) -> Unit
){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 40.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.JOIN_STEP3_TITLE),
            style = TextStyle(fontSize = 26.sp),
            modifier = Modifier.padding(bottom = 40.dp)
        )
        if(!isInterestSelected){
            Text(
                text = stringResource(R.string.JOIN_STEP3_ALERT_EMPTY_INTEREST_LIST),
                style = TextStyle(fontSize = 16.sp, color = colorResource(R.color.redColor)),
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Interest.entries.forEach {
                var isSelected by remember { mutableStateOf(false) }
                AssistChip(
                    onClick = {
                        if(isSelected){
                            removeFromInterestList(it.str)
                        }else{
                            addToInterestList(it.str)
                        }
                        isSelected = !isSelected
                    },
                    label = {
                        Text(
                            text = it.str,
                            fontSize = 18.sp,
                            color = if(isSelected){
                                colorResource(R.color.white)
                            }else{
                                colorResource(R.color.textGray)
                            }
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if(isSelected){
                            colorResource(R.color.pointColor)
                        }else{
                            Color.Unspecified
                        }
                    )
                )
            }
        }
    }
}
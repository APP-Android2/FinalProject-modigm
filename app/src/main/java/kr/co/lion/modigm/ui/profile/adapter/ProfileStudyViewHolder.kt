package kr.co.lion.modigm.ui.profile.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowProfileStudyBinding
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.repository.StudyRepository

class ProfileStudyViewHolder(
    private val context: Context,
    private val rowProfileStudyBinding: RowProfileStudyBinding,
    private val rowClickListener: (Int) -> Unit, ): RecyclerView.ViewHolder(rowProfileStudyBinding.root) {

    // 구성요소 세팅
    fun bind(data: SqlStudyData, rowClickListener: (Int) -> Unit) {
        rowProfileStudyBinding.apply {
            CoroutineScope(Dispatchers.Main).launch {
                // 데이터베이스로부터 썸네일을 불러온다
                // studyRepository.loadStudyThumbnail(context, data.studyPic, imageRowHostStudy)
                // 스터디 제목
                textViewRowHostStudyTitle.text = data.studyTitle
                // 스터디 분류 아이콘
                when (data.studyType) {
                    "스터디" -> imageCategory.setImageResource(R.drawable.icon_closed_book_24px)
                    "프로젝트" -> imageCategory.setImageResource(R.drawable.icon_code_box_24px)
                    "공모전" -> imageCategory.setImageResource(R.drawable.icon_trophy_24px)
                    else -> imageCategory.setImageResource(R.drawable.icon_closed_book_24px)
                }
                // 스터디 분류
                textViewRowHostStudyCategory.text = data.studyType
                // 스터디 진행 방식
                textViewRowHostStudyLocation.text = data.studyOnOffline
                // 모집 중 / 모집 완료
                if (data.studyState) {
                    textViewRowHostStudyMember.text = "모집중"
                } else {
                    textViewRowHostStudyMember.text = "모집완료"
                }
            }

            // 항목에 대한 설정
            root.apply {
                // 항목 클릭 시 클릭되는 범위 설정
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                // 클릭 리스너 설정: 스터디 고유번호 전달
                setOnClickListener {
                    rowClickListener.invoke(data.studyIdx)
                }
            }
        }
    }
}
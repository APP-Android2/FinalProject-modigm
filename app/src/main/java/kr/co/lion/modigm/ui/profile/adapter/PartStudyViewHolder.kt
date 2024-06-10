package kr.co.lion.modigm.ui.profile.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.databinding.RowPartStudyBinding
import kr.co.lion.modigm.db.study.RemoteStudyDataSource
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.StudyRepository

class PartStudyViewHolder(
    private val context: Context,
    private val rowPartStudyBinding: RowPartStudyBinding,
    private val rowClickListener: (Int) -> Unit, ): RecyclerView.ViewHolder(rowPartStudyBinding.root) {

    private val studyRepository = StudyRepository()

    // 구성요소 세팅
    fun bind(data: StudyData, rowClickListener: (Int) -> Unit) {
        rowPartStudyBinding.apply {
            CoroutineScope(Dispatchers.Main).launch {
                // 데이터베이스로부터 썸네일을 불러온다
                studyRepository.loadStudyThumbnail(context, data.studyPic, imageRowPartStudy)
                // 스터디 제목
                textViewRowPartStudy.text = data.studyTitle
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
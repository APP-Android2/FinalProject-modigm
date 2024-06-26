package kr.co.lion.modigm.ui.profile.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowHostStudyBinding
import kr.co.lion.modigm.db.study.RemoteStudyDataSource
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.repository.StudyRepository
import kr.co.lion.modigm.util.StudyCategory
import kr.co.lion.modigm.util.StudyPlace

class HostStudyViewHolder(
    private val context: Context,
    private val rowHostStudyBinding: RowHostStudyBinding,
    private val rowClickListener: (Int) -> Unit, ): RecyclerView.ViewHolder(rowHostStudyBinding.root) {

    private val studyRepository = StudyRepository()

    // 구성요소 세팅
    fun bind(data: StudyData, rowClickListener: (Int) -> Unit) {
        rowHostStudyBinding.apply {
            CoroutineScope(Dispatchers.Main).launch {
                // 데이터베이스로부터 썸네일을 불러온다
                studyRepository.loadStudyThumbnail(context, data.studyPic, imageRowHostStudy)
                // 스터디 제목
                textViewRowHostStudyTitle.text = data.studyTitle
                // 스터디 분류 아이콘
                when (StudyCategory.fromNum(data.studyType)) {
                    StudyCategory.STUDY -> imageCategory.setImageResource(R.drawable.icon_closed_book_24px)
                    StudyCategory.PROJECT -> imageCategory.setImageResource(R.drawable.icon_code_box_24px)
                    StudyCategory.COMPETITION -> imageCategory.setImageResource(R.drawable.icon_trophy_24px)
                    null -> imageCategory.setImageResource(R.drawable.icon_closed_book_24px)
                }
                // 스터디 분류
                textViewRowHostStudyCategory.text = StudyCategory.fromNum(data.studyType)!!.str
                // 스터디 진행 방식
                textViewRowHostStudyLocation.text = StudyPlace.fromNum(data.studyOnOffline)!!.str
                // 스터디 인원
                textViewRowHostStudyMember.text = "${data.studyUidList.size}/${data.studyMaxMember}"
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
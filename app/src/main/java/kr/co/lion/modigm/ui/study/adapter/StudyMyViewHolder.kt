package kr.co.lion.modigm.ui.study.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowStudyMyBinding
import kr.co.lion.modigm.model.StudyData

class StudyMyViewHolder(
    private val binding: RowStudyMyBinding,
    private val rowClickListener: (Int) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root) {


    // 전체 스터디 항목별 세팅
    fun bind(studyData: Pair<StudyData, Int>, rowClickListener: (Int) -> Unit) {

        with(binding) {
            // 항목 하나
            with(root) {
                layoutParams = ViewGroup.LayoutParams(
                    // 항목 클릭 시 클릭되는 범위 설정
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                // 클릭 리스너 설정.
                setOnClickListener {
                    rowClickListener.invoke(studyData.first.studyIdx)
                }

                // 스터디 이미지
                with(imageViewStudyMyPic){

                    // 이미지 클릭 시
                    setOnClickListener {

                    }
                }

                // 모집중, 모집완료
                when (studyData.first.studyState) {
                    true -> {
                        textViewStudyMyState.text = "모집중"
                    }
                    false -> {
                        textViewStudyMyState.text = "모집완료"
                    }
                }
                // 스터디 진행 방식
                with(textViewStudyMyStateMeet){

                    when (studyData.first.studyOnOffline){

                        1 -> {
                            text = "온라인"
                            setTextColor(android.graphics.Color.parseColor("#0FA981"))
                        }
                        2 -> {
                            text = "오프라인"
                            setTextColor(android.graphics.Color.parseColor("#EB9C58"))
                        }
                        3 -> {
                            text = "온오프혼합"
                            setTextColor(android.graphics.Color.parseColor("#0096FF"))
                        }
                    }
                }
                // 스터디 제목
                textViewStudyMyTitle.text = studyData.first.studyTitle
                // 스터디 기간
                with(textViewStudyMyStatePeriod){
                    when (studyData.first.studyPeriod) {
                        1 -> {
                            text = "단기"
                        }
                        2 -> {
                            text = "정규"
                        }
                        else -> {
                            text = "도전"
                        }
                    }
                }

                // 스터디 최대 인원수
                textViewStudyMyStateInwon.text = studyData.second.toString()+"/"+studyData.first.studyMaxMember.toString()

                // 찜 버튼
                with(imageViewStudyMyHeart){

                    // 클릭 시
                    setOnClickListener {
                        val currentIconResId = tag as? Int ?: R.drawable.icon_favorite_24px

                        if (currentIconResId == R.drawable.icon_favorite_24px) {
                            // 좋아요 채워진 아이콘으로 변경
                            setImageResource(R.drawable.icon_favorite_full_24px)
                            // 상태 태그 업데이트
                            tag = R.drawable.icon_favorite_full_24px

                            // 새 색상을 사용하여 틴트 적용
                            setColorFilter(android.graphics.Color.parseColor("#D73333"))

                        } else {
                            // 기본 아이콘으로 변경
                            setImageResource(R.drawable.icon_favorite_24px)
                            // 상태 태그 업데이트
                            tag = R.drawable.icon_favorite_24px

                            // 틴트 제거 (원래 아이콘 색상으로 복원)
                            clearColorFilter()
                        }
                    }
                }
            }
        }
    }
}
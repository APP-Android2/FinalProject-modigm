package kr.co.lion.modigm.ui.study.adapter

import android.graphics.Color
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
    fun bind(studyData: StudyData, rowClickListener: (Int) -> Unit) {

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
                    rowClickListener.invoke(studyData.studyIdx)
                }


                // 좋아요 버튼 상태 토글
                fun toggleLikeButton() {
                    // 현재 설정된 이미지 리소스 ID를 확인하고 상태를 토글
                    val currentIconResId = imageViewStudyMyHeart.tag as? Int ?: R.drawable.icon_favorite_24px
                    if (currentIconResId == R.drawable.icon_favorite_24px) {
                        // 좋아요 채워진 아이콘으로 변경
                        imageViewStudyMyHeart.setImageResource(R.drawable.icon_favorite_full_24px)
                        // 상태 태그 업데이트
                        imageViewStudyMyHeart.tag = R.drawable.icon_favorite_full_24px

                        // 새 색상을 사용하여 틴트 적용
                        imageViewStudyMyHeart.setColorFilter(Color.parseColor("#D73333"))
                    } else {
                        // 기본 아이콘으로 변경
                        imageViewStudyMyHeart.setImageResource(R.drawable.icon_favorite_24px)
                        // 상태 태그 업데이트
                        imageViewStudyMyHeart.tag = R.drawable.icon_favorite_24px

                        // 틴트 제거 (원래 아이콘 색상으로 복원)
                        imageViewStudyMyHeart.clearColorFilter()
                    }
                }
                with(imageViewStudyMyHeart){
                    setOnClickListener {
                        toggleLikeButton()
                    }
                }
            }
        }
    }
}
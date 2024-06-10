package kr.co.lion.modigm.ui.like.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowLikeBinding
import kr.co.lion.modigm.model.StudyData

class LikeAdapter(private var studyList: List<StudyData>, private val onLikeClick: (StudyData) -> Unit) : RecyclerView.Adapter<LikeAdapter.StudyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyViewHolder {
        val binding = RowLikeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudyViewHolder, position: Int) {
        holder.bind(studyList[position])
    }

    override fun getItemCount(): Int = studyList.size

    fun updateData(newStudyList: List<StudyData>) {
        studyList = newStudyList
        notifyDataSetChanged()
    }

    inner class StudyViewHolder(private val binding: RowLikeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(study: StudyData) {
            binding.apply {

                // Firebase Storage에서 이미지 URL 가져오기
                val storageReference =
                    FirebaseStorage.getInstance().reference.child("studyPic/${study.studyPic}")
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(itemView.context)
                        .load(uri)
                        .error(R.drawable.icon_error_24px) // 로드 실패 시 표시할 이미지
                        .into(imageViewLikeCover) // ImageView에 이미지 로드
                }.addOnFailureListener {
                    // 에러 처리
                }

                // 모집 상태에 따라 텍스트와 색상 설정
                textViewLikeState.text = if (study.studyState) "모집 중" else "모집 마감"
                val textColor = if (study.studyState) ContextCompat.getColor(itemView.context, R.color.pointColor)
                else ContextCompat.getColor(itemView.context, R.color.dividerView)
                textViewLikeState.setTextColor(textColor)

                // 스터디 진행 방식
                textViewLikeStateMeet.text = when (study.studyOnOffline) {
                    1 -> "온라인"
                    2 -> "오프라인"
                    else -> "온오프혼합"
                }

                // 모집 상태에 따라 진행 방식 텍스트 색상 설정
                textViewLikeStateMeet.setTextColor(
                    if (study.studyState) {
                        when (study.studyOnOffline) {
                            0 -> Color.parseColor("#EB9C58") // 오프라인 색상
                            1 -> Color.parseColor("#0FA981") // 온라인 색상
                            else -> Color.parseColor("#0096FF") // 온오프혼합 색상
                        }
                    } else {
                        ContextCompat.getColor(itemView.context, R.color.dividerView) // 모집 종료 시 회색
                    }
                )

                // 스터디 제목
                textViewLikeTitle.text = study.studyTitle

                // 스터디 종류
                textViewLikeStatePeriod.apply {
                    text = when (study.studyType) {
                        1 -> "스터디"
                        2 -> "프로젝트"
                        else -> "공모전"
                    }
                }
                // 스터디 신청 인원
                textViewLikeJoinMember.text = study.studyUidList.size.toString()

                // 스터디 최대 인원
                textViewLikeTotalMember.text = study.studyMaxMember.toString()

                // 스터디 종류에 따라 아이콘 변경
                imageViewLikeStatePeriod.setImageResource(
                    when (study.studyType) {
                        0 -> R.drawable.icon_closed_book_24px
                        1 -> R.drawable.icon_code_box_24px
                        else -> R.drawable.icon_trophy_24px
                    }
                )

                // 스터디 신청 방식에 따른 텍스트 설정
                textViewLikeApplyType.text = when (study.studyApplyMethod) {
                    1 -> "신청제"
                    else -> "선착순"
                }

                // 좋아요 상태에 따라 하트 아이콘 및 색상 변경
                if (study.studyLikeState) {
                    imageViewLikeHeart.setImageResource(R.drawable.icon_favorite_full_24px)
                    imageViewLikeHeart.setColorFilter(Color.parseColor("#D73333"))
                } else {
                    imageViewLikeHeart.setImageResource(R.drawable.icon_favorite_24px)
                    imageViewLikeHeart.setColorFilter(ContextCompat.getColor(itemView.context, R.color.pointColor))
                }

                // 좋아요 아이콘 클릭 이벤트 처리
                imageViewLikeHeart.setOnClickListener {
                    onLikeClick(study)
                }
            }
        }
    }

}
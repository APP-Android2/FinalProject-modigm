package kr.co.lion.modigm.ui.study.adapter

import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowStudyAllBinding
import kr.co.lion.modigm.model.StudyData

class StudyAllViewHolder(
    private val binding: RowStudyAllBinding,
    private val rowClickListener: (Int) -> Unit,
    private val likeClickListener: (Int) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(studyData: Pair<StudyData, Int>) {
        with(binding) {
            setupRootView(studyData)
            loadStudyImage(studyData.first.studyPic)
            setStudyState(studyData.first.studyState)
            setStudyOnOffline(studyData.first.studyOnOffline)
            textViewStudyAllTitle.text = studyData.first.studyTitle
            setStudyType(studyData.first.studyType)
            setStudyMembers(studyData)
            setStudyApplyMethod(studyData)
            setupFavoriteButton(studyData)
        }
    }

    private fun setupRootView(studyData: Pair<StudyData, Int>) {
        with(binding.root) {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                rowClickListener.invoke(studyData.first.studyIdx)
            }
            binding.imageViewStudyAllPic.setOnClickListener {
                // 현재는 빈 구현
            }
        }
    }

    private fun loadStudyImage(imageFileName: String) {
        if (imageFileName.isNotEmpty()) {
            val storageRef = FirebaseStorage.getInstance().reference.child("studyPic/$imageFileName")
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(itemView.context)
                    .load(uri)
                    .into(binding.imageViewStudyAllPic)
            }.addOnFailureListener {
                binding.imageViewStudyAllPic.setImageResource(R.drawable.image_detail_1)
            }
        } else {
            binding.imageViewStudyAllPic.setImageResource(R.drawable.image_detail_2)
        }
    }

    private fun setStudyState(studyState: Boolean) {
        binding.textViewStudyAllCanApply.text = if (studyState) "모집중" else "모집 완료"
    }

    private fun setStudyOnOffline(studyOnOffline: Int) {
        with(binding.textViewStudyAllOnOffline) {
            when (studyOnOffline) {
                1 -> {
                    text = "온라인"
                    setTextColor(Color.parseColor("#0FA981"))
                }
                2 -> {
                    text = "오프라인"
                    setTextColor(Color.parseColor("#EB9C58"))
                }
                3 -> {
                    text = "온오프혼합"
                    setTextColor(Color.parseColor("#0096FF"))
                }
            }
        }
    }

    private fun setStudyType(studyType: Int) {
        with(binding.textViewStudyAllType) {
            when (studyType) {
                1 -> {
                    text = "스터디"
                    binding.imageViewStudyAllStudyTypeIcon.setImageResource(R.drawable.icon_closed_book_24px)
                }
                2 -> {
                    text = "프로젝트"
                    binding.imageViewStudyAllStudyTypeIcon.setImageResource(R.drawable.icon_code_box_24px)
                }
                3 -> {
                    text = "공모전"
                    binding.imageViewStudyAllStudyTypeIcon.setImageResource(R.drawable.icon_trophy_24px)
                }
            }
        }
    }

    private fun setStudyMembers(studyData: Pair<StudyData, Int>) {
        binding.textViewStudyAllMaxMember.text = studyData.first.studyMaxMember.toString()
        binding.textViewStudyAllCurrentMember.text = studyData.second.toString()
    }

    private fun setStudyApplyMethod(studyData: Pair<StudyData, Int>) {
        with(binding) {
            textViewStudyAllApplyMethod.text = when (studyData.first.studyApplyMethod) {
                1 -> "선착순"
                2 -> "신청제"
                else -> ""
            }
        }
    }

    private fun setupFavoriteButton(studyData: Pair<StudyData, Int>) {
        with(binding.imageViewStudyAllFavorite) {
            // 초기 좋아요 상태 설정
            if (studyData.first.studyLikeState) {
                setImageResource(R.drawable.icon_favorite_full_24px)
                setColorFilter(Color.parseColor("#D73333"))
            } else {
                setImageResource(R.drawable.icon_favorite_24px)
                clearColorFilter()
            }

            // 클릭 리스너 설정
            setOnClickListener {
                likeClickListener.invoke(studyData.first.studyIdx)
                // 좋아요 상태 변경 후 UI 업데이트
                studyData.first.studyLikeState = !studyData.first.studyLikeState
                if (studyData.first.studyLikeState) {
                    setImageResource(R.drawable.icon_favorite_full_24px)
                    setColorFilter(Color.parseColor("#D73333"))
                } else {
                    setImageResource(R.drawable.icon_favorite_24px)
                    clearColorFilter()
                }
            }
        }
    }
}

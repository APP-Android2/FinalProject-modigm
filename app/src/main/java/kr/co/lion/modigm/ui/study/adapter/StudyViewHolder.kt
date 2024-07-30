package kr.co.lion.modigm.ui.study.adapter

import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowStudyBinding
import kr.co.lion.modigm.model.SqlStudyData

class StudyViewHolder(
    private val binding: RowStudyBinding,
    private val rowClickListener: (Int) -> Unit,
    private val favoriteClickListener: (Int, Boolean) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(studyData: Triple<SqlStudyData, Int, Boolean>) {
        with(binding) {
            setupRootView(studyData)
            // 스터디 이미지 설정
//            setStudyImage(studyData.first.studyPic)
            // 스터디 모집 상태 (모집중, 모집완료)
            textViewStudyCanApply.text = studyData.first.studyCanApply
            // 진행 방식 (온라인, 오프라인, 온/오프혼합)
            setStudyOnOffline(studyData.first.studyOnOffline)
            // 스터디 제목
            textViewStudyTitle.text = studyData.first.studyTitle
            // 활동 타입 (스터디, 프로젝트, 공모전)
            setStudyType(studyData.first.studyType)
            // 스터디 현재 인원수, 최대 인원수
            setStudyMembers(studyData)
            // 신청 방식 (선착순, 신청제)
            textViewStudyApplyMethod.text = studyData.first.studyApplyMethod
            // 찜 상태
            setFavoriteButton(studyData)
        }
    }

    private fun setupRootView(studyData: Triple<SqlStudyData, Int, Boolean>) {
        with(binding.root) {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                rowClickListener.invoke(studyData.first.studyIdx)
            }
            binding.imageViewStudyPic.setOnClickListener {
                // 현재는 빈 구현
            }
        }
    }

    // 스터디 진행 방식 설정 (온라인/오프라인/혼합)
    private fun setStudyOnOffline(studyOnOffline: String) {
        with(binding.textViewStudyOnOffline) {
            // 스터디 진행 방식에 따라 텍스트와 텍스트 색상을 설정
            when (studyOnOffline) {
                "온라인" -> {
                    text = "온라인"
                    setTextColor(Color.parseColor("#0FA981"))
                }
                "오프라인" -> {
                    text = "오프라인"
                    setTextColor(Color.parseColor("#EB9C58"))
                }
                "온오프혼합" -> {
                    text = "온오프혼합"
                    setTextColor(Color.parseColor("#0096FF"))
                }
            }
        }
    }

//    private fun setStudyImage(imageFileName: String) {
//        if (imageFileName.isNotEmpty()) {
//            val storageRef = FirebaseStorage.getInstance().reference.child("studyPic/$imageFileName")
//            storageRef.downloadUrl.addOnSuccessListener { uri ->
//                Glide.with(itemView.context)
//                    .load(uri)
//                    .into(binding.imageViewStudyAllPic)
//            }.addOnFailureListener {
//                binding.imageViewStudyAllPic.setImageResource(R.drawable.image_detail_1)
//            }
//        } else {
//            binding.imageViewStudyAllPic.setImageResource(R.drawable.image_detail_2)
//        }
//    }

    private fun setStudyType(studyType: String) {
        with(binding.textViewStudyType) {
            when (studyType) {
                "스터디" -> {
                    text = "스터디"
                    binding.imageViewStudyStudyTypeIcon.setImageResource(R.drawable.icon_closed_book_24px)
                }
                "프로젝트" -> {
                    text = "프로젝트"
                    binding.imageViewStudyStudyTypeIcon.setImageResource(R.drawable.icon_code_box_24px)
                }
                "공모전" -> {
                    text = "공모전"
                    binding.imageViewStudyStudyTypeIcon.setImageResource(R.drawable.icon_trophy_24px)
                }
            }
        }
    }

    private fun setStudyMembers(studyData: Triple<SqlStudyData, Int, Boolean>) {
        binding.textViewStudyMaxMember.text = studyData.first.studyMaxMember.toString()
        binding.textViewStudyCurrentMember.text = studyData.second.toString()
    }

    private fun setFavoriteButton(studyData: Triple<SqlStudyData, Int, Boolean>) {
        with(binding.imageViewStudyFavorite) {
            if (studyData.third) {
                setImageResource(R.drawable.icon_favorite_full_24px)
                setColorFilter(Color.parseColor("#D73333"))
            } else {
                setImageResource(R.drawable.icon_favorite_24px)
                clearColorFilter()
            }

            setOnClickListener {
                favoriteClickListener.invoke(studyData.first.studyIdx, studyData.third)
            }
        }
    }
}
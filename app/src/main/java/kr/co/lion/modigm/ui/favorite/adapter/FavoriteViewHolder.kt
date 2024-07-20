package kr.co.lion.modigm.ui.favorite.adapter

import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowFavoriteBinding
import kr.co.lion.modigm.model.SqlStudyData

class FavoriteViewHolder(
    private val binding: RowFavoriteBinding,
    private val rowClickListener: (Int) -> Unit,
    private val favoriteClickListener: (Int) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(favoriteData: Triple<SqlStudyData, Int, Boolean>) {
        with(binding) {
            setupRootView(favoriteData)
            // 스터디 이미지 설정
//            setStudyImage(studyData.first.studyPic)
            // 스터디 모집 상태 (모집중, 모집완료)
            textViewFavoriteCanApply.text = favoriteData.first.studyCanApply
            // 진행 방식 (온라인, 오프라인, 온/오프혼합)
            setStudyOnOffline(favoriteData.first.studyOnOffline)
            // 스터디 제목
            textViewFavoriteTitle.text = favoriteData.first.studyTitle
            // 활동 타입 (스터디, 프로젝트, 공모전)
            setStudyType(favoriteData.first.studyType)
            // 스터디 현재 인원수, 최대 인원수
            setStudyMembers(favoriteData)
            // 신청 방식 (선착순, 신청제)
            textViewFavoriteApplyMethod.text = favoriteData.first.studyApplyMethod
            // 찜 상태
            setupFavoriteButton(favoriteData)
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
            binding.imageViewFavoritePic.setOnClickListener {
                // 현재는 빈 구현
            }
        }
    }

    // 스터디 진행 방식 설정 (온라인/오프라인/혼합)
    private fun setStudyOnOffline(studyOnOffline: String) {
        with(binding.textViewFavoriteOnOffline) {
            // 스터디 진행 방식에 따라 텍스트와 텍스트 색상을 설정
            when (studyOnOffline) {
                "온라인" -> {
                    text = "온라인"
                    setTextColor(android.graphics.Color.parseColor("#0FA981"))
                }

                "오프라인" -> {
                    text = "오프라인"
                    setTextColor(android.graphics.Color.parseColor("#EB9C58"))
                }

                "온오프혼합" -> {
                    text = "온오프혼합"
                    setTextColor(android.graphics.Color.parseColor("#0096FF"))
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
        with(binding.textViewFavoriteType) {
            when (studyType) {
                "스터디" -> {
                    text = "스터디"
                    binding.imageViewFavoriteFavoriteTypeIcon.setImageResource(kr.co.lion.modigm.R.drawable.icon_closed_book_24px)
                }

                "프로젝트" -> {
                    text = "프로젝트"
                    binding.imageViewFavoriteFavoriteTypeIcon.setImageResource(kr.co.lion.modigm.R.drawable.icon_code_box_24px)
                }

                "공모전" -> {
                    text = "공모전"
                    binding.imageViewFavoriteFavoriteTypeIcon.setImageResource(kr.co.lion.modigm.R.drawable.icon_trophy_24px)
                }
            }
        }
    }

    private fun setStudyMembers(studyData: Triple<SqlStudyData, Int, Boolean>) {
        binding.textViewFavoriteMaxMember.text = studyData.first.studyMaxMember.toString()
        binding.textViewFavoriteCurrentMember.text = studyData.second.toString()
    }

    private fun setupFavoriteButton(studyData: Triple<SqlStudyData, Int, Boolean>) {
        with(binding.imageViewFavoriteFavorite) {
            if (studyData.third) {
                setImageResource(R.drawable.icon_favorite_full_24px)
                setColorFilter(Color.parseColor("#D73333"))
            } else {
                setImageResource(R.drawable.icon_favorite_24px)
                clearColorFilter()
            }

            setOnClickListener {
                favoriteClickListener.invoke(studyData.first.studyIdx)
            }
        }
    }
}

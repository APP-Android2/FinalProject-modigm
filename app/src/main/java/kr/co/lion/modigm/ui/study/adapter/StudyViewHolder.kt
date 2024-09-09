package kr.co.lion.modigm.ui.study.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowStudyBinding
import kr.co.lion.modigm.model.StudyData

class StudyViewHolder(
    private val binding: RowStudyBinding,
    private val rowClickListener: (Int) -> Unit,
    private val favoriteClickListener: (Int, Boolean) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    // 태그
    private val logTag by lazy { StudyViewHolder::class.simpleName }

    fun bind(studyData: Triple<StudyData, Int, Boolean>) {
        with(binding) {
            setupRootView(studyData)
            // 스터디 이미지 설정
            setStudyImage(studyData)
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
            // 찜 상태
            setFavoriteButton(studyData)
        }
    }

    private fun setupRootView(studyData: Triple<StudyData, Int, Boolean>) {
        with(binding) {
            root.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    rowClickListener.invoke(studyData.first.studyIdx)
                }
            }
        }
    }

    // 스터디 이미지 설정
    private fun setStudyImage(studyData: Triple<StudyData, Int, Boolean>) {
        with(binding) {
            // 프로그래스바를 활성화하고 보이도록 설정
            progressBarStudyPic.visibility = View.VISIBLE
            imageViewStudyPic.visibility = View.GONE

            // Glide로 비동기 이미지 로딩
            val requestOptions = RequestOptions()
                .placeholder(R.drawable.image_loading_gray) // 로딩 중일 때 표시할 이미지
                .error(R.drawable.image_detail_1) // 오류 발생 시 표시할 이미지

            Glide.with(itemView.context)
                .load(studyData.first.studyPic)
                .apply(requestOptions)
                .into(object : CustomViewTarget<ImageView, Drawable>(imageViewStudyPic) {
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        // 로딩 실패 시
                        progressBarStudyPic.visibility = View.GONE
                        imageViewStudyPic.visibility = View.VISIBLE
                        imageViewStudyPic.setImageDrawable(errorDrawable)
                    }

                    override fun onResourceCleared(placeholder: Drawable?) {
                        // 리소스가 클리어 될 때
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                    ) {
                        // 로딩 성공 시
                        progressBarStudyPic.visibility = View.GONE
                        imageViewStudyPic.visibility = View.VISIBLE
                        imageViewStudyPic.setImageDrawable(resource)
                    }
                })

            // 이미지 클릭 시
            imageViewStudyPic.setOnClickListener {
                rowClickListener.invoke(studyData.first.studyIdx)
            }
        }
    }

    // 스터디 진행 방식 설정 (온라인/오프라인/혼합)
    private fun setStudyOnOffline(studyOnOffline: String) {
        with(binding) {
            textViewStudyOnOffline.apply {
                // 스터디 진행 방식에 따라 텍스트와 텍스트 색상을 설정
                text = studyOnOffline
                when (studyOnOffline) {
                    "온라인" -> setTextColor(Color.parseColor("#0FA981"))
                    "오프라인" -> setTextColor(Color.parseColor("#EB9C58"))
                    "온오프혼합" -> setTextColor(Color.parseColor("#0096FF"))
                }
            }
        }
    }

    private fun setStudyType(studyType: String) {
        with(binding) {
            textViewStudyType.apply {
                text = studyType
                with(imageViewStudyStudyTypeIcon) {
                    when (studyType) {
                        "스터디" -> setImageResource(R.drawable.icon_closed_book_24px)
                        "프로젝트" -> setImageResource(R.drawable.icon_code_box_24px)
                        "공모전" -> setImageResource(R.drawable.icon_trophy_24px)
                    }
                }
            }
        }
    }

    private fun setStudyMembers(studyData: Triple<StudyData, Int, Boolean>) {
        with(binding) {
            textViewStudyMaxMember.apply {
                text = studyData.first.studyMaxMember.toString()
            }
            textViewStudyCurrentMember.apply {
                text = studyData.second.toString()
            }
        }
    }

    private fun setFavoriteButton(studyData: Triple<StudyData, Int, Boolean>) {
        with(binding) {
            imageViewStudyFavorite.apply {
                if (studyData.third) {
                    setImageResource(R.drawable.icon_favorite_full_24px)
                    setColorFilter(Color.parseColor("#D73333"))
                    isEnabled = true
                } else {
                    setImageResource(R.drawable.icon_favorite_24px)
                    clearColorFilter()
                    isEnabled = true
                }

                setOnClickListener {
                    isEnabled = false
                    favoriteClickListener.invoke(studyData.first.studyIdx, studyData.third)
                }
            }
        }

    }
}
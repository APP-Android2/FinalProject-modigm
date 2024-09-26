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
        setupRootView(studyData)
        // 스터디 이미지 설정
        setStudyImage(studyData)
        // 스터디 모집 상태 (모집중, 모집완료)
        setStudyCanApply(studyData)
        // 진행 방식 (온라인, 오프라인, 온/오프혼합)
        setStudyOnOffline(studyData)
        // 스터디 제목
        setStudyTitle(studyData)
        // 활동 타입 (스터디, 프로젝트, 공모전)
        setStudyType(studyData)
        // 스터디 현재 인원수, 최대 인원수
        setStudyMembers(studyData)
        // 찜 상태
        setFavoriteButton(studyData)
    }

    // 스터디 뷰 설정
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
            imageViewStudyPic.apply {
                setOnClickListener {
                    rowClickListener.invoke(studyData.first.studyIdx)
                }
            }
        }
    }

    // 스터디 모집 상태 설정 (모집중, 모집완료)
    private fun setStudyCanApply(studyData: Triple<StudyData, Int, Boolean>) {
        with(binding) {
            textViewStudyCanApply.apply {
                if(studyData.first.studyCanApply == "모집중") {
                    // 모집중인 경우
                    text = studyData.first.studyCanApply
                    setTextColor(Color.parseColor("#1A51C5"))
                } else {
                    // 모집완료인 경우
                    text = studyData.first.studyCanApply
                    setTextColor(Color.parseColor("#BBBBBB"))
                }
            }
        }
    }

    // 스터디 진행 방식 설정 (온라인/오프라인/혼합)
    private fun setStudyOnOffline(studyData: Triple<StudyData, Int, Boolean>) {
        with(binding) {
            textViewStudyOnOffline.apply {
                text = studyData.first.studyOnOffline
                if(studyData.first.studyCanApply == "모집중") {
                    // 모집중인 경우
                    // 스터디 진행 방식에 따라 텍스트와 텍스트 색상을 설정
                    when (studyData.first.studyOnOffline) {
                        "온라인" -> setTextColor(Color.parseColor("#0FA981"))
                        "오프라인" -> setTextColor(Color.parseColor("#EB9C58"))
                        "온오프혼합" -> setTextColor(Color.parseColor("#0096FF"))
                    }
                } else {
                    // 모집완료인 경우
                    setTextColor(Color.parseColor("#BBBBBB"))
                }
            }
        }
    }

    private fun setStudyTitle(studyData: Triple<StudyData, Int, Boolean>) {
        with(binding) {
            textViewStudyTitle.apply {
                // 스터디 제목 설정
                text = studyData.first.studyTitle
                if(studyData.first.studyCanApply == "모집중") {
                    // 모집중인 경우(블랙)
                    setTextColor(Color.parseColor("#000000"))
                } else {
                    // 모집완료인 경우(그레이)
                    setTextColor(Color.parseColor("#BBBBBB"))
                }

            }
        }
    }

    // 스터디 타입 설정 (스터디, 프로젝트, 공모전)
    private fun setStudyType(studyData: Triple<StudyData, Int, Boolean>) {
        with(binding) {
            // 스터디 타입 텍스트 설정
            textViewStudyType.apply {
                if(studyData.first.studyCanApply == "모집중") {
                    // 모집중인 경우
                    setTextColor(Color.parseColor("#777777"))
                } else {
                    // 모집완료인 경우
                    setTextColor(Color.parseColor("#BBBBBB"))
                }
                text = studyData.first.studyType

            }
            // 스터디 타입 아이콘 설정
            imageViewStudyStudyTypeIcon.apply {
                if(studyData.first.studyCanApply == "모집중") {
                    // 모집중인 경우
                    setColorFilter(Color.parseColor("#777777"))
                } else {
                    // 모집완료인 경우
                    setColorFilter(Color.parseColor("#BBBBBB"))
                }
                when (studyData.first.studyType) {
                    "스터디" -> setImageResource(R.drawable.icon_closed_book_24px)
                    "프로젝트" -> setImageResource(R.drawable.icon_code_box_24px)
                    "공모전" -> setImageResource(R.drawable.icon_trophy_24px)
                }
            }
        }
    }

    private fun setStudyMembers(studyData: Triple<StudyData, Int, Boolean>) {
        with(binding) {

            textViewStudyMaxMember.apply {
                if(studyData.first.studyCanApply == "모집중") {
                    // 모집중인 경우
                    setTextColor(Color.parseColor("#777777"))
                } else {
                    // 모집완료인 경우
                    setTextColor(Color.parseColor("#BBBBBB"))
                }
                text = studyData.first.studyMaxMember.toString()
            }
            textViewStudyCurrentMember.apply {
                if(studyData.first.studyCanApply == "모집중") {
                    // 모집중인 경우
                    setTextColor(Color.parseColor("#777777"))
                } else {
                    // 모집완료인 경우
                    setTextColor(Color.parseColor("#BBBBBB"))
                }
                text = studyData.second.toString()
            }
            imageViewStudyMaxMemberIcon.apply {
                if(studyData.first.studyCanApply == "모집중") {
                    // 모집중인 경우
                    setColorFilter(Color.parseColor("#777777"))
                } else {
                    // 모집완료인 경우
                    setColorFilter(Color.parseColor("#BBBBBB"))
                }
            }
            textViewStudyMaxMemberSlash.apply {
                if(studyData.first.studyCanApply == "모집중") {
                    // 모집중인 경우
                    setTextColor(Color.parseColor("#777777"))
                } else {
                    // 모집완료인 경우
                    setTextColor(Color.parseColor("#BBBBBB"))
                }
            }
            textViewStudyMaxMemberLast.apply {
                if(studyData.first.studyCanApply == "모집중") {
                    // 모집중인 경우
                    setTextColor(Color.parseColor("#777777"))
                } else {
                    // 모집완료인 경우
                    setTextColor(Color.parseColor("#BBBBBB"))
                }
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
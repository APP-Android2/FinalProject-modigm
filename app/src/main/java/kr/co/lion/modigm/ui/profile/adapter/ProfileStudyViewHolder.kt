package kr.co.lion.modigm.ui.profile.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowProfileStudyBinding
import kr.co.lion.modigm.model.StudyData

class ProfileStudyViewHolder(
    private val context: Context,
    private val rowProfileStudyBinding: RowProfileStudyBinding,
    private val rowClickListener: (Int) -> Unit, ): RecyclerView.ViewHolder(rowProfileStudyBinding.root) {

    // 구성요소 세팅
    fun bind(data: StudyData, rowClickListener: (Int) -> Unit) {
        rowProfileStudyBinding.apply {
            // 썸네일
            setStudyImage(data)
            // 스터디 제목
            textViewRowHostStudyTitle.text = data.studyTitle
            // 스터디 분류 아이콘
            when (data.studyType) {
                "스터디" -> imageCategory.setImageResource(R.drawable.icon_closed_book_24px)
                "프로젝트" -> imageCategory.setImageResource(R.drawable.icon_code_box_24px)
                "공모전" -> imageCategory.setImageResource(R.drawable.icon_trophy_24px)
                else -> imageCategory.setImageResource(R.drawable.icon_closed_book_24px)
            }
            // 스터디 분류
            textViewRowHostStudyCategory.text = data.studyType
            // 스터디 진행 방식
            textViewRowHostStudyLocation.text = data.studyOnOffline
            // 모집 중 / 모집 완료
            textViewRowHostStudyMember.text = data.studyCanApply

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

    // 스터디 이미지 설정
    private fun setStudyImage(data: StudyData) {
        Log.d("ProfileStudyViewHolder", "setStudyImage called with: ${data.studyPic}")

        rowProfileStudyBinding.apply {
            // 프로그래스바를 활성화하고 보이도록 설정
            progressBarProfileStudyPic.visibility = View.VISIBLE
            imageRowHostStudy.visibility = View.INVISIBLE

            Log.d("ProfileStudyViewHolder", "1")

            val requestOptions = RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.image_loading_gray) // 필요 시 기본 플레이스홀더 설정
                .error(R.drawable.image_detail_1) // 이미지 로딩 실패 시 표시할 이미지

            Log.d("ProfileStudyViewHolder", "2")


            Glide.with(itemView.context)
                .load(data.studyPic)
                .apply(requestOptions)
                .into(object : CustomViewTarget<ImageView, Drawable>(imageRowHostStudy) {
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        // 로딩 실패 시
                        Log.d("ProfileStudyViewHolder", "Glide onLoadFailed called with: ${data.studyPic}")
//                        progressBarProfileStudyPic.visibility = View.GONE
//                        imageRowHostStudy.visibility = View.VISIBLE
                        imageRowHostStudy.setImageDrawable(errorDrawable)
                    }

                    override fun onResourceCleared(placeholder: Drawable?) {
                        // 리소스가 클리어 될 때
                        Log.d("ProfileStudyViewHolder", "Glide onResourceCleared called")
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                    ) {
                        // 로딩 성공 시
                        Log.d("ProfileStudyViewHolder", "Glide onResourceReady called with: ${data.studyPic}")
                        progressBarProfileStudyPic.visibility = View.GONE
                        imageRowHostStudy.visibility = View.VISIBLE
                        imageRowHostStudy.setImageDrawable(resource)
                    }
                })

            Log.d("ProfileStudyViewHolder", "3")

        }
    }
}
package kr.co.lion.modigm.ui.profile.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowLinkBinding

class LinkViewHolder(
    private val context: Context,
    private val rowLinkBinding: RowLinkBinding,
    private val rowClickListener: (String) -> Unit, ): RecyclerView.ViewHolder(rowLinkBinding.root) {

    // 도메인에 따른 아이콘을 저장하는 Map
    val domainIcons = mapOf(
        "youtube.com" to R.drawable.icon_youtube_logo,
        "github.com" to R.drawable.icon_github_logo,
        "linkedin.com" to R.drawable.icon_linkedin_logo,
        "facebook.com" to "📘",
        "twitter.com" to "🐦",
        "linkedin.com" to "🔗",

        "default" to "🌐"  // 도메인을 찾을 수 없는 경우 기본 아이콘
    )

    val iconStr = domainIcons["youtube.com"] ?: domainIcons["default"]

    // 구성요소 세팅
    fun bind(data: String, rowClickListener: (String) -> Unit) {
        rowLinkBinding.apply {
            // 아이콘
            imageRowLink.setImageResource(R.drawable.icon_arrow_back_24px)

            // 항목에 대한 설정
            root.apply {
                // 항목 클릭 시 클릭되는 범위 설정
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                // 클릭 리스너 설정: Url 전달..?
                setOnClickListener {
                    rowClickListener.invoke("https://github.com/orgs/APP-Android2/projects/25/views/1")
                }
            }
        }
    }
}
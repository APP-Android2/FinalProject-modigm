package kr.co.lion.modigm.ui.profile.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowLinkBinding
import kr.co.lion.modigm.model.UserData
import java.net.URL

class LinkViewHolder(
    private val context: Context,
    private val rowLinkBinding: RowLinkBinding,
    private val rowClickListener: (String) -> Unit, ): RecyclerView.ViewHolder(rowLinkBinding.root) {

    // 도메인에 따른 아이콘을 저장하는 Map
    val domainIcons = mapOf(
        "youtube.com" to R.drawable.icon_youtube_logo,
        "github.com" to R.drawable.icon_github_logo,
        "linkedin.com" to R.drawable.icon_linkedin_logo,
        "velog.com" to R.drawable.icon_velog_logo,
        "tistory.com" to R.drawable.icon_tistory_logo,
        "instagram.com" to R.drawable.icon_instagram_logo,
        "notion.com" to R.drawable.icon_notion_logo,
        "facebook.com" to R.drawable.icon_facebook_logo,
        "twitter.com" to R.drawable.icon_twitter_logo,
        "default" to R.drawable.icon_link,  // 도메인을 찾을 수 없는 경우 기본 아이콘
    )

    val iconStr = domainIcons["youtube.com"] ?: domainIcons["default"]

    // 구성요소 세팅
    fun bind(data: String, rowClickListener: (String) -> Unit) {
        rowLinkBinding.apply {
            // 도메인 추출
            val domain = extractDomain(data)

            // 아이콘
            imageRowLink.setImageResource(domainIcons[domain] ?: domainIcons["default"]!!)

            // 항목에 대한 설정
            root.apply {
                // 항목 클릭 시 클릭되는 범위 설정
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                // 클릭 리스너 설정: Url 전달..?
                setOnClickListener {
                    rowClickListener.invoke(data)
                }
            }
        }
    }

    // URL에서 도메인을 추출하는 함수
    private fun extractDomain(url: String): String {
        return try {
            val uri = URL(url)
            val domain = uri.host
            // www. 접두사를 제거
            if (domain.startsWith("www.")) domain.substring(4) else domain
        } catch (e: Exception) {
            "invalid"
        }
    }
}
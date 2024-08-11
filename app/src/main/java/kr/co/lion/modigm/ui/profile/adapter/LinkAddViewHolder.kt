package kr.co.lion.modigm.ui.profile.adapter

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowLinkAddBinding
import kr.co.lion.modigm.ui.profile.vm.EditProfileViewModel
import java.net.URL

class LinkAddViewHolder(
    private val context: Context,
    private val rowLinkAddBinding: RowLinkAddBinding,
    private val editProfileViewModel: EditProfileViewModel): RecyclerView.ViewHolder(rowLinkAddBinding.root) {

    // 도메인에 따른 아이콘을 저장하는 Map
    val domainIcons = mapOf(
        "youtube.com" to R.drawable.icon_youtube_logo,
        "github.com" to R.drawable.icon_github_logo,
        "linkedin.com" to R.drawable.icon_linkedin_logo,
        "velog.io" to R.drawable.icon_velog_logo,
        "instagram.com" to R.drawable.icon_instagram_logo,
        "notion.com" to R.drawable.icon_notion_logo,
        "facebook.com" to R.drawable.icon_facebook_logo,
        "twitter.com" to R.drawable.icon_twitter_logo,
        "open.kakao.com" to R.drawable.kakaotalk_sharing_btn_small,
        "default" to R.drawable.icon_link,  // 도메인을 찾을 수 없는 경우 기본 아이콘
    )

    // 구성요소 세팅
    fun bind(data: String) {
        rowLinkAddBinding.apply {
            // 도메인 추출
            val domain = extractDomain(data)
            Log.d("test1234", domain)

            // 항목 클릭 시 클릭되는 범위 설정
            root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // 아이콘
            imageRowLinkAddIcon.setImageResource(domainIcons[domain] ?: domainIcons["default"]!!)

            // 링크
            textViewRowLinkAdd.text = data

            // 삭제
            iconRowLinkAddDelete.setOnClickListener {
                editProfileViewModel.removeLinkFromList(data)
            }

            // 아이템을 길게 눌렀을 때 햅틱 피드백 추가
            root.setOnLongClickListener {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(50)
                }

                root.setBackgroundColor(ContextCompat.getColor(context, R.color.dividerView))

                true // true를 반환하면 long click 이벤트가 소비됩니다.
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
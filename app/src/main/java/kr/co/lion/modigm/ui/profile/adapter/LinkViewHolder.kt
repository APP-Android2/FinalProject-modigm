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

    // 구성요소 세팅
    fun bind(data: String, rowClickListener: (String) -> Unit) { // String 말고 모델이어야함
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
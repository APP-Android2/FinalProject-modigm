package kr.co.lion.modigm.ui.study.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowStudyMyBinding

class StudyMyViewHolder(
    private val binding: RowStudyMyBinding,
    private val rowClickListener: (Int) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root) {


    // 전체 스터디 항목별 세팅
    fun bind(rowClickListener: (Int) -> Unit) {

        with(binding) {
            // 항목 하나
            with(root) {
                layoutParams = ViewGroup.LayoutParams(
                    // 항목 클릭 시 클릭되는 범위 설정
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                // 클릭 리스너 설정.
                setOnClickListener {
                }
            }
        }
    }
}
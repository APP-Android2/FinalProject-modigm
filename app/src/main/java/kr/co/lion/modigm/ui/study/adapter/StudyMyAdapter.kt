package kr.co.lion.modigm.ui.study.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowStudyMyBinding

class StudyMyAdapter(
    private val rowClickListener: (Int) -> Unit,
) : RecyclerView.Adapter<StudyMyViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): StudyMyViewHolder {
        val binding: RowStudyMyBinding =
            RowStudyMyBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return StudyMyViewHolder(
            binding,
            rowClickListener,
        )
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(holder: StudyMyViewHolder, position: Int) {
        holder.bind(rowClickListener)
    }
}
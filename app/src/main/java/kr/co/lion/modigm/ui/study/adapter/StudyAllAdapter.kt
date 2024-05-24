package kr.co.lion.modigm.ui.study.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowStudyAllBinding

class StudyAllAdapter(
    private val rowClickListener: (Int) -> Unit,
) : RecyclerView.Adapter<StudyAllViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): StudyAllViewHolder {
        val binding: RowStudyAllBinding =
            RowStudyAllBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return StudyAllViewHolder(
            binding,
            rowClickListener,
        )
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(holder: StudyAllViewHolder, position: Int) {
        holder.bind(rowClickListener)
    }
}




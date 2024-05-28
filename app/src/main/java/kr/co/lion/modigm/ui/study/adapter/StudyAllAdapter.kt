package kr.co.lion.modigm.ui.study.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowStudyAllBinding
import kr.co.lion.modigm.model.StudyData

class StudyAllAdapter(
    private var studyList: List<StudyData>,
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
        return studyList.size
    }

    override fun onBindViewHolder(holder: StudyAllViewHolder, position: Int) {
        holder.bind(studyList[position],rowClickListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<StudyData>) {
        studyList = list
        notifyDataSetChanged()
    }
}




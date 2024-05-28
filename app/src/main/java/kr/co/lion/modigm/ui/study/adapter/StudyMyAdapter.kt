package kr.co.lion.modigm.ui.study.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowStudyMyBinding
import kr.co.lion.modigm.model.StudyData

class StudyMyAdapter(
    private var studyList: List<StudyData>,
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
        return studyList.size
    }

    override fun onBindViewHolder(holder: StudyMyViewHolder, position: Int) {
        holder.bind(studyList[position],rowClickListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<StudyData>) {
        studyList = list
        notifyDataSetChanged()
    }
}
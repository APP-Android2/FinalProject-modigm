package kr.co.lion.modigm.ui.study.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowStudyAllBinding
import kr.co.lion.modigm.model.StudyData

class StudyAllAdapter(
    // 모집중인 스터디 리스트
    private var studyList: List<Pair<StudyData, Int>>,
    // 항목 1개 클릭 리스너
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
        holder.bind(studyList[position])
    }

    // 목록 새로고침
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<Pair<StudyData, Int>>) {
        studyList = list
        notifyDataSetChanged()
        Log.d("update adapter", list.toString())
    }
}




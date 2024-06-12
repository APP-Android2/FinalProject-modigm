package kr.co.lion.modigm.ui.study.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowStudyMyBinding
import kr.co.lion.modigm.model.StudyData

class StudyMyAdapter(
    // 내 스터디 리스트
    private var studyList: List<Pair<StudyData, Int>>,
    // 항목 1개 클릭 리스너
    private val rowClickListener: (Int) -> Unit,
    private val likeClickListener: (Int) -> Unit,
) : RecyclerView.Adapter<StudyMyViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): StudyMyViewHolder {
        val binding: RowStudyMyBinding =
            RowStudyMyBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return StudyMyViewHolder(
            binding,
            rowClickListener,
            likeClickListener,
        )
    }

    override fun getItemCount(): Int {
        return studyList.size
    }

    override fun onBindViewHolder(holder: StudyMyViewHolder, position: Int) {
        holder.bind(studyList[position])
        Log.d("StudyMyAdapter", "onBindViewHolder: position = $position, studyIdx = ${studyList[position].first.studyIdx}")
    }

    // 목록 새로고침
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<Pair<StudyData, Int>>) {
        studyList = list
        notifyDataSetChanged()
        Log.d("StudyMyAdapter", "updateData: ${list.size} 개의 데이터로 업데이트")
    }
}
package kr.co.lion.modigm.ui.study.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowStudyMyBinding
import kr.co.lion.modigm.model.StudyData

class StudySearchAdapter(
    private var studyList: List<Pair<StudyData, Int>>,
    // 항목 1개 클릭 리스너
    private val rowClickListener: (Int) -> Unit,
    private val likeClickListener: (Int) -> Unit,
) : RecyclerView.Adapter<StudySearchViewHolder>() {

    private var filteredList: List<Pair<StudyData, Int>> = studyList

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): StudySearchViewHolder {
        val binding: RowStudyMyBinding =
            RowStudyMyBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return StudySearchViewHolder(
            binding,
            rowClickListener,
            likeClickListener,
        )
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: StudySearchViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    // 데이터 필터링
    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            emptyList() // 검색어가 빈 문자열일 때 빈 리스트로 설정
        } else {
            studyList.filter { it.first.studyTitle.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
        Log.d("update adapter", filteredList.toString())
    }


    // 목록 새로고침
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<Pair<StudyData, Int>>) {
        studyList = list
        notifyDataSetChanged()
        Log.d("update adapter", list.toString())
    }
}

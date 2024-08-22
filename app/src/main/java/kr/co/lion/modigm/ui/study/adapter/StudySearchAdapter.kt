package kr.co.lion.modigm.ui.study.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowStudyBinding
import kr.co.lion.modigm.model.SqlStudyData

class StudySearchAdapter(
    private var studyList: List<Triple<SqlStudyData, Int, Boolean>>,
    // 항목 1개 클릭 리스너
    private val rowClickListener: (Int) -> Unit,
    private val favoriteClickListener: (Int, Boolean) -> Unit,
) : RecyclerView.Adapter<StudyViewHolder>() {

    private var searchList: List<Triple<SqlStudyData, Int, Boolean>> = studyList

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): StudyViewHolder {
        val binding: RowStudyBinding =
            RowStudyBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return StudyViewHolder(
            binding,
            rowClickListener,
            favoriteClickListener,
        )
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

    override fun onBindViewHolder(holder: StudyViewHolder, position: Int) {
        holder.bind(searchList[position])
    }

    // 데이터 필터링
    @SuppressLint("NotifyDataSetChanged")
    fun search(query: String) {
        searchList = if (query.isEmpty()) {
            emptyList() // 검색어가 빈 문자열일 때 빈 리스트로 설정
        } else {
            studyList.filter { it.first.studyTitle.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
        Log.d("update adapter", searchList.toString())
    }


    // 목록 새로고침
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<Triple<SqlStudyData, Int, Boolean>>) {
        studyList = list
        notifyDataSetChanged()
        Log.d("update adapter", list.toString())
    }
}
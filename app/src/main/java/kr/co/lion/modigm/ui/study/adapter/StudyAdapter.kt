package kr.co.lion.modigm.ui.study.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowStudyBinding
import kr.co.lion.modigm.model.SqlStudyData

class StudyAdapter(
    // 스터디 리스트
    private var studyList: List<Triple<SqlStudyData, Int, Boolean>>,
    // 항목 1개 클릭 리스너
    private val rowClickListener: (Int) -> Unit,
    private val favoriteClickListener: (Int) -> Unit,
) : RecyclerView.Adapter<StudyViewHolder>()  {

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
        return studyList.size
    }

    override fun onBindViewHolder(holder: StudyViewHolder, position: Int) {
        holder.bind(studyList[position])
        Log.d("StudyAdapter", "onBindViewHolder: position = $position, studyIdx = ${studyList[position].first.studyIdx}")
    }

    // 목록 새로고침
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<Triple<SqlStudyData, Int, Boolean>>) {
        studyList = list
        notifyDataSetChanged()
        Log.d("StudyAdapter", "updateData: ${list.size} 개의 데이터로 업데이트")
    }

    fun updateItem(studyIdx: Int, isLiked: Boolean) {
        Log.d("updateItem", "함수 호출됨 - studyIdx: $studyIdx, isLiked: $isLiked")

        val index = studyList.indexOfFirst { it.first.studyIdx == studyIdx }
        Log.d("updateItem", "찾은 index: $index")

        if (index != -1) {
            val item = studyList[index]
            Log.d("updateItem", "기존 아이템: studyIdx: ${item.first.studyIdx}, isLiked: ${item.third}")

            // studyList를 변경하기 전 상태 출력
            Log.d("updateItem", "변경 전 studyList: $studyList")

            // studyList를 변경하고 상태 출력
            studyList = studyList.toMutableList().apply {
                set(index, Triple(item.first, item.second, isLiked))
            }

            // 변경된 studyList 상태 출력
            Log.d("updateItem", "변경 후 studyList: $studyList")

            // 아이템 변경 알림
            notifyItemChanged(index)
            Log.d("updateItem", "notifyItemChanged 호출됨 - index: $index")
        } else {
            Log.d("updateItem", "studyIdx에 해당하는 아이템을 찾을 수 없음 - studyIdx: $studyIdx")
        }
    }

}
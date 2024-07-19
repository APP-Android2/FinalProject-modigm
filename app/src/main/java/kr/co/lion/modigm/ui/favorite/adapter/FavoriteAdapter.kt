package kr.co.lion.modigm.ui.favorite.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowFavoriteBinding
import kr.co.lion.modigm.model.SqlStudyData

class FavoriteAdapter(
    private var favoriteList: List<Triple<SqlStudyData, Int, Boolean>>,
    private val rowClickListener: (Int) -> Unit,
    private val favoriteClickListener: (Int) -> Unit,
) : RecyclerView.Adapter<FavoriteViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding: RowFavoriteBinding =
            RowFavoriteBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return FavoriteViewHolder(
            binding,
            rowClickListener,
            favoriteClickListener,
        )
    }

    override fun getItemCount(): Int {
        return favoriteList.size
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favoriteList[position])
        Log.d(
            "FavoriteAdapter",
            "onBindViewHolder: position = $position, studyIdx = ${favoriteList[position].first.studyIdx}"
        )
    }




    // 목록 새로고침
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<Triple<SqlStudyData, Int, Boolean>>) {
        favoriteList = list
        notifyDataSetChanged()
        Log.d("StudyAdapter", "updateData: ${list.size} 개의 데이터로 업데이트")
    }

    fun updateItem(studyIdx: Int, isLiked: Boolean) {
        Log.d("updateItem", "함수 호출됨 - studyIdx: $studyIdx, isLiked: $isLiked")

        val index = favoriteList.indexOfFirst { it.first.studyIdx == studyIdx }
        Log.d("updateItem", "찾은 index: $index")

        if (index != -1) {
            val item = favoriteList[index]
            Log.d("updateItem", "기존 아이템: studyIdx: ${item.first.studyIdx}, isLiked: ${item.third}")

            // favoriteList를 변경하기 전 상태 출력
            Log.d("updateItem", "변경 전 favoriteList: $favoriteList")

            // favoriteList를 변경하고 상태 출력
            favoriteList = favoriteList.toMutableList().apply {
                set(index, Triple(item.first, item.second, isLiked))
            }

            // 변경된 favoriteList 상태 출력
            Log.d("updateItem", "변경 후 favoriteList: $favoriteList")

            // 아이템 변경 알림
            notifyItemChanged(index)
            Log.d("updateItem", "notifyItemChanged 호출됨 - index: $index")
        } else {
            Log.d("updateItem", "studyIdx에 해당하는 아이템을 찾을 수 없음 - studyIdx: $studyIdx")
        }
    }
}

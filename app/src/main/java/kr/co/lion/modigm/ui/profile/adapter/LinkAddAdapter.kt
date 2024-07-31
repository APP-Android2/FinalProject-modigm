package kr.co.lion.modigm.ui.profile.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowLinkAddBinding
import kr.co.lion.modigm.ui.profile.vm.EditProfileViewModel

class LinkAddAdapter(
    private var linkList: MutableList<String>,
    private val context: Context,
    private val editProfileViewModel: EditProfileViewModel
) : RecyclerView.Adapter<LinkAddViewHolder>(), ItemTouchHelperListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkAddViewHolder {
        val binding = RowLinkAddBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LinkAddViewHolder(context, binding, editProfileViewModel)
    }

    override fun onBindViewHolder(holder: LinkAddViewHolder, position: Int) {
        holder.bind(linkList[position])
    }

    override fun getItemCount(): Int {
        return linkList.size
    }

    // 아이템을 드래그되면 호출되는 메소드
    override fun onItemMove(from: Int, to: Int): Boolean {
        val name = linkList[from]
        // 리스트 갱신
        linkList.removeAt(from)
        linkList.add(to, name)

        // fromPosition에서 toPosition으로 아이템 이동 공지
        notifyItemMoved(from, to)
        // 링크 리스트 순서 변경
        editProfileViewModel.reorderLinks(from, to)
        return true
    }

    // 아이템 스와이프되면 호출되는 메소드
    override fun onItemSwipe(position: Int) {
        // 리스트 아이템 삭제
        linkList.removeAt(position)
        // 아이템 삭제되었다고 공지
        notifyItemRemoved(position)
    }

    fun updateData(newLinkList: MutableList<String>) {
        linkList = newLinkList
        notifyDataSetChanged()
    }
}

package kr.co.lion.modigm.ui.profile.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowLinkAddBinding
import kr.co.lion.modigm.ui.profile.vm.EditProfileViewModel

class LinkAddAdapter(
    private var linkList: List<String>,
    private val context: Context,
    private val editProfileViewModel: EditProfileViewModel
) : RecyclerView.Adapter<LinkAddViewHolder>() {

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

    fun updateData(newLinkList: List<String>) {
        linkList = newLinkList
        notifyDataSetChanged()
    }
}

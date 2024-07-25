package kr.co.lion.modigm.ui.profile.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowLinkBinding
import kr.co.lion.modigm.model.SqlUserLinkData
import java.util.Locale

class LinkAdapter(
    private var linkList: List<String>,
    private val rowClickListener: (String) -> Unit,
) : RecyclerView.Adapter<LinkViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): LinkViewHolder {
        val rowLinkBinding: RowLinkBinding = RowLinkBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return LinkViewHolder(viewGroup.context, rowLinkBinding, rowClickListener)
    }

    override fun getItemCount(): Int {
        return linkList.size
    }

    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        holder.bind(linkList[position], rowClickListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<String>) {
        linkList = list
        notifyDataSetChanged()
        Log.d("update adapter", list.toString())
    }
}
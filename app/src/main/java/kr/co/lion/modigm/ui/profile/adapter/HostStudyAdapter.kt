package kr.co.lion.modigm.ui.profile.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowHostStudyBinding
import kr.co.lion.modigm.databinding.RowPartStudyBinding

class HostStudyAdapter(
    private var hostStudyList: List<String>, // 바꿔야함
    private val rowClickListener: (String) -> Unit,
) : RecyclerView.Adapter<HostStudyViewHolder>() {


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): HostStudyViewHolder {
        val rowHostStudyBinding: RowHostStudyBinding = RowHostStudyBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return HostStudyViewHolder(viewGroup.context, rowHostStudyBinding, rowClickListener)
    }

    override fun getItemCount(): Int {
        return 5 //deliveryList.size
    }

    override fun onBindViewHolder(holder: HostStudyViewHolder, position: Int) {
        holder.bind("data", rowClickListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<String>) { //바꿔야함
        hostStudyList = list
        notifyDataSetChanged()
        Log.d("update adapter", list.toString())
    }
}
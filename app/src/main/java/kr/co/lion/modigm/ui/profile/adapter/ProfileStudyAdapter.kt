package kr.co.lion.modigm.ui.profile.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowHostStudyBinding
import kr.co.lion.modigm.model.StudyData

class ProfileStudyAdapter(
    private var hostStudyList: List<StudyData>,
    private val rowClickListener: (Int) -> Unit,
) : RecyclerView.Adapter<HostStudyViewHolder>() {


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): HostStudyViewHolder {
        val rowHostStudyBinding: RowHostStudyBinding = RowHostStudyBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return HostStudyViewHolder(viewGroup.context, rowHostStudyBinding, rowClickListener)
    }

    override fun getItemCount(): Int {
        return hostStudyList.size
    }

    override fun onBindViewHolder(holder: HostStudyViewHolder, position: Int) {
        holder.bind(hostStudyList[position], rowClickListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<StudyData>) {
        hostStudyList = list
        notifyDataSetChanged()
        Log.d("update adapter", list.toString())
    }
}
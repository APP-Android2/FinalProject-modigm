package kr.co.lion.modigm.ui.profile.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.databinding.RowProfileStudyBinding
import kr.co.lion.modigm.model.StudyData

class ProfileStudyAdapter(
    private var profileStudyList: List<StudyData>,
    private val rowClickListener: (Int) -> Unit, ) : RecyclerView.Adapter<ProfileStudyViewHolder>() {


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ProfileStudyViewHolder {
        val rowProfileStudyBinding: RowProfileStudyBinding = RowProfileStudyBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ProfileStudyViewHolder(viewGroup.context, rowProfileStudyBinding, rowClickListener)
    }

    override fun getItemCount(): Int {
        return profileStudyList.size
    }

    override fun onBindViewHolder(holder: ProfileStudyViewHolder, position: Int) {
        holder.bind(profileStudyList[position], rowClickListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<StudyData>) {
        profileStudyList = list
        notifyDataSetChanged()
        Log.d("update adapter", list.toString())
    }
}
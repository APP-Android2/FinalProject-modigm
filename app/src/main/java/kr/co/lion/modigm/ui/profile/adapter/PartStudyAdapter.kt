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
import kr.co.lion.modigm.databinding.RowPartStudyBinding
import kr.co.lion.modigm.model.StudyData
import java.util.Locale

// 배송지 화면의 RecyclerView의 어뎁터
class PartStudyAdapter(
    private var partStudyList: List<StudyData>, // 바꿔야함
    private val rowClickListener: (String) -> Unit,
) : RecyclerView.Adapter<PartStudyViewHolder>() {


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PartStudyViewHolder {
        val rowPartStudyBinding: RowPartStudyBinding = RowPartStudyBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return PartStudyViewHolder(viewGroup.context, rowPartStudyBinding, rowClickListener)
    }

    override fun getItemCount(): Int {
        return partStudyList.size
    }

    override fun onBindViewHolder(holder: PartStudyViewHolder, position: Int) {
        holder.bind(partStudyList[position], rowClickListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<StudyData>) { //바꿔야함
        partStudyList = list
        notifyDataSetChanged()
        Log.d("update adapter", list.toString())
    }
}
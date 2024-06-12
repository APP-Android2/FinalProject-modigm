package kr.co.lion.modigm.ui.write.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.SpinnerBinding

class OptionSpinnerAdapter(
    context: Context,
    @LayoutRes private val resId: Int,
    private val menuList: List<String>
) : ArrayAdapter<String>(context, resId, menuList) {

    // 드롭다운하지 않은 상태의 Spinner 항목의 뷰
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = SpinnerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        binding.inquireSpinnerText.text = menuList[position]

        return binding.root

//        return super.getView(position, convertView, parent) // 기본 스피너 아이템을 반환합니다.
    }

    // 드롭다운된 항목들 리스트의 뷰
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = SpinnerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.inquireSpinnerText.text = menuList[position]

        // Divider 추가
        if (position != menuList.size - 1) { // 마지막 항목이 아닌 경우에만 divider를 추가합니다.
            binding.spinnerDivider.visibility = View.VISIBLE
        } else {
            binding.spinnerDivider.visibility = View.GONE
        }

        return binding.root

    }

    override fun getCount() = menuList.size
}
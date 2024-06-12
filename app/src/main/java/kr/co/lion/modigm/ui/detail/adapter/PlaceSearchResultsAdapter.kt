package kr.co.lion.modigm.ui.detail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.detail.SimplePlace

class PlaceSearchResultsAdapter(
private var items: MutableList<SimplePlace>,
private val onItemClick: (SimplePlace) -> Unit // 클릭 이벤트를 위한 콜백 함수
) : RecyclerView.Adapter<PlaceSearchResultsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewName: TextView = view.findViewById(R.id.textViewPlaceName)
        val textViewAddress: TextView = view.findViewById(R.id.textViewPlaceAddress)
        fun bind(place: SimplePlace, clickListener: (SimplePlace) -> Unit) {
            textViewName.text = place.name
            textViewAddress.text = place.address
            itemView.setOnClickListener { clickListener(place) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_place, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount():Int = items.size

    fun updateItems(newItems: List<SimplePlace>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
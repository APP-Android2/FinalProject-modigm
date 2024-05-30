package kr.co.lion.modigm.ui.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R

class ChatRoomMemberAdapter(private val members: List<String>) : RecyclerView.Adapter<ChatRoomMemberAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val memberName: TextView = view.findViewById(R.id.textViewChatRoomMemberName)
        val memberProfile: ImageView = view.findViewById(R.id.imageViewRowChatRoomMemberProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_chatroom_member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = members[position]
        holder.memberName.text = member

        // Glide 라이브러리를 사용하여 프로필 이미지 로드
//        Glide.with(holder.itemView.context)
//            .load(member.memberProfileUrl)
//            .placeholder(R.drawable.ic_default_profile)
//            .into(holder.memberProfile)
    }
    override fun getItemCount() = members.size
}
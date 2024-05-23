package kr.co.lion.modigm.ui.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.chat.ChatRoomItem
import kr.co.lion.modigm.util.FragmentName

class ChatRoomAdapter(
    private val roomList: List<ChatRoomItem>,
    private val onItemClick: (ChatRoomItem) -> Unit,
    private val mainActivity: MainActivity
) : RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_chatroom_filed, parent, false)
        return ChatRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(roomList[position])
    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    inner class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val roomTitleTextView: TextView = itemView.findViewById(R.id.textViewRowChatroomFiledRoomTitle)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val room = roomList[position]
                    onItemClick(room)
                    mainActivity.replaceFragment(FragmentName.CHAT_ROOM, true, true, null)
                }
            }
        }

        fun bind(room: ChatRoomItem) {
            roomTitleTextView.text = room.chatTitle
        }
    }
}
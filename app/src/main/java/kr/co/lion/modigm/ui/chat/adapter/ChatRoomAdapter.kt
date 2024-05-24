package kr.co.lion.modigm.ui.chat.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.model.ChatRoomData
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.util.FragmentName

class ChatRoomAdapter(
    private val roomList: List<ChatRoomData>,
    private val onItemClick: (ChatRoomData) -> Unit,
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
        private val roomLastTextView: TextView = itemView.findViewById(R.id.textViewRowChatroomFiledLastMessage)
        private val roomTimeTextView: TextView = itemView.findViewById(R.id.textViewRowChatroomFiledTime)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val room = roomList[position]
                    onItemClick(room)

                    // ChatRoomFragment로 데이터 전달
                    val bundle = Bundle().apply {
                        putInt("chatIdx", room.chatIdx)
                        putString("chatTitle", room.chatTitle)
                        putStringArrayList("chatMemberList", ArrayList(room.chatMemberList))
                        putInt("participantCount", room.participantCount)
                        putBoolean("groupChat", room.groupChat)
                        Log.d("test1234", "${room}")
                    }
                    mainActivity.replaceFragment(FragmentName.CHAT_ROOM, true, true, bundle)
                }
            }
        }

        fun bind(room: ChatRoomData) {
            val position = adapterPosition
            val rooms = roomList[position]
            // 채팅 방 제목
            roomTitleTextView.text = room.chatTitle
            if ((room.lastChatMessage).isEmpty()) {
                // 마지막 대화 내용
                roomLastTextView.text = room.lastChatMessage
                // 마지막 채팅 시간 ( 적용이 안되는 오류 / 왜지..? )
                roomTimeTextView.text = room.lastChatTime
            }
        }
    }
}
package kr.co.lion.modigm.ui.chat.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.chat.ChatRoomItem
import kr.co.lion.modigm.ui.chat.Message
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
                        putBoolean("isGroupChat", room.isGroupChat)
                    }
                    mainActivity.replaceFragment(FragmentName.CHAT_ROOM, true, true, bundle)
                }
            }
        }

        fun bind(room: ChatRoomItem) {
            val position = adapterPosition
            val rooms = roomList[position]
            roomTitleTextView.text = room.chatTitle
            // Messages DB 가져와서 룸 인덱스 번호의 맨 마지막 내용, 마지막 대화 시간
            // 아래 내용 다 지워야 함 임시임
            if (rooms.isGroupChat == true) {
                if (rooms.chatIdx % 2 == 0) {
                    roomLastTextView.text = "혹시 OOO님도 채팅 한번 쳐주세요!"
                    roomTimeTextView.text = "02:21"
                }
                else {
                    roomLastTextView.text = "반갑습니다~!"
                    roomTimeTextView.text = "01:36"
                }
            }
            else {
                if (rooms.chatIdx % 2 == 0) {
                    roomLastTextView.text = "좋네요~"
                    roomTimeTextView.text = "02:21"
                }
                else {
                    roomLastTextView.text = "네 알려드릴게요!! 어떤게 궁금하신가요?"
                    roomTimeTextView.text = "23:00"
                }
            }

        }
    }
}
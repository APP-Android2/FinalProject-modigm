package kr.co.lion.modigm.ui.chat.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.model.ChatRoomData
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.util.FragmentName

class ChatRoomAdapter(
    private val roomList: MutableList<ChatRoomData>,
    private val onItemClick: (ChatRoomData) -> Unit,
    private val mainActivity: MainActivity,
    private val loginUserId: String // 현재 사용자의 ID를 추가
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
        private val roomImageImageView: ImageView = itemView.findViewById(R.id.imageViewRowChatroomFiledImage)
        private val roomMessageCountButton: Button = itemView.findViewById(R.id.buttonRowChatroomFiledMessageCount)

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
                        Log.d("chatLog1", "Room Adapter - ${room}")
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

            // 프로필 설정(회원 아이디 별 사진으로) (아직 Firebase 정보 없음) - DB 연동해야함
            if(rooms.groupChat == false){
                if (rooms.chatMemberList.contains("iuUser")) {
                    roomImageImageView.setImageResource(R.drawable.test_profile_image_iu)
                } else if (rooms.chatMemberList.contains("sonUser")) {
                    roomImageImageView.setImageResource(R.drawable.test_profile_image_son)
                } else if (rooms.chatMemberList.contains("ryuUser")) {
                    roomImageImageView.setImageResource(R.drawable.test_profile_image_ryu)
                } else {
                    roomImageImageView.setImageResource(R.drawable.test_profile_image)
                }
            }
            // 그룹 채팅 사진은 글 작성 -> 그 이미지로 설정 (아직 Firebase 정보 없음) - DB 연동해야함
            else {
                roomImageImageView.setImageResource(R.drawable.test_profile_image)
            }

            if ((room.lastChatMessage).isNotEmpty()) {
                // 마지막 대화 내용
                roomLastTextView.text = room.lastChatMessage
                // 마지막 채팅 시간
                roomTimeTextView.text = room.lastChatTime
            }

            // 안 읽은 메시지 수 설정
            val unreadCount = room.unreadMessageCount[loginUserId] ?: 0
            if (unreadCount > 0) {
                roomMessageCountButton.visibility = View.VISIBLE
                roomMessageCountButton.text = unreadCount.toString()
            } else {
                roomMessageCountButton.visibility = View.GONE
            }
        }
    }
}
package kr.co.lion.modigm.ui.chat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowChatroomFiledBinding
import kr.co.lion.modigm.db.chat.ChatRoomDataSource
import kr.co.lion.modigm.model.ChatRoomData

class ChatRoomAdapter(
    private var roomList: MutableList<ChatRoomData>,
    private val onItemClick: (ChatRoomData) -> Unit,
    private val loginUserId: String // 현재 사용자의 ID를 추가
) : RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val binding: RowChatroomFiledBinding =
            RowChatroomFiledBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ChatRoomViewHolder(binding, onItemClick)
    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(roomList[position])
    }

    inner class ChatRoomViewHolder(
        binding: RowChatroomFiledBinding,
        onItemClick: (ChatRoomData) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

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
                }
            }
        }

        fun bind(room: ChatRoomData) {
            val position = adapterPosition
            val context = itemView.context

            // 프로필 설정(회원 아이디 별 사진으로) (아직 Firebase 정보 없음) - DB 연동해야함
            if(room.groupChat == false){
                val title = room.chatMemberList.filter { it != loginUserId }
                CoroutineScope(Dispatchers.Main).launch {
                    val userNameTitle = ChatRoomDataSource.getUserNameByUid(title[0]) ?: "Unknown User"
                    val userProfile = ChatRoomDataSource.getUserProfilePicByUid(title[0]) ?: ""
                    withContext(Dispatchers.Main) {
                        // 채팅 방 제목
                        roomTitleTextView.text = userNameTitle
                        // 채팅 방 이미지 설정
                        if (userProfile.isNotEmpty()) {
                            Log.v("chatLog2", "RoomAdapter - $userProfile")
                            ChatRoomDataSource.loadUserProfilePic(context, userProfile, roomImageImageView)
                        } else {
                            roomImageImageView.setImageResource(R.drawable.test_profile_image)
                        }
                    }
                }
                roomImageImageView.setImageResource(R.drawable.test_profile_image)
            }
            // 그룹 채팅 사진은 글 작성 -> 그 이미지로 설정 (아직 Firebase 정보 없음) - DB 연동해야함
            else {
                // 채팅 방 제목
                roomTitleTextView.text = room.chatTitle
                // 채팅 방 이미지 존재 X
                if (room.chatRoomImage.isNullOrEmpty()){
                    roomImageImageView.setImageResource(R.drawable.test_profile_image)
                }
                // 채팅 방 이미지 존재 O
                else {
                    // 채팅 방 대표 사진 설정
                    CoroutineScope(Dispatchers.Main).launch {
                        ChatRoomDataSource.loadChatRoomImage(context, room.chatRoomImage, roomImageImageView)
                    }
                }
            }

            if ((room.lastChatMessage).isNotEmpty()) {
                // 마지막 대화 내용
                roomLastTextView.visibility = View.VISIBLE
                roomLastTextView.text = room.lastChatMessage
                // 마지막 채팅 시간
                roomTimeTextView.visibility = View.VISIBLE
                roomTimeTextView.text = room.lastChatTime
            } else {
                roomLastTextView.visibility = View.GONE
                roomTimeTextView.visibility = View.GONE
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
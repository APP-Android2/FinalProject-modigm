package kr.co.lion.modigm.ui.chat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.ItemMessageReceivedBinding
import kr.co.lion.modigm.databinding.ItemMessageSentBinding
import kr.co.lion.modigm.databinding.RowChatroomFiledBinding
import kr.co.lion.modigm.db.chat.ChatMessagesDataSource
import kr.co.lion.modigm.db.chat.ChatRoomDataSource
import kr.co.lion.modigm.model.ChatMessagesData
import kr.co.lion.modigm.model.ChatRoomData
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.util.FragmentName

class MessageAdapter(
    private val loginUserId: String,
    private var messages: MutableList<ChatMessagesData>,
    private val onItemClick: (ChatMessagesData) -> Unit,
    private val usersDataHashMap: HashMap<String, UserData>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].chatSenderId == loginUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding: ItemMessageSentBinding =
                ItemMessageSentBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            SentMessageViewHolder(binding)
        } else {
            val binding: ItemMessageReceivedBinding =
                ItemMessageReceivedBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            ReceivedMessageViewHolder(binding, messages, onItemClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val thisUid = message.chatSenderId
        val userData = usersDataHashMap[thisUid]
        var currentDate = message.chatDateSeparator // 현재 메시지의 날짜
        var dateCheck = true // 날짜 구분선 적용 해야 (한다/안한다)

        // 이전 메시지의 날짜와 비교하여 날짜가 변경되었는지 확인
        if (position > 0) {
            val previousMessage = messages[position - 1]
            val previousDate = previousMessage.chatDateSeparator // 이전 메시지의 날짜
            currentDate = message.chatDateSeparator // 현재 메시지의 날짜

            // 날짜가 변경되었을 경우, 날짜 구분자를 추가
            if (currentDate != previousDate) {
                dateCheck = true
            } else {
                dateCheck = false
            }
        }

        // 메시지 ViewHolder 설정
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).bind(message, dateCheck, currentDate)
        } else {
            val receivedHolder = holder as ReceivedMessageViewHolder
            receivedHolder.bind(message, userData, dateCheck, currentDate)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    // 송신자 ViewHolder
    class SentMessageViewHolder(
        binding: ItemMessageSentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val textDate: TextView = itemView.findViewById(R.id.text_date)
        private val messageBody: TextView = itemView.findViewById(R.id.text_message_body)
        private val messageImageBody: ImageView = itemView.findViewById(R.id.image_message_body)
        private val messageTime: TextView = itemView.findViewById(R.id.text_message_time)

        fun bind(message: ChatMessagesData, dateCheck: Boolean, currentDate: String) {

            if (message.chatMessage.endsWith(".jpg")) {
                // 이미지가 있으면 Glide 등을 사용하여 이미지를 로드합니다.
                val context = itemView.context
                try {
                    CoroutineScope(Dispatchers.Main).launch {
                        ChatMessagesDataSource.loadChatImageMessage(context,
                            message.chatMessage, messageImageBody)
                    }
                    messageImageBody.visibility = View.VISIBLE
                    messageBody.visibility = View.GONE
                } catch (e: Exception) {
                    Log.v("chatLog2", "$e")
                    messageBody.text = message.chatMessage
                }
            } else {
                messageBody.text = message.chatMessage
            }
            messageTime.text = message.chatTime

            // 날자 구분선
            if (dateCheck == true) {
                textDate.text = currentDate
                textDate.visibility = View.VISIBLE
            } else {
                textDate.visibility = View.GONE
            }
        }
    }

    // 수신자 ViewHolder
    class ReceivedMessageViewHolder(
        binding: ItemMessageReceivedBinding,
        messages: MutableList<ChatMessagesData>,
        onItemClick: (ChatMessagesData) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val textDate: TextView = itemView.findViewById(R.id.text_date)
        private val messageBody: TextView = itemView.findViewById(R.id.text_message_body)
        private val messageImageBody: ImageView = itemView.findViewById(R.id.image_message_body)
        private val messageTime: TextView = itemView.findViewById(R.id.text_message_time)
        private val messageSender: TextView = itemView.findViewById(R.id.text_message_sender)
        private val imageChatroomFiledImage: ImageView = itemView.findViewById(R.id.imageViewRowChatMessageProfileImage)

        init {
            imageChatroomFiledImage.setOnClickListener {
                val position = adapterPosition
                val messageItem = messages[position]
                onItemClick(messageItem)
                Log.v("chatLog2", "메세지 어댑터 클릭: $messageItem")
            }
        }

        fun bind(message: ChatMessagesData, userData: UserData?, dateCheck: Boolean, currentDate: String) {

            // 해당 메시지가 사진인지 판독
            if (message.chatMessage.endsWith(".jpg")) {
                // 이미지가 있으면 Glide 등을 사용하여 이미지를 로드합니다.
                val context = itemView.context
                try {
                    CoroutineScope(Dispatchers.Main).launch {
                        ChatMessagesDataSource.loadChatImageMessage(context,
                            message.chatMessage, messageImageBody)
                    }
                    messageImageBody.visibility = View.VISIBLE
                    messageBody.visibility = View.GONE
                } catch (e: Exception) {
                    Log.v("chatLog2", "$e")
                    messageBody.text = message.chatMessage
                }
            } else {
                messageBody.text = message.chatMessage
            }
            
            messageTime.text = message.chatTime

            // 날자 구분선
            if (dateCheck == true) {
                textDate.text = currentDate
                textDate.visibility = View.VISIBLE
            } else {
                textDate.visibility = View.GONE
            }

            // UserData 유무 검사
            if (userData == null) {
                messageSender.text = "알 수 없는 사용자" // 기본값 설정
                imageChatroomFiledImage.setImageResource(R.drawable.base_profile_image2) // 기본 이미지 설정
                // messageSender.text = message.chatSenderName
            }
            else {
                messageSender.text = userData.userName
            }

            // 프로필 사진 유무 검사
            if (userData?.userProfilePic.isNullOrEmpty()){
                imageChatroomFiledImage.setImageResource(R.drawable.base_profile_image2)
            } else {
                // 이미지가 있으면 Glide 등을 사용하여 이미지를 로드합니다.
                val context = itemView.context
                CoroutineScope(Dispatchers.Main).launch {
                    ChatRoomDataSource.loadUserProfilePic(context,
                        userData?.userProfilePic.toString(), imageChatroomFiledImage)
                }
            }

//            imageChatroomFiledImage.setOnClickListener {
//                Log.v("chatLog2", "${userData?.userUid} 프로필 클릭")
//            }
        }
    }
}
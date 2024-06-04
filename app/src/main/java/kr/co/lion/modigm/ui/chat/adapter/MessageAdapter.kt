package kr.co.lion.modigm.ui.chat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.db.user.RemoteUserDataSource
import kr.co.lion.modigm.model.ChatMessagesData
import kr.co.lion.modigm.model.UserData

class MessageAdapter(
    private val loginUserId: String,
    private val messages: MutableList<ChatMessagesData>,
    private val usersDataHashMap: HashMap<String, UserData>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2
    private val VIEW_TYPE_DATE = 3 // 날짜 아이템 뷰 홀더 추가 (00시 기준 날짜 바뀔 때마다 알려 줘야함) - 아직 미구현

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].chatSenderId == loginUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_message_sent, viewGroup, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_message_received, viewGroup, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val thisUid = message.chatSenderId
        val userData = usersDataHashMap[thisUid]
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).bind(message)
        } else {
            val receivedHolder = holder as ReceivedMessageViewHolder
            receivedHolder.bind(message, userData)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageBody: TextView = itemView.findViewById(R.id.text_message_body)
        private val messageTime: TextView = itemView.findViewById(R.id.text_message_time)

        fun bind(message: ChatMessagesData) {
            messageBody.text = message.chatMessage
            messageTime.text = message.chatTime
        }
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageBody: TextView = itemView.findViewById(R.id.text_message_body)
        private val messageTime: TextView = itemView.findViewById(R.id.text_message_time)
        private val messageSender: TextView = itemView.findViewById(R.id.text_message_sender)
        private val imageChatroomFiledImage: ImageView = itemView.findViewById(R.id.imageViewRowChatroomFiledImage)
        fun bind(message: ChatMessagesData, userData: UserData?) {
            Log.v("chatLog1", "MessageAdapter : ${userData}")
            messageBody.text = message.chatMessage
            messageTime.text = message.chatTime
            if (userData == null) {
                messageSender.text = message.chatSenderName
            }
            else {
                messageSender.text = userData.userName
            }


            // Glide 라이브러리를 사용하여 프로필 이미지 로드
            Glide.with(itemView.context)
                .load("gs://modigm-4afde.appspot.com/userProfile/${userData?.userProfilePic}")
                .placeholder(R.drawable.test_profile_image)
                .into(imageChatroomFiledImage)

            if (userData?.userProfilePic.isNullOrEmpty()){
                imageChatroomFiledImage.setImageResource(R.drawable.test_profile_image)
            } else {
                // 이미지가 있으면 Glide 등을 사용하여 이미지를 로드합니다.
                val context = itemView.context
                CoroutineScope(Dispatchers.Main).launch {
                    RemoteUserDataSource.loadUserProfilePic(context,
                        userData?.userProfilePic.toString(), imageChatroomFiledImage)
                }
            }
        }
    }
}
package kr.co.lion.modigm.ui.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kr.co.lion.modigm.R
import kr.co.lion.modigm.model.ChatMessagesData

class MessageAdapter(
    private val loginUserId: String,
    private val messages: MutableList<ChatMessagesData>,
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
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).bind(message)
        } else {
            (holder as ReceivedMessageViewHolder).bind(message)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun updateMessages(updatedMessages: List<ChatMessagesData>) {
        messages.clear()
        messages.addAll(updatedMessages)
        notifyDataSetChanged()
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
        fun bind(message: ChatMessagesData) {
            messageBody.text = message.chatMessage
            messageTime.text = message.chatTime
            messageSender.text = message.chatSenderName

            // chatSenderName에 따라서 이미지 변경
            when (message.chatSenderName) {
                "손흥민" -> imageChatroomFiledImage.setImageResource(R.drawable.test_profile_image_son)
                "류현진" -> imageChatroomFiledImage.setImageResource(R.drawable.test_profile_image_ryu)
                "아이유" -> imageChatroomFiledImage.setImageResource(R.drawable.test_profile_image_iu)
                // 기본 이미지 설정 또는 다른 케이스 추가
                else -> imageChatroomFiledImage.setImageResource(R.drawable.test_profile_image)
            }
        }
    }
}
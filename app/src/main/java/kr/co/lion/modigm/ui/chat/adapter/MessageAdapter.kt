package kr.co.lion.modigm.ui.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].chatSenderId == loginUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
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
        fun bind(message: ChatMessagesData) {
            messageBody.text = message.chatMessage
            messageTime.text = message.chatTime
            messageSender.text = message.chatSenderId
        }
    }
}
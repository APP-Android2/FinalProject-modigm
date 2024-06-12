package kr.co.lion.modigm.ui.chat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowChatroomFiledBinding
import kr.co.lion.modigm.databinding.RowChatroomMemberBinding
import kr.co.lion.modigm.db.chat.ChatRoomDataSource
import kr.co.lion.modigm.db.user.RemoteUserDataSource
import kr.co.lion.modigm.model.ChatMessagesData
import kr.co.lion.modigm.model.ChatRoomData
import kr.co.lion.modigm.model.UserData


class ChatRoomMemberAdapter(
    private val members: List<UserData>,
    private val loginUserId: String,
    private val onItemClick: (UserData) -> Unit,
) : RecyclerView.Adapter<ChatRoomMemberAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: RowChatroomMemberBinding =
            RowChatroomMemberBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding, onItemClick, members)
    }

    override fun getItemCount() = members.size

    class ViewHolder(
        binding: RowChatroomMemberBinding,
        onItemClick: (UserData) -> Unit,
        members: List<UserData>
    ) : RecyclerView.ViewHolder(binding.root) {

        val memberName: TextView = itemView.findViewById(R.id.textViewChatRoomMemberName)
        val memberProfile: ImageView = itemView.findViewById(R.id.imageViewRowChatRoomMemberProfile)
        val memberMy: Button = itemView.findViewById(R.id.buttonRowChatRoomMemberMy)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val member = members[position]
                    onItemClick(member)
                }
            }
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userData = members[position]

        // 해당 멤버가 내 아이디와 일치하면 내 아이디만 "나" 표시 해주기
        if (userData.userUid == loginUserId){
            holder.memberMy.visibility = View.VISIBLE
        } else {
            holder.memberMy.visibility = View.GONE
        }

        // 해당 유저 데이터의 이름
        if (userData.userName.isNullOrEmpty()) {
            holder.memberName.text = "알수없음"
        } else {
            holder.memberName.text = userData.userName
        }

        // 사용자 프로필 사진 설정
        val context = holder.itemView.context
        CoroutineScope(Dispatchers.Main).launch {
            ChatRoomDataSource.loadUserProfilePic(context, userData.userProfilePic, holder.memberProfile)
        }
    }
}
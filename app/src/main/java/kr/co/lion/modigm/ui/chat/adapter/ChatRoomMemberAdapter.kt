package kr.co.lion.modigm.ui.chat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.R
import kr.co.lion.modigm.db.user.RemoteUserDataSource
import kr.co.lion.modigm.model.UserData


class ChatRoomMemberAdapter(private val members: List<UserData>) : RecyclerView.Adapter<ChatRoomMemberAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val memberName: TextView = view.findViewById(R.id.textViewChatRoomMemberName)
        val memberProfile: ImageView = view.findViewById(R.id.imageViewRowChatRoomMemberProfile)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_chatroom_member, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userData = members[position]
        if (userData.userName.isNullOrEmpty()) {
            holder.memberName.text = "알수없음"
        } else {
            holder.memberName.text = userData.userName
        }

        // 사용자 프로필 사진 설정
        val context = holder.itemView.context
        CoroutineScope(Dispatchers.Main).launch {
            RemoteUserDataSource.loadUserProfilePic(context, userData.userProfilePic, holder.memberProfile)
        }
    }
    override fun getItemCount() = members.size
}
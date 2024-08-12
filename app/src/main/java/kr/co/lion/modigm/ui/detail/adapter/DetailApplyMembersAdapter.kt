package kr.co.lion.modigm.ui.detail.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowDetailApplyMemberBinding
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.ui.chat.vm.ChatRoomViewModel
import kr.co.lion.modigm.ui.detail.vm.SqlDetailViewModel

class DetailApplyMembersAdapter(
    private val viewModel: SqlDetailViewModel,
    private val chatRoomViewModel: ChatRoomViewModel,
    private val currentUserId: String,
    private val studyIdx: Int,
    private val onItemClicked: (UserData) -> Unit
) : ListAdapter<UserData, DetailApplyMembersAdapter.MemberViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = RowDetailApplyMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val userData = getItem(position)
        holder.bind(userData)
    }

    inner class MemberViewHolder(private val binding: RowDetailApplyMemberBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserData) {

            itemView.setOnClickListener {
                onItemClicked(user)
            }

            binding.textViewDetailApplyMemberName.text = user.userName
            binding.textViewDetailApplyMemberIntro.text = user.userIntro

            // 거절 버튼
            binding.buttonDetailRefuse.setOnClickListener {
                showRefuseDialog(user)
            }

            // 승인 버튼
            binding.buttonDetailAccept.setOnClickListener {

//                viewModel.acceptUser(studyIdx, user.userUid) { success ->
//                    if (success) {
//                        val snackbar = Snackbar.make(itemView, "${user.userName}님의 신청이 승인되었습니다", Snackbar.LENGTH_LONG)
//                        val snackbarView = snackbar.view
//                        val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
//                        val textSizeInPx = dpToPx(itemView.context, 16f)
//                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx)
//                        snackbar.show()
//
//                        // 채팅방에 사용자 추가 / chatMemberList 배열에 UID 추가
//                        CoroutineScope(Dispatchers.Main).launch {
//                            val coroutine1 = chatRoomViewModel.addUserToChatMemberList(studyIdx, user.userUid)
//                            coroutine1.join()
//                        }
//
//                        // 리스트에서 아이템 제거
//                        val position = adapterPosition
//                        if (position != RecyclerView.NO_POSITION) {
//                            removeItem(position)
//                        }
//
//                    } else {
//                        Log.d("Dialog", "Failed to accept user")
//                    }
//                }
            }

            // Firebase Storage에서 이미지 URL 가져오기
            val storageReference =
                FirebaseStorage.getInstance().reference.child("userProfile/${user.userProfilePic}")
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(itemView.context)
                    .load(uri)
                    .error(R.drawable.icon_error_24px) // 로드 실패 시 표시할 이미지
                    .into(binding.imageViewDetailApplyMember) // ImageView에 이미지 로드
            }.addOnFailureListener {
                // 에러 처리
            }

        }

        // 스낵바 글시 크기 설정을 위해 dp를 px로 변환
        fun dpToPx(context: Context, dp: Float): Float {
            return dp * context.resources.displayMetrics.density
        }



        // custom dialog
        fun showRefuseDialog(user: UserData) {
            val dialogView = LayoutInflater.from(itemView.context).inflate(R.layout.custom_dialog, null)
            val dialog = MaterialAlertDialogBuilder(itemView.context, R.style.dialogColor)
                .setTitle("거절 확인")
                .setMessage("정말로 ${user.userName}을(를) 거절하시겠습니까?")
                .setView(dialogView)
                .create()

            dialogView.findViewById<TextView>(R.id.btnYes).setOnClickListener {
                // 예 버튼 로직
                Log.d("Dialog", "확인을 선택했습니다.")
//                viewModel.removeUserFromApplyList(studyIdx, user.userUid) { success ->
//                    if (success) {
//                        Log.d("Dialog", "User removed from apply list")
//                        val position = adapterPosition
//                        if (position != RecyclerView.NO_POSITION) {
//                            removeItem(position)
//                        }
//                    } else {
//                        Log.d("Dialog", "Failed to remove user from apply list")
//                    }
//                }
                dialog.dismiss()
            }

            dialogView.findViewById<TextView>(R.id.btnNo).setOnClickListener {
                // 아니요 버튼 로직
                Log.d("Dialog", "취소를 선택했습니다.")
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    fun removeItem(position: Int) {
        // 현재 리스트에서 해당 아이템을 제거
        val newList = currentList.toMutableList().apply {
            removeAt(position)
        }
        submitList(newList)  // 변경된 리스트를 다시 제출
        notifyItemRemoved(position)  // 특정 위치의 아이템 제거 알림

        // If the list is empty after removing the item, update LiveData
        if (newList.isEmpty()) {
//            viewModel.loadApplyMembers(studyIdx)
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UserData>() {
        override fun areItemsTheSame(oldItem: UserData, newItem: UserData): Boolean {
            return oldItem.userUid == newItem.userUid
        }

        override fun areContentsTheSame(oldItem: UserData, newItem: UserData): Boolean {
            return oldItem == newItem
        }
    }
}
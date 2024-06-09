package kr.co.lion.modigm.ui.detail.adapter

import android.app.AlertDialog
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.CustomDialogBinding
import kr.co.lion.modigm.databinding.RowDetailJoinMemberBinding
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.ui.detail.Member

class DetailJoinMembersAdapter(private val currentUserId: String) : ListAdapter<UserData, DetailJoinMembersAdapter.MemberViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = RowDetailJoinMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val userData = getItem(position)
        holder.bind(userData)
    }

//    override fun getItemCount() = members.size

    inner class MemberViewHolder(private val binding: RowDetailJoinMemberBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserData) {
            binding.textViewDetailJoinMemberName.text = user.userName
            binding.textViewDetailJoinMemberIntro.text = user.userIntro

            Log.d("DetailAdapter", "name: ${user.userName}, intro:${user.userIntro}")

            if (user.userUid == currentUserId) {
                binding.textViewDetailJoinKick.text = "스터디장"
                binding.textViewDetailJoinKick.setTextColor(Color.BLACK) // 글씨 색상을 검은색으로 설정
                binding.textViewDetailJoinKick.isClickable = false // 클릭 비활성화
                binding.textViewDetailJoinKick.setOnClickListener {
                    // "스터디장"일 때 아무 동작도 하지 않도록 빈 리스너 설정
                }
            } else {
                binding.textViewDetailJoinKick.text = "내보내기"
                binding.textViewDetailJoinKick.setOnClickListener {
                    showKickDialog(user) // 강퇴 다이얼로그 표시
                }
            }

            // Firebase Storage에서 이미지 URL 가져오기
            val storageReference =
                FirebaseStorage.getInstance().reference.child("userProfile/${user.userProfilePic}")
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(itemView.context)
                    .load(uri)
                    .into(binding.imageViewDetailJoinMember) // ImageView에 이미지 로드
            }.addOnFailureListener {
                // 에러 처리
            }
        }


        // custom dialog
        fun showKickDialog(member: UserData) {
//            val dialogView = LayoutInflater.from(itemView.context).inflate(R.layout.custom_dialog, null)
            val dialogBinding = CustomDialogBinding.inflate(LayoutInflater.from(itemView.context))
            val dialog =MaterialAlertDialogBuilder(itemView.context,R.style.dialogColor)
                .setTitle("내보내기 확인")
                .setMessage("정말로 ${member.userName}을(를) 내보내시겠습니까?")
                .setView(dialogBinding.root)
                .create()

            dialogBinding.btnYes.setOnClickListener {
                // 예 버튼 로직
                Log.d("Dialog", "확인을 선택했습니다.")
                dialog.dismiss()
            }

            dialogBinding.btnNo.setOnClickListener {
                // 아니요 버튼 로직
                Log.d("Dialog", "취소를 선택했습니다.")
                dialog.dismiss()
            }

            dialog.show()
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
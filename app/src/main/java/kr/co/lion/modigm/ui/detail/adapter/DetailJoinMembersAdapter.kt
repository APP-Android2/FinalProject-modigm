package kr.co.lion.modigm.ui.detail.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.CustomDialogBinding
import kr.co.lion.modigm.databinding.RowDetailJoinMemberBinding
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.ui.detail.vm.DetailViewModel
import kr.co.lion.modigm.util.ModigmApplication

class DetailJoinMembersAdapter(
    private val viewModel: DetailViewModel,
    private var currentUserId: Int,
    private val studyIdx: Int,
    private val studyTitle:String,
    private val imageUrl: String,
    private val onItemClicked: (UserData) -> Unit
) : ListAdapter<UserData, DetailJoinMembersAdapter.MemberViewHolder>(UserDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = RowDetailJoinMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val userData = getItem(position)
        holder.bind(userData)
    }

    override fun submitList(list: List<UserData>?) {
        if (list != null) {
            // 스터디장의 userIdx를 알기 위해 전달받은 currentUserId와 비교
            val studyLeader = list.filter { it.userIdx == currentUserId }  // 스터디장 구분
            val otherMembers = list.filter { it.userIdx != currentUserId }  // 스터디장 아닌 멤버들

            // 스터디장이 맨 위로 가도록 리스트 정렬
            val sortedList = studyLeader + otherMembers

            // 정렬된 리스트를 어댑터에 제출
            super.submitList(sortedList)
        } else {
            super.submitList(list)
        }
    }


    inner class MemberViewHolder(private val binding: RowDetailJoinMemberBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserData) {

            itemView.setOnClickListener {
                onItemClicked(user)
            }

            binding.textViewDetailJoinMemberName.text = user.userName
            binding.textViewDetailJoinMemberIntro.text = user.userIntro

            // 사용자 프로필 이미지를 로드
            loadUserImage(user.userProfilePic)

            Log.d("DetailAdapter", "name: ${user.userName}, intro:${user.userIntro}")

            currentUserId = ModigmApplication.prefs.getInt("currentUserIdx", 0)

            // 글 작성자(즉 현재 로그인된 사용자 currentUserId)와 참여자의 아이디가 같을 경우
            if (user.userIdx == currentUserId) {
                binding.textViewDetailJoinKick.text = "스터디장"
                binding.textViewDetailJoinKick.setTextColor(Color.BLACK) // 글씨 색상을 검은색으로 설정
                binding.textViewDetailJoinKick.isClickable = false // 클릭 비활성화
                binding.layoutDetailJoinKick.setOnClickListener(null)
            } else {
                binding.textViewDetailJoinKick.text = "내보내기"
                binding.layoutDetailJoinKick.setOnClickListener {
                    showKickDialog(user, studyIdx) // 강퇴 다이얼로그 표시
                }
            }
        }

        private fun loadUserImage(imageUrl: String?) {
            if (imageUrl != null) {
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.image_loading_gray)
                            .error(R.drawable.image_default_profile)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                    )
                    .into(binding.imageViewDetailJoinMember)
            } else {
                // 이미지 URL이 없을 때 기본 이미지 설정
                binding.imageViewDetailJoinMember.setImageResource(R.drawable.image_default_profile)
            }
        }


        // custom dialog
        fun showKickDialog(member: UserData, studyIdx: Int) {
            val dialogBinding = CustomDialogBinding.inflate(LayoutInflater.from(itemView.context))
            val dialog =MaterialAlertDialogBuilder(itemView.context,R.style.dialogColor)
                .setTitle("내보내기 확인")
                .setMessage("정말로 ${member.userName}을(를) 내보내시겠습니까?")
                .setView(dialogBinding.root)
                .create()

            dialogBinding.btnYes.setOnClickListener {
                viewModel.removeUserFromStudy(studyIdx, member.userIdx)  // 스터디에서 사용자 삭제 호출
                viewModel.notifyUserKicked(itemView.context, member.userIdx, studyIdx, studyTitle) // 내보내기 푸시 알림 전송
                removeItem(adapterPosition)
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

        fun removeItem(position: Int) {
            // 현재 리스트에서 해당 아이템을 제거
            val newList = currentList.toMutableList().apply {
                removeAt(position)
            }
            submitList(newList)  // 변경된 리스트를 다시 제출
            notifyItemRemoved(position)  // 특정 위치의 아이템 제거 알림
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
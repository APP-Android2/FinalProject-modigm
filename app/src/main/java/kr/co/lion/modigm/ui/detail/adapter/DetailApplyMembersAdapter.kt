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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowDetailApplyMemberBinding
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.ui.detail.vm.DetailViewModel

class DetailApplyMembersAdapter(
    private val context: Context, // Context를 생성자에서 받습니다.
    private val viewModel: DetailViewModel,
    private val currentUserId: Int,
    private val studyIdx: Int,
    private val studyTitle: String, // studyTitle 추가
    private val imageUrl: String,
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

            // 사용자 프로필 이미지를 로드
            loadUserImage(user.userProfilePic)

            // 거절 버튼
            binding.buttonDetailRefuse.setOnClickListener {
                showRefuseDialog(user)
            }

            // 승인 버튼
            binding.buttonDetailAccept.setOnClickListener {
                viewModel.acceptUser(studyIdx, user.userIdx)
                viewModel.notifyUserAccepted(context,user.userIdx, studyIdx, studyTitle) // 신청 승인 알림 전송
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
                    .into(binding.imageViewDetailApplyMember)
            } else {
                // 이미지 URL이 없을 때 기본 이미지 설정
                binding.imageViewDetailApplyMember.setImageResource(R.drawable.image_default_profile)
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
                viewModel.removeUserFromApplyList(studyIdx, user.userIdx)
                viewModel.notifyUserRejected(context,user.userIdx, studyIdx, studyTitle) // 신청 거절 알림 전송
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

    class UserDiffCallback : DiffUtil.ItemCallback<UserData>() {
        override fun areItemsTheSame(oldItem: UserData, newItem: UserData): Boolean {
            return oldItem.userUid == newItem.userUid
        }

        override fun areContentsTheSame(oldItem: UserData, newItem: UserData): Boolean {
            return oldItem == newItem
        }
    }
}
package kr.co.lion.modigm.ui.detail.adapter

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowDetailApplyMemberBinding
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.ui.detail.vm.DetailViewModel

class DetailApplyMembersAdapter (private val viewModel: DetailViewModel, private val currentUserId: String, private val studyIdx: Int) : ListAdapter<UserData, DetailApplyMembersAdapter.MemberViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = RowDetailApplyMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val userData = getItem(position)
        holder.bind(userData)
    }

//    override fun getItemCount() = members.size

    inner class MemberViewHolder(private val binding: RowDetailApplyMemberBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserData) {
            binding.textViewDetailApplyMemberName.text = user.userName
            binding.textViewDetailApplyMemberIntro.text = user.userIntro

            // 거절 버튼
            binding.buttonDetailRefuse.setOnClickListener {
                showRefuseDialog(user)
            }

            // 승인 버튼
            binding.buttonDetailAccept.setOnClickListener {

                val snackbar = Snackbar.make(itemView, "${user.userName}님의 신청이 승인되었습니다", Snackbar.LENGTH_LONG)

                // 스낵바의 뷰를 가져옵니다.
                val snackbarView = snackbar.view

                // 스낵바 텍스트 뷰 찾기
                val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

                // 텍스트 크기를 dp 단위로 설정
                val textSizeInPx = dpToPx(itemView.context, 16f)
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx)

                snackbar.show()
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
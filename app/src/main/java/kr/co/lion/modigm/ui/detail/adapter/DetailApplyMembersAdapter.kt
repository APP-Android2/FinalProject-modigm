package kr.co.lion.modigm.ui.detail.adapter

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.detail.Member

class DetailApplyMembersAdapter (private val members: List<Member>) : RecyclerView.Adapter<DetailApplyMembersAdapter.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_detail_apply_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.bind(member)
    }

    override fun getItemCount() = members.size

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewDetailApplyMemberName)
        private val introTextView: TextView = itemView.findViewById(R.id.textViewDetailApplyMemberIntro)
        private val refuseButton: Button = itemView.findViewById(R.id.buttonDetailRefuse)
        private val acceptButton: Button = itemView.findViewById(R.id.buttonDetailAccept)

        fun bind(member: Member) {
            nameTextView.text = member.name
            introTextView.text = member.intro

            // 거절 버튼
            refuseButton.setOnClickListener {
                showRefuseDialog(member)
            }

            // 승인 버튼
            acceptButton.setOnClickListener {

                val snackbar = Snackbar.make(itemView, "승인되었습니다: ${member.name}", Snackbar.LENGTH_LONG)

                // 스낵바의 뷰를 가져옵니다.
                val snackbarView = snackbar.view

                // 스낵바 텍스트 뷰 찾기
                val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

                // 텍스트 크기를 dp 단위로 설정
                val textSizeInPx = dpToPx(itemView.context, 16f)
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPx)

                snackbar.show()
            }
        }

        // 스낵바 글시 크기 설정을 위해 dp를 px로 변환
        fun dpToPx(context: Context, dp: Float): Float {
            return dp * context.resources.displayMetrics.density
        }



        // custom dialog
        fun showRefuseDialog(member: Member) {
            val dialogView = LayoutInflater.from(itemView.context).inflate(R.layout.custom_dialog, null)
            val dialog = MaterialAlertDialogBuilder(itemView.context, R.style.dialogColor)
                .setTitle("거절 확인")
                .setMessage("정말로 ${member.name}을(를) 거절하시겠습니까?")
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
}
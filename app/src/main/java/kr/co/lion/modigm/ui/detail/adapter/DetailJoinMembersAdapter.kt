package kr.co.lion.modigm.ui.detail.adapter

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.detail.Member

class DetailJoinMembersAdapter(private val members: List<Member>) :
    RecyclerView.Adapter<DetailJoinMembersAdapter.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_detail_join_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.bind(member)
    }

    override fun getItemCount() = members.size

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewDetailJoinMemberName)
        private val introTextView: TextView = itemView.findViewById(R.id.textViewDetailJoinMemberIntro)
        private val kickTextView: TextView = itemView.findViewById(R.id.textViewDetailJoinKick)

        fun bind(member: Member) {
            nameTextView.text = member.name
            introTextView.text = member.intro

            kickTextView.setOnClickListener {
                showKickDialog(member)
            }
        }


        // custom dialog
        fun showKickDialog(member: Member) {
            val dialogView = LayoutInflater.from(itemView.context).inflate(R.layout.custom_dialog, null)
            val dialog =MaterialAlertDialogBuilder(itemView.context,R.style.dialogColor)
                .setTitle("내보내기 확인")
                .setMessage("정말로 ${member.name}을(를) 내보내시겠습니까?")
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
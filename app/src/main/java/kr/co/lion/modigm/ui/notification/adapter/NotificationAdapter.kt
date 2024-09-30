package kr.co.lion.modigm.ui.notification.adapter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.RowNotificationBinding
import kr.co.lion.modigm.model.NotificationData
import kr.co.lion.modigm.ui.detail.DetailFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private var notifications: MutableList<NotificationData>,
    private val onDeleteClick: (NotificationData) -> Unit,
    private val onMarkAsRead: (NotificationData) -> Unit // 알림 읽음 상태로 표시하는 콜백 추가
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private lateinit var context: Context

    // 브로드캐스트 리시버 정의
    private val markAllReadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("NotificationAdapter", "Received broadcast to mark all notifications as read")
            notifications.forEach { it.isNew = false }
            notifyDataSetChanged()
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
        LocalBroadcastManager.getInstance(context).registerReceiver(
            markAllReadReceiver,
            IntentFilter("ACTION_MARK_ALL_READ")
        )
        Log.d("NotificationAdapter", "BroadcastReceiver registered")
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        LocalBroadcastManager.getInstance(context).unregisterReceiver(markAllReadReceiver)
        Log.d("NotificationAdapter", "BroadcastReceiver unregistered")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        context = parent.context
        val binding = RowNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = notifications.size

    inner class NotificationViewHolder(private val binding: RowNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: NotificationData) {
            binding.textViewNotificationTitle.text = notification.notificationTitle
            binding.textViewNotificationContent.text = notification.notificationContent

            // 날짜와 시간 포맷팅
            val originalDate = notification.notificationTime
            val formattedDate = formatTimeTo24Hour(originalDate)
            binding.textViewNotificationTime.text = formattedDate

            // 이미지 URL이 있는 경우 Glide를 사용하여 이미지를 로드합니다.
            Glide.with(binding.root.context)
                .load(notification.coverPhotoUrl)
                .placeholder(R.drawable.icon_notifications_24px)
                .into(binding.imageViewNotificationIcon)

            // X 아이콘 클릭 리스너 설정
            binding.imageViewDeleteNotification.setOnClickListener {
                onDeleteClick(notification) // X 아이콘 클릭 시 콜백 호출
            }

            // 서버에서 가져온 isNew 값에 따라 배지를 표시
            if (notification.isNew) {
                binding.badgeNewNotification.visibility = View.VISIBLE
            } else {
                binding.badgeNewNotification.visibility = View.GONE
            }

            // 항목 클릭 리스너 설정
            binding.root.setOnClickListener {
                val studyIdx = notification.studyIdx

                // Fragment로 이동하는 로직 추가
                val activity = it.context as? AppCompatActivity // Context를 AppCompatActivity로 캐스팅
                val detailFragment = DetailFragment().apply {
                    arguments = Bundle().apply {
                        putInt("studyIdx", studyIdx)
                    }
                }

                activity?.supportFragmentManager?.commit {
                    replace(R.id.containerMain, detailFragment)
                    addToBackStack("DETAIL") // Fragment 이름을 추가하여 백스택에 저장
                }

                // 클릭한 항목만 읽음 상태로 업데이트
                notification.isNew = false
                notifyItemChanged(adapterPosition) // 변경된 항목만 갱신

                // 서버에 상태 업데이트 호출
                onMarkAsRead(notification)

            }
        }
    }

    private fun formatTimeTo24Hour(date: Date): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return format.format(date)
    }

    fun updateData(newNotifications: List<NotificationData>) {
        Log.d("NotificationAdapter", "Updating data: ${newNotifications.size} items")
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged() // RecyclerView 갱신
    }

    fun onDestroy() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(markAllReadReceiver)
        Log.d("NotificationAdapter", "onDestroy: BroadcastReceiver unregistered")
    }
}

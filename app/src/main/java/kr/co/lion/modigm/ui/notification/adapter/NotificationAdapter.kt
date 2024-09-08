package kr.co.lion.modigm.ui.notification.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
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
    private val onDeleteClick: (NotificationData) -> Unit) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
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
            }
        }
    }

    private fun formatTimeTo24Hour(date: Date): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return format.format(date)
    }

    fun updateData(newNotifications: List<NotificationData>) {
//        notifications = newNotifications
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged() // RecyclerView 갱신
    }
}

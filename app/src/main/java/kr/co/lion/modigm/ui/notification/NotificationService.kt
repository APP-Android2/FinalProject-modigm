package kr.co.lion.modigm.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.co.lion.modigm.R
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.util.ModigmApplication.Companion.prefs

class NotificationService : FirebaseMessagingService(){

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // FCM 메시지에 알림 페이로드가 있는 경우
        remoteMessage.notification?.let {
            val title = it.title ?: "Default Title"
            val body = it.body ?: "Default Body"

            Log.d(TAG, "Message Notification Title: ${it.title}, Body: ${it.body}")

            // 데이터 페이로드가 비어있는 경우에만 알림을 생성
            if (remoteMessage.data.isEmpty()) {
                showNotification(title, body)
            }
        }

        // 데이터 메시지가 포함되어 있는 경우
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            val title = remoteMessage.data["title"] ?: "Default Title" // 데이터 페이로드에서 제목을 가져옴
            val body = remoteMessage.data["body"] ?: "Default Body"   // 데이터 페이로드에서 내용을 가져옴
            val studyIdx = remoteMessage.data["studyIdx"]?.toIntOrNull()

            if (studyIdx != null) {
                showNotification(title, body, studyIdx)
            } else {
                Log.e("NotificationService", "Received notification without valid studyIdx")
                showNotification(title, body)
            }
        }

        // 배지 상태 저장 (알림이 도착했으므로 true로 설정)
        prefs.setBoolean("hasUnreadNotifications", true)

        // 화면을 갱신하도록 브로드캐스트를 보냄
        notifyDataChanged()
    }

    private fun notifyDataChanged() {
        val intent = Intent("ACTION_REFRESH_DATA")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun showNotification(title: String?, body: String?, studyIdx: Int? = null, userIdx: Int? = null) {
        // 현재 앱에 로그인된 사용자 ID
        val currentUserIdx = prefs.getInt("currentUserIdx", -1)

        // 만약 FCM 메시지의 userIdx가 현재 로그인된 사용자와 다르다면 알림을 무시
        if (userIdx != null && userIdx != currentUserIdx) {
            Log.d(TAG, "Received notification for another user. Ignoring notification.")
            return
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()

        // 알림 그룹 ID 설정
        val groupKey = "com.example.APP_NOTIFICATION_GROUP"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 클릭 시 MainActivity로 이동하도록 설정
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP) // 기존 액티비티를 재사용하고, 스택 최상단으로 이동
            putExtra("fromNotification", true) // 알림에서 이동했음을 표시하는 플래그 추가

            if (studyIdx != null) {
                putExtra("studyIdx", studyIdx) // studyIdx 전달
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // 플래그 수정
        )
        // 개별 알림 생성
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.logo_modigm)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(groupKey) // 그룹 설정

        // 그룹 요약 알림 생성
        val summaryNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("스터디 알림")
            .setContentText("새로운 알림이 있습니다.")
            .setSmallIcon(R.drawable.logo_modigm)
            .setStyle(NotificationCompat.InboxStyle()
                .addLine(body)
                .setSummaryText("스터디 알림"))
            .setGroup(groupKey)
            .setGroupSummary(true) // 요약 알림 설정

        notificationManager.notify(notificationId, notificationBuilder.build())
        notificationManager.notify(0, summaryNotification.build()) // 요약 알림 ID는 고정된 값 사용
    }

    companion object {
        private const val TAG = "MyFirebaseMessagingService"
        private const val CHANNEL_ID = "default_channel_id"
    }

}
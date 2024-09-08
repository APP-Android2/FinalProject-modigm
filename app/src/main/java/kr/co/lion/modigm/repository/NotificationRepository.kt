package kr.co.lion.modigm.repository


import kr.co.lion.modigm.db.notification.RemoteNotificationDataSource
import kr.co.lion.modigm.model.NotificationData

class NotificationRepository {
    private val dataSource = RemoteNotificationDataSource()

    suspend fun getNotifications(userIdx: Int): List<NotificationData> {
        return dataSource.getNotificationsByUserId(userIdx)
    }

    // 알림 삭제 메서드 추가
    suspend fun deleteNotification(notification: NotificationData): Boolean {
        return dataSource.deleteNotification(notification)
    }
}

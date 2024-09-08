package kr.co.lion.modigm.db.notification

import android.util.Log
import kr.co.lion.modigm.model.NotificationData

class RemoteNotificationDataSource {
    private val TAG = "RemoteNotificationDataSource"
    private val notificationDao = RemoteNotificationDao()

    // 특정 userIdx에 해당하는 알림 데이터를 가져오는 메소드
    suspend fun getNotificationsByUserId(userIdx: Int): List<NotificationData> {
        return try {
            notificationDao.getNotificationsByUserId(userIdx)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching notifications by user ID: ${e.message}")
            emptyList()
        }
    }

    // 알림 삭제 메서드 추가
    suspend fun deleteNotification(notification: NotificationData): Boolean {
        return try {
            notificationDao.deleteNotificationById(notification.notificationIdx)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification: ${e.message}")
            false
        }
    }
}

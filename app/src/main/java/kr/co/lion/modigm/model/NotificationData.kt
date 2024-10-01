package kr.co.lion.modigm.model

import java.sql.ResultSet
import java.util.Date

data class NotificationData(
    val notificationIdx: Int,
    val userIdx: Int,
    val notificationTitle: String,
    val notificationContent: String,
    val coverPhotoUrl: String?,
    val notificationTime: Date,
    val studyIdx: Int,
    var isNew: Boolean = true, // 알림이 새로운지 여부를 나타내는 속성
    var isRead: Boolean = false
) {
    companion object {
        fun getNotificationData(resultSet: ResultSet): NotificationData {
            return NotificationData(
                notificationIdx = resultSet.getInt("notificationIdx"),
                userIdx = resultSet.getInt("userIdx"),
                notificationTitle = resultSet.getString("notificationTitle"),
                notificationContent = resultSet.getString("notificationContent"),
                coverPhotoUrl = resultSet.getString("cover_photo_url"),
                notificationTime = resultSet.getTimestamp("notificationTime"),
                studyIdx = resultSet.getInt("studyIdx"),
                isNew = resultSet.getBoolean("isNew")
            )
        }
    }
}

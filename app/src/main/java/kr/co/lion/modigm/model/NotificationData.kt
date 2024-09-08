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
    val studyIdx: Int
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
                studyIdx = resultSet.getInt("studyIdx")
            )
        }
    }
}

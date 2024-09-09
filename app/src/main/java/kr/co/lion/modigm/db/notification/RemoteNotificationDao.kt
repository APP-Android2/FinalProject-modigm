package kr.co.lion.modigm.db.notification

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.db.HikariCPDataSource
import kr.co.lion.modigm.model.NotificationData
import java.sql.ResultSet

class RemoteNotificationDao {
    private val TAG = "RemoteNotificationDao"

    // 쿼리를 실행하고 결과를 처리하는 공통 메소드
    suspend fun <T> executeQuery(query: String, vararg params: Any, block: (ResultSet) -> T?): List<T> = withContext(Dispatchers.IO) {
        try {
            val results = mutableListOf<T>()
            HikariCPDataSource.getConnection().use { connection ->
                Log.d(TAG, "Executing query: $query with params: ${params.joinToString()}")
                connection.prepareStatement(query).use { statement ->
                    params.forEachIndexed { index, param ->
                        statement.setObject(index + 1, param)
                    }
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        block(resultSet)?.let { results.add(it) }
                    }
                }
            }
            Log.d(TAG, "Query executed successfully: $query with results: $results")
            results
        } catch (e: Exception) {
            Log.e(TAG, "쿼리 실행 중 오류 발생", e)
            emptyList()
        }
    }

    // 특정 사용자의 알림 데이터를 가져오는 메소드
    suspend fun getNotificationsByUserId(userIdx: Int): List<NotificationData> {
        val query = "SELECT * FROM tb_notification WHERE userIdx = ? ORDER BY notificationTime DESC"
        return executeQuery(query, userIdx) { resultSet ->
            NotificationData.getNotificationData(resultSet)
        }
    }

    suspend fun deleteNotificationById(notificationId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "DELETE FROM tb_notification WHERE notificationIdx = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, notificationId)
                    statement.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification", e)
            false
        }
    }

    // 알림을 읽음으로 표시하는 메서드 추가
    suspend fun markNotificationAsRead(notificationIdx: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "UPDATE tb_notification SET isNew = FALSE WHERE notificationIdx = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, notificationIdx)
                    statement.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            Log.e("RemoteNotificationDao", "Error marking notification as read", e)
            false
        }
    }

    // 모든 알림을 읽음으로 표시하는 메서드 추가
    suspend fun markAllNotificationsAsRead(userIdx: Int) = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "UPDATE tb_notification SET isNew = FALSE WHERE userIdx = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, userIdx)
                    statement.executeUpdate()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking all notifications as read", e)
        }
    }

}

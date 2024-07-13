package kr.co.lion.modigm.db.favorite

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.model.SqlStudyData
import java.sql.Connection
import java.sql.DriverManager

class FavoriteListDao {

    private val tag = "FavoriteListDao"
    private val dbUrl = BuildConfig.DB_URL
    private val dbUser = BuildConfig.DB_USER
    private val dbPassword = BuildConfig.DB_PASSWORD

    // 데이터베이스 연결을 생성하는 메소드
    private suspend fun getConnection(): Connection = withContext(Dispatchers.IO) {
        Class.forName("com.mysql.jdbc.Driver")
        DriverManager.getConnection(dbUrl, dbUser, dbPassword)
    }

    // 좋아요한 스터디 목록 조회
    suspend fun selectFavoriteStudies(userIdx: Int): HashMap<Int, Triple<SqlStudyData, Int, Boolean>> = withContext(Dispatchers.IO) {
        val favoriteStudiesMap = hashMapOf<Int, Triple<SqlStudyData, Int, Boolean>>()
        try {
            getConnection().use { connection ->
                val query = """
                    SELECT s.*, COUNT(sm.userIdx) as memberCount, 
                           IF(f.favoriteIdx IS NOT NULL, TRUE, FALSE) as isFavorite
                    FROM Favorite f
                    INNER JOIN Study s ON f.studyIdx = s.studyIdx
                    LEFT JOIN StudyMember sm ON s.studyIdx = sm.studyIdx
                    WHERE f.userIdx = ?
                    GROUP BY s.studyIdx
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, userIdx)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val studyData = SqlStudyData.getStudyData(resultSet)
                        val memberCount = resultSet.getInt("memberCount")
                        val isFavorite = resultSet.getBoolean("isFavorite")
                        favoriteStudiesMap[studyData.studyIdx] = Triple(studyData, memberCount, isFavorite)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "좋아요한 스터디 목록 조회 중 오류 발생", e)
        }
        favoriteStudiesMap
    }

    // 좋아요 토글
    suspend fun toggleFavorite(userIdx: Int, studyIdx: Int): Boolean = withContext(Dispatchers.IO) {
        var isFavorite = false
        try {
            getConnection().use { connection ->
                // 좋아요 상태 확인
                val checkFavoriteQuery = """
                    SELECT * FROM Favorite WHERE userIdx = ? AND studyIdx = ?
                """
                connection.prepareStatement(checkFavoriteQuery).use { statement ->
                    statement.setInt(1, userIdx)
                    statement.setInt(2, studyIdx)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        // 좋아요 되어 있으면 삭제
                        val deleteFavoriteQuery =
                            "DELETE FROM Favorite WHERE userIdx = ? AND studyIdx = ?"
                        connection.prepareStatement(deleteFavoriteQuery).use { deleteStatement ->
                            deleteStatement.setInt(1, userIdx)
                            deleteStatement.setInt(2, studyIdx)
                            deleteStatement.executeUpdate()
                        }
                    } else {
                        // 좋아요 되어 있지 않으면 추가
                        val insertFavoriteQuery =
                            "INSERT INTO Favorite (userIdx, studyIdx) VALUES (?, ?)"
                        connection.prepareStatement(insertFavoriteQuery).use { insertStatement ->
                            insertStatement.setInt(1, userIdx)
                            insertStatement.setInt(2, studyIdx)
                            insertStatement.executeUpdate()
                        }
                        isFavorite = true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "좋아요 토글 중 오류 발생", e)
        }
        isFavorite
    }
}

package kr.co.lion.modigm.db.study

import android.util.Log
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.*
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.model.SqlStudyData
import java.sql.Connection

class StudyListDao {

    private val tag = "StudyListDao"


    // HikariCP 설정을 초기화하는 suspend 함수
    private suspend fun initDataSource(): HikariDataSource = withContext(Dispatchers.IO) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = BuildConfig.DB_URL
            username = BuildConfig.DB_USER
            password = BuildConfig.DB_PASSWORD
            driverClassName = "com.mysql.jdbc.Driver"
            maximumPoolSize = 10
            minimumIdle = 10
            connectionTimeout = 30000 // 30초
            idleTimeout = 600000 // 10분
            maxLifetime = 1800000 // 30분
            validationTimeout = 5000 // 5초
            leakDetectionThreshold = 0 // 비활성화
        }
        HikariDataSource(hikariConfig)
    }

    private val dataSourceDeferred = CompletableDeferred<HikariDataSource>()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            dataSourceDeferred.complete(initDataSource())
        }
    }

    // 데이터베이스 연결을 생성하는 메소드
    private suspend fun getConnection(): Connection {
        val dataSource = dataSourceDeferred.await()
        return withContext(Dispatchers.IO) {
            dataSource.connection
        }
    }

    // 스터디와 스터디 멤버 데이터 조회 (좋아요 여부 포함)
    suspend fun selectAllStudyAndMemberCount(userIdx: Int): HashMap<Int, Triple<SqlStudyData, Int, Boolean>> =
        withContext(Dispatchers.IO) {
            val studyMemberCountMap = hashMapOf<Int, Triple<SqlStudyData, Int, Boolean>>()
            try {
                getConnection().use { connection ->
                    val combinedQuery = """
                    SELECT s.*, COUNT(sm.userIdx) as memberCount, 
                           IF(f.favoriteIdx IS NOT NULL, TRUE, FALSE) as isFavorite
                    FROM tb_study s
                    LEFT JOIN tb_study_member sm ON s.studyIdx = sm.studyIdx
                    LEFT JOIN tb_favorite f ON s.studyIdx = f.studyIdx AND f.userIdx = ?
                    WHERE s.studyState = true
                    GROUP BY s.studyIdx
                """
                    connection.prepareStatement(combinedQuery).use { statement ->
                        statement.setInt(1, userIdx)
                        val resultSet = statement.executeQuery()
                        while (resultSet.next()) {
                            val studyData = SqlStudyData.getStudyData(resultSet)
                            val memberCount = resultSet.getInt("memberCount")
                            val isFavorite = resultSet.getBoolean("isFavorite")
                            studyMemberCountMap[studyData.studyIdx] =
                                Triple(studyData, memberCount, isFavorite)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "스터디 및 멤버 수 데이터 조회 중 오류 발생", e)
            }
            studyMemberCountMap
        }

    // 특정 userIdx에 해당하는 스터디와 스터디 멤버 데이터 조회 (좋아요 여부 포함)
    suspend fun selectMyStudyAndMemberCount(userIdx: Int): HashMap<Int, Triple<SqlStudyData, Int, Boolean>> =
        withContext(Dispatchers.IO) {
            val studyMemberCountMap = hashMapOf<Int, Triple<SqlStudyData, Int, Boolean>>()
            try {
                getConnection().use { connection ->
                    val combinedQuery = """
                    SELECT s.*, COUNT(sm.userIdx) as memberCount,
                           IF(f.favoriteIdx IS NOT NULL, TRUE, FALSE) as isFavorite
                    FROM tb_study s
                    LEFT JOIN tb_study_member sm ON s.studyIdx = sm.studyIdx
                    LEFT JOIN tb_favorite f ON s.studyIdx = f.studyIdx AND f.userIdx = ?
                    WHERE sm.userIdx = ?
                    GROUP BY s.studyIdx
                """
                    connection.prepareStatement(combinedQuery).use { statement ->
                        statement.setInt(1, userIdx)
                        statement.setInt(2, userIdx)
                        val resultSet = statement.executeQuery()
                        while (resultSet.next()) {
                            val studyData = SqlStudyData.getStudyData(resultSet)
                            val memberCount = resultSet.getInt("memberCount")
                            val isFavorite = resultSet.getBoolean("isFavorite")
                            studyMemberCountMap[studyData.studyIdx] =
                                Triple(studyData, memberCount, isFavorite)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "사용자별 스터디 및 멤버 수 데이터 조회 중 오류 발생", e)
            }
            studyMemberCountMap
        }

    // 좋아요 토글 메소드
    suspend fun toggleFavorite(userIdx: Int, studyIdx: Int): Boolean = withContext(Dispatchers.IO) {
        var isFavorite = false
        try {
            getConnection().use { connection ->
                // 좋아요 상태 확인
                val checkFavoriteQuery = """
                    SELECT * FROM tb_favorite WHERE userIdx = ? AND studyIdx = ?
                """
                connection.prepareStatement(checkFavoriteQuery).use { statement ->
                    statement.setInt(1, userIdx)
                    statement.setInt(2, studyIdx)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        // 좋아요 되어 있으면 삭제
                        val deleteFavoriteQuery =
                            "DELETE FROM tb_favorite WHERE userIdx = ? AND studyIdx = ?"
                        connection.prepareStatement(deleteFavoriteQuery).use { deleteStatement ->
                            deleteStatement.setInt(1, userIdx)
                            deleteStatement.setInt(2, studyIdx)
                            deleteStatement.executeUpdate()
                        }
                    } else {
                        // 좋아요 되어 있지 않으면 추가
                        val insertFavoriteQuery =
                            "INSERT INTO tb_favorite (userIdx, studyIdx) VALUES (?, ?)"
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

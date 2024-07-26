package kr.co.lion.modigm.db.study

import android.util.Log
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.model.SqlStudyData
import java.sql.Connection

class StudyDao {

    private val tag = "StudyDao"

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
            leakDetectionThreshold = 30000 // 30초
        }
        HikariDataSource(hikariConfig)
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(tag, "HikariCP coroutineExceptionHandler 에러 ", throwable)
    }

    private val job = Job()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + job + coroutineExceptionHandler)
    private val dataSourceDeferred: Deferred<HikariDataSource> by lazy {
        coroutineScope.async {
            initDataSource()
        }
    }

    private suspend fun getConnection(): Connection {
        val dataSource = dataSourceDeferred.await()
        return withContext(Dispatchers.IO) {
            dataSource.connection
        }
    }

    // 스터디와 스터디 멤버 데이터 조회 (좋아요 여부 포함)
    suspend fun selectAllStudyAndMemberCount(userIdx: Int): Result<List<Triple<SqlStudyData, Int, Boolean>>> =
        withContext(Dispatchers.IO) {
            runCatching {
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
                        val result = mutableListOf<Triple<SqlStudyData, Int, Boolean>>()
                        while (resultSet.next()) {
                            val studyData = SqlStudyData.getStudyData(resultSet)
                            val memberCount = resultSet.getInt("memberCount")
                            val isFavorite = resultSet.getBoolean("isFavorite")
                            result.add(Triple(studyData, memberCount, isFavorite))
                        }
                        result
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "스터디 및 멤버 수 데이터 조회 중 오류 발생", e)
            }
        }


    // 특정 userIdx에 해당하는 스터디와 스터디 멤버 데이터 조회 (좋아요 여부 포함)
    suspend fun selectMyStudyAndMemberCount(userIdx: Int): Result<List<Triple<SqlStudyData, Int, Boolean>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                getConnection().use { connection ->
                    val query = """
                    SELECT s.*, COUNT(sm.userIdx) as memberCount,
                           IF(f.favoriteIdx IS NOT NULL, TRUE, FALSE) as isFavorite
                    FROM tb_study s
                    LEFT JOIN tb_study_member sm ON s.studyIdx = sm.studyIdx
                    LEFT JOIN tb_favorite f ON s.studyIdx = f.studyIdx AND f.userIdx = ?
                    WHERE sm.userIdx = ?
                    GROUP BY s.studyIdx
                """
                    connection.prepareStatement(query).use { statement ->
                        statement.setInt(1, userIdx)
                        statement.setInt(2, userIdx)
                        val resultSet = statement.executeQuery()
                        val result = mutableListOf<Triple<SqlStudyData, Int, Boolean>>()
                        while (resultSet.next()) {
                            val studyData = SqlStudyData.getStudyData(resultSet)
                            val memberCount = resultSet.getInt("memberCount")
                            val isFavorite = resultSet.getBoolean("isFavorite")
                            result.add(Triple(studyData, memberCount, isFavorite))
                        }
                        result
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "내 스터디 목록 조회 중 오류 발생", e)
            }
        }

    // 좋아요 토글 메소드
    suspend fun toggleFavorite(userIdx: Int, studyIdx: Int): Result<Boolean> =
        withContext(Dispatchers.IO) {
            runCatching {
                getConnection().use { connection ->
                    if (isFavorite(connection, userIdx, studyIdx).getOrThrow()) {
                        removeFavorite(connection, userIdx, studyIdx)
                        false
                    } else {
                        addFavorite(connection, userIdx, studyIdx)
                        true
                    }
                }
            }.onFailure { e ->
                Log.e("FavoriteToggle", "좋아요 토글 중 오류 발생", e)
            }
        }

    // 좋아요 상태 확인 함수
    private fun isFavorite(connection: Connection, userIdx: Int, studyIdx: Int): Result<Boolean> =
        runCatching {
            val query = "SELECT * FROM tb_favorite WHERE userIdx = ? AND studyIdx = ?"
            connection.prepareStatement(query).use { statement ->
                statement.setInt(1, userIdx)
                statement.setInt(2, studyIdx)
                val resultSet = statement.executeQuery()
                resultSet.next()
            }
        }.onFailure { e ->
            Log.e(tag, "좋아요 상태 확인 중 오류 발생", e)
        }

    // 좋아요 추가 함수
    private fun addFavorite(connection: Connection, userIdx: Int, studyIdx: Int) {
        val query = "INSERT INTO tb_favorite (userIdx, studyIdx) VALUES (?, ?)"
        connection.prepareStatement(query).use { statement ->
            statement.setInt(1, userIdx)
            statement.setInt(2, studyIdx)
            statement.executeUpdate()
        }
    }

    // 좋아요 삭제 함수
    private fun removeFavorite(connection: Connection, userIdx: Int, studyIdx: Int) {
        val query = "DELETE FROM tb_favorite WHERE userIdx = ? AND studyIdx = ?"
        connection.prepareStatement(query).use { statement ->
            statement.setInt(1, userIdx)
            statement.setInt(2, studyIdx)
            statement.executeUpdate()
        }
    }

    suspend fun close() {
        try {
            // Job을 취소하고 모든 하위 코루틴이 종료될 때까지 대기
            job.cancelAndJoin()
            // 데이터 소스가 완료된 경우, 데이터 소스를 안전하게 종료
            withContext(Dispatchers.IO) {
                if (dataSourceDeferred.isCompleted) {
                    dataSourceDeferred.await().close()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "HikariCP 코루틴 취소 실패", e)
        }
    }
}

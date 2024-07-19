package kr.co.lion.modigm.db.favorite

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

class FavoriteStudyDao {

    private val tag = "FavoriteStudyDao"

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

    // 좋아요한 스터디 목록 조회
    suspend fun selectMyFavoriteStudyDataList(userIdx: Int): HashMap<Int, Triple<SqlStudyData, Int, Boolean>> =
        withContext(Dispatchers.IO) {
            val favoriteStudiesMap = hashMapOf<Int, Triple<SqlStudyData, Int, Boolean>>()
            try {
                getConnection().use { connection ->
                    val query = """
                    SELECT s.*, COUNT(sm.userIdx) as memberCount, 
                           IF(f.favoriteIdx IS NOT NULL, TRUE, FALSE) as isFavorite
                    FROM tb_favorite f
                    INNER JOIN tb_study s ON f.studyIdx = s.studyIdx
                    LEFT JOIN tb_study_member sm ON s.studyIdx = sm.studyIdx
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
                            favoriteStudiesMap[studyData.studyIdx] =
                                Triple(studyData, memberCount, isFavorite)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "좋아요한 스터디 목록 조회 중 오류 발생", e)
            }
            favoriteStudiesMap
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

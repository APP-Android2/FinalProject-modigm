package kr.co.lion.modigm.db

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
import java.sql.Connection

object HikariCPDataSource {

    private const val TAG = "HikariCPDataSource"

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "HikariCP coroutineExceptionHandler 에러 ", throwable)
    }

    private val job = Job()
    private val coroutineScope: CoroutineScope =
        CoroutineScope(Dispatchers.IO + job + coroutineExceptionHandler)
    private val dataSourceDeferred: Deferred<HikariDataSource> by lazy {
        coroutineScope.async {
            initDataSource()
        }
    }

    // HikariCP 설정을 초기화하는 함수
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

    // 데이터베이스 연결을 얻는 함수
    suspend fun getConnection(): Connection {
        val dataSource = dataSourceDeferred.await()
        return withContext(Dispatchers.IO) {
            dataSource.connection
        }
    }

    // 데이터 소스를 안전하게 종료하는 함수
    suspend fun closeDataSource() {
        try {
            // Job을 취소하고 모든 하위 코루틴이 종료될 때까지 대기
            job.cancelAndJoin()
            // 데이터 소스가 완료된 경우, 데이터 소스를 안전하게 종료
            withContext(Dispatchers.IO + coroutineExceptionHandler) {
                if (dataSourceDeferred.isCompleted) {
                    dataSourceDeferred.await().close()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "HikariCP 코루틴 취소 실패", e)

        }
    }
}
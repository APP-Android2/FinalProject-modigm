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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.BuildConfig
import java.sql.Connection

object HikariCPDataSource {

    private val tag by lazy { HikariCPDataSource::class.simpleName }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(tag, "HikariCP coroutineExceptionHandler 에러 ", throwable)
    }

    private val job = Job()
    private val coroutineScope: CoroutineScope =
        CoroutineScope(Dispatchers.IO + job + coroutineExceptionHandler)

    private val mutex = Mutex()
    @Volatile
    private var dataSourceDeferred: Deferred<HikariDataSource> = createDataSourceDeferred()

    private fun createDataSourceDeferred(): Deferred<HikariDataSource> {
        return coroutineScope.async {
            initDataSource()
        }
    }

    private suspend fun initDataSource(): HikariDataSource = withContext(Dispatchers.IO) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = BuildConfig.DB_URL
            username = BuildConfig.DB_USER
            password = BuildConfig.DB_PASSWORD
            driverClassName = "com.mysql.jdbc.Driver"
            maximumPoolSize = 10
            minimumIdle = 10
            connectionTimeout = 30000
            idleTimeout = 600000
            maxLifetime = 1800000
            validationTimeout = 5000
            leakDetectionThreshold = 30000
        }
        HikariDataSource(hikariConfig)
    }

    // 데이터베이스 연결을 얻는 함수
    suspend fun getConnection(): Connection {
        mutex.withLock {
            if (!dataSourceDeferred.isCompleted || dataSourceDeferred.await().isClosed) {
                dataSourceDeferred = createDataSourceDeferred()
            }
        }
        val dataSource = dataSourceDeferred.await()
        return withContext(Dispatchers.IO) {
            dataSource.connection
        }
    }

    suspend fun closeDataSource() {
        try {
            job.cancelAndJoin()
            withContext(Dispatchers.IO + coroutineExceptionHandler) {
                if (dataSourceDeferred.isCompleted) {
                    dataSourceDeferred.await().close()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "HikariCP 코루틴 취소 실패", e)
        }
    }
}

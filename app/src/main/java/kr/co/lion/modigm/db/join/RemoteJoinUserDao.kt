package kr.co.lion.modigm.db.join

import android.util.Log
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.BuildConfig
import java.sql.Connection
import java.sql.PreparedStatement

class RemoteJoinUserDao {
    private val TAG = "JoinUserDao"

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
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine exception", throwable)
    }
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + Job() + coroutineExceptionHandler)

    init {
        coroutineScope.launch {
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

    // Dao가 더 이상 필요 없을 때 자원을 해제하는 메소드 (destroy에 호출)
    suspend fun closeConn() {
        try{
            coroutineScope.coroutineContext[Job]?.cancelAndJoin()
            if (dataSourceDeferred.isCompleted) {
                dataSourceDeferred.await().close()
            }
        }catch (e: Exception){
            Log.d("JoinUserDao", "Error closing db")
        }
    }

    suspend fun insertUserData(model: Map<String, Any>): Int?{
        var preparedStatement: PreparedStatement?
        val columns = model.keys
        val values = model.values
        var idx:Int? = null

        return try {
            val columnsString = StringBuilder()
            val valuesString = StringBuilder()
            columns.forEach { column ->
                columnsString.append("$column,")
                valuesString.append("?,")
            }

            columnsString.deleteCharAt(columnsString.length-1)
            valuesString.deleteCharAt(valuesString.length-1)
            val sql = "INSERT INTO tb_user ($columnsString) VALUES ($valuesString)"

            withContext(Dispatchers.IO){
                getConnection().use { connection ->
                    preparedStatement = connection.prepareStatement(sql) // PreparedStatement 생성
                    values.forEachIndexed { index, value ->
                        // 쿼리 매개변수 설정
                        if(value is String){
                            preparedStatement?.setString(index+1, value)
                        }
                        if(value is Int){
                            preparedStatement?.setInt(index+1, value)
                        }
                        if(value is Boolean){
                            preparedStatement?.setBoolean(index+1, value)
                        }
                    }
                    preparedStatement?.executeUpdate() // 쿼리 실행

                    val afterExecute = connection?.prepareStatement("SELECT LAST_INSERT_ID()")
                    val resultSet = afterExecute?.executeQuery()
                    resultSet?.next()
                    if(resultSet != null) idx = resultSet.getInt("LAST_INSERT_ID()")
                }
            }
            idx ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error in insertUserData", e) // 오류 로그 출력
            0
        }
    }
}
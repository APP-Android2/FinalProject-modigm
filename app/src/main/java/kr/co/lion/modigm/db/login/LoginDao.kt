package kr.co.lion.modigm.db.login

import android.util.Log
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.*
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.model.SqlUserData
import kr.co.lion.modigm.ui.login.LoginError
import java.sql.Connection
import java.sql.SQLDataException
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.SQLSyntaxErrorException
import java.sql.SQLTimeoutException

class LoginDao {

    private val tag = "LoginDao"

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
    private val coroutineScope: CoroutineScope =
        CoroutineScope(Dispatchers.IO + job + coroutineExceptionHandler)
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

    // userIdx를 통해 해당 유저의 데이터 조회
    suspend fun selectUserDataByUserIdx(userIdx: Int): Result<SqlUserData> =
        withContext(Dispatchers.IO) {
            runCatching {
                getConnection().use { connection ->
                    val query = """
                        SELECT *
                        FROM tb_user
                        WHERE userIdx = ?
                    """
                    connection.prepareStatement(query).use { statement ->
                        statement.setInt(1, userIdx)
                        statement.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                SqlUserData.getUserData(resultSet)
                            } else {
                                throw LoginError.DatabaseConnectionError
                            }
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "userIdx로 유저 데이터 조회 중 오류 발생", e)
                throw mapToLoginError(e)
            }
        }

    // uid를 통해 이미 존재하는 유저인지 확인
    suspend fun isUserAlreadyRegistered(uid: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            runCatching {
                getConnection().use { connection ->
                    val query = """
                        SELECT *
                        FROM tb_user
                        WHERE userUid = ?
                    """
                    connection.prepareStatement(query).use { statement ->
                        statement.setString(1, uid)
                        statement.executeQuery().use { resultSet ->
                            resultSet.next()
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "이미 존재하는 유저인지 조회 중 오류 발생", e)
                throw mapToLoginError(e)
            }
        }

    // userUid를 통해 해당 유저의 데이터 조회
    suspend fun selectUserDataByUserUid(userUid: String): Result<SqlUserData> =
        withContext(Dispatchers.IO) {
            runCatching {
                getConnection().use { connection ->
                    val query = """
                        SELECT *
                        FROM tb_user
                        WHERE userUid = ?
                    """
                    connection.prepareStatement(query).use { statement ->
                        statement.setString(1, userUid)
                        statement.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                SqlUserData.getUserData(resultSet)
                            } else {
                                throw LoginError.DatabaseConnectionError
                            }
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "userUid로 유저 데이터 조회 중 오류 발생", e)
                throw mapToLoginError(e)
            }
        }

    suspend fun selectUserIdxByUserUid(userUid: String): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                getConnection().use { connection ->
                    val query = """
                        SELECT userIdx
                        FROM tb_user
                        WHERE userUid = ?
                    """
                    connection.prepareStatement(query).use { statement ->
                        statement.setString(1, userUid)
                        statement.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                resultSet.getInt("userIdx") // userIdx 값을 추출하여 리턴
                            } else {
                                throw LoginError.DatabaseConnectionError
                            }
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "userUid로 유저 데이터 조회 중 오류 발생", e)
                throw mapToLoginError(e)
            }
        }

    suspend fun selectUserUidByUserIdx(userIdx: Int): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                getConnection().use { connection ->
                    val query = """
                        SELECT userUid
                        FROM tb_user
                        WHERE userIdx = ?
                    """
                    connection.prepareStatement(query).use { statement ->
                        statement.setInt(1, userIdx)
                        statement.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                resultSet.getString("userUid") // userUid 값을 추출하여 리턴
                            } else {
                                throw LoginError.DatabaseConnectionError
                            }
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "userUid로 유저 데이터 조회 중 오류 발생", e)
                throw mapToLoginError(e)
            }
        }

    private fun mapToLoginError(e: Throwable): LoginError {
        return when (e) {
            is SQLSyntaxErrorException -> LoginError.DatabaseSyntaxError
            is SQLIntegrityConstraintViolationException -> LoginError.DatabaseIntegrityError
            is SQLDataException -> LoginError.DatabaseConnectionError
            is SQLTimeoutException -> LoginError.DatabaseTimeoutError
            else -> LoginError.DatabaseUnknownError
        }
    }

    suspend fun daoCoroutineCancel() {
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
            Log.e(tag, "HikariCP 코루틴 취소 실패", e)
        }
    }
}

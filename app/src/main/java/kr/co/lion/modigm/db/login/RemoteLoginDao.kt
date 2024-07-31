package kr.co.lion.modigm.db.login

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.db.HikariCPDataSource
import kr.co.lion.modigm.model.SqlUserData
import kr.co.lion.modigm.ui.login.LoginError
import java.sql.SQLDataException
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.SQLInvalidAuthorizationSpecException
import java.sql.SQLNonTransientConnectionException
import java.sql.SQLNonTransientException
import java.sql.SQLRecoverableException
import java.sql.SQLSyntaxErrorException
import java.sql.SQLTimeoutException
import java.sql.SQLTransactionRollbackException
import java.sql.SQLTransientConnectionException

class RemoteLoginDao {

    private val tag by lazy { "LoginDao" }

    /**
     * userIdx를 통해 해당 유저의 데이터 조회
     * @param userIdx 사용자 인덱스
     * @return Result<SqlUserData> 조회된 사용자 데이터를 반환
     */
    suspend fun selectUserDataByUserIdx(userIdx: Int): Result<SqlUserData> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
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
                Result.failure<SqlUserData>(mapToLoginError(e))
            }
        }

    /**
     * userUid를 통해 해당 유저의 데이터 조회
     * @param userUid 사용자 UID
     * @return Result<SqlUserData> 조회된 사용자 데이터를 반환
     */
    suspend fun selectUserDataByUserUid(userUid: String): Result<SqlUserData> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
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
                Result.failure<SqlUserData>(mapToLoginError(e))
            }
        }

    /**
     * userUid를 통해 해당 유저의 인덱스 조회
     * @param userUid 사용자 UID
     * @return Result<Int> 조회된 사용자 인덱스를 반환
     */
    suspend fun selectUserIdxByUserUid(userUid: String): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = """
                        SELECT userIdx
                        FROM tb_user
                        WHERE userUid = ?
                    """
                    connection.prepareStatement(query).use { statement ->
                        statement.setString(1, userUid)
                        statement.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                resultSet.getInt("userIdx")
                            } else {
                                throw LoginError.DatabaseConnectionError
                            }
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "userUid로 유저 데이터 조회 중 오류 발생", e)
                Result.failure<Int>(mapToLoginError(e))
            }
        }

    /**
     * userIdx를 통해 해당 유저의 UID 조회
     * @param userIdx 사용자 인덱스
     * @return Result<String> 조회된 사용자 UID를 반환
     */
    suspend fun selectUserUidByUserIdx(userIdx: Int): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = """
                        SELECT userUid
                        FROM tb_user
                        WHERE userIdx = ?
                    """
                    connection.prepareStatement(query).use { statement ->
                        statement.setInt(1, userIdx)
                        statement.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                resultSet.getString("userUid")
                            } else {
                                throw LoginError.DatabaseConnectionError
                            }
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "userIdx로 유저 데이터 조회 중 오류 발생", e)
                Result.failure<String>(mapToLoginError(e))
            }
        }

    /**
     * userPhone을 통해 해당 유저의 데이터 조회
     * @param userPhone 사용자 전화번호
     * @return Result<SqlUserData> 조회된 사용자 데이터를 반환
     */
    suspend fun selectUserDataByUserPhone(userPhone: String): Result<SqlUserData> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = """
                        SELECT *
                        FROM tb_user
                        WHERE userPhone = ?
                    """
                    connection.prepareStatement(query).use { statement ->
                        statement.setString(1, userPhone)
                        statement.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                SqlUserData.getUserData(resultSet)
                            } else {
                                Log.e(tag, "없는 전화번호")
                                throw LoginError.DatabaseConnectionError
                            }
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "userPhone로 유저 데이터 조회 중 오류 발생", e)
                Result.failure<SqlUserData>(mapToLoginError(e))
            }
        }

    /**
     * userEmail을 통해 해당 유저의 데이터 조회
     * @param userEmail 사용자 이메일
     * @return Result<SqlUserData> 조회된 사용자 데이터를 반환
     */
    suspend fun selectUserDataByUserEmail(userEmail: String): Result<SqlUserData> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = """
                        SELECT *
                        FROM tb_user
                        WHERE userEmail = ?
                    """
                    connection.prepareStatement(query).use { statement ->
                        statement.setString(1, userEmail)
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
                Log.e(tag, "userEmail로 유저 데이터 조회 중 오류 발생", e)
                Result.failure<SqlUserData>(mapToLoginError(e))
            }
        }

    /**
     * SQL 예외를 LoginError로 매핑
     * @param e 발생한 예외
     * @return LoginError 매핑된 로그인 오류
     */
    private fun mapToLoginError(e: Throwable): LoginError {
        return when (e) {
            is SQLSyntaxErrorException -> LoginError.DatabaseSyntaxError
            is SQLIntegrityConstraintViolationException -> LoginError.DatabaseIntegrityError
            is SQLDataException -> LoginError.DatabaseConnectionError
            is SQLTimeoutException -> LoginError.DatabaseTimeoutError
            is SQLTransientConnectionException -> LoginError.DatabaseNetworkError
            is SQLInvalidAuthorizationSpecException -> LoginError.DatabaseAuthenticationError
            is SQLNonTransientConnectionException -> LoginError.DatabaseDiskError
            is SQLTransactionRollbackException -> LoginError.DatabaseConstraintError
            is SQLRecoverableException -> LoginError.DatabaseDataCorruptionError
            is SQLNonTransientException -> LoginError.DatabaseShutdownError
            else -> LoginError.DatabaseUnknownError
        }
    }
}

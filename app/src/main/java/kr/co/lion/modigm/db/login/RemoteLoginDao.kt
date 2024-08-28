package kr.co.lion.modigm.db.login

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.db.HikariCPDataSource
import kr.co.lion.modigm.model.UserData
import java.sql.SQLDataException

class RemoteLoginDao {

    private val tag by lazy { RemoteLoginDao::class.simpleName }

    suspend fun selectUserDataByUserIdx(userIdx: Int): Result<UserData> =
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
                                UserData.getUserData(resultSet)
                            } else {
                                throw SQLDataException("해당 유저를 찾을 수 없습니다.")
                            }
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "userIdx로 유저 데이터 조회 중 오류 발생", e)
                Result.failure<UserData>(e)
            }
        }

    suspend fun selectUserDataByUserUid(userUid: String): Result<UserData> =
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
                                UserData.getUserData(resultSet)
                            } else {
                                throw SQLDataException("해당 유저를 찾을 수 없습니다.")
                            }
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "userUid로 유저 데이터 조회 중 오류 발생", e)
                Result.failure<UserData>(e)
            }
        }

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
                                throw SQLDataException("해당 유저를 찾을 수 없습니다.")
                            }
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "userUid로 유저 데이터 조회 중 오류 발생", e)
                Result.failure<Int>(e)
            }
        }

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
                                throw SQLDataException("해당 유저를 찾을 수 없습니다.")
                            }
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "userIdx로 유저 데이터 조회 중 오류 발생", e)
                Result.failure<String>(e)
            }
        }

    suspend fun selectUserDataByUserPhone(userPhone: String): Result<UserData> =
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
                                UserData.getUserData(resultSet)
                            } else {
                                throw SQLDataException("해당하는 전화번호를 찾을 수 없습니다.")
                            }
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "userPhone로 유저 데이터 조회 중 오류 발생", e)
                Result.failure<UserData>(e)
            }
        }

    suspend fun selectUserDataByUserEmail(userEmail: String): Result<UserData> =
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
                                UserData.getUserData(resultSet)
                            } else {
                                throw SQLDataException("해당 유저를 찾을 수 없습니다.")
                            }
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "userEmail로 유저 데이터 조회 중 오류 발생", e)
                Result.failure<UserData>(e)
            }
        }

    suspend fun updatePhoneByUserIdx(userIdx: Int, userPhone: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = """
                        UPDATE tb_user
                        SET userPhone = ?
                        WHERE userIdx = ?
                    """
                    connection.prepareStatement(query).use { statement ->
                        statement.setString(1, userPhone)
                        statement.setInt(2, userIdx)
                        statement.executeUpdate()
                        true
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "전화번호 변경 중 오류 발생", e)
                Result.failure<Boolean>(e)
            }
        }
}


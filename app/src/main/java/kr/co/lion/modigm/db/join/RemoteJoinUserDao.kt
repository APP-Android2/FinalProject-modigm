package kr.co.lion.modigm.db.join

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.db.HikariCPDataSource
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.LocalDateTime
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.util.Date
import javax.inject.Inject

class RemoteJoinUserDao @Inject constructor() {
    private val TAG = "JoinUserDao"

    suspend fun insertUserData(model: Map<String, Any>): Result<Int>
        = withContext(Dispatchers.IO) {
            runCatching{
                var preparedStatement: PreparedStatement?
                val columns = model.keys
                val values = model.values
                var idx:Int? = null

                val columnsString = StringBuilder()
                val valuesString = StringBuilder()
                columns.forEach { column ->
                    columnsString.append("$column,")
                    valuesString.append("?,")
                }

                columnsString.deleteCharAt(columnsString.length-1)
                valuesString.deleteCharAt(valuesString.length-1)
                val sql = "INSERT INTO tb_user ($columnsString) VALUES ($valuesString)"

                HikariCPDataSource.getConnection().use { connection ->
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
                        if(value is LocalDateTime){
                            preparedStatement?.setTimestamp(index+1, DateTimeUtils.toSqlTimestamp(value))
                        }
                    }
                    preparedStatement?.executeUpdate() // 쿼리 실행

                    val afterExecute = connection.prepareStatement("SELECT LAST_INSERT_ID()")
                    val resultSet = afterExecute?.executeQuery()
                    resultSet?.next()
                    if(resultSet != null) idx = resultSet.getInt("LAST_INSERT_ID()")
                }
                idx ?: -1
            }.onFailure { e ->
                Log.e(TAG, "Error in insertUserData", e) // 오류 로그 출력
            }
        }

    suspend fun checkUserByPhone(phone: String): Result<Map<String, String>?>
        = withContext(Dispatchers.IO) {
            runCatching {
                var preparedStatement: PreparedStatement?
                var resultMap: MutableMap<String, String>? = mutableMapOf()
                val sql = "SELECT userEmail, userProvider FROM tb_user WHERE userPhone = ?"
                HikariCPDataSource.getConnection().use { connection ->
                    preparedStatement = connection.prepareStatement(sql) // PreparedStatement 생성
                    preparedStatement?.setString(1, phone)
                    val resultSet = preparedStatement?.executeQuery() // 쿼리 실행
                    val hasRow = resultSet?.next()
                    if(hasRow == true){
                        resultMap?.set("userEmail", resultSet.getString("userEmail"))
                        resultMap?.set("userProvider", resultSet.getString("userProvider"))
                    }else{
                        resultMap = null
                    }
                }
                resultMap
            }.onFailure { e ->
                Log.e(TAG, "Error in checkUserByPhone", e) // 오류 로그 출력
            }
    }
}
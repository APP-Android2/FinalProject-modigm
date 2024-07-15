package kr.co.lion.modigm.db.join

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kr.co.lion.modigm.BuildConfig
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

class JoinUserDao {
    private var connection: Connection? = null
    private val TAG = "JoinUserDao"

    suspend fun insertUserData(model: Map<String, Any>): Boolean{
        var preparedStatement: PreparedStatement? = null
        val columns = model.keys
        val values = model.values
        return try {
            val columnsString = StringBuilder()
            val valuesString = StringBuilder()
            columns.forEach { column ->
                columnsString.append("$column,")
                valuesString.append("?,")
            }

            columnsString.deleteCharAt(columnsString.length-1)
            valuesString.deleteCharAt(valuesString.length-1)
            val sql = "INSERT INTO tb_user_test ($columnsString) VALUES ($valuesString)"

            val deferred = CoroutineScope(Dispatchers.IO).async {
                Class.forName("com.mysql.jdbc.Driver")
                connection = DriverManager.getConnection(BuildConfig.DB_URL, BuildConfig.DB_USER, BuildConfig.DB_PASSWORD)
                preparedStatement = connection?.prepareStatement(sql) // PreparedStatement 생성
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
            }
            awaitAll(deferred)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error in insertUserData", e) // 오류 로그 출력
            false
        } finally {
            try {
                preparedStatement?.close() // PreparedStatement 닫기
                connection?.close() // 데이터베이스 연결 닫기
                connection = null
            } catch (e: Exception) {
                Log.e(TAG, "Error closing resources", e) // 리소스 닫기 오류 로그 출력
            }
        }
    }
}
package kr.co.lion.modigm.db.write

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kr.co.lion.modigm.BuildConfig
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

class RemoteWriteStudyDao {
    private var connection: Connection? = null
    private val TAG = "MySQLDataSource"

    suspend fun insertStudyData(
        userIdx: Int,
        model: Map<String, Any>,
        studyTechStack: List<Int>
    ):Int? {
        var preparedStatement: PreparedStatement? = null
        val columns = model.keys
        val values = model.values
        var idx:Int? = null
        try {
            val columnsString = StringBuilder()
            val valuesString = StringBuilder()
            columns.forEach { column ->
                columnsString.append("$column,")
                valuesString.append("?,")
            }

            columnsString.deleteCharAt(columnsString.length-1)
            valuesString.deleteCharAt(valuesString.length-1)
            val sql = "INSERT INTO tb_study ($columnsString) VALUES ($valuesString)"

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

                // insert된 행의 idx를 가져온다.
                // == 마지막으로 insert된 행의 auto_increment 값을 가져온다.
                val afterExecute = connection?.prepareStatement("SELECT LAST_INSERT_ID()")
                val resultSet = afterExecute?.executeQuery()
                resultSet?.next()
                if(resultSet != null) idx = resultSet.getInt("LAST_INSERT_ID()")

                if(idx != null){
                    // 기술 스택 등록
                    studyTechStack.forEach {
                        preparedStatement = connection?.prepareStatement(
                            "INSERT INTO tb_study_tech_stack (studyIdx,techIdx) VALUES (?, ?)"
                        ) // PreparedStatement 생성
                        preparedStatement?.setInt(1, idx!!)
                        preparedStatement?.setInt(2, it)
                        preparedStatement?.executeUpdate() // 쿼리 실행
                    }
                    // 스터디 멤버 등록
                    val sql = "INSERT INTO tb_study_member (studyIdx,userIdx) VALUES (?, ?)"
                    preparedStatement = connection?.prepareStatement(sql) // PreparedStatement 생성
                    preparedStatement?.setInt(1, idx!!)
                    preparedStatement?.setInt(2, userIdx)
                    preparedStatement?.executeUpdate() // 쿼리 실행
                }
            }
            awaitAll(deferred)
        } catch (e: Exception) {
            Log.e(TAG, "Error in insertStudyData", e) // 오류 로그 출력
        } finally {
            try {
                preparedStatement?.close() // PreparedStatement 닫기
                connection?.close() // 데이터베이스 연결 닫기
                connection = null
            } catch (e: Exception) {
                Log.e(TAG, "Error closing resources", e) // 리소스 닫기 오류 로그 출력
            }
        }
        return idx
    }

}
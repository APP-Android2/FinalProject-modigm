package kr.co.lion.modigm.db

import android.util.Log
import kr.co.lion.modigm.model.StudyData
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.util.Properties

class MySqlConn {

    private var connection: Connection? = null
    private val localProperties = Properties()
    private val databaseUrl = localProperties.getProperty("database_url") ?: ""
    private val databaseUser = localProperties.getProperty("database_user") ?: ""
    private val databasePassword = localProperties.getProperty("database_password") ?: ""
    private val TAG = localProperties.getProperty("database_tag") ?: ""

    private fun getConnection() {
        Class.forName("com.mysql.jdbc.Driver")
        connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword)
    }

    private fun closeConnection(){
        connection?.close()
        connection = null
    }

    /** sql 쿼리문과 쿼리 매개변수를 array로 전달 */
    fun insertStudyData(model: StudyData){
        var preparedStatement: PreparedStatement? = null
        val columns = model.getColumns()
        val values = model.getValues()

        try {
            val columnsString = StringBuilder()
            val valuesString = StringBuilder()
            columns.forEachIndexed { index, column ->
                if(column != "studyIdx") {
                    columnsString.append("$column,")
                    valuesString.append("?,")
                }
            }

            columnsString.deleteCharAt(columnsString.length-1)
            valuesString.deleteCharAt(valuesString.length-1)
            val sql = "INSERT INTO Study ($columnsString) VALUES ($valuesString)"

            getConnection()
            preparedStatement = connection?.prepareStatement(sql) // PreparedStatement 생성
            values.forEachIndexed { index, value ->
                // 쿼리 매개변수 설정
                if(value is String){
                    preparedStatement?.setString(index + 1, value)
                }
                if(value is Int){
                    preparedStatement?.setInt(index + 1, value)
                }
                if(value is Boolean){
                    preparedStatement?.setBoolean(index + 1, value)
                }
            }
            preparedStatement?.executeUpdate() // 쿼리 실행
        } catch (e: Exception) {
            Log.e(TAG, "Error in insertStudyData", e) // 오류 로그 출력
        } finally {
            try {
                preparedStatement?.close() // PreparedStatement 닫기
                closeConnection()  // 데이터베이스 연결 닫기
            } catch (e: Exception) {
                Log.e(TAG, "Error closing resources", e) // 리소스 닫기 오류 로그 출력
            }
        }
    }

    fun selectStudyOneByIdx(idx:Int): StudyData?{
        val selectSQL = "SELECT * FROM Study WHERE idx = ?"  // 선택 SQL 쿼리
        var statement: Statement? = null
        var resultSet: ResultSet? = null
        var result:StudyData? = null

        try {
            getConnection()
            statement = connection?.createStatement()  // Statement 생성
            resultSet = statement?.executeQuery(selectSQL)  // 쿼리 실행 및 결과 얻기
            if(resultSet != null){
                resultSet.next()
                result = StudyData.getStudyData(resultSet)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in selectStudyOneByIdx", e)  // 오류 로그 출력
        } finally {
            try {
                resultSet?.close()  // ResultSet 닫기
                statement?.close()  // Statement 닫기
                closeConnection()  // 데이터베이스 연결 닫기
            } catch (e: Exception) {
                Log.e(TAG, "Error closing resources", e)  // 리소스 닫기 오류 로그 출력
            }
        }
        return result // 결과 반환
    }

    fun selectStudyAll(whereStatement:String): MutableList<StudyData>{
        val selectSQL = "SELECT * FROM Study WHERE $whereStatement"  // 선택 SQL 쿼리
        var statement: Statement? = null
        var resultSet: ResultSet? = null
        val result = mutableListOf<StudyData>()

        try {
            getConnection()
            statement = connection?.createStatement()  // Statement 생성
            resultSet = statement?.executeQuery(selectSQL)  // 쿼리 실행 및 결과 얻기
            if(resultSet != null){
                while(resultSet.next()){
                    result.add(StudyData.getStudyData(resultSet))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in selectStudyAll", e)  // 오류 로그 출력
        } finally {
            try {
                resultSet?.close()  // ResultSet 닫기
                statement?.close()  // Statement 닫기
                closeConnection()  // 데이터베이스 연결 닫기
            } catch (e: Exception) {
                Log.e(TAG, "Error closing resources", e)  // 리소스 닫기 오류 로그 출력
            }
        }
        return result // 결과 반환
    }

    fun updateStudy(model: StudyData){
        var preparedStatement: PreparedStatement? = null
        val columns = model.getColumns()
        val values = model.getValues()

        try {
            var setString = StringBuilder()
            columns.forEachIndexed { index, column ->
                setString.append("$column = ?,")
            }
            setString.deleteCharAt(setString.length)
            val sql = "UPDATE Study SET $setString WHERE idx='${model.studyIdx}'"  // 선택 SQL 쿼리

            getConnection()
            preparedStatement = connection?.prepareStatement(sql) // PreparedStatement 생성
            values.forEachIndexed { index, value ->
                // 쿼리 매개변수 설정
                if(value is String){
                    preparedStatement?.setString(index + 1, value)
                }
                if(value is Int){
                    preparedStatement?.setInt(index + 1, value)
                }
                if(value is Boolean){
                    preparedStatement?.setBoolean(index + 1, value)
                }
            }
            preparedStatement?.executeUpdate() // 쿼리 실행
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateStudy", e)  // 오류 로그 출력
        } finally {
            try {
                preparedStatement?.close()  // preparedStatement 닫기
                closeConnection()  // 데이터베이스 연결 닫기
            } catch (e: Exception) {
                Log.e(TAG, "Error closing resources", e)  // 리소스 닫기 오류 로그 출력
            }
        }
    }

    fun deleteStudy(idx:Int){
        var preparedStatement: PreparedStatement? = null
        val sql = "DELETE Study WHERE studyIdx='$idx'"  // 선택 SQL 쿼리

        try {
            getConnection()
            preparedStatement = connection?.prepareStatement(sql) // PreparedStatement 생성
            preparedStatement?.executeUpdate() // 쿼리 실행
        } catch (e: Exception) {
            Log.e(TAG, "Error in deleteStudy", e)  // 오류 로그 출력
        } finally {
            try {
                preparedStatement?.close()  // preparedStatement 닫기
                closeConnection()  // 데이터베이스 연결 닫기
            } catch (e: Exception) {
                Log.e(TAG, "Error closing resources", e)  // 리소스 닫기 오류 로그 출력
            }
        }
    }

}
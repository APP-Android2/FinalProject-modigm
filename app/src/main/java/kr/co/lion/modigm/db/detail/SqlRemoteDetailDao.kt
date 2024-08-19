package kr.co.lion.modigm.db.detail

import android.util.Log
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.*
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.db.HikariCPDataSource
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.model.SqlUserData
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class SqlRemoteDetailDao {
    private val TAG = "SqlRemoteDetailDao"

    // 쿼리를 실행하고 결과를 처리하는 공통 메소드
    suspend fun <T> executeQuery(query: String, vararg params: Any, block: (ResultSet) -> T?): List<T> = withContext(Dispatchers.IO) {
        try {
            val results = mutableListOf<T>() // 결과를 저장할 리스트
            HikariCPDataSource.getConnection().use { connection -> // 데이터베이스 연결을 가져와 사용
                Log.d(TAG, "Executing query: $query with params: ${params.joinToString()}") // 쿼리 실행 전 로그
                connection.prepareStatement(query).use { statement -> // 쿼리 준비
                    // 쿼리 매개변수 설정
                    params.forEachIndexed { index, param ->
                        statement.setObject(index + 1, param)
                    }
                    // 쿼리 실행 및 결과 처리
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) { // 결과셋을 순회하며 결과를 처리
                        block(resultSet)?.let { results.add(it) } // 결과를 리스트에 추가
                    }
                }
            }
            Log.d(TAG, "Query executed successfully: $query with results: $results") // 쿼리 성공 로그
            results // 결과 리스트 반환
        } catch (e: Exception) {
            Log.e(TAG, "쿼리 실행 중 오류 발생", e) // 오류 로그
            emptyList() // 오류 발생 시 빈 리스트 반환
        }
    }

    // 스터디 데이터를 모두 가져오는 메소드
    suspend fun getAllStudies(): List<SqlStudyData> {
        val query = "SELECT * FROM tb_study" // 쿼리문
        return executeQuery(query) { resultSet ->
            SqlStudyData.getStudyData(resultSet) // 결과셋에서 스터디 데이터를 가져오는 메소드 호출
        }
    }

    // 모든 스터디 멤버 수를 가져오는 메소드
    suspend fun getAllStudyMembers(): List<Pair<Int, Int>> {
        val query = "SELECT studyIdx, COUNT(userIdx) AS memberCount FROM tb_study_member GROUP BY studyIdx" // 쿼리문
        return executeQuery(query) { resultSet ->
            Pair(resultSet.getInt("studyIdx"), resultSet.getInt("memberCount")) // 스터디 ID와 멤버 수를 쌍으로 반환
        }
    }

    // 특정 studyIdx에 해당하는 userIdx 리스트를 가져오는 메소드
    suspend fun getUserIdsByStudyIdx(studyIdx: Int): List<Int> {
        val query = "SELECT userIdx FROM tb_study_member WHERE studyIdx = ?"
        return executeQuery(query, studyIdx) { resultSet ->
            resultSet.getInt("userIdx")
        }
    }

    // 특정 사용자의 모든 좋아요 정보를 가져오는 메소드
    suspend fun getAllFavorites(userIdx: Int): List<Pair<Int, Boolean>> {
        val query = "SELECT studyIdx, IF(favoriteIdx IS NOT NULL, TRUE, FALSE) as isFavorite FROM tb_favorite WHERE userIdx = ?" // 쿼리문
        return executeQuery(query, userIdx) { resultSet ->
            Pair(resultSet.getInt("studyIdx"), resultSet.getBoolean("isFavorite")) // 스터디 ID와 좋아요 여부를 쌍으로 반환
        }
    }

    // 모든 사용자 데이터를 가져오는 메소드
    suspend fun getAllUsers(): List<SqlUserData> {
        val query = "SELECT * FROM tb_user" // 쿼리문
        return executeQuery(query) { resultSet ->
            SqlUserData.getUserData(resultSet) // 결과셋에서 사용자 데이터를 가져오는 메소드 호출
        }
    }

    // StudyTechStack 테이블의 모든 데이터를 가져오는 메소드
    suspend fun getAllStudyTechStack(): List<Pair<Int, Int>> {
        val query = "SELECT studyIdx, techIdx FROM tb_study_tech_stack" // 쿼리문
        return executeQuery(query) { resultSet ->
            Pair(resultSet.getInt("studyIdx"), resultSet.getInt("techIdx")) // studyIdx와 techIdx를 쌍으로 반환
        }
    }

    // 특정 스터디의 기술 스택을 조회하는 메소드
    suspend fun getStudyTechStack(studyIdx: Int): List<Int> {
        val query = "SELECT techIdx FROM tb_study_tech_stack WHERE studyIdx = ?"
        return executeQuery(query, studyIdx) { resultSet ->
            resultSet.getInt("techIdx")
        }
    }

    // studyState 값을 업데이트하는 메소드 추가
    suspend fun updateStudyState(studyIdx: Int, newState: Int): Int = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "UPDATE tb_study SET studyState = ? WHERE studyIdx = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, newState)
                    statement.setInt(2, studyIdx)
                    return@withContext statement.executeUpdate()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating studyState", e)
            return@withContext 0
        }
    }

    // 데이터 업데이트 메소드
    suspend fun updateStudy(studyData: SqlStudyData): Int = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = """
                    UPDATE tb_study SET 
                        studyTitle = ?, 
                        studyContent = ?, 
                        studyType = ?, 
                        studyPeriod = ?, 
                        studyOnOffline = ?, 
                        studyPlace = ?, 
                        studyDetailPlace = ?, 
                        studyApplyMethod = ?, 
                        studyCanApply = ?, 
                        studyPic = ?, 
                        studyMaxMember = ?, 
                        studyState = ?, 
                        userIdx = ? 
                    WHERE studyIdx = ?
                """
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, studyData.studyTitle)
                    statement.setString(2, studyData.studyContent)
                    statement.setString(3, studyData.studyType)
                    statement.setString(4, studyData.studyPeriod)
                    statement.setString(5, studyData.studyOnOffline)
                    statement.setString(6, studyData.studyPlace)
                    statement.setString(7, studyData.studyDetailPlace)
                    statement.setString(8, studyData.studyApplyMethod)
                    statement.setString(9, studyData.studyCanApply)
                    statement.setString(10, studyData.studyPic)
                    statement.setInt(11, studyData.studyMaxMember)
                    statement.setBoolean(12, studyData.studyState)
                    statement.setInt(13, studyData.userIdx)
                    statement.setInt(14, studyData.studyIdx)
                    return@withContext statement.executeUpdate()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating study data", e)
            return@withContext 0
        }
    }

    // 스터디 테이블에 tech stack 데이터를 삽입하는 메서드
    suspend fun insertStudyTechStack(studyIdx: Int, techStack: List<Int>) {
        val existingTechStack = getStudyTechStack(studyIdx).toSet()
        val newTechStack = techStack.filter { it !in existingTechStack }

        if (newTechStack.isNotEmpty()) {
            val sql = "INSERT INTO tb_study_tech_stack (studyIdx, techIdx) VALUES (?, ?)"
            val paramsList = newTechStack.map { arrayOf<Any>(studyIdx, it) }
            executeBatchUpdate(sql, paramsList)
        }
    }

    // 여러 개의 SQL 업데이트를 한 번에 실행하는 배치 업데이트 함수
    private suspend fun executeBatchUpdate(sql: String, paramsList: List<Array<out Any>>) {
        var preparedStatement: PreparedStatement? = null
        try {
            withContext(Dispatchers.IO) {
                HikariCPDataSource.getConnection().use { connection ->
                    preparedStatement = connection.prepareStatement(sql)
                    paramsList.forEach { params ->
                        params.forEachIndexed { index, value ->
                            when (value) {
                                is String -> preparedStatement?.setString(index + 1, value)
                                is Int -> preparedStatement?.setInt(index + 1, value)
                            }
                        }
                        preparedStatement?.addBatch()
                    }
                    preparedStatement?.executeBatch()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in executeBatchUpdate", e)
        } finally {
            preparedStatement?.close()
        }
    }

    // 특정 studyIdx와 userIdx에 해당하는 사용자를 tb_study_member 테이블에서 삭제하는 메소드
    suspend fun removeUserFromStudy(studyIdx: Int, userIdx: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            HikariCPDataSource.getConnection().use { connection ->
                val query = "DELETE FROM tb_study_member WHERE studyIdx = ? AND userIdx = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setInt(1, studyIdx)
                    statement.setInt(2, userIdx)
                    return@withContext statement.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing user from study", e)
            return@withContext false
        }
    }
}
package kr.co.lion.modigm.db.study

import android.util.Log
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.BuildConfig
import kr.co.lion.modigm.model.SqlStudyData
import kr.co.lion.modigm.model.SqlUserData
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class SqlRemoteDetailDao {
    private val TAG = "SqlRemoteDetailDao"
    private var dataSource: HikariDataSource? = null

    init {
        // HikariCP 초기화를 비동기로 수행
        initializeDataSource()
    }

    fun initializeDataSource() {
        // 코루틴을 사용하여 HikariCP 초기화를 IO 디스패처에서 수행
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // HikariConfig를 사용하여 HikariCP 설정
                val config = HikariConfig().apply {
                    jdbcUrl = BuildConfig.DB_URL
                    username = BuildConfig.DB_USER
                    password = BuildConfig.DB_PASSWORD
                    maximumPoolSize = 10 // 최대 연결 풀 크기
                    connectionTimeout = 30000 // 연결 타임아웃 (밀리초)
                    idleTimeout = 600000 // 유휴 연결 타임아웃 (밀리초)
                    maxLifetime = 1800000 // 연결 최대 수명 (밀리초)
                }
                dataSource = HikariDataSource(config) // 설정을 적용하여 HikariDataSource 생성
                Log.d(TAG, "HikariCP initialized successfully") // 초기화 성공 로그
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize HikariCP", e) // 초기화 실패 로그
            }
        }
    }

    // 데이터베이스 연결을 가져오는 메소드
    suspend fun getConnection(): Connection = withContext(Dispatchers.IO) {
        // HikariCP 데이터소스에서 연결을 가져오거나 DriverManager에서 직접 연결을 가져옴(오류 발생시 직접 연결)
        dataSource?.connection ?: DriverManager.getConnection(BuildConfig.DB_URL, BuildConfig.DB_USER, BuildConfig.DB_PASSWORD)
    }

    // 쿼리를 실행하고 결과를 처리하는 공통 메소드
    suspend fun <T> executeQuery(query: String, vararg params: Any, block: (ResultSet) -> T?): List<T> = withContext(Dispatchers.IO) {
        try {
            val results = mutableListOf<T>() // 결과를 저장할 리스트
            getConnection().use { connection -> // 데이터베이스 연결을 가져와 사용
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

    // studyState 값을 업데이트하는 메소드 추가
    suspend fun updateStudyState(studyIdx: Int, newState: Int): Int = withContext(Dispatchers.IO) {
        try {
            getConnection().use { connection ->
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
}
package kr.co.lion.modigm.db.study

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.db.HikariCPDataSource
import kr.co.lion.modigm.model.SqlStudyData

class StudyDao {

    private val tag by lazy { StudyDao::class.simpleName }

    /**
     * 모든 스터디와 스터디 멤버 데이터 조회 (좋아요 여부 포함)
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun selectAllStudyData(userIdx: Int): Result<List<Triple<SqlStudyData, Int, Boolean>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val combinedQuery = """
                SELECT s.*, COUNT(sm.userIdx) as memberCount, 
                       IF(f.favoriteIdx IS NOT NULL, TRUE, FALSE) as isFavorite
                FROM tb_study s
                LEFT JOIN tb_study_member sm ON s.studyIdx = sm.studyIdx
                LEFT JOIN tb_favorite f ON s.studyIdx = f.studyIdx AND f.userIdx = ?
                WHERE s.studyState = true
                GROUP BY s.studyIdx
                """
                    connection.prepareStatement(combinedQuery).use { statement ->
                        statement.setInt(1, userIdx)
                        val resultSet = statement.executeQuery()
                        val result = mutableListOf<Triple<SqlStudyData, Int, Boolean>>()
                        while (resultSet.next()) {
                            val studyData = SqlStudyData.getStudyData(resultSet)
                            val memberCount = resultSet.getInt("memberCount")
                            val isFavorite = resultSet.getBoolean("isFavorite")
                            result.add(Triple(studyData, memberCount, isFavorite))
                        }
                        result
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "스터디 및 멤버 수 데이터 조회 중 오류 발생", e)
                Result.failure<List<Triple<SqlStudyData, Int, Boolean>>>(e)
            }
        }

    /**
     * 특정 userIdx에 해당하는 스터디와 스터디 멤버 데이터 조회 (좋아요 여부 포함)
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun selectMyStudyData(userIdx: Int): Result<List<Triple<SqlStudyData, Int, Boolean>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = """
                    SELECT s.*, COUNT(sm.userIdx) as memberCount,
                           IF(f.favoriteIdx IS NOT NULL, TRUE, FALSE) as isFavorite
                    FROM tb_study s
                    LEFT JOIN tb_study_member sm ON s.studyIdx = sm.studyIdx
                    LEFT JOIN tb_favorite f ON s.studyIdx = f.studyIdx AND f.userIdx = ?
                    WHERE sm.userIdx = ?
                    GROUP BY s.studyIdx
                """
                    connection.prepareStatement(query).use { statement ->
                        statement.setInt(1, userIdx)
                        statement.setInt(2, userIdx)
                        val resultSet = statement.executeQuery()
                        val result = mutableListOf<Triple<SqlStudyData, Int, Boolean>>()
                        while (resultSet.next()) {
                            val studyData = SqlStudyData.getStudyData(resultSet)
                            val memberCount = resultSet.getInt("memberCount")
                            val isFavorite = resultSet.getBoolean("isFavorite")
                            result.add(Triple(studyData, memberCount, isFavorite))
                        }
                        result
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "내 스터디 목록 조회 중 오류 발생", e)
                Result.failure<List<Triple<SqlStudyData, Int, Boolean>>>(e)
            }
        }

    /**
     * 좋아요한 스터디 목록 조회
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun selectFavoriteStudyData(userIdx: Int): Result<List<Triple<SqlStudyData, Int, Boolean>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = """
                SELECT s.*, COUNT(sm.userIdx) as memberCount, 
                       IF(f.favoriteIdx IS NOT NULL, TRUE, FALSE) as isFavorite
                FROM tb_favorite f
                INNER JOIN tb_study s ON f.studyIdx = s.studyIdx
                LEFT JOIN tb_study_member sm ON s.studyIdx = sm.studyIdx
                WHERE f.userIdx = ?
                GROUP BY s.studyIdx
                """
                    connection.prepareStatement(query).use { statement ->
                        statement.setInt(1, userIdx)
                        val resultSet = statement.executeQuery()
                        val result = mutableListOf<Triple<SqlStudyData, Int, Boolean>>()
                        while (resultSet.next()) {
                            val studyData = SqlStudyData.getStudyData(resultSet)
                            val memberCount = resultSet.getInt("memberCount")
                            val isFavorite = resultSet.getBoolean("isFavorite")
                            result.add(Triple(studyData, memberCount, isFavorite))
                        }
                        result
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "좋아요한 스터디 목록 조회 중 오류 발생", e)
                Result.failure<List<Triple<SqlStudyData, Int, Boolean>>>(e)
            }
        }

    /**
     * 좋아요 추가 메소드
     * @param userIdx 사용자 인덱스
     * @param studyIdx 스터디 인덱스
     * @return Result<Boolean> 좋아요 추가 성공 여부를 반환
     */
    suspend fun addFavorite(userIdx: Int, studyIdx: Int): Result<Boolean> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val queryInsert = "INSERT INTO tb_favorite (userIdx, studyIdx) VALUES (?, ?)"
                    connection.prepareStatement(queryInsert).use { statementInsert ->
                        statementInsert.setInt(1, userIdx)
                        statementInsert.setInt(2, studyIdx)
                        statementInsert.executeUpdate()
                    }
                    true
                }
            }.onFailure { e ->
                Log.e("FavoriteAdd", "좋아요 추가 중 오류 발생", e)
                Result.failure<Boolean>(e)
            }
        }

    /**
     * 좋아요 삭제 메소드
     * @param userIdx 사용자 인덱스
     * @param studyIdx 스터디 인덱스
     * @return Result<Boolean> 좋아요 삭제 성공 여부를 반환
     */
    suspend fun removeFavorite(userIdx: Int, studyIdx: Int): Result<Boolean> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val queryDelete = "DELETE FROM tb_favorite WHERE userIdx = ? AND studyIdx = ?"
                    connection.prepareStatement(queryDelete).use { statementDelete ->
                        statementDelete.setInt(1, userIdx)
                        statementDelete.setInt(2, studyIdx)
                        statementDelete.executeUpdate()
                    }
                    true
                }
            }.onFailure { e ->
                Log.e("FavoriteRemove", "좋아요 삭제 중 오류 발생", e)
                Result.failure<Boolean>(e)
            }
        }
}

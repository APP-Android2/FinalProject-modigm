package kr.co.lion.modigm.db.study

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.db.HikariCPDataSource
import kr.co.lion.modigm.model.FavoriteData
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.StudyMemberData
import kr.co.lion.modigm.model.StudyTechStackData
import kr.co.lion.modigm.model.TechStackData

class RemoteStudyDao {

    private val logTag by lazy { RemoteStudyDao::class.simpleName }

    /**
     * 모든 스터디 데이터 조회
     * @return Result<List<StudyData>> 조회된 스터디 데이터를 반환
     */
    suspend fun selectTableStudy(): Result<List<StudyData>> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = "SELECT * FROM tb_study WHERE studyState = true"
                    connection.prepareStatement(query).use { statement ->
                        val resultSet = statement.executeQuery()
                        val result = mutableListOf<StudyData>()
                        while (resultSet.next()) {
                            val studyData = StudyData.getStudyData(resultSet)
                            result.add(studyData)
                        }
                        result
                    }
                }
            }.onFailure { e ->
                Log.e(logTag, "스터디 데이터 조회 중 오류 발생", e)
                Result.failure<List<StudyData>>(e)
            }
        }

    /**
     * 모든 스터디 멤버 데이터 조회
     * @return Result<List<StudyMemberData>> 조회된 스터디 멤버 데이터를 반환
     */
    suspend fun selectTableStudyMembers(): Result<List<StudyMemberData>> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = "SELECT * FROM tb_study_member"
                    connection.prepareStatement(query).use { statement ->
                        val resultSet = statement.executeQuery()
                        val result = mutableListOf<StudyMemberData>()
                        while (resultSet.next()) {
                            val studyMemberData = StudyMemberData.getStudyMemberData(resultSet)
                            result.add(studyMemberData)
                        }
                        result
                    }
                }
            }.onFailure { e ->
                Log.e(logTag, "스터디 멤버 데이터 조회 중 오류 발생", e)
                Result.failure<List<StudyMemberData>>(e)
            }
        }

    /**
     * 좋아요 데이터 조회
     * @return Result<List<FavoriteData>> 조회된 좋아요 데이터를 반환
     */
    suspend fun selectTableFavorites(): Result<List<FavoriteData>> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = "SELECT * FROM tb_favorite"
                    connection.prepareStatement(query).use { statement ->
                        val resultSet = statement.executeQuery()
                        val result = mutableListOf<FavoriteData>()
                        while (resultSet.next()) {
                            val favoriteData = FavoriteData.getFavoriteData(resultSet)
                            result.add(favoriteData)
                        }
                        result
                    }
                }
            }.onFailure { e ->
                Log.e(logTag, "좋아요 데이터 조회 중 오류 발생", e)
                Result.failure<List<FavoriteData>>(e)
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

    /**
     * 기술 스택 데이터를 조회하는 메소드
     * @return Result<List<TechStackData>> 조회된 기술 스택 데이터를 반환
     */
    suspend fun selectTableTechStack(): Result<List<TechStackData>> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = "SELECT * FROM tb_tech_stack"
                    connection.prepareStatement(query).use { statement ->
                        val resultSet = statement.executeQuery()
                        val techStacks = mutableListOf<TechStackData>()
                        while (resultSet.next()) {
                            val techStack = TechStackData(
                                resultSet.getInt("techIdx"),
                                resultSet.getString("techName"),
                                resultSet.getString("techCategory")
                            )
                            techStacks.add(techStack)
                        }
                        techStacks
                    }
                }
            }.onFailure { e ->
                Log.e(logTag, "기술 스택 데이터 조회 중 오류 발생", e)
                Result.failure<List<Triple<Int, String, String>>>(e)
            }
        }

    /**
     * StudyTechStack 테이블 조회
     * @return List<StudyTechStackData> 조회된 StudyTechStackData 목록을 반환
     */
    suspend fun selectTableStudyTechStack(): Result<List<StudyTechStackData>> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = "SELECT * FROM tb_study_tech_stack"
                    connection.prepareStatement(query).use { statement ->
                        val resultSet = statement.executeQuery()
                        val studyTechStacks = mutableListOf<StudyTechStackData>()
                        while (resultSet.next()) {
                            val studyTechStack = StudyTechStackData(
                                resultSet.getInt("studyIdx"),
                                resultSet.getInt("techIdx")
                            )
                            studyTechStacks.add(studyTechStack)
                        }
                        studyTechStacks
                    }
                }
            }.onFailure { e ->
                Log.e(logTag, "스터디-기술 스택 데이터 조회 중 오류 발생", e)
                Result.failure<List<StudyTechStackData>>(e)
            }
        }
}

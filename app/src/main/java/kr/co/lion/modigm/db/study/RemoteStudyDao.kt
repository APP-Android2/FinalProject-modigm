package kr.co.lion.modigm.db.study

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.db.HikariCPDataSource
import kr.co.lion.modigm.model.FilterStudyData
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.util.FragmentName

class RemoteStudyDao {

    private val logTag by lazy { RemoteStudyDao::class.simpleName }

    /**
     * 모든 스터디와 스터디 멤버 데이터 조회 (좋아요 여부 포함)
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun selectAllStudyData(userIdx: Int): Result<List<Triple<StudyData, Int, Boolean>>> =
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
                        val result = mutableListOf<Triple<StudyData, Int, Boolean>>()
                        while (resultSet.next()) {
                            val studyData = StudyData.getStudyData(resultSet)
                            val memberCount = resultSet.getInt("memberCount")
                            val isFavorite = resultSet.getBoolean("isFavorite")
                            result.add(Triple(studyData, memberCount, isFavorite))
                        }
                        result
                    }
                }
            }.onFailure { e ->
                Log.e(logTag, "스터디 및 멤버 수 데이터 조회 중 오류 발생", e)
                Result.failure<List<Triple<StudyData, Int, Boolean>>>(e)
            }
        }

    /**
     * 특정 userIdx에 해당하는 스터디와 스터디 멤버 데이터 조회 (좋아요 여부 포함)
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun selectMyStudyData(userIdx: Int): Result<List<Triple<StudyData, Int, Boolean>>> =
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
                    AND s.studyState = true
                    GROUP BY s.studyIdx
                """
                    connection.prepareStatement(query).use { statement ->
                        statement.setInt(1, userIdx)
                        statement.setInt(2, userIdx)
                        val resultSet = statement.executeQuery()
                        val result = mutableListOf<Triple<StudyData, Int, Boolean>>()
                        while (resultSet.next()) {
                            val studyData = StudyData.getStudyData(resultSet)
                            val memberCount = resultSet.getInt("memberCount")
                            val isFavorite = resultSet.getBoolean("isFavorite")
                            result.add(Triple(studyData, memberCount, isFavorite))
                        }
                        result
                    }
                }
            }.onFailure { e ->
                Log.e(logTag, "내 스터디 목록 조회 중 오류 발생", e)
                Result.failure<List<Triple<StudyData, Int, Boolean>>>(e)
            }
        }

    /**
     * 좋아요한 스터디 목록 조회
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun selectFavoriteStudyData(userIdx: Int): Result<List<Triple<StudyData, Int, Boolean>>> =
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
                AND s.studyState = true
                GROUP BY s.studyIdx
                """
                    connection.prepareStatement(query).use { statement ->
                        statement.setInt(1, userIdx)
                        val resultSet = statement.executeQuery()
                        val result = mutableListOf<Triple<StudyData, Int, Boolean>>()
                        while (resultSet.next()) {
                            val studyData = StudyData.getStudyData(resultSet)
                            val memberCount = resultSet.getInt("memberCount")
                            val isFavorite = resultSet.getBoolean("isFavorite")
                            result.add(Triple(studyData, memberCount, isFavorite))
                        }
                        result
                    }
                }
            }.onFailure { e ->
                Log.e(logTag, "좋아요한 스터디 목록 조회 중 오류 발생", e)
                Result.failure<List<Triple<StudyData, Int, Boolean>>>(e)
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
     * 필터링된 전체 스터디 목록 조회
     */
    suspend fun selectFilteredStudyData(filter: FilterStudyData): Result<List<Triple<StudyData, Int, Boolean>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = """
                        SELECT s.*, COUNT(sm.userIdx) as memberCount,
                               IF(f.favoriteIdx IS NOT NULL, TRUE, FALSE) as isFavorite
                        FROM tb_study s
                        LEFT JOIN tb_study_member sm ON s.studyIdx = sm.studyIdx
                        LEFT JOIN tb_favorite f ON s.studyIdx = f.studyIdx
                        LEFT JOIN tb_study_tech_stack sts ON s.studyIdx = sts.studyIdx
                        LEFT JOIN tb_tech_stack ts ON sts.techIdx = ts.techIdx
                        WHERE s.studyState = true
                        ${buildFilterQuery(filter)}
                        GROUP BY s.studyIdx
                    """

                    connection.prepareStatement(query).use { statement ->
                        val resultSet = statement.executeQuery()
                        val result = mutableListOf<Triple<StudyData, Int, Boolean>>()
                        while (resultSet.next()) {
                            val studyData = StudyData.getStudyData(resultSet)
                            val memberCount = resultSet.getInt("memberCount")
                            val isFavorite = resultSet.getBoolean("isFavorite")
                            result.add(Triple(studyData, memberCount, isFavorite))
                        }
                        result
                    }
                }
            }
        }

    /**
     * 필터링된 내 스터디 목록 조회
     */
    suspend fun selectFilteredMyStudyData(userIdx: Int, filter: FilterStudyData): Result<List<Triple<StudyData, Int, Boolean>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = """
                        SELECT s.*, COUNT(sm.userIdx) as memberCount,
                               IF(f.favoriteIdx IS NOT NULL, TRUE, FALSE) as isFavorite
                        FROM tb_study s
                        LEFT JOIN tb_study_member sm ON s.studyIdx = sm.studyIdx
                        LEFT JOIN tb_favorite f ON s.studyIdx = f.studyIdx AND f.userIdx = ?
                        LEFT JOIN tb_study_tech_stack sts ON s.studyIdx = sts.studyIdx
                        LEFT JOIN tb_tech_stack ts ON sts.techIdx = ts.techIdx
                        WHERE s.studyState = true
                        AND sm.userIdx = ?
                        ${buildFilterQuery(filter)}
                        GROUP BY s.studyIdx
                    """

                    connection.prepareStatement(query).use { statement ->
                        // 사용자 인덱스 설정
                        statement.setInt(1, userIdx)
                        statement.setInt(2, userIdx) // 내 스터디 필터링에 필요
                        val resultSet = statement.executeQuery()
                        val result = mutableListOf<Triple<StudyData, Int, Boolean>>()
                        while (resultSet.next()) {
                            val studyData = StudyData.getStudyData(resultSet)
                            val memberCount = resultSet.getInt("memberCount")
                            val isFavorite = resultSet.getBoolean("isFavorite")
                            result.add(Triple(studyData, memberCount, isFavorite))
                        }
                        result
                    }
                }
            }.onFailure { e ->
                Log.e(logTag, "내 스터디 목록 필터링 조회 중 오류 발생", e)
                Result.failure<List<Triple<StudyData, Int, Boolean>>>(e)
            }
        }

    private fun buildFilterQuery(filter: FilterStudyData): String {
        val conditions = mutableListOf<String>()

        filter.studyType.takeIf { it.isNotEmpty() }?.let {
            conditions.add("s.studyType = '$it'")
        }

        // 기간 필터 문자열 비교
        filter.studyPeriod.takeIf { it.isNotEmpty() }?.let { periodFilter ->
            when (periodFilter) {
                "1개월이하" -> conditions.add("s.studyPeriod IN ('1개월이하')")
                "2개월이하" -> conditions.add("s.studyPeriod IN ('1개월이하', '2개월이하')")
                "3개월이하" -> conditions.add("s.studyPeriod IN ('1개월이하', '2개월이하', '3개월이하')")
                "4개월이하" -> conditions.add("s.studyPeriod IN ('1개월이하', '2개월이하', '3개월이하', '4개월이하')")
                "5개월이하" -> conditions.add("s.studyPeriod IN ('1개월이하', '2개월이하', '3개월이하', '4개월이하', '5개월이하')")
                "6개월미만" -> conditions.add("s.studyPeriod IN ('1개월이하', '2개월이하', '3개월이하', '4개월이하', '5개월이하', '6개월미만')")
                "6개월이상" -> conditions.add("s.studyPeriod IN ('6개월이상')")
                else -> {}
            }
        }

        filter.studyOnOffline.takeIf { it.isNotEmpty() }?.let {
            conditions.add("s.studyOnOffline = '$it'")
        }

        // 인원수 필터 문자열 비교
        filter.studyMaxMember.takeIf { it.isNotEmpty() }?.let { maxMemberFilter ->
            when (maxMemberFilter) {
                "2~5명" -> conditions.add("s.studyMaxMember BETWEEN 2 AND 5")
                "6~10명" -> conditions.add("s.studyMaxMember BETWEEN 6 AND 10")
                "11명이상" -> conditions.add("s.studyMaxMember >= 11")
                else -> {} // 전체 선택 시 필터링하지 않음
            }
        }

        // 기술 스택 필터링
        filter.studyTechStack.takeIf { it.isNotEmpty() }?.let { techStack ->
            val techStackCondition = techStack.joinToString(",") { "'$it'" }
            Log.d(FragmentName.FILTER_SORT.str, "techStackCondition: $techStackCondition")
            conditions.add("sts.techIdx IN ($techStackCondition)")
        }

        return if (conditions.isNotEmpty()) {
            "AND " + conditions.joinToString(" AND ")
        } else {
            ""
        }
    }

    /**
     * 기술 스택 데이터를 조회하는 메소드
     */
    suspend fun selectAllTechStack(): Result<List<Triple<Int, String, String>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                HikariCPDataSource.getConnection().use { connection ->
                    val query = """
                        SELECT *
                        FROM tb_tech_stack
                    """
                    connection.prepareStatement(query).use { statement ->
                        val resultSet = statement.executeQuery()
                        val result = mutableListOf<Triple<Int, String, String>>()
                        while (resultSet.next()) {
                            val techIdx = resultSet.getInt("techIdx")
                            val techName = resultSet.getString("techName")
                            val techCategory = resultSet.getString("techCategory")
                            result.add(Triple(techIdx, techName, techCategory))
                        }
                        result
                    }
                }
            }.onFailure { e ->
                Log.e(logTag, "기술 스택 데이터 조회 중 오류 발생", e)
                Result.failure<List<Triple<Int, String, String>>>(e)
            }
        }

}

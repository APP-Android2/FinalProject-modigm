package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.study.StudyDataSource
import kr.co.lion.modigm.model.SqlStudyData

class StudyListRepository() {

    private val studyDataSource by lazy { StudyDataSource() }

    /**
     * 전체 스터디 목록을 가져오는 메소드 (좋아요 여부 포함)
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun getAllStudyData(userIdx: Int): Result<List<Triple<SqlStudyData, Int, Boolean>>> {
        return studyDataSource.getAllStudyData(userIdx)
    }

    /**
     * 내 스터디 목록을 가져오는 메소드 (좋아요 여부 포함)
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun getMyStudyData(userIdx: Int): Result<List<Triple<SqlStudyData, Int, Boolean>>> {
        return studyDataSource.getMyStudyData(userIdx)
    }

    /**
     * 좋아요한 스터디 목록을 가져오는 메소드
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun getFavoriteStudyData(userIdx: Int): Result<List<Triple<SqlStudyData, Int, Boolean>>> {
        return studyDataSource.getFavoriteStudyData(userIdx)
    }

    /**
     * 좋아요 추가 메소드
     * @param userIdx 사용자 인덱스
     * @param studyIdx 스터디 인덱스
     * @return Result<Boolean> 좋아요 추가 성공 여부를 반환
     */
    suspend fun addFavorite(userIdx: Int, studyIdx: Int): Result<Boolean>{
        return studyDataSource.addFavorite(userIdx, studyIdx)
    }

    /**
     * 좋아요 삭제 메소드
     * @param userIdx 사용자 인덱스
     * @param studyIdx 스터디 인덱스
     * @return Result<Boolean> 좋아요 삭제 성공 여부를 반환
     */
    suspend fun removeFavorite(userIdx: Int, studyIdx: Int): Result<Boolean>{
        return studyDataSource.removeFavorite(userIdx, studyIdx)
    }
}

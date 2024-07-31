package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.study.StudyDataSource
import kr.co.lion.modigm.model.SqlStudyData

class StudyListRepository() {

    private val studyDataSource by lazy { StudyDataSource() }

    // 전체 스터디 목록을 가져오는 메소드 (좋아요 여부 포함)
    suspend fun getAllStudyData(userIdx: Int): Result<List<Triple<SqlStudyData, Int, Boolean>>> {
        return studyDataSource.getAllStudyData(userIdx)
    }

    // 내 스터디 목록을 가져오는 메소드 (좋아요 여부 포함)
    suspend fun getMyStudyData(userIdx: Int): Result<List<Triple<SqlStudyData, Int, Boolean>>> {
        return studyDataSource.getMyStudyData(userIdx)
    }

    suspend fun getFavoriteStudyData(userIdx: Int): Result<List<Triple<SqlStudyData, Int, Boolean>>> {
        return studyDataSource.getFavoriteStudyData(userIdx)
    }

    suspend fun addFavorite(userIdx: Int, studyIdx: Int): Result<Boolean>{
        return studyDataSource.addFavorite(userIdx, studyIdx)
    }

    suspend fun removeFavorite(userIdx: Int, studyIdx: Int): Result<Boolean>{
        return studyDataSource.removeFavorite(userIdx, studyIdx)
    }

    // 리소스를 해제하는 메서드 추가
    suspend fun closeDataSource() {
        studyDataSource.closeDataSource()
    }
}
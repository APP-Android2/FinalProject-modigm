package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.study.StudyDataSource
import kr.co.lion.modigm.model.SqlStudyData

class StudyListRepository() {

    private val studyDataSource = StudyDataSource()

    // 전체 스터디 목록을 가져오는 메소드 (좋아요 여부 포함)
    suspend fun getAllStudyAndMemberCount(userIdx: Int): List<Triple<SqlStudyData, Int, Boolean>> {
        return studyDataSource.getAllStudyAndMemberCount(userIdx)
    }

    // 내 스터디 목록을 가져오는 메소드 (좋아요 여부 포함)
    suspend fun getMyStudyList(userIdx: Int): List<Triple<SqlStudyData, Int, Boolean>> {
        return studyDataSource.getMyStudyAndMemberCount(userIdx)
    }

    // 좋아요 토글 메소드
    suspend fun toggleFavorite(userIdx: Int, studyIdx: Int): Boolean {
        return studyDataSource.toggleFavorite(userIdx, studyIdx)
    }

    // 리소스를 해제하는 메서드 추가
    suspend fun close() {
        studyDataSource.close()
    }
}
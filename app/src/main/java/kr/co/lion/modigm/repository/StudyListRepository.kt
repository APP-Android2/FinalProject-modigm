package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.study.StudyListDataSource
import kr.co.lion.modigm.model.SqlStudyData

class StudyListRepository() {

    private val studyListDataSource = StudyListDataSource()

    // 전체 스터디 목록을 가져오는 메소드 (좋아요 여부 포함)
    suspend fun getAllStudyAndMemberCount(userIdx: Int): List<Triple<SqlStudyData, Int, Boolean>> {
        return studyListDataSource.getAllStudyAndMemberCount(userIdx)
    }

    // 내 스터디 목록을 가져오는 메소드 (좋아요 여부 포함)
    suspend fun getMyStudyList(userIdx: Int): List<Triple<SqlStudyData, Int, Boolean>> {
        return studyListDataSource.getMyStudyAndMemberCount(userIdx)
    }

    // 좋아요 토글 메소드
    suspend fun toggleFavorite(userIdx: Int, studyIdx: Int): Boolean {
        return studyListDataSource.toggleFavorite(userIdx, studyIdx)
    }
}
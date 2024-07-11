package kr.co.lion.modigm.db.study

import android.util.Log
import kr.co.lion.modigm.model.SqlStudyData

class StudyListDataSource {

    private val tag = "StudyListDataSource"
    private val dao = StudyListDao()

    // 모집 중인 전체 스터디 목록 조회 (좋아요 여부 포함)
    suspend fun getAllStudyAndMemberCount(userIdx: Int): List<Triple<SqlStudyData, Int, Boolean>> {
        return try {
            val studyMemberCountMap = dao.selectAllStudyAndMemberCount(userIdx)
            studyMemberCountMap.values.toList() // 해시맵 -> 리스트
        } catch (e: Exception) {
            Log.e(tag, "전체 스터디 목록 조회 중 오류 발생", e)
            emptyList() // 에러 발생 시 빈 리스트 반환
        }
    }

    // 내가 속한 스터디 목록 조회 (좋아요 여부 포함)
    suspend fun getMyStudyAndMemberCount(userIdx: Int): List<Triple<SqlStudyData, Int, Boolean>> {
        return try {
            val studyMemberCountMap = dao.selectMyStudyAndMemberCount(userIdx)
            studyMemberCountMap.values.toList() // 해시맵 -> 리스트
        } catch (e: Exception) {
            Log.e(tag, "내 스터디 목록 조회 중 오류 발생", e)
            emptyList() // 에러 발생 시 빈 리스트 반환
        }
    }

    // 좋아요 토글 메소드
    suspend fun toggleFavorite(userIdx: Int, studyIdx: Int): Boolean {
        return dao.toggleFavorite(userIdx, studyIdx)
    }
}

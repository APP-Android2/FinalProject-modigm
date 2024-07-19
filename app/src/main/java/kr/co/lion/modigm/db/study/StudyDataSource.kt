package kr.co.lion.modigm.db.study

import android.util.Log
import kr.co.lion.modigm.model.SqlStudyData

class StudyDataSource {

    private val tag = "StudyDataSource"
    private val dao = StudyDao()

    // 모집 중인 전체 스터디 목록 조회 (좋아요 여부 포함)
    suspend fun getAllStudyAndMemberCount(userIdx: Int): List<Triple<SqlStudyData, Int, Boolean>> {
        return dao.selectAllStudyAndMemberCount(userIdx).fold(
            onSuccess = { result ->
                result
            },
            onFailure = { e ->
                Log.e(tag, "전체 스터디 목록 조회 중 오류 발생", e)
                emptyList()
            }
        )
    }

    // 내가 속한 스터디 목록 조회 (좋아요 여부 포함)
    suspend fun getMyStudyAndMemberCount(userIdx: Int): List<Triple<SqlStudyData, Int, Boolean>> {
        return dao.selectMyStudyAndMemberCount(userIdx).fold(
            onSuccess = { result ->
                result
            },
            onFailure = { e ->
                Log.e(tag, "내 스터디 목록 조회 중 오류 발생", e)
                emptyList()
            }
        )
    }

    // 좋아요 토글 메소드
    suspend fun toggleFavorite(userIdx: Int, studyIdx: Int): Boolean {
        return dao.toggleFavorite(userIdx, studyIdx).fold(
            onSuccess = { result ->
                result
            },
            onFailure = { e ->
                Log.e(tag, "좋아요 토글 중 오류 발생", e)
                false
            }
        )
    }
    // 리소스를 해제하는 메서드 추가
    suspend fun close() {
        dao.close()
    }

}

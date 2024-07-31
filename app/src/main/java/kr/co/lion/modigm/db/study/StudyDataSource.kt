package kr.co.lion.modigm.db.study

import android.util.Log
import kr.co.lion.modigm.model.SqlStudyData

class StudyDataSource {

    private val tag = "StudyDataSource"
    private val dao = StudyDao()

    // 모집 중인 전체 스터디 목록 조회 (좋아요 여부 포함)
    suspend fun getAllStudyData(userIdx: Int): Result<List<Triple<SqlStudyData, Int, Boolean>>> {
        return runCatching {
            dao.selectAllStudyData(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "전체 스터디 목록 조회 중 오류 발생", e)
            Result.failure<List<Triple<SqlStudyData, Int, Boolean>>>(e)
        }
    }

    // 내가 속한 스터디 목록 조회 (좋아요 여부 포함)
    suspend fun getMyStudyData(userIdx: Int): Result<List<Triple<SqlStudyData, Int, Boolean>>> {
        return runCatching {
            dao.selectMyStudyData(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "전체 스터디 목록 조회 중 오류 발생", e)
            Result.failure<List<Triple<SqlStudyData, Int, Boolean>>>(e)
        }
    }

    suspend fun getFavoriteStudyData(userIdx: Int): Result<List<Triple<SqlStudyData, Int, Boolean>>> {
        return runCatching {
            dao.selectFavoriteStudyData(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "좋아요한 스터디 목록 조회 중 오류 발생", e)
            Result.failure<List<Triple<SqlStudyData, Int, Boolean>>>(e)
        }
    }

    // 좋아요 추가 메소드
    suspend fun addFavorite(userIdx: Int, studyIdx: Int): Result<Boolean> {
        return runCatching {
            dao.addFavorite(userIdx, studyIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "좋아요 추가 중 오류 발생", e)
            Result.failure<Boolean>(e)
        }
    }

    // 좋아요 삭제 메소드
    suspend fun removeFavorite(userIdx: Int, studyIdx: Int): Result<Boolean> {
        return runCatching {
            dao.removeFavorite(userIdx, studyIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "좋아요 삭제 중 오류 발생", e)
            Result.failure<Boolean>(e)
        }
    }

    // 리소스를 해제하는 메서드 추가
    suspend fun close() {
        dao.close()
    }

}

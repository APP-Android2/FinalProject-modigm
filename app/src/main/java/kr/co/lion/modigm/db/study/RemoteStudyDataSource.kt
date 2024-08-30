package kr.co.lion.modigm.db.study

import android.util.Log
import kr.co.lion.modigm.model.FilterStudyData
import kr.co.lion.modigm.model.StudyData

class RemoteStudyDataSource {

    private val tag by lazy { RemoteStudyDataSource::class.simpleName }
    private val dao by lazy { RemoteStudyDao() }

    /**
     * 모집 중인 전체 스터디 목록 조회 (좋아요 여부 포함)
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun getAllStudyData(userIdx: Int): Result<List<Triple<StudyData, Int, Boolean>>> {
        return runCatching {
            dao.selectAllStudyData(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "전체 스터디 목록 조회 중 오류 발생", e)
            Result.failure<List<Triple<StudyData, Int, Boolean>>>(e)
        }
    }

    /**
     * 내가 속한 스터디 목록 조회 (좋아요 여부 포함)
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun getMyStudyData(userIdx: Int): Result<List<Triple<StudyData, Int, Boolean>>> {
        return runCatching {
            dao.selectMyStudyData(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "내 스터디 목록 조회 중 오류 발생", e)
            Result.failure<List<Triple<StudyData, Int, Boolean>>>(e)
        }
    }

    /**
     * 좋아요한 스터디 목록 조회
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun getFavoriteStudyData(userIdx: Int): Result<List<Triple<StudyData, Int, Boolean>>> {
        return runCatching {
            dao.selectFavoriteStudyData(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "좋아요한 스터디 목록 조회 중 오류 발생", e)
            Result.failure<List<Triple<StudyData, Int, Boolean>>>(e)
        }
    }

    /**
     * 좋아요 추가 메소드
     * @param userIdx 사용자 인덱스
     * @param studyIdx 스터디 인덱스
     * @return Result<Boolean> 좋아요 추가 성공 여부를 반환
     */
    suspend fun addFavorite(userIdx: Int, studyIdx: Int): Result<Boolean> {
        return runCatching {
            dao.addFavorite(userIdx, studyIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "좋아요 추가 중 오류 발생", e)
            Result.failure<Boolean>(e)
        }
    }

    /**
     * 좋아요 삭제 메소드
     * @param userIdx 사용자 인덱스
     * @param studyIdx 스터디 인덱스
     * @return Result<Boolean> 좋아요 삭제 성공 여부를 반환
     */
    suspend fun removeFavorite(userIdx: Int, studyIdx: Int): Result<Boolean> {
        return runCatching {
            dao.removeFavorite(userIdx, studyIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "좋아요 삭제 중 오류 발생", e)
            Result.failure<Boolean>(e)
        }
    }

    /**
     * 필터링된 전체 스터디 목록 가져오기
     */
    suspend fun getFilteredStudyList(filter: FilterStudyData): Result<List<Triple<StudyData, Int, Boolean>>> {
        return dao.selectFilteredStudyData(filter)
    }

    /**
     * 필터링된 내 스터디 목록 가져오기
     */
    suspend fun getFilteredMyStudyList(userIdx: Int, filter: FilterStudyData): Result<List<Triple<StudyData, Int, Boolean>>> {
        return dao.selectFilteredMyStudyData(userIdx, filter)
    }
}

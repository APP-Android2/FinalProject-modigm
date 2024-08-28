package kr.co.lion.modigm.repository

import android.util.Log
import kr.co.lion.modigm.db.study.RemoteStudyDataSource
import kr.co.lion.modigm.model.StudyData

class StudyRepository() {

    private val tag by lazy { StudyRepository::class.simpleName }

    private val remoteStudyDataSource by lazy { RemoteStudyDataSource() }

    /**
     * 전체 스터디 목록을 가져오는 메소드 (좋아요 여부 포함)
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun getAllStudyData(userIdx: Int): Result<List<Triple<StudyData, Int, Boolean>>> {
        return runCatching {
            remoteStudyDataSource.getAllStudyData(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "전체 스터디 목록 조회 중 오류 발생: ${e.message}", e)
            Result.failure<List<Triple<StudyData, Int, Boolean>>>(e)
        }
    }

    /**
     * 내 스터디 목록을 가져오는 메소드 (좋아요 여부 포함)
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun getMyStudyData(userIdx: Int): Result<List<Triple<StudyData, Int, Boolean>>> {
        return runCatching {
            remoteStudyDataSource.getMyStudyData(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "내 스터디 목록 조회 중 오류 발생: ${e.message}", e)
        }
    }

    /**
     * 좋아요한 스터디 목록을 가져오는 메소드
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<SqlStudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun getFavoriteStudyData(userIdx: Int): Result<List<Triple<StudyData, Int, Boolean>>> {
        return runCatching {
            remoteStudyDataSource.getFavoriteStudyData(userIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "좋아요한 스터디 목록 조회 중 오류 발생: ${e.message}", e)
        }
    }

    /**
     * 좋아요 추가 메소드
     * @param userIdx 사용자 인덱스
     * @param studyIdx 스터디 인덱스
     * @return Result<Boolean> 좋아요 추가 성공 여부를 반환
     */
    suspend fun addFavorite(userIdx: Int, studyIdx: Int): Result<Boolean>{
        return runCatching {
            remoteStudyDataSource.addFavorite(userIdx, studyIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "좋아요 추가 중 오류 발생: ${e.message}", e)
            Result.failure<Boolean>(e)
        }
    }

    /**
     * 좋아요 삭제 메소드
     * @param userIdx 사용자 인덱스
     * @param studyIdx 스터디 인덱스
     * @return Result<Boolean> 좋아요 삭제 성공 여부를 반환
     */
    suspend fun removeFavorite(userIdx: Int, studyIdx: Int): Result<Boolean>{
        return runCatching {
            remoteStudyDataSource.removeFavorite(userIdx, studyIdx).getOrThrow()
        }.onFailure { e ->
            Log.e(tag, "좋아요 삭제 중 오류 발생: ${e.message}", e)
            Result.failure<Boolean>(e)
        }
    }
}

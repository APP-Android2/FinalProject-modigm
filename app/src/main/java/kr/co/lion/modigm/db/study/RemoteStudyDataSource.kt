package kr.co.lion.modigm.db.study

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.model.FilterStudyData
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.TechStackData

class RemoteStudyDataSource {

    private val tag by lazy { RemoteStudyDataSource::class.simpleName }
    private val dao by lazy { RemoteStudyDao() }

    /**
     * 모집 중인 전체 스터디 목록 조회 (좋아요 여부 포함)
     * @return Result<List<Triple<StudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun getAllStudyData(): Result<List<Triple<StudyData, Int, Boolean>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val studies = dao.selectTableStudy().getOrThrow()
                val members = dao.selectTableStudyMembers().getOrThrow()
                val favorites = dao.selectTableFavorites().getOrThrow()

                studies.map { study ->
                    val memberCount = members.count { it.studyIdx == study.studyIdx }
                    val isFavorite = favorites.any { it.studyIdx == study.studyIdx }
                    Triple(study, memberCount, isFavorite)
                }
            }.onFailure { e ->
                Log.e(tag, "전체 스터디 목록 조회 중 오류 발생", e)
                Result.failure<List<Triple<StudyData, Int, Boolean>>>(e)
            }
        }

    /**
     * 내가 속한 스터디 목록 조회 (좋아요 여부 포함)
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<StudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun getMyStudyData(userIdx: Int): Result<List<Triple<StudyData, Int, Boolean>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val studies = dao.selectTableStudy().getOrThrow()
                val members = dao.selectTableStudyMembers().getOrThrow()
                val favorites = dao.selectTableFavorites().getOrThrow()

                studies.filter { study ->
                    members.any { it.studyIdx == study.studyIdx && it.userIdx == userIdx }
                }.map { study ->
                    val memberCount = members.count { it.studyIdx == study.studyIdx }
                    val isFavorite = favorites.any { it.studyIdx == study.studyIdx && it.userIdx == userIdx }
                    Triple(study, memberCount, isFavorite)
                }
            }.onFailure { e ->
                Log.e(tag, "내 스터디 목록 조회 중 오류 발생", e)
                Result.failure<List<Triple<StudyData, Int, Boolean>>>(e)
            }
        }

    /**
     * 좋아요한 스터디 목록 조회
     * @param userIdx 사용자 인덱스
     * @return Result<List<Triple<StudyData, Int, Boolean>>> 조회된 스터디 데이터를 반환
     */
    suspend fun getFavoriteStudyData(userIdx: Int): Result<List<Triple<StudyData, Int, Boolean>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val studies = dao.selectTableStudy().getOrThrow()
                val members = dao.selectTableStudyMembers().getOrThrow()
                val favorites = dao.selectTableFavorites().getOrThrow()

                studies.filter { study ->
                    favorites.any { it.studyIdx == study.studyIdx && it.userIdx == userIdx }
                }.map { study ->
                    val memberCount = members.count { it.studyIdx == study.studyIdx }
                    val isFavorite = favorites.any { it.studyIdx == study.studyIdx && it.userIdx == userIdx }
                    Triple(study, memberCount, isFavorite)
                }
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
    suspend fun getFilteredStudyList(filter: FilterStudyData): Result<List<Triple<StudyData, Int, Boolean>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val studies = getAllStudyData().getOrThrow()
                val allStudyTechStackData = dao.selectTableStudyTechStack().getOrThrow()
                FilterStudyData.applyFilter(studies, filter, allStudyTechStackData)  // 필터링 로직 호출
            }.onFailure { e ->
                Log.e(tag, "필터링된 스터디 목록 조회 중 오류 발생", e)
                Result.failure<List<Triple<StudyData, Int, Boolean>>>(e)
            }
        }

    /**
     * 필터링된 내 스터디 목록 가져오기
     */
    suspend fun getFilteredMyStudyList(userIdx: Int, filter: FilterStudyData): Result<List<Triple<StudyData, Int, Boolean>>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val myStudies = getMyStudyData(userIdx).getOrThrow()
                val allStudyTechStackData = dao.selectTableStudyTechStack().getOrThrow()
                FilterStudyData.applyFilter(myStudies, filter, allStudyTechStackData)  // 필터링 로직 호출
            }.onFailure { e ->
                Log.e(tag, "필터링된 내 스터디 목록 조회 중 오류 발생", e)
                Result.failure<List<Triple<StudyData, Int, Boolean>>>(e)
            }
        }

    /**
     * 기술 스택 데이터를 조회하는 메소드
     */
    suspend fun getTechStackData(): Result<List<TechStackData>> {
        return dao.selectTableTechStack()
    }
}

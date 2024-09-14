package kr.co.lion.modigm.db.write

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kr.co.lion.modigm.model.StudyData

class RemoteWriteStudyDataSource {

    private val dao by lazy { RemoteWriteStudyDao() }

    suspend fun uploadStudyData(studyData: StudyData, studyTechStack: List<Int>): Result<Int?> {
        return runCatching {
            // 스터디 데이터를 먼저 삽입하고, 그 ID를 받아 처리
            val resultStudyIdx = dao.insertStudyData(studyData)

            // studyIdx가 성공적으로 반환되었을 때만 추가 작업 진행
            resultStudyIdx.getOrThrow().let { studyIdx ->
                coroutineScope {
                    // 스터디 기술 스택 삽입 작업과 멤버 삽입 작업을 비동기로 처리
                    val insertTechStackJob = async { dao.insertStudyTechStack(studyIdx, studyTechStack) }
                    val insertMemberJob = async { dao.insertStudyMember(studyIdx, listOf(studyData.userIdx)) }

                    // 모든 작업이 완료될 때까지 대기
                    insertTechStackJob.await()
                    insertMemberJob.await()
                }
                studyIdx  // 최종적으로 삽입된 스터디 ID 반환
            }
        }.onFailure { e ->
            // 실패 시 로그 출력 및 실패 결과 반환
            Log.e("RemoteWriteStudyDataSource Error", "Error uploadStudyData: ${e.message}")
            Result.failure<Int?>(e)
        }
    }

    suspend fun uploadImageToS3(context: Context, uri: Uri): Result<String> {
        return runCatching {
            dao.uploadImageToS3(context, uri)
        }.onFailure { e ->
            Log.e("RemoteWriteStudyDataSource Error", "Error uploadImageToS3: ${e.message}")
            Result.failure<Uri>(e)
        }

    }

}
package kr.co.lion.modigm.db.write

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.model.StudyData

class RemoteWriteStudyDataSource {

    private val dao by lazy { RemoteWriteStudyDao() }

    // 사용자 정보를 저장한다.
    suspend fun uploadStudyData(userIdx: Int, study: StudyData, studyTechStack: List<Int>, studyPicUrl: String?): Result<Int?> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val studyIdx = dao.insertStudyData(study.toMap(), studyPicUrl)
                studyIdx?.let {
                    coroutineScope {
                        val insertTechStackJob = async { dao.insertStudyTechStack(it, studyTechStack) }
                        val insertMemberJob = async { dao.insertStudyMember(it, listOf(userIdx)) }
                        insertTechStackJob.await()
                        insertMemberJob.await()
                    }
                }
                studyIdx
            }
        }.onFailure { e ->
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
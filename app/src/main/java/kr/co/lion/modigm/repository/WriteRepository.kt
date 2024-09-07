package kr.co.lion.modigm.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import kr.co.lion.modigm.db.write.RemoteWriteStudyDataSource
import kr.co.lion.modigm.model.StudyData

class WriteRepository {
    private val writeStudyDataSource = RemoteWriteStudyDataSource()

    suspend fun uploadStudyData(userIdx: Int, study: StudyData, studyTechStack: List<Int>, studyPicUrl: String?): Result<Int?> {
        return runCatching {
            writeStudyDataSource.uploadStudyData(userIdx, study, studyTechStack, studyPicUrl).getOrThrow()
        }.onFailure { e ->
            Log.e("WriteStudyRepository Error", "Error uploadStudyData: ${e.message}")
            Result.failure<Int?>(e)
        }
    }

    suspend fun uploadImageToS3(context: Context, uri: Uri): Result<String> {
        return runCatching {
            writeStudyDataSource.uploadImageToS3(context, uri).getOrThrow()
        }.onFailure { e ->
            Log.e("WriteStudyRepository Error", "Error uploadImageToS3: ${e.message}")
            Result.failure<Uri>(e)
        }
    }
}
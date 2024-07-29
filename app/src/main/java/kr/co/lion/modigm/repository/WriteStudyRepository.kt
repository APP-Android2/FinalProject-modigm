package kr.co.lion.modigm.repository

import android.util.Log
import kr.co.lion.modigm.db.write.RemoteWriteStudyDao
import kr.co.lion.modigm.db.write.RemoteWriteStudyDataSource
import kr.co.lion.modigm.model.SqlStudyData

class WriteStudyRepository {
    private val writeStudyDataSource = RemoteWriteStudyDataSource()

    suspend fun uploadStudyData(userIdx: Int, study: SqlStudyData, studyTechStack: List<Int>, studyPicUrl: String?): Int? {
        return try {
            writeStudyDataSource.uploadStudyData(userIdx, study, studyTechStack, studyPicUrl)
        } catch (e: Exception) {
            Log.e("WriteStudyRepository Error", "Error uploadStudyData: ${e.message}")
            null
        }
    }
}
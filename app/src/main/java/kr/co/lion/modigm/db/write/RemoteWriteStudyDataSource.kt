package kr.co.lion.modigm.db.write

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.lion.modigm.model.SqlStudyData

class RemoteWriteStudyDataSource {

    // 사용자 정보를 저장한다.
    suspend fun uploadStudyData(userIdx: Int, study: SqlStudyData, studyTechStack: List<Int>, studyPicUrl: String?): Int? {
        return try {
            val dao = RemoteWriteStudyDao()
            withContext(Dispatchers.IO) {
                val studyId = dao.insertStudyData(study.toMap(), studyPicUrl)
                studyId?.let {
                    coroutineScope {
                        val insertTechStackJob = async { dao.insertStudyTechStack(it, studyTechStack) }
                        val insertMemberJob = async { dao.insertStudyMember(it, listOf(userIdx)) }
                        insertTechStackJob.await()
                        insertMemberJob.await()
                    }
                }
                studyId
            }
        } catch (e: Exception) {
            Log.e("RemoteWriteStudyDataSource Error", "Error uploadStudyData: ${e.message}")
            null
        }
    }

}
package kr.co.lion.modigm.db.write

import android.util.Log
import kr.co.lion.modigm.model.SqlStudyData

class WriteStudyDataSource {

    // 사용자 정보를 저장한다.
    suspend fun uploadStudyData(study: SqlStudyData):Int?{
        return try {
            val db = WriteStudyDao()
            db.insertStudyData(study)
        } catch (e: Exception) {
            Log.e("WriteStudyDataSource Error", "Error uploadStudyData: ${e.message}")
            null
        }
    }

    fun uploadStudyTechStack(studyIdx:Int, studyTechStack: List<Int>):Boolean {
        return try {
            val db = WriteStudyDao()
            db.uploadStudyTechStack(studyIdx, studyTechStack)
            true
        } catch (e: Exception) {
            Log.e("WriteStudyDataSource Error", "Error uploadStudyData: ${e.message}")
            false
        }
    }

    fun uploadStudyMember(studyIdx: Int, userIdx: Int): Any {
        return try {
            val db = WriteStudyDao()
            db.uploadStudyMember(studyIdx, userIdx)
            true
        } catch (e: Exception) {
            Log.e("WriteStudyDataSource Error", "Error uploadStudyMember: ${e.message}")
            false
        }
    }

}
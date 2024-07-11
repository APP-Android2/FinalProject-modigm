package kr.co.lion.modigm.db.write

import android.util.Log
import kr.co.lion.modigm.model.SqlStudyData

class WriteStudyDataSource {

    // 사용자 정보를 저장한다.
    suspend fun uploadStudyData(userIdx: Int, study: SqlStudyData, studyTechStack: List<Int>):Int?{
        return try {
            val db = WriteStudyDao()
            db.insertStudyData(userIdx, study.toMap(), studyTechStack)
        } catch (e: Exception) {
            Log.e("WriteStudyDataSource Error", "Error uploadStudyData: ${e.message}")
            null
        }
    }

}
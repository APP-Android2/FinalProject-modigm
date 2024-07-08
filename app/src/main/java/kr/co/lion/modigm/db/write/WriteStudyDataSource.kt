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
            Log.e("Firebase Error", "Error dbAddStudyData: ${e.message}")
            null
        }
    }

}
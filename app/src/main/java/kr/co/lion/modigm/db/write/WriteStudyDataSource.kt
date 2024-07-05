package kr.co.lion.modigm.db.write

import android.util.Log
import kr.co.lion.modigm.model.StudyData

class WriteStudyDataSource {

    // 사용자 정보를 저장한다.
    suspend fun uploadStudyData(study: StudyData):Int?{
        return try {
            val db = MySqlConn()
            db.insertStudyData(study)
        } catch (e: Exception) {
            Log.e("Firebase Error", "Error dbAddStudyData: ${e.message}")
            null
        }
    }

}
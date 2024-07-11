package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.write.WriteStudyDataSource
import kr.co.lion.modigm.model.SqlStudyData

class WriteStudyRepository {
    private val writeStudyDataSource = WriteStudyDataSource()

    // 스터디 정보 업로드
    suspend fun uploadStudyData(userIdx: Int, studyData: SqlStudyData, studyTechStack: List<Int>):Int? =
        writeStudyDataSource.uploadStudyData(userIdx, studyData, studyTechStack)

}
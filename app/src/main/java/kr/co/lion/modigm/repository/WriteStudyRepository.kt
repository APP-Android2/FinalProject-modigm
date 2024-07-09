package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.write.WriteStudyDataSource
import kr.co.lion.modigm.model.SqlStudyData

class WriteStudyRepository {
    private val writeStudyDataSource = WriteStudyDataSource()

    // 스터디 정보 업로드
    suspend fun uploadStudyData(studyData: SqlStudyData):Int? =
        writeStudyDataSource.uploadStudyData(studyData)

    // 스터디 기술 스택 업로드
    fun uploadStudyTechStack(studyIdx:Int, studyTechStack: List<Int>) =
        writeStudyDataSource.uploadStudyTechStack(studyIdx, studyTechStack)

    // 스터디 멤버 업로드
    fun uploadStudyMember(studyIdx: Int, userIdx: Int) =
        writeStudyDataSource.uploadStudyMember(studyIdx, userIdx)
}
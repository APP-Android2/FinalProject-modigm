package kr.co.lion.modigm.repository

import kr.co.lion.modigm.db.remote.StudyDataSource

class StudyRepository {

    private val studyDataSource = StudyDataSource()

    // 스터디 시퀀스 값을 가져온다.
    suspend fun getStudySequence() = studyDataSource.getStudySequence()

    // 스터디 시퀀스 값을 업데이트한다.
    suspend fun updateStudySequence(studySequence: Int) = studyDataSource.updateStudySequence(studySequence)

    // 전체 스터디 목록을 가져온다.
    suspend fun getStudyAllData() = studyDataSource.getStudyAllData()

    // 전체 스터디 목록 중 모집중인 스터디만 가져온다.
    suspend fun getStudyStateTrueData() = studyDataSource.getStudyStateTrueData()

    // 내 스터디 목록을 가져온다. (홈화면 내 스터디 접근 시)
    suspend fun getStudyMyData() = studyDataSource.getStudyMyData()
}
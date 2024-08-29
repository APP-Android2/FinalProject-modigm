package kr.co.lion.modigm.model

import java.sql.ResultSet

data class FilterStudyData(
    val studyType: String = "",          // 활동 타입
    val studyPeriod: String = "",        // 진행 기간
    val studyOnOffline: String = "",     // 진행 방식
    val studyMaxMember: Int = 0,         // 최대 인원수
    val studyApplyMethod: String = "",   // 신청 방식
    val studySkillList: String = "",     // 기술 스택 (필요 시 추가)
    val programmingLanguage: String = "" // 프로그래밍 언어 (필요 시 추가)
) {
    companion object {
        // ResultSet에서 필터링에 필요한 데이터만 추출하는 메서드
        fun getFilterStudyData(resultSet: ResultSet): FilterStudyData {
            return FilterStudyData(
                resultSet.getString("studyType"),
                resultSet.getString("studyPeriod"),
                resultSet.getString("studyOnOffline"),
                resultSet.getInt("studyMaxMember"),
                resultSet.getString("studyApplyMethod"),
                resultSet.getString("studySkillList"),
                resultSet.getString("programmingLanguage")
            )
        }
    }
    fun toStudyData(): StudyData {
        return StudyData(
            studyType = this.studyType,
            studyPeriod = this.studyPeriod,
            studyOnOffline = this.studyOnOffline,
            studyMaxMember = this.studyMaxMember,
            studyApplyMethod = this.studyApplyMethod
        )
    }
}
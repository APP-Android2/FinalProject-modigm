package kr.co.lion.modigm.model

data class FilterStudyData(
    val studyType: String = "",          // 활동 타입
    val studyPeriod: String = "",        // 진행 기간
    val studyOnOffline: String = "",     // 진행 방식
    val studyMaxMember: String = "",         // 최대 인원수
    val studyTechStack: List<Int> = emptyList() // 기술 스택
)
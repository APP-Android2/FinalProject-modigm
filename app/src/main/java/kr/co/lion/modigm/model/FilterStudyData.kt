package kr.co.lion.modigm.model

data class FilterStudyData(
    val studyType: String = "",          // 활동 타입
    val studyPeriod: String = "",        // 진행 기간
    val studyOnOffline: String = "",     // 진행 방식
    val studyMaxMember: String = "",     // 최대 인원수
    val studyTechStack: List<Int> = emptyList() // 기술 스택
) {

    companion object {
        fun applyFilter(
            studyList: List<Triple<StudyData, Int, Boolean>>,
            filter: FilterStudyData,
            allStudyTechStackData: List<StudyTechStackData>
        ): List<Triple<StudyData, Int, Boolean>> {

            return studyList.filter { (study, _, _) ->
                val techStackIds = allStudyTechStackData
                    .filter { it.studyIdx == study.studyIdx }
                    .map { it.techIdx }

                val matchesStudyType = filter.studyType.isEmpty() || study.studyType == filter.studyType
                val matchesPeriod = filter.studyPeriod.isEmpty() || studyPeriodMatches(study.studyPeriod, filter.studyPeriod)
                val matchesOnOffline = filter.studyOnOffline.isEmpty() || study.studyOnOffline == filter.studyOnOffline
                val matchesMaxMember = filter.studyMaxMember.isEmpty() || studyMaxMemberMatches(study.studyMaxMember, filter.studyMaxMember)
                val matchesTechStack = filter.studyTechStack.isEmpty() || filter.studyTechStack.any { tech -> techStackIds.contains(tech) }

                matchesStudyType && matchesPeriod && matchesOnOffline && matchesMaxMember && matchesTechStack
            }
        }

        private fun studyPeriodMatches(studyPeriod: String, periodFilter: String): Boolean {
            return when (periodFilter) {
                "1개월이하" -> studyPeriod == "1개월이하"
                "2개월이하" -> studyPeriod == "1개월이하" || studyPeriod == "2개월이하"
                "3개월이하" -> studyPeriod == "1개월이하" || studyPeriod == "2개월이하" || studyPeriod == "3개월이하"
                "4개월이하" -> studyPeriod in listOf("1개월이하", "2개월이하", "3개월이하", "4개월이하")
                "5개월이하" -> studyPeriod in listOf("1개월이하", "2개월이하", "3개월이하", "4개월이하", "5개월이하")
                "6개월미만" -> studyPeriod != "6개월이상"
                "6개월이상" -> studyPeriod == "6개월이상"
                else -> true
            }
        }

        private fun studyMaxMemberMatches(maxMember: Int, maxMemberFilter: String): Boolean {
            return when (maxMemberFilter) {
                "2~5명" -> maxMember in 2..5
                "6~10명" -> maxMember in 6..10
                "11명이상" -> maxMember >= 11
                else -> true
            }
        }
    }
}

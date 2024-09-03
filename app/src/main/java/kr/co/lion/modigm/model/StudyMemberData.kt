package kr.co.lion.modigm.model

import java.sql.ResultSet

data class StudyMemberData(
    val studyIdx: Int,
    val userIdx: Int
){
    companion object {
        fun getStudyMemberData(resultSet: ResultSet): StudyMemberData {
            return StudyMemberData(
                resultSet.getInt("studyIdx"),
                resultSet.getInt("userIdx")
            )
        }
    }
}
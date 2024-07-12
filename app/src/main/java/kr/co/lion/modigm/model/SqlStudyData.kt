package kr.co.lion.modigm.model

import java.sql.ResultSet

data class SqlStudyData(
    val studyIdx: Int = -1,                     // 스터디 아이디
    val studyTitle: String = "",                // 제목
    val studyContent: String = "",              // 내용
    val studyType: String = "",                 // 활동 타입 (스터디, 프로젝트, 공모전)
    val studyPeriod: String = "",               // 진행 기간
    val studyOnOffline: String = "",            // 진행 방식 (온라인, 오프라인, 온/오프 혼합)
    val studyDetailPlace: String = "",          // 오프라인 시 진행 장소 상세 주소
    val studyPlace: String = "",                // 오프라인 시 진행 장소
    val studyApplyMethod: String = "",          // 신청 방식 (선착순, 신청제)
    val studyCanApply: String = "",             // 모집 상태 (모집 중, 모집 완료)
    val studyPic: String = "",                  // 썸네일 사진
    val studyMaxMember: Int = 0,                // 최대 인원수
    val studyState: Boolean = true,             // 삭제 여부 (존재함, 삭제됨)
    val userIdx: Int = -1,                      // 사용자 번호
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["studyTitle"] = this.studyTitle
        map["studyContent"] = this.studyContent
        map["studyType"] = this.studyType
        map["studyPeriod"] = this.studyPeriod
        map["studyOnOffline"] = this.studyOnOffline
        map["studyDetailPlace"] = this.studyDetailPlace
        map["studyPlace"] = this.studyPlace
        map["studyApplyMethod"] = this.studyApplyMethod
        map["studyCanApply"] = this.studyCanApply
        map["studyPic"] = this.studyPic
        map["studyMaxMember"] = this.studyMaxMember
        map["studyState"] = this.studyState
        map["userIdx"] = this.userIdx
        return map
    }

    companion object {
        fun fromMap(map: Map<String, Any>): SqlStudyData {
            return SqlStudyData(
                studyIdx = map["studyIdx"] as Int,
                studyTitle = map["studyTitle"] as String,
                studyContent = map["studyContent"] as String,
                studyType = map["studyType"] as String,
                studyPeriod = map["studyPeriod"] as String,
                studyOnOffline = map["studyOnOffline"] as String,
                studyDetailPlace = map["studyDetailPlace"] as String,
                studyPlace = map["studyPlace"] as String,
                studyApplyMethod = map["studyApplyMethod"] as String,
                studyCanApply = map["studyCanApply"] as String,
                studyPic = map["studyPic"] as String,
                studyMaxMember = map["studyMaxMember"] as Int,
                studyState = map["studyState"] as Boolean,
                userIdx = map["userIdx"] as Int
            )
        }

        fun getStudyData(resultSet: ResultSet): SqlStudyData {
            return SqlStudyData(
                resultSet.getInt("studyIdx"),
                resultSet.getString("studyTitle"),
                resultSet.getString("studyContent"),
                resultSet.getString("studyType"),
                resultSet.getString("studyPeriod"),
                resultSet.getString("studyOnOffline"),
                resultSet.getString("studyDetailPlace"),
                resultSet.getString("studyPlace"),
                resultSet.getString("studyApplyMethod"),
                resultSet.getString("studyCanApply"),
                resultSet.getString("studyPic"),
                resultSet.getInt("studyMaxMember"),
                resultSet.getBoolean("studyState"),
                resultSet.getInt("userIdx"),
            )
        }
    }
}